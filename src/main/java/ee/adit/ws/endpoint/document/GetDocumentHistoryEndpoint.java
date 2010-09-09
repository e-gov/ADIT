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
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.Activity;
import ee.adit.pojo.ActivityActor;
import ee.adit.pojo.ActivitySubject;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetDocumentHistoryResponseAttachment;
import ee.adit.pojo.GetDocumentHistoryRequest;
import ee.adit.pojo.GetDocumentHistoryResponse;
import ee.adit.pojo.GetDocumentHistoryResponseDocument;
import ee.adit.pojo.Message;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "getDocumentHistory" web method (web service request).
 * Contains request input validation, request-specific workflow
 * and response composition.  
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "getDocumentHistory", version = "v1")
@Component
public class GetDocumentHistoryEndpoint extends AbstractAditBaseEndpoint {
	
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
		LOG.debug("getDocumentHistory invoked. Version: " + version);

		if (version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
	}
	
	/**
	 * Executes "V1" version of "getDocumentHistory" request.
	 * 
	 * @param requestObject		Request body object
	 * @return					Response body object
	 */
	protected Object v1(Object requestObject) {
		GetDocumentHistoryResponse response = new GetDocumentHistoryResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;

		try {
			LOG.debug("getDocumentHistory.v1 invoked.");
			GetDocumentHistoryRequest request = (GetDocumentHistoryRequest) requestObject;
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
				AditCodedException exception = new AditCodedException("application.notRegistered");
				exception.setParameters(new Object[] { applicationName });
				throw exception;
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid näha
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel < 1) {
				AditCodedException exception = new AditCodedException("application.insufficientPrivileges.read");
				exception.setParameters(new Object[] { applicationName });
				throw exception;
			}

			// Kontrollime, kas päringus märgitud isik on teenuse kasutaja
			String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader().getIsikukood();
			AditUser user = this.getUserService().getUserByID(userCode);
			if (user == null) {
				AditCodedException exception = new AditCodedException("user.nonExistent");
				exception.setParameters(new Object[] { userCode });
				throw exception;
			}

			// Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
			if ((user.getActive() == null) || !user.getActive()) {
				AditCodedException exception = new AditCodedException("user.inactive");
				exception.setParameters(new Object[] { userCode });
				throw exception;
			}
			
			// Check whether or not the application has rights to
			// read current user's data.
			int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
			if(applicationAccessLevelForUser < 1) {
				AditCodedException exception = new AditCodedException("application.insufficientPrivileges.forUser.read");
				exception.setParameters(new Object[] { applicationName, user.getUserCode() });
				throw exception;
			}

			// Now it is safe to load the document from database
			// (and even necessary to do all the document-specific checks)
			Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());

			// Check whether the document exists
			if (doc == null) {
				LOG.debug("Requested document does not exist. Document ID: " + request.getDocumentId());
				AditCodedException exception = new AditCodedException("document.nonExistent");
				exception.setParameters(new Object[] { request.getDocumentId().toString() });
				throw exception;
			}
			
			// Check whether the document is marked as deleted
			if ((doc.getDeleted() != null) && doc.getDeleted()) {
				LOG.debug("Requested document is deleted. Document ID: " + request.getDocumentId());
				AditCodedException exception = new AditCodedException("document.deleted");
				exception.setParameters(new Object[] { request.getDocumentId().toString() });
				throw exception;
			}
			
