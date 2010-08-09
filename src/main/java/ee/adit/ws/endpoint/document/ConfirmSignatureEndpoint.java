package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.ws.mime.Attachment;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.ConfirmSignatureRequest;
import ee.adit.pojo.ConfirmSignatureResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "confirmSignature", version = "v1")
@Component
public class ConfirmSignatureEndpoint extends AbstractAditBaseEndpoint {
	private static Logger LOG = Logger.getLogger(ConfirmSignatureEndpoint.class);
	private UserService userService;
	private DocumentService documentService;
	private Resource digidocConfigurationFile;

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
	
	public Resource getDigidocConfigurationFile() {
		return digidocConfigurationFile;
	}

	public void setDigidocConfigurationFile(Resource digidocConfigurationFile) {
		this.digidocConfigurationFile = digidocConfigurationFile;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object invokeInternal(Object requestObject) {
		ConfirmSignatureResponse response = new ConfirmSignatureResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;
		
		try {
			LOG.debug("confirmSignature.v1 invoked.");
			ConfirmSignatureRequest request = (ConfirmSignatureRequest) requestObject;
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
				String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
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
			
			if (isOwner) {
				// Get user signature from attachment
				String signatureFile = null;
				Iterator<Attachment> i = this.getRequestMessage().getAttachments();
				int attachmentCount = 0;
				while(i.hasNext()) {
					if(attachmentCount == 0) {
						Attachment attachment = i.next();
						LOG.debug("Attachment: " + attachment.getContentId());
						
						// Extract the SOAP message to a temporary file
						String base64EncodedFile = extractXML(attachment);
						
						// Base64 decode and unzip the temporary file
						signatureFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());
						LOG.debug("Attachment unzipped to temporary file: " + signatureFile);
					} else {
						String errorMessage = this.getMessageSource().getMessage("request.attachments.tooMany", new Object[] { applicationName }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
					attachmentCount++;
				}
				
				if (signatureFile == null) {
					String errorMessage = this.getMessageSource().getMessage("request.confirmSignature.missingSignature", new Object[] { doc.getDeflateDate() }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
				
				this.documentService.getDocumentDAO().confirmSignature(
						doc.getId(),
						signatureFile,
						header.getIsikukood(),
						digidocConfigurationFile.getFile().getAbsolutePath(),
						this.getConfiguration().getTempDir());
				
				// Send scheduler notification to document owner.
				// Notification does not need to be sent if user signed
				// his/her own document.
				if (!user.getUserCode().equalsIgnoreCase(doc.getCreatorCode())) {
					AditUser docCreator = this.getUserService().getUserByID(doc.getCreatorCode());
					if ((docCreator != null) && (userService.findNotification(docCreator.getUserNotifications(), ScheduleClient.NotificationType_Sign) != null)) {
						ScheduleClient.addEvent(
							docCreator,
							this.getMessageSource().getMessage("scheduler.message.sign", new Object[] { doc.getTitle(), docCreator.getUserCode() }, Locale.ENGLISH),
							this.getConfiguration().getSchedulerEventTypeName(),
							requestDate,
							ScheduleClient.NotificationType_Sign,
							doc.getId(),
							this.userService);
					}
				}
			} else {
				LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);
				String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.confirmSignature.success", new Object[] { }, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			additionalInformationForLog = "Request failed: " + e.getMessage();
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
		ConfirmSignatureResponse response = new ConfirmSignatureResponse();
		response.setSuccess(false);
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
	
	private void checkRequest(ConfirmSignatureRequest request) {
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
	
	private static void printRequest(ConfirmSignatureRequest request) {
		LOG.debug("-------- ConfirmSignatureRequest -------");
		LOG.debug("Document ID: " + request.getDocumentId());
		if (request.getSignature() != null) {
			LOG.debug("Signature HREF: " + request.getSignature().getHref());
		}
		LOG.debug("----------------------------------------");
	}
}
