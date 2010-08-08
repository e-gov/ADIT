package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.DeleteDocumentFileRequest;
import ee.adit.pojo.DeleteDocumentFileResponse;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "deleteDocumentFile", version = "v1")
@Component
public class DeleteDocumentFileEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(DeleteDocumentFileEndpoint.class);
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
		DeleteDocumentFileResponse response = new DeleteDocumentFileResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;
		
		try {
			LOG.debug("deleteDocumentFile.v1 invoked.");
			DeleteDocumentFileRequest request = (DeleteDocumentFileRequest) requestObject;
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
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
			if (!applicationRegistered) {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid muuta (või üldse näha)
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if(accessLevel != 2) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.write", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Kontrollime, kas päringus märgitud isik on teenuse kasutaja
			String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader().getIsikukood(); 
			AditUser user = this.getUserService().getUserByID(userCode);
			if(user == null) {
				String errorMessage = this.getMessageSource().getMessage("user.nonExistent", new Object[] { userCode }, Locale.ENGLISH);
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
			if((user.getActive() == null) || !user.getActive()) {
				String errorMessage = this.getMessageSource().getMessage("user.inactive", new Object[] { userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
				
			Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());
			
			// Kontrollime, kas ID-le vastav dokument on olemas
			if (doc != null) {
				// Kontrollime, kas dokument kuulub päringu käivitanud kasutajale
				if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
					// Make sure that the document is not deleted
					// NB! doc.getDeleted() can be NULL
					if ((doc.getDeleted() != null) && doc.getDeleted()) {
						String errorMessage = this.getMessageSource().getMessage("document.deleted", new Object[] { documentId }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
					
					// Make sure that the document is not locked
					// NB! doc.getLocked() can be NULL
					if ((doc.getLocked() != null) && doc.getLocked()) {
						String errorMessage = this.getMessageSource().getMessage("request.deleteDocumentFile.document.locked", new Object[] { documentId }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
					
					String resultCode = this.documentService.deflateDocumentFile(request.getDocumentId(), request.getFileId(), true);
					if (resultCode.equalsIgnoreCase("already_deleted")) {
						String errorMessage = this.getMessageSource().getMessage("file.isDeleted", new Object[] { request.getFileId() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					} else if (resultCode.equalsIgnoreCase("file_does_not_exist")) {
						String errorMessage = this.getMessageSource().getMessage("file.nonExistent", new Object[] { request.getFileId() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					} else if (resultCode.equalsIgnoreCase("file_does_not_belong_to_document")) {
						String errorMessage = this.getMessageSource().getMessage("file.doesNotBelongToDocument", new Object[] { request.getFileId(), request.getDocumentId() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
				} else {
					String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
			} else {
				String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// If deletion was successful then add history event
			DocumentHistory historyEvent = new DocumentHistory(
				DocumentService.HistoryType_DeleteFile,
				documentId,
				requestDate.getTime(),
				user,
				xroadRequestUser,
				header);
			this.getDocumentService().getDocumentHistoryDAO().save(historyEvent);
			
			// Set response messages
			response.setSuccess(new Success(true));
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.deleteDocumentFile.success", new Object[] { }, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			additionalInformationForLog = "Request failed: " + e.getMessage();
			response.setSuccess(new Success(false));
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditException) {
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
		DeleteDocumentFileResponse response = new DeleteDocumentFileResponse();
		response.setSuccess(new Success(false));
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		return response;
	}
	
	private void checkHeader(CustomXTeeHeader header) throws Exception {
		String errorMessage = null;
		if(header != null) {
			if ((header.getIsikukood() == null) || (header.getIsikukood().length() < 1)) {
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
	
	private void checkRequest(DeleteDocumentFileRequest request) {
		String errorMessage = null; 
		if(request != null) {
			if (request.getDocumentId() <= 0) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if (request.getDocumentId() <= 0) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.fileId", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}
	
	private static void printRequest(DeleteDocumentFileRequest request) {
		LOG.debug("-------- DeleteDocumentFileRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		LOG.debug("File ID: " + String.valueOf(request.getFileId()));
		LOG.debug("------------------------------------------");
	}
}