			// Document history can only be viewed if:
			// a) document belongs to user
			// b) document is sent or shared to user
			boolean docBelongsToUser = false;
			if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
				docBelongsToUser = true;
			} else {
				if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
					Iterator it = doc.getDocumentSharings().iterator();
					while (it.hasNext()) {
						DocumentSharing sharing = (DocumentSharing)it.next();
						if (sharing.getUserCode().equalsIgnoreCase(userCode)) {
							docBelongsToUser = true;
							break;
						}
					}
				}
			}

			if (docBelongsToUser) {
				List<Activity> activityList = new ArrayList<Activity>();
				if ((doc.getDocumentHistories() != null) && (!doc.getDocumentHistories().isEmpty())) {
					List<DocumentHistory> historyList = this.getDocumentService().getDocumentHistoryDAO().getSortedList(documentId);
					for (DocumentHistory historyEvent : historyList) {
						Activity activity = new Activity();
						activity.setTime(historyEvent.getEventDate());
						activity.setType(historyEvent.getDocumentHistoryType());
						activity.setApplication(historyEvent.getRemoteApplicationName());
						activity.setActors(new ArrayList<ActivityActor>());
						activity.setSubjects(new ArrayList<ActivitySubject>());

						ActivityActor actor = new ActivityActor();
						actor.setCode(historyEvent.getUserCode());
						actor.setName(historyEvent.getUserName());
						if (historyEvent.getUserCode().equalsIgnoreCase(userCode)
							&& !historyEvent.getUserCode().equalsIgnoreCase(historyEvent.getXteeUserCode())) {
							actor.setUserCode(historyEvent.getXteeUserCode());
							actor.setUserName(historyEvent.getXteeUserName());
						}
						activity.getActors().add(actor);
						
						if (historyEvent.getDocumentHistoryType().equalsIgnoreCase(DocumentService.HistoryType_Send)
							|| historyEvent.getDocumentHistoryType().equalsIgnoreCase(DocumentService.HistoryType_Share)) {
							
							if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
								Iterator sharingIterator = doc.getDocumentSharings().iterator();
								while (sharingIterator.hasNext()) {
									DocumentSharing sharing = (DocumentSharing)sharingIterator.next();
									if (sharing.getCreationDate().compareTo(historyEvent.getEventDate()) == 0) {
										
										ActivitySubject subject = new ActivitySubject();
										subject.setCode(sharing.getUserCode());
										subject.setName(sharing.getUserName());
										activity.getSubjects().add(subject);
									}
								}
							}
						}
						
						activityList.add(activity);
					}
				} else {
					LOG.debug("Document history is empty.");
				}
				
				GetDocumentHistoryResponseAttachment result = new GetDocumentHistoryResponseAttachment();
				result.setId(doc.getId());
				result.setGuid(doc.getGuid());
				result.setActivityList(activityList);
				
				// 1. Convert java list to XML string and output to file
				String xmlFile = marshal(result);
				
				// 2. GZip the temporary file
				// Base64 encoding will be done at SOAP envelope level
				String gzipFileName = Util.gzipFile(xmlFile, this.getConfiguration().getTempDir());

				// 3. Add as an attachment
				String contentID = addAttachment(gzipFileName);
				GetDocumentHistoryResponseDocument responseDoc = new GetDocumentHistoryResponseDocument();
				responseDoc.setHref("cid:" + contentID);
				response.setDocumentHistoryList(responseDoc);
				
				
				
			} else {
				LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);
				AditCodedException exception = new AditCodedException("document.doesNotBelongToUser");
				exception.setParameters(new Object[] { request.getDocumentId().toString(), userCode });
				throw exception;
			}
			
			// Set response messages
			response.setSuccess(true);
			messages.setMessage(this.getMessageService().getMessages("request.getDocumentHistory.success", new Object[] { }));
			response.setMessages(messages);
			
			String additionalMessage = this.getMessageService().getMessage("request.getDocumentHistory.success", new Object[] {}, Locale.ENGLISH);
			additionalInformationForLog = LogService.RequestLog_Success + ": " + additionalMessage;
			
		} catch (Exception e) {
			String errorMessage = null;
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

			additionalInformationForLog = errorMessage;
			super.logError(documentId, requestDate.getTime(), LogService.ErrorLogLevel_Error, e.getMessage());
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}

		super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
		return response;
	}
	
	@Override
	protected Object getResultForGenericException(Exception ex) {
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, ex.getMessage());
		GetDocumentHistoryResponse response = new GetDocumentHistoryResponse();
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
	 * @param request				Request body as {@link GetDocumentHistoryRequest} object.
	 * @throws AditCodedException	Exception describing error found in requet body.
	 */
	private void checkRequest(GetDocumentHistoryRequest request) throws AditCodedException {
		if (request != null) {
			if ((request.getDocumentId() == null) || (request.getDocumentId() <= 0)) {
				throw new AditCodedException("request.body.undefined.documentId");
			}
		} else {
			throw new AditCodedException("request.body.empty");
		}
	}

	/**
	 * Writes request parameters to application DEBUG log.
	 * 
	 * @param request	Request body as {@link GetDocumentHistoryRequest} object.
	 */
	private void printRequest(GetDocumentHistoryRequest request) {
		LOG.debug("-------- GetDocumentHistoryRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		LOG.debug("------------------------------------------");
	}
}
