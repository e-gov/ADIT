package ee.adit.ws.endpoint.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.ArrayOfRecipientStatus;
import ee.adit.pojo.Message;
import ee.adit.pojo.RecipientStatus;
import ee.adit.pojo.UnShareDocumentRequest;
import ee.adit.pojo.UnShareDocumentResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "unShareDocument", version = "v1")
@Component
public class UnShareDocumentEndpoint extends AbstractAditBaseEndpoint {
	
	private static Logger LOG = Logger.getLogger(UnShareDocumentEndpoint.class);
	private UserService userService;
	private DocumentService documentService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		UnShareDocumentResponse response = new UnShareDocumentResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		ArrayOfRecipientStatus statusArray = new ArrayOfRecipientStatus();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;

		try {
			LOG.debug("unShareDocument.v1 invoked.");
			UnShareDocumentRequest request = (UnShareDocumentRequest) requestObject;
			if (request != null) {
				documentId = request.getDocumentId();
			}
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();

			// Log request
			Util.printHeader(header);
			printRequest(request);

			// Check header for required fields
			checkHeader(header);

			// Check request body
			checkRequest(request);

			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis
			// registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
			if (!applicationRegistered) {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid näha
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel < 1) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.read", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringus märgitud isik on teenuse kasutaja
			String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader().getIsikukood();
			AditUser user = this.getUserService().getUserByID(userCode);
			if (user == null) {
				String errorMessage = this.getMessageSource().getMessage("user.nonExistent", new Object[] { userCode },	Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			AditUser xroadRequestUser = null;
			if (user.getUsertype().getShortName().equalsIgnoreCase("person")) {
				xroadRequestUser = user;
			} else {
				try {
					xroadRequestUser = this.getUserService().getUserByID(header.getIsikukood());
				} catch (Exception ex) {
					LOG.debug("Error when attempting to find local user matchinig the person that executed a company request.");
				}
			}

			// Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
			if ((user.getActive() == null) || !user.getActive()) {
				String errorMessage = this.getMessageSource().getMessage("user.inactive", new Object[] { userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check whether or not the application has rights to
			// read current user's data.
			int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
			if(applicationAccessLevelForUser < 1) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.forUser.read", new Object[] { applicationName, user.getUserCode() }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Now it is safe to load the document from database
			// (and even necessary to do all the document-specific checks)
			Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());

			// Check whether the document exists
			if (doc == null) {
				LOG.debug("Requested document does not exist. Document ID: " + request.getDocumentId());
				String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() },	Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check whether the document is marked as deleted
			if ((doc.getDeleted() != null) && doc.getDeleted()) {
				LOG.debug("Requested document is deleted. Document ID: " + request.getDocumentId());
				String errorMessage = this.getMessageSource().getMessage("document.deleted", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check whether the document is marked as deflated
			if ((doc.getDeflated() != null) && doc.getDeflated()) {
				LOG.debug("Requested document is deflated. Document ID: " + request.getDocumentId());
				String errorMessage = this.getMessageSource().getMessage("document.deflated", new Object[] { Util.dateToEstonianDateString(doc.getDeflateDate()) }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Check whether the document belongs to user
			if (!doc.getCreatorCode().equalsIgnoreCase(userCode)) {
				LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);
				String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// All checks are successfully passed
			boolean saveDocument = false;
			List<String> userCodes = new ArrayList<String>();
			if ((request.getRecipientList() != null) && (request.getRecipientList().getCode() != null)) {
				userCodes.addAll(request.getRecipientList().getCode());
			}
			
			if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
				Iterator it = doc.getDocumentSharings().iterator();
				while (it.hasNext()) {
					DocumentSharing sharing = (DocumentSharing)it.next();
					if ((request.getRecipientList() == null)
						|| (request.getRecipientList().getCode() == null)
						|| (request.getRecipientList().getCode().size() < 1)
						|| (request.getRecipientList().getCode().contains(sharing.getUserCode()))) {
						
						if (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SharingType_Share)
							|| sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SharingType_Sign)) {
							
							// Remove sharing
							doc.getDocumentSharings().remove(sharing);
							sharing.setDocumentId(0);
							saveDocument = true;
							
							// Create response object
							RecipientStatus status = new RecipientStatus();
							status.setSuccess(true);
							status.setCode(sharing.getUserCode());
							ArrayOfMessage statusMessages = new ArrayOfMessage();
							statusMessages.addMessage(new Message("en", this.getMessageSource().getMessage("request.unShareDocument.recipientStatus.success", new Object[] { }, Locale.ENGLISH)));
							status.setMessages(statusMessages);
							statusArray.addRecipient(status);
							
							if (userCodes.contains(sharing.getUserCode())) {
								userCodes.remove(sharing.getUserCode());
							}
						}
					}
				}
			} else {
				String errorMessage = this.getMessageSource().getMessage("request.unShareDocument.document.notShared", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		
			// If the document was not shared to some users in request's
			// user list then compose corresponding error messages
			if (!userCodes.isEmpty()) {
				for (String code : userCodes) {
					RecipientStatus status = new RecipientStatus();
					status.setSuccess(false);
					status.setCode(code);
					ArrayOfMessage statusMessages = new ArrayOfMessage();
					statusMessages.addMessage(new Message("en", this.getMessageSource().getMessage("request.unShareDocument.recipientStatus.notShared", new Object[] { }, Locale.ENGLISH)));
					status.setMessages(statusMessages);
					statusArray.addRecipient(status);
				}
			}
			
			// Add history event about unsharing
			doc.getDocumentHistories().add(new DocumentHistory(
					DocumentService.HistoryType_UnShare,
					doc.getId(),
					requestDate.getTime(),
					user,
					xroadRequestUser,
					header));
			
			if (saveDocument) {
				// If all sharings are removed then remove locking
				if (doc.getDocumentSharings().isEmpty()) {
					doc.setLocked(false);
					doc.setLockingDate(null);
					
					// Lisame lukustamise ajaloosündmuse
					doc.getDocumentHistories().add(new DocumentHistory(
							DocumentService.HistoryType_UnLock,
							doc.getId(),
							requestDate.getTime(),
							user,
							xroadRequestUser,
							header));
				}
				
				this.documentService.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);
				
				// Send notification to every user who was removed from
				// sharing recipients list (assuming they have requested such notifications)
				for (RecipientStatus status : statusArray.getRecipient()) {
					if ((status != null) && status.isSuccess()) {
						AditUser recipient = this.getUserService().getUserByID(status.getCode());
						if ((recipient != null) && (userService.findNotification(recipient.getUserNotifications(), ScheduleClient.NotificationType_Share) != null)) {
							ScheduleClient.addEvent(
								recipient,
								this.getMessageSource().getMessage("scheduler.message.unShare", new Object[] { userCode, doc.getTitle() }, Locale.ENGLISH),
								this.getConfiguration().getSchedulerEventTypeName(),
								requestDate,
								ScheduleClient.NotificationType_Share,
								doc.getId(),
								this.userService);
						}
					}
				}
			}

			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.unShareDocument.success",	new Object[] { request.getDocumentId() }, Locale.ENGLISH)));
			response.setMessages(messages);
			response.setRecipientList(statusArray);
		} catch (Exception e) {
			additionalInformationForLog = "Request failed: " + e.getMessage();
			LOG.error("Exception: ", e);
			response.setSuccess(false);
			response.setRecipientList(statusArray);
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

			if (e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(new Message("en", e.getMessage()));
			} else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
			}

			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}

		super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
		return response;
	}

	@Override
	protected Object getResultForGenericException(Exception ex) {
		UnShareDocumentResponse response = new UnShareDocumentResponse();
		response.setSuccess(false);
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		return response;
	}
	
	private void checkHeader(CustomXTeeHeader header) throws Exception {
		String errorMessage = null;
		if (header != null) {
			if ((header.getIsikukood() == null)	|| (header.getIsikukood().length() < 1)) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.personalCode", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if ((header.getInfosysteem() == null) || (header.getInfosysteem().length() < 1)) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.systemName", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if ((header.getAsutus() == null) || (header.getAsutus().length() < 1)) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.institution", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		}
	}

	private void checkRequest(UnShareDocumentRequest request) {
		String errorMessage = null;
		if (request != null) {
			if (request.getDocumentId() <= 0) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {},	Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}

	private static void printRequest(UnShareDocumentRequest request) {
		LOG.debug("-------- UnShareDocumentRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		if ((request.getRecipientList() != null) && (request.getRecipientList().getCode() != null)) {
			for (String userCode : request.getRecipientList().getCode()) {
				LOG.debug("User code: " + userCode);
			}
		}
		LOG.debug("---------------------------------------");
	}
}
