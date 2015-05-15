package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Date;
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
import ee.adit.exception.AditInternalException;
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

/**
 * Implementation of "confirmSignature" web method (web service request).
 * Contains request input validation, request-specific workflow
 * and response composition.  
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
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

	@Override
	protected Object invokeInternal(Object requestObject, int version) throws Exception {
		LOG.debug("confirmSignature invoked. Version: " + version);

		if (version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
	}
	
	/**
	 * Executes "V1" version of "confirmSignature" request.
	 * 
	 * @param requestObject		Request body object
	 * @return					Response body object
	 */
	protected Object v1(Object requestObject) {
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
					Iterator<DocumentSharing> it = doc.getDocumentSharings().iterator();
					while (it.hasNext()) {
						DocumentSharing sharing = it.next();
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
				
				String attachmentID = null;
				// Check if the attachment ID is specified
				if(request.getSignature() != null && request.getSignature().getHref() != null && !request.getSignature().getHref().trim().equals("")) {
					attachmentID = Util.extractContentID(request.getSignature().getHref());
				} else {
					throw new AditCodedException("request.saveDocument.attachment.id.notSpecified");
				}
				
				// All primary checks passed.
				LOG.debug("Processing attachment with id: '" + attachmentID + "'");
				// Extract the SOAP message to a temporary file
				String base64EncodedFile = extractAttachmentXML(this.getRequestMessage(), attachmentID);
				
				// Base64 decode and unzip the temporary file
				signatureFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());
				LOG.debug("Attachment unzipped to temporary file: " + signatureFile);
				
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
				aditCodedException.setParameters(new Object[] { request.getDocumentId().toString(), userCode });
				throw aditCodedException;
			}

			// Set response messages
			response.setSuccess(true);
			messages.setMessage(this.getMessageService().getMessages("request.confirmSignature.success", new Object[] { }));
			response.setMessages(messages);
			
			String additionalMessage = this.getMessageService().getMessage("request.confirmSignature.success", new Object[] {}, Locale.ENGLISH);
			additionalInformationForLog = LogService.RequestLog_Success + ": " + additionalMessage;
			
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			String errorMessage = null;			
			response.setSuccess(false);
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

			if(e instanceof AditCodedException) {
				LOG.debug("Adding exception messages to response object.");
				arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
				errorMessage = this.getMessageService().getMessage(e.getMessage(), ((AditCodedException) e).getParameters(), Locale.ENGLISH);
				errorMessage = "ERROR: " + errorMessage;
			} else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
				errorMessage = "ERROR: " + e.getMessage();
			}

			additionalInformationForLog = errorMessage;
			super.logError(documentId, requestDate.getTime(), LogService.ErrorLogLevel_Error, errorMessage);
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
		return response;
	}
	
	@Override
	protected Object getResultForGenericException(Exception ex) {
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, "ERROR: " + ex.getMessage());
		ConfirmSignatureResponse response = new ConfirmSignatureResponse();
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
	 * @param request				Request body as {@link ConfirmSignatureRequest} object.
	 * @throws AditCodedException	Exception describing error found in requet body.
	 */
	private void checkRequest(ConfirmSignatureRequest request) throws AditCodedException {
		if(request != null) {
			if (request.getDocumentId() <= 0) {
				throw new AditCodedException("request.body.undefined.documentId");
			}
		} else {
			throw new AditCodedException("request.body.empty");
		}
	}
	
	/**
	 * Writes request parameters to application DEBUG log.
	 * 
	 * @param request	Request body as {@link ConfirmSignatureRequest} object.
	 */
	private void printRequest(ConfirmSignatureRequest request) {
		LOG.debug("-------- ConfirmSignatureRequest -------");
		LOG.debug("Document ID: " + request.getDocumentId());
		if (request.getSignature() != null) {
			LOG.debug("Signature HREF: " + request.getSignature().getHref());
		}
		LOG.debug("----------------------------------------");
	}
}
