package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.ws.mime.Attachment;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.SaveDocumentRequest;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveDocumentResponse;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.FileSplitResult;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "saveDocument", version = "v1")
@Component
public class SaveDocumentEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(SaveDocumentEndpoint.class);
	
	private UserService userService;
	
	private DocumentService documentService;
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		SaveDocumentResponse response = new SaveDocumentResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;
		
		try {
			LOG.debug("SaveDocumentEndpoint.v1 invoked.");
			SaveDocumentRequest request = (SaveDocumentRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
			
			if (applicationRegistered) {
				
				// Kas päringus märgitud isik on teenuse kasutaja
				AditUser user = this.getUserService().getUserByID(this.getHeader().getIsikukood());
				
				if(user != null) {
					if(user.getActive() != null && user.getActive()) {
						
						// Kas infosüsteemil on antud kasutaja jaoks kirjutamisõigus
						int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
						
						if(applicationAccessLevelForUser == 2) {
							
							// Kas kasutajal on piisavalt vaba kettaruumi
							long remainingDiskQuota = this.getUserService().getRemainingDiskQuota(user);
							
							// TODO: check the size of the incoming document and compare it to the remaining disk space
							Iterator<Attachment> i = this.getRequestMessage().getAttachments();
							
							int attachmentCount = 0;
							while(i.hasNext()) {
								if(attachmentCount == 0) {
									Attachment attachment = i.next();
									LOG.debug("Attachment: " + attachment.getContentId());
									
									// Extract the SOAP message to a temporary file
									String base64EncodedFile = extractXML(attachment);
									
									// Base64 decode and unzip the temporary file
									String xmlFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());
									LOG.debug("Attachment unzipped to temporary file: " + xmlFile);
									
									// Extract large files from main document
									Util.splitOutTags(xmlFile, "data", false, false, true, true);
									
									// Unmarshal the XML from the temporary file
									Object unmarshalledObject = unMarshal(xmlFile);
									
									// Check if the marshalling result is what we expected
									if(unmarshalledObject != null) {
										LOG.debug("XML unmarshalled to type: " + unmarshalledObject.getClass());
										if(unmarshalledObject instanceof SaveDocumentRequestAttachment) {
											
											SaveDocumentRequestAttachment document = (SaveDocumentRequestAttachment) unmarshalledObject;
											
											// Check document metadata
											this.getDocumentService().checkAttachedDocumentMetadataForNewDocument(document, remainingDiskQuota, xmlFile, this.getConfiguration().getTempDir());
											
											if(document.getId() != null && document.getId() != 0) {
												// Determine whether or not this document can be modified
												Document doc = this.documentService.getDocumentDAO().getDocument(document.getId());
												runExistingDocumentChecks(doc, user.getUserCode());
												
												LOG.debug("Modifying document. ID: " + document.getId());
												
												// Check document metadata
												// List<String> fileNames = this.getDocumentService().checkAttachedDocumentMetadataForNewDocument(document, remainingDiskQuota, xmlFile, this.getConfiguration().getTempDir());
												
												// Document to database
												documentId = this.getDocumentService().save(document, user.getUserCode(), applicationName);
												LOG.debug("Document saved with ID: " + documentId.toString());
												response.setDocumentId(documentId);
												
											} else {
												LOG.debug("Adding new document. GUID: " + document.getGuid());
												
												// Check document metadata
												// List<String> fileNames = this.getDocumentService().checkAttachedDocumentMetadataForNewDocument(document, remainingDiskQuota, xmlFile, this.getConfiguration().getTempDir());
												
												// Document to database
												documentId = this.getDocumentService().save(document, user.getUserCode(), applicationName);
												LOG.debug("Document saved with ID: " + documentId.toString());
												response.setDocumentId(documentId);
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
							
						} else {
							String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.forUser.write", new Object[] { applicationName, user.getUserCode() }, Locale.ENGLISH);
							throw new AditException(errorMessage);
						}						
					} else {
						String errorMessage = this.getMessageSource().getMessage("user.inactive", new Object[] { this.getHeader().getIsikukood() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
				} else {
					String errorMessage = this.getMessageSource().getMessage("user.nonExistent", new Object[] { this.getHeader().getIsikukood() }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}				
			} else {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Set response messages
			response.setSuccess(new Success(true));
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.saveDocument.success", new Object[] { }, Locale.ENGLISH)));
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
		SaveDocumentResponse response = new SaveDocumentResponse();
		response.setSuccess(new Success(false));
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		return response;
	}
	
	protected void runExistingDocumentChecks(Document existingDoc, String userCode) throws AditException {
		if (!userCode.equalsIgnoreCase(existingDoc.getCreatorCode())) {
			String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { existingDoc.getId(), userCode }, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
		if (existingDoc.getLocked()) {
			String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.locked", new Object[] { existingDoc.getId(), userCode }, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
		if (existingDoc.getDeflated()) {
			String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.deflated", new Object[] { existingDoc.getDeflateDate() }, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
		if (existingDoc.getDeleted()) {
			String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.deleted", new Object[] { }, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}
	
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

}
