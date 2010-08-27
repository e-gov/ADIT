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
		LOG.debug("JoinEndpoint invoked. Version: " + version);

		if (version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
	}
	
	protected Object v1(Object requestObject) {
		SaveDocumentFileResponse response = new SaveDocumentFileResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;
		
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
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid muuta
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel != 2) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.write", new Object[] { applicationName }, Locale.ENGLISH);
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
			
			// Check user's disk quota
			long remainingDiskQuota = this.getUserService().getRemainingDiskQuota(user, this.getConfiguration().getGlobalDiskQuota());

			// Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
			if ((user.getActive() == null) || !user.getActive()) {
				String errorMessage = this.getMessageSource().getMessage("user.inactive", new Object[] { userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check whether or not the application has rights to
			// modify current user's data.
			int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
			if(applicationAccessLevelForUser != 2) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.forUser.write", new Object[] { applicationName, user.getUserCode() }, Locale.ENGLISH);
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
			
			// Check whether the document is marked as signable
			if ((doc.getSignable() == null) || !doc.getSignable()) {
				LOG.debug("Requested document is not signable. Document ID: " + request.getDocumentId());
				String errorMessage = this.getMessageSource().getMessage("document.notSignable", new Object[] { }, Locale.ENGLISH);
				throw new AditException(errorMessage);
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
				String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);

			}
			
			String xmlFile = null;
			Iterator<Attachment> i = this.getRequestMessage().getAttachments();
			int attachmentCount = 0;
			boolean updatedExistingFile = false;
			while(i.hasNext()) {
				if(attachmentCount == 0) {
					Attachment attachment = i.next();
					LOG.debug("Attachment: " + attachment.getContentId());
					
					// Extract the SOAP message to a temporary file
					String base64EncodedFile = extractXML(attachment);
					
					// Base64 decode and unzip the temporary file
					xmlFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());
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
							SaveItemInternalResult saveResult = this.getDocumentService().saveDocumentFile(doc.getId(), docFile, xmlFile, remainingDiskQuota, this.getConfiguration().getTempDir());
							if (saveResult.isSuccess()) {
								long fileId = saveResult.getItemId();
								LOG.debug("File saved with ID: " + fileId);
								response.setFileId(fileId);
							} else {
								if ((saveResult.getMessages() != null) && (saveResult.getMessages().size() > 0)) {
									throw new AditException(saveResult.getMessages().get(0).getValue());
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
					
				} else {
					String errorMessage = this.getMessageSource().getMessage("request.attachments.tooMany", new Object[] { applicationName }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
				attachmentCount++;
			}
			
			// If no attachments were found then throw exception
			if (attachmentCount < 1) {
				String errorMessage = this.getMessageSource().getMessage("request.saveDocumentFile.file.noFilesSupplied", new Object[] { }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// If saving was successful then add history event
			DocumentHistory historyEvent = new DocumentHistory(
				(updatedExistingFile ? DocumentService.HistoryType_ModifyFile : DocumentService.HistoryType_AddFile),
				documentId,
				requestDate.getTime(),
				user,
				xroadRequestUser,
				header);
			this.getDocumentService().getDocumentHistoryDAO().save(historyEvent);
			
			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.saveDocumentFile.success", new Object[] { }, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			additionalInformationForLog = "Request failed: " + e.getMessage();
			super.logError(documentId, requestDate.getTime(), LogService.ErrorLogLevel_Error, e.getMessage());
			
			response.setSuccess(false);
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

			if(e instanceof AditCodedException) {
				LOG.debug("Adding exception messages to response object.");
				arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
			} else if (e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(new Message("en", e.getMessage()));
			} else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
			}

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
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, ex.getMessage());
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
	
	private void checkRequest(SaveDocumentFileRequest request) {
		String errorMessage = null; 
		if(request != null) {
			if(request.getDocumentId() <= 0) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}
	
	private static void printRequest(SaveDocumentFileRequest request) {
		LOG.debug("-------- SaveDocumentFileRequest -------");
		LOG.debug("Document ID: " + request.getDocumentId());
		LOG.debug("----------------------------------------");
	}
}
