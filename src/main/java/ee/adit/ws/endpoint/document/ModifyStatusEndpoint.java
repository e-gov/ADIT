package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dao.pojo.DocumentWfStatus;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.ModifyStatusRequest;
import ee.adit.pojo.ModifyStatusResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "modifyStatus", version = "v1")
@Component
public class ModifyStatusEndpoint extends AbstractAditBaseEndpoint {
	private static Logger LOG = Logger.getLogger(ModifyStatusEndpoint.class);
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
		ModifyStatusResponse response = new ModifyStatusResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;

		try {
			LOG.debug("modifyStatus.v1 invoked.");
			ModifyStatusRequest request = (ModifyStatusRequest) requestObject;
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

			// Check whether the specified workflow status exists
			DocumentWfStatus status = this.documentService.getDocumentWfStatusDAO().getDocumentWfStatus(request.getDocumentStatusId());
			if (status == null) {
				String errorMessage = this.getMessageSource().getMessage("documentWorkflowStatus.nonExistent", new Object[] { request.getDocumentStatusId() },	Locale.ENGLISH);
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


			// Document status can be changed only if:
			// a) document belongs to user
			// b) document is sent or shared to user
			boolean saveDocument = false;
			if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
				doc.setDocumentWfStatusId(request.getDocumentStatusId());
				doc.setLastModifiedDate(new Date());
				saveDocument = true;
			} else {
				if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
					Iterator it = doc.getDocumentSharings().iterator();
					while (it.hasNext()) {
						DocumentSharing sharing = (DocumentSharing)it.next();
						if (sharing.getUserCode().equalsIgnoreCase(userCode)) {
							sharing.setDocumentWfStatus(request.getDocumentStatusId());
							sharing.setLastAccessDate(new Date());
							saveDocument = true;
							break;
						}
					}
				}
			}
			
			if (saveDocument) {
				this.documentService.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);
				
				// If document status was successfully modified then send
				// a message to document owner.
				// Notification does not need to be sent if user changed
				// his/her own document.
				if (!user.getUserCode().equalsIgnoreCase(doc.getCreatorCode())) {
					AditUser docCreator = this.getUserService().getUserByID(doc.getCreatorCode());
					if ((docCreator != null) && (userService.findNotification(docCreator.getUserNotifications(), ScheduleClient.NotificationType_Modify) != null)) {
						ScheduleClient.addEvent(
							docCreator,
							this.getMessageSource().getMessage("scheduler.message.modify", new Object[] { doc.getTitle(), docCreator.getUserCode() }, Locale.ENGLISH),
							this.getConfiguration().getSchedulerEventTypeName(),
							requestDate,
							ScheduleClient.NotificationType_Modify,
							doc.getId(),
							this.userService);
					}
				}
			} else {
				LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);
				String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// If status change was successful then add history event
			DocumentHistory historyEvent = new DocumentHistory(
				DocumentService.HistoryType_ModifyStatus,
				documentId,
				requestDate.getTime(),
				user,
				xroadRequestUser,
				header);
			this.getDocumentService().getDocumentHistoryDAO().save(historyEvent);

			
			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.modifyStatus.success", new Object[] { }, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
			additionalInformationForLog = "Request failed: " + e.getMessage();
			LOG.error("Exception: ", e);
			response.setSuccess(false);
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
		ModifyStatusResponse response = new ModifyStatusResponse();
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

	private void checkRequest(ModifyStatusRequest request) {
		String errorMessage = null;
		if (request != null) {
			if ((request.getDocumentId() == null) || (request.getDocumentId() <= 0)) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if ((request.getDocumentStatusId() == null) || (request.getDocumentStatusId() <= 0)) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentStatusId", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}

	private static void printRequest(ModifyStatusRequest request) {
		LOG.debug("-------- ModifyStatusRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		LOG.debug("Document status ID: " + String.valueOf(request.getDocumentStatusId()));
		LOG.debug("------------------------------------");
	}
}
