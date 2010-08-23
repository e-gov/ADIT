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
import ee.adit.exception.AditCodedException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.ConfirmSignatureRequest;
import ee.adit.pojo.ConfirmSignatureResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
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
	protected Object invokeInternal(Object requestObject, int version) {
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
				aditCodedException.setParameters(new Object[] { request.getDocumentId() });
				throw aditCodedException;
			}
			
			// Check whether the document is marked as deleted
			if ((doc.getDeleted() != null) && doc.getDeleted()) {
				LOG.debug("Requested document is deleted. Document ID: " + request.getDocumentId());				
				AditCodedException aditCodedException = new AditCodedException("document.deleted");
				aditCodedException.setParameters(new Object[] { request.getDocumentId() });
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
				aditCodedException.setParameters(new Object[] { });
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
						AditCodedException aditCodedException = new AditCodedException("request.attachments.tooMany");
						aditCodedException.setParameters(new Object[] { applicationName });
						throw aditCodedException;
					}
					attachmentCount++;
				}
				
				if (signatureFile == null) {					
					AditCodedException aditCodedException = new AditCodedException("request.confirmSignature.missingSignature");
					aditCodedException.setParameters(new Object[] { doc.getDeflateDate() });
					throw aditCodedException;
				}
				
				this.documentService.confirmSignature(
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
				AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
				aditCodedException.setParameters(new Object[] { request.getDocumentId(), userCode });
				throw aditCodedException;
			}

			// Set response messages
			response.setSuccess(true);
			messages.setMessage(this.getMessageService().getMessages("request.confirmSignature.success", new Object[] { }));
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
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, ex.getMessage());
		ConfirmSignatureResponse response = new ConfirmSignatureResponse();
		response.setSuccess(false);
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		return response;
	}

	private void checkHeader(CustomXTeeHeader header) throws Exception {
		if(header != null) {
			if ((header.getIsikukood() == null) || (header.getIsikukood().length() < 1)) {
				throw new AditCodedException("request.header.undefined.personalCode");
			} else if ((header.getInfosysteem() == null) || (header.getInfosysteem().length() < 1)) {
				throw new AditCodedException("request.header.undefined.systemName");
			} else if ((header.getAsutus() == null) || (header.getAsutus().length() < 1)) {
				throw new AditCodedException("request.header.undefined.institution");
			}
		}
	}
	
	private void checkRequest(ConfirmSignatureRequest request) {
		if(request != null) {
			if(request.getDocumentId() <= 0) {
				throw new AditCodedException("request.body.undefined.documentId");
			}
		} else {
			throw new AditCodedException("request.body.empty");
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
