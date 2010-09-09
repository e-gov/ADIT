package ee.adit.ws.endpoint.document;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.ws.mime.Attachment;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.exception.AditMultipleException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.SaveDocumentFileRequest;
import ee.adit.pojo.SaveDocumentFileRequestFile;
import ee.adit.pojo.SaveDocumentFileResponse;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.FileSplitResult;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "saveDocumentFile" web method (web service request).
 * Contains request input validation, request-specific workflow
 * and response composition.  
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "saveDocumentFile", version = "v1")
@Component
public class SaveDocumentFileEndpoint extends AbstractAditBaseEndpoint {
	private static Logger LOG = Logger.getLogger(SaveDocumentFileEndpoint.class);
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
		LOG.debug("saveDocumentFile invoked. Version: " + version);

		if (version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
	}
	
	/**
	 * Executes "V1" version of "saveDocumentFile" request.
	 * 
	 * @param requestObject		Request body object
	 * @return					Response body object
	 */
	protected Object v1(Object requestObject) {
		SaveDocumentFileResponse response = new SaveDocumentFileResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;
		boolean updatedExistingFile = false;
		long documentFileID = 0;
		
		try {
			LOG.debug("saveDocumentFile.v1 invoked.");
			SaveDocumentFileRequest request = (SaveDocumentFileRequest) requestObject;
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
			// andmeid muuta
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel != 2) {
				AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.write");
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
			
			// Check user's disk quota
			long remainingDiskQuota = this.getUserService().getRemainingDiskQuota(user, this.getConfiguration().getGlobalDiskQuota());

			// Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
			if ((user.getActive() == null) || !user.getActive()) {
				AditCodedException aditCodedException = new AditCodedException("user.inactive");
				aditCodedException.setParameters(new Object[] { userCode });
				throw aditCodedException;
			}
			
			// Check whether or not the application has rights to
			// modify current user's data.
			int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
			if(applicationAccessLevelForUser != 2) {
				AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.forUser.write");
				aditCodedException.setParameters(new Object[] { applicationName, user.getUserCode() });
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
			
			// Check whether the document is marked as signable
			if ((doc.getSignable() == null) || !doc.getSignable()) {
				LOG.debug("Requested document is not signable. Document ID: " + request.getDocumentId());
				AditCodedException aditCodedException = new AditCodedException("document.notSignable");
				aditCodedException.setParameters(new Object[] {});
				throw aditCodedException;
			}


			// Document can be signed only if:
			// a) document belongs to user
			// b) document is sent or shared to user
			boolean isOwner = false;
			if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
				isOwner = true;
			} else {
				if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
					Iterator it = doc.getDocumentSharings().iterator();
					while (it.hasNext()) {
						DocumentSharing sharing = (DocumentSharing)it.next();
						if (sharing.getUserCode().equalsIgnoreCase(userCode)) {
							isOwner = true;
							break;
						}
					}
				}
			}
			
			if (!isOwner) {
				LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);
				AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
				aditCodedException.setParameters(new Object[] { request.getDocumentId().toString(), userCode });
				throw aditCodedException;

			}
			
			
			
			String attachmentID = null;
			// Check if the attachment ID is specified
			if(request.getFile() != null && request.getFile().getHref() != null && !request.getFile().getHref().trim().equals("")) {
				attachmentID = Util.extractContentID(request.getFile().getHref());
			} else {
				throw new AditCodedException("request.saveDocument.attachment.id.notSpecified");
			}
			
			// All primary checks passed.
			LOG.debug("Processing attachment with id: '" + attachmentID + "'");
			// Extract the SOAP message to a temporary file
			String base64EncodedFile = extractAttachmentXML(this.getRequestMessage(), attachmentID);
			
