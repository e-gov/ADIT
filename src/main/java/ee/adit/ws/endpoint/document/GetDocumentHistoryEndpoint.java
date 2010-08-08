package ee.adit.ws.endpoint.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

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
	protected Object invokeInternal(Object requestObject) throws Exception {
		GetDocumentHistoryResponse response = new GetDocumentHistoryResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Date requestDate = Calendar.getInstance().getTime();
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

			// Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
			if ((user.getActive() == null) || !user.getActive()) {
				String errorMessage = this.getMessageSource().getMessage("user.inactive", new Object[] { userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check whether or not the application has rights to
			// modify current user's data.
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
				String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
				throw new AditException(errorMessage);
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
				
				// 2. GZip and Base64 encode the temporary file
				String gzipFileName = Util.gzipAndBase64Encode(xmlFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());

				// 3. Add as an attachment
				String contentID = addAttachment(gzipFileName);
				GetDocumentHistoryResponseDocument responseDoc = new GetDocumentHistoryResponseDocument();
				responseDoc.setHref("cid:" + contentID);
				response.setDocumentHistoryList(responseDoc);
			} else {
				LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);
				String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.getDocumentHistory.success", new Object[] { }, Locale.ENGLISH)));
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

		super.logCurrentRequest(documentId, requestDate, additionalInformationForLog);
		return response;
	}
	
	@Override
	protected Object getResultForGenericException(Exception ex) {
		GetDocumentHistoryResponse response = new GetDocumentHistoryResponse();
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

	private void checkRequest(GetDocumentHistoryRequest request) {
		String errorMessage = null;
		if (request != null) {
			if ((request.getDocumentId() == null) || (request.getDocumentId() <= 0)) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}

	private static void printRequest(GetDocumentHistoryRequest request) {
		LOG.debug("-------- GetDocumentHistoryRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		LOG.debug("------------------------------------------");
	}
}
