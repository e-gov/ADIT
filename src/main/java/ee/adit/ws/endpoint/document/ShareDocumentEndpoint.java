package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

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
import ee.adit.pojo.ShareDocumentRequest;
import ee.adit.pojo.ShareDocumentResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "shareDocument", version = "v1")
@Component
public class ShareDocumentEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(ShareDocumentEndpoint.class);
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
		ShareDocumentResponse response = new ShareDocumentResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		ArrayOfRecipientStatus statusArray = new ArrayOfRecipientStatus();
		String additionalInformationForLog = null;
		Long documentId = null;
		
		// Get a fixed request date
		// This becomes useful if we later have to compare
		// records in different database tables by date
		Calendar requestDate = Calendar.getInstance();

		try {
			LOG.debug("shareDocument.v1 invoked.");
			ShareDocumentRequest request = (ShareDocumentRequest) requestObject;
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
				String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check whether the document is marked as deflated
			if ((doc.getDeflated() != null) && doc.getDeflated()) {
				LOG.debug("Requested document is deflated. Document ID: " + request.getDocumentId());
				String errorMessage = this.getMessageSource().getMessage("document.deflated", new Object[] { doc.getDeflateDate() }, Locale.ENGLISH);
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
			boolean completeSuccess = true;
			for (String recipientCode : request.getRecipientList().getCode()) {
				boolean isSuccess = false;
				ArrayOfMessage statusMessages = new ArrayOfMessage();
				
				AditUser recipient = this.getUserService().getUserByID(recipientCode);
				if (recipient == null) {
					statusMessages.addMessage(new Message("en", this.getMessageSource().getMessage("request.shareDocument.recipientStatus.recipient.nonExistant", new Object[] { }, Locale.ENGLISH)));
				} else if (recipient.getActive() != true) {
					statusMessages.addMessage(new Message("en", this.getMessageSource().getMessage("request.shareDocument.recipientStatus.recipient.inactive", new Object[] { }, Locale.ENGLISH)));
				} else if (sharingExists(doc.getDocumentSharings(), recipientCode)) {
					statusMessages.addMessage(new Message("en", this.getMessageSource().getMessage("request.shareDocument.recipientStatus.alreadySharedToUser", new Object[] { recipientCode }, Locale.ENGLISH)));
				} else {
					DocumentSharing sharing = new DocumentSharing();
					sharing.setCreationDate(requestDate.getTime());
					sharing.setDocumentId(doc.getId());
					
					if (request.getSharedForSigning() == true) {
						sharing.setDocumentSharingType(DocumentService.SharingType_Sign);
					} else {
						sharing.setDocumentSharingType(DocumentService.SharingType_Share);
					}
					
					sharing.setTaskDescription(request.getReasonForSharing());
					sharing.setUserCode(recipient.getUserCode());
					sharing.setUserName(recipient.getFullName());
					doc.getDocumentSharings().add(sharing);
					
					isSuccess = true;
					saveDocument = true;
					statusMessages.addMessage(new Message("en", this.getMessageSource().getMessage("request.shareDocument.recipientStatus.success", new Object[] { }, Locale.ENGLISH)));
				}
				
				// Create response object
				RecipientStatus status = new RecipientStatus();
				status.setSuccess(isSuccess);
				status.setCode(recipientCode);
				status.setMessages(statusMessages);
				statusArray.addRecipient(status);
				
				completeSuccess = (completeSuccess && isSuccess);
			}
						
			if (saveDocument) {
				doc.setLocked(true);
				if (doc.getLockingDate() == null) {
					doc.setLockingDate(requestDate.getTime());
				}
				doc.setSignable(true);
				
				// Lisame jagamise ajaloosündmuse
				DocumentHistory sharingEvent = new DocumentHistory();
				sharingEvent.setRemoteApplicationName(applicationName);
				sharingEvent.setDocumentId(doc.getId());
				sharingEvent.setDocumentHistoryType(DocumentService.HistoryType_Share);
				sharingEvent.setEventDate(requestDate.getTime());
				sharingEvent.setUserCode(user.getUserCode());
				sharingEvent.setUserName(user.getFullName());
				sharingEvent.setXteeUserCode(header.getIsikukood());
				if (xroadRequestUser != null) {
					sharingEvent.setXteeUserName(xroadRequestUser.getFullName());
				}
				doc.getDocumentHistories().add(sharingEvent);
				
				// Lisame lukustamise ajaloosündmuse
				DocumentHistory lockingEvent = new DocumentHistory();
				lockingEvent.setRemoteApplicationName(applicationName);
				lockingEvent.setDocumentId(doc.getId());
				lockingEvent.setDocumentHistoryType(DocumentService.HistoryType_Lock);
				lockingEvent.setEventDate(requestDate.getTime());
				lockingEvent.setUserCode(user.getUserCode());
				lockingEvent.setUserName(user.getFullName());
				lockingEvent.setXteeUserCode(header.getIsikukood());
				if (xroadRequestUser != null) {
					lockingEvent.setXteeUserName(xroadRequestUser.getFullName());
				}
				doc.getDocumentHistories().add(lockingEvent);
				
				this.documentService.getDocumentDAO().save(doc, null, null);
				
				// Send notification to every user the document was shared to
				// (assuming they have requested such notifications)
				for (RecipientStatus status : statusArray.getRecipient()) {
					if ((status != null) && status.isSuccess()) {
						AditUser recipient = this.getUserService().getUserByID(status.getCode());
						if ((recipient != null) && (userService.findNotification(recipient.getUserNotifications(), ScheduleClient.NotificationType_Share) != null)) {
							ScheduleClient.addEvent(
								recipient.getUserCode(),
								this.getMessageSource().getMessage("scheduler.message.share", new Object[] { doc.getTitle(), userCode }, Locale.ENGLISH),
								this.getConfiguration().getSchedulerEventTypeName(),
								requestDate,
								ScheduleClient.NotificationType_Share,
								doc.getId(),
								this.userService);
						}
					}
				}
			} else {
				String errorMessage = this.getMessageSource().getMessage("request.shareDocument.recipients.noneSucceeded", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.shareDocument.success",	new Object[] { request.getDocumentId() }, Locale.ENGLISH)));
			response.setMessages(messages);
			response.setRecipientList(statusArray);
		} catch (Exception e) {
			additionalInformationForLog = "Request failed: " + e.getMessage();
			LOG.error("Exception: ", e);
			response.setSuccess(false);
			//response.setRecipientList(statusArray);
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
	
	@SuppressWarnings("unchecked")
	private boolean sharingExists(Set documentSharings, String userCode) {
		boolean result = false;
		if ((documentSharings != null) && (!documentSharings.isEmpty())) {
			Iterator it = documentSharings.iterator();
			while (it.hasNext()) {
				DocumentSharing sharing = (DocumentSharing)it.next();
				if (userCode.equalsIgnoreCase(sharing.getUserCode())
					&& (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SharingType_Share)
						|| sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SharingType_Sign))) {
					result = true;
					break;
				}
			}
		}
		
		return result;
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

	private void checkRequest(ShareDocumentRequest request) {
		String errorMessage = null;
		if (request != null) {
			if ((request.getDocumentId() == null) || (request.getDocumentId() <= 0)) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {},	Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if ((request.getRecipientList() == null)
				|| (request.getRecipientList().getCode() == null)
				|| request.getRecipientList().getCode().isEmpty()) {
				errorMessage = this.getMessageSource().getMessage("request.shareDocument.recipients.unspecified", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}

	private static void printRequest(ShareDocumentRequest request) {
		LOG.debug("-------- ShareDocumentRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		if ((request.getRecipientList() != null) && (request.getRecipientList().getCode() != null)) {
			for (String userCode : request.getRecipientList().getCode()) {
				LOG.debug("User code: " + userCode);
			}
		}
		LOG.debug("Reason for sharing: " + request.getReasonForSharing());
		LOG.debug("Shared for signing: " + request.getSharedForSigning());
		LOG.debug("-------------------------------------");
	}
}
