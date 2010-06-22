package ee.adit.ws.endpoint.document;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.ws.mime.Attachment;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetUserInfoRequestAttachmentUserList;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.SaveDocumentRequest;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveDocumentResponse;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.adit.ws.endpoint.user.JoinEndpoint;
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
									
									// Unmarshal the XML from the temporary file
									Object unmarshalledObject = unMarshal(xmlFile);
									
									// Check if the marshalling result is what we expected
									if(unmarshalledObject != null) {
										LOG.debug("XML unmarshalled to type: " + unmarshalledObject.getClass());
										if(unmarshalledObject instanceof SaveDocumentRequestAttachment) {
											
											SaveDocumentRequestAttachment document = (SaveDocumentRequestAttachment) unmarshalledObject;
											
											if(document.getId() != null && document.getId() != 0) {
												LOG.debug("Modifying document. ID: " + document.getId());
												
												//TODO: implement
												
											} else {
												LOG.debug("Saving document. GUID: " + document.getGuid());
												
												// Check document metadata
												List<String> fileNames = this.getDocumentService().checkAttachedDocumentMetadataForNewDocument(document, remainingDiskQuota, xmlFile, this.getConfiguration().getTempDir());
												
												// TODO: Document to database
												this.getDocumentService().save(document, fileNames);
												
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
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			response.setSuccess(new Success(false));
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(e.getMessage());
			} else {
				arrayOfMessage.getMessage().add("Service error");
			}
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		
		return response;
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
