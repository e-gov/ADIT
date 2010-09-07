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
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.ModifyStatusRequest;
import ee.adit.pojo.ModifyStatusResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "modifyStatus" web method (web service request).
 * Contains request input validation, request-specific workflow
 * and response composition.  
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
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
	
	@Override
	protected Object invokeInternal(Object requestObject, int version) throws Exception {
		LOG.debug("modifyStatus invoked. Version: " + version);

		if (version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
	}
	
	/**
	 * Executes "V1" version of "modifyStatus" request.
	 * 
	 * @param requestObject		Request body object
	 * @return					Response body object
	 */
	protected Object v1(Object requestObject) {
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
				AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
				aditCodedException.setParameters(new Object[] { applicationName });
				throw aditCodedException;
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid näha
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel < 1) {
				AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.read");
				aditCodedException.setParameters(new Object[] { applicationName });
				throw aditCodedException;
			}

			// Kontrollime, kas päringus märgitud isik on teenuse kasutaja
			String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader().getIsikukood();
			AditUser user = this.getUserService().getUserByID(userCode);
			if (user == null) {
				AditCodedException aditCodedException = new AditCodedException("user.nonExistent");
				aditCodedException.setParameters(new Object[] { userCode });
				throw aditCodedException;
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
				AditCodedException aditCodedException = new AditCodedException("user.inactive");
				aditCodedException.setParameters(new Object[] { userCode });
				throw aditCodedException;
			}

			// Check whether or not the application has rights to
			// read current user's data.
			int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
			if(applicationAccessLevelForUser < 1) {
				AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.forUser.read");
				aditCodedException.setParameters(new Object[] { applicationName, user.getUserCode() });
				throw aditCodedException;
			}
			
			// Check whether the specified workflow status exists
			DocumentWfStatus status = this.documentService.getDocumentWfStatusDAO().getDocumentWfStatus(request.getDocumentStatusId());
			if (status == null) {
				AditCodedException aditCodedException = new AditCodedException("documentWorkflowStatus.nonExistent");
				aditCodedException.setParameters(new Object[] { request.getDocumentStatusId() });
				throw aditCodedException;
			}
			
			// Now it is safe to load the document from database
			// (and even necessary to do all the document-specific checks)
			Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());

			// Check whether the document exists
			if (doc == null) {
				LOG.debug("Requested document does not exist. Document ID: " + request.getDocumentId());
				AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
				aditCodedException.setParameters(new Object[] { request.getDocumentId().toString() });
				throw aditCodedException;
			}
			
			// Check whether the document is marked as deleted
			if ((doc.getDeleted() != null) && doc.getDeleted()) {
				LOG.debug("Requested document is deleted. Document ID: " + request.getDocumentId());
				AditCodedException aditCodedException = new AditCodedException("document.deleted");
				aditCodedException.setParameters(new Object[] { request.getDocumentId().toString() });
				throw aditCodedException;
			}
			
			// Check whether the document is marked as deflated
			if ((doc.getDeflated() != null) && doc.getDeflated()) {
				LOG.debug("Requested document is deflated. Document ID: " + request.getDocumentId());
				AditCodedException aditCodedException = new AditCodedException("document.deflated");
				aditCodedException.setParameters(new Object[] { Util.dateToEstonianDateString(doc.getDeflateDate()) });
				throw aditCodedException;
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
				AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
				aditCodedException.setParameters(new Object[] { request.getDocumentId().toString(), userCode });
				throw aditCodedException;
			}

			// If status change was successful then add history event
			DocumentHistory historyEvent = new DocumentHistory(
				DocumentService.HistoryType_ModifyStatus,
				documentId,
				requestDate.getTime(),
				user,
				xroadRequestUser,
				header);
			historyEvent.setDescription(DocumentService.DocumentHistoryDescription_ModifyStatus + request.getDocumentStatusId());
			this.getDocumentService().getDocumentHistoryDAO().save(historyEvent);

			
			// Set response messages
			response.setSuccess(true);
			messages.setMessage(this.getMessageService().getMessages("request.modifyStatus.success", new Object[] {  }));
			response.setMessages(messages);
			
			String additionalMessage = this.getMessageService().getMessage("request.modifyStatus.success", new Object[] { }, Locale.ENGLISH);
			additionalInformationForLog = LogService.RequestLog_Success + ": " + additionalMessage;
			
		} catch (Exception e) {
			String errorMessage = null;
			additionalInformationForLog = "Request failed: " + e.getMessage();
			LOG.error("Exception: ", e);			
			response.setSuccess(false);
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

			if(e instanceof AditCodedException) {
				LOG.debug("Adding exception messages to response object.");
				arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
				errorMessage = this.getMessageService().getMessage(e.getMessage(), ((AditCodedException) e).getParameters(), Locale.ENGLISH);
				errorMessage = "ERROR: " + errorMessage;
			} else if (e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(new Message("en", e.getMessage()));
				errorMessage = "ERROR: " + e.getMessage();
			} else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
				errorMessage = "ERROR: " + e.getMessage();
			}

			super.logError(documentId, requestDate.getTime(), LogService.ErrorLogLevel_Error, errorMessage);
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}

		super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
		return response;
	}

	@Override
	protected Object getResultForGenericException(Exception ex) {
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, ex.getMessage());
		ModifyStatusResponse response = new ModifyStatusResponse();
		response.setSuccess(false);
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		return response;
	}
	
	/**
	 * Validates request body and makes sure that all
	 * required fields exist and are not empty.
	 * <br><br>
	 * Throws {@link AditCodedException} if any errors in request data are found.
	 * 
	 * @param request				Request body as {@link ModifyStatusRequest} object.
	 * @throws AditCodedException	Exception describing error found in requet body.
	 */
	private void checkRequest(ModifyStatusRequest request) throws AditCodedException {
		if (request != null) {
			if ((request.getDocumentId() == null) || (request.getDocumentId() <= 0)) {
				throw new AditCodedException("request.body.undefined.documentId");
			} else if ((request.getDocumentStatusId() == null) || (request.getDocumentStatusId() <= 0)) {
				throw new AditCodedException("request.body.undefined.documentStatusId");
			}
		} else {
			throw new AditCodedException("request.body.empty");
		}
	}

	/**
	 * Writes request parameters to application DEBUG log.
	 * 
	 * @param request	Request body as {@link ModifyStatusRequest} object.
	 */
	private void printRequest(ModifyStatusRequest request) {
		LOG.debug("-------- ModifyStatusRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		LOG.debug("Document status ID: " + String.valueOf(request.getDocumentStatusId()));
		LOG.debug("------------------------------------");
	}
}