			// Base64 decode and unzip the temporary file
			String xmlFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());
			LOG.debug("Attachment unzipped to temporary file: " + xmlFile);
			
			// Extract large files from main document
			FileSplitResult splitResult = Util.splitOutTags(xmlFile, "data", false, false, true, true);
			
			// Decode base64-encoded files
			if ((splitResult.getSubFiles() != null) && (splitResult.getSubFiles().size() > 0)) {
				for (String fileName : splitResult.getSubFiles()) {
					String resultFile = Util.base64DecodeFile(fileName, this.getConfiguration().getTempDir());
					// Replace encoded file with decoded file
					(new File(fileName)).delete();
					(new File(resultFile)).renameTo(new File(fileName));
				}
			}
			
			// Unmarshal the XML from the temporary file
			Object unmarshalledObject = unMarshal(xmlFile);
			
			// Check if the marshalling result is what we expected
			if(unmarshalledObject != null) {
				LOG.debug("XML unmarshalled to type: " + unmarshalledObject.getClass());
				if(unmarshalledObject instanceof OutputDocumentFile) {
					OutputDocumentFile docFile = (OutputDocumentFile) unmarshalledObject;
					updatedExistingFile = ((docFile.getId() != null) &&(docFile.getId() > 0)); 
					documentFileID = docFile.getId();
					SaveItemInternalResult saveResult = this.getDocumentService().saveDocumentFile(doc.getId(), docFile, remainingDiskQuota, this.getConfiguration().getTempDir());
					if (saveResult.isSuccess()) {
						long fileId = saveResult.getItemId();
						documentFileID = fileId;
						LOG.debug("File saved with ID: " + fileId);
						response.setFileId(fileId);
					} else {
						if ((saveResult.getMessages() != null) && (saveResult.getMessages().size() > 0)) {
							AditMultipleException aditMultipleException = new AditMultipleException("MultiException");
							aditMultipleException.setMessages(saveResult.getMessages());
							throw aditMultipleException;
						} else {
							throw new AditInternalException("File saving failed!");
						}
					}
				} else {
					throw new AditInternalException("Unmarshalling returned wrong type. Expected " + SaveDocumentRequestAttachment.class + ", got " + unmarshalledObject.getClass());
				}
			} else {
				throw new AditInternalException("Unmarshalling failed for XML in file: " + xmlFile);
			}
			
			// If saving was successful then add history event
			DocumentHistory historyEvent = new DocumentHistory(
				(updatedExistingFile ? DocumentService.HistoryType_ModifyFile : DocumentService.HistoryType_AddFile),
				documentId,
				requestDate.getTime(),
				user,
				xroadRequestUser,
				header);
			historyEvent.setDescription(DocumentService.DocumentHistoryDescription_ModifyFile + documentFileID);
			this.getDocumentService().getDocumentHistoryDAO().save(historyEvent);
			
			// Set response messages
			response.setSuccess(true);
			messages.setMessage(this.getMessageService().getMessages("request.saveDocumentFile.success", new Object[] {  }));
			response.setMessages(messages);
			
			String additionalMessage = this.getMessageService().getMessage("request.saveDocumentFile.success", new Object[] { }, Locale.ENGLISH);
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
			} else if(e instanceof AditMultipleException) {
				AditMultipleException aditMultipleException = (AditMultipleException) e;
				arrayOfMessage.setMessage(aditMultipleException.getMessages());
				if(aditMultipleException.getMessages() != null && aditMultipleException.getMessages().size() > 0) {
					errorMessage = "ERROR: " + aditMultipleException.getMessages().get(0);
				}				
			} else if (e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(new Message("en", e.getMessage()));
				errorMessage = "ERROR: " + e.getMessage();
			}else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
				errorMessage = "ERROR: " + e.getMessage();
			}

			additionalInformationForLog = errorMessage;
			super.logError(documentId, requestDate.getTime(), LogService.ErrorLogLevel_Error, errorMessage);
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
			
			LOG.debug("Adding request attachments to response object.");
			try {
				super.setIgnoreAttachmentHeaders(true);
				boolean cidAdded = false;
				Iterator<Attachment> i = this.getRequestMessage().getAttachments();
				while(i.hasNext()) {
					Attachment attachment = i.next();
					String contentId = attachment.getContentId();
					if ((contentId == null) || (contentId.length() < 1)) {
						contentId = Util.generateRandomID();
					} else {
						contentId = Util.stripContentID(contentId);
					}
					this.getResponseMessage().addAttachment(contentId, attachment.getDataHandler());
					if (!cidAdded) {
						response.setFile(new SaveDocumentFileRequestFile("cid:" + contentId));
						cidAdded = true;
					}
				}
			} catch (Exception ex) {
				LOG.error("Failed sending request attachments back within response object!", ex);
			}
		}
		
		super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object getResultForGenericException(Exception ex) {
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, "ERROR: " + ex.getMessage());
		SaveDocumentFileResponse response = new SaveDocumentFileResponse();
		response.setSuccess(false);
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		LOG.debug("Adding request attachments to response object.");
		try {
			super.setIgnoreAttachmentHeaders(true);
			boolean cidAdded = false;
			Iterator<Attachment> i = this.getRequestMessage().getAttachments();
			while(i.hasNext()) {
				Attachment attachment = i.next();
				String contentId = attachment.getContentId();
				if ((contentId == null) || (contentId.length() < 1)) {
					contentId = Util.generateRandomID();
				}
				this.getResponseMessage().addAttachment(contentId, attachment.getDataHandler());
				if (!cidAdded) {
					response.setFile(new SaveDocumentFileRequestFile("cid:" + contentId));
					cidAdded = true;
				}
			}
		} catch (Exception e) {
			LOG.error("Failed sending request attachments back within response object!", ex);
		}
		return response;
	}
	
	/**
	 * Validates request body and makes sure that all
	 * required fields exist and are not empty.
	 * <br><br>
	 * Throws {@link AditCodedException} if any errors in request data are found.
	 * 
	 * @param request				Request body as {@link SaveDocumentFileRequest} object.
	 * @throws AditCodedException	Exception describing error found in requet body.
	 */
	private void checkRequest(SaveDocumentFileRequest request) throws AditCodedException {
		if(request != null) {
			if(request.getDocumentId() <= 0) {
				throw new AditCodedException("request.body.undefined.documentId");
			}
		} else {
			throw new AditCodedException("request.body.empty");
		}
	}
	
	/**
	 * Writes request parameters to application DEBUG log.
	 * 
	 * @param request	Request body as {@link SaveDocumentFileRequest} object.
	 */
	private void printRequest(SaveDocumentFileRequest request) {
		LOG.debug("-------- SaveDocumentFileRequest -------");
		LOG.debug("Document ID: " + request.getDocumentId());
		LOG.debug("----------------------------------------");
	}
}
