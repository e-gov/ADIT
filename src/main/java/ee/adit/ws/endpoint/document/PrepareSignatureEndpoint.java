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
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.PrepareSignatureInternalResult;
import ee.adit.pojo.PrepareSignatureRequest;
import ee.adit.pojo.PrepareSignatureResponse;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "prepareSignature" web method (web service request).
 * Contains request input validation, request-specific workflow
 * and response composition.  
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "prepareSignature", version = "v1")
@Component
public class PrepareSignatureEndpoint extends AbstractAditBaseEndpoint {
	private static Logger LOG = Logger.getLogger(PrepareSignatureEndpoint.class);
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
		LOG.debug("prepareSignature invoked. Version: " + version);

		if (version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
	}
	
	/**
	 * Executes "V1" version of "prepareSignature" request.
	 * 
	 * @param requestObject		Request body object
	 * @return					Response body object
	 */
	protected Object v1(Object requestObject) {
		PrepareSignatureResponse response = new PrepareSignatureResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;
		
		try {
			LOG.debug("prepareSignature.v1 invoked.");
			PrepareSignatureRequest request = (PrepareSignatureRequest) requestObject;
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
			// b) document is sent or shared to user for signing
			boolean isOwner = false;
			if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
				isOwner = true;
			} else {
				if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
					Iterator it = doc.getDocumentSharings().iterator();
					while (it.hasNext()) {
						DocumentSharing sharing = (DocumentSharing)it.next();
						if (sharing.getUserCode().equalsIgnoreCase(userCode) && DocumentService.SharingType_Sign.equalsIgnoreCase(sharing.getDocumentSharingType())) {
							isOwner = true;
							break;
						}
					}
				}
			}
			
			if (isOwner) {
				// Get user certificate from attachment
				String certFile = null;
				
				String attachmentID = null;
				// Check if the attachment ID is specified
				if(request.getSignerCertificate() != null && request.getSignerCertificate().getHref() != null && !request.getSignerCertificate().getHref().trim().equals("")) {
					attachmentID = Util.extractContentID(request.getSignerCertificate().getHref());
				} else {
					throw new AditCodedException("request.saveDocument.attachment.id.notSpecified");
				}
				
				// All primary checks passed.
				LOG.debug("Processing attachment with id: '" + attachmentID + "'");
				// Extract the SOAP message to a temporary file
				String base64EncodedFile = extractAttachmentXML(this.getRequestMessage(), attachmentID);
				
				// Base64 decode and unzip the temporary file
				certFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());
				LOG.debug("Attachment unzipped to temporary file: " + certFile);
				
				if (certFile == null) {
					AditCodedException aditCodedException = new AditCodedException("request.prepareSignature.missingCertificate");
					aditCodedException.setParameters(new Object[] {});
					throw aditCodedException;
				}
				
				PrepareSignatureInternalResult sigResult = this.documentService.prepareSignature(
						doc.getId(),
						request.getManifest(),
						request.getCountry(),
						request.getState(),
						request.getCity(),
						request.getZip(),
						certFile,
						digidocConfigurationFile.getFile().getAbsolutePath(),
						this.getConfiguration().getTempDir(),
						xroadRequestUser);
				
				if (sigResult.isSuccess()) {
					response.setSignatureHash(sigResult.getSignatureHash());
				} else {
					AditCodedException aditCodedException = new AditCodedException(sigResult.getErrorCode());
					throw aditCodedException;
				}
			} else {
				LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);
				AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
				aditCodedException.setParameters(new Object[] { request.getDocumentId().toString(), userCode });
				throw aditCodedException;
			}

			// Set response messages
			response.setSuccess(true);
			messages.setMessage(this.getMessageService().getMessages("request.prepareSignature.success", new Object[] {  }));
			response.setMessages(messages);
			
			String additionalMessage = this.getMessageService().getMessage("request.prepareSignature.success", new Object[] { }, Locale.ENGLISH);
			additionalInformationForLog = LogService.RequestLog_Success + ": " + additionalMessage;
			
		} catch (Exception e) {
			String errorMessage = null;
			LOG.error("Exception: ", e);
			additionalInformationForLog = "Request failed: " + e.getMessage();			
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

			super.logError(documentId, requestDate.getTime(), LogService.ErrorLogLevel_Error, errorMessage);
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
		return response;
	}

	@Override
	protected Object getResultForGenericException(Exception ex) {
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, ex.getMessage());
		PrepareSignatureResponse response = new PrepareSignatureResponse();
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
	 * @param request				Request body as {@link PrepareSignatureRequest} object.
	 * @throws AditCodedException	Exception describing error found in requet body.
	 */
	private void checkRequest(PrepareSignatureRequest request) throws AditCodedException {
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
	 * @param request	Request body as {@link PrepareSignatureRequest} object.
	 */
	private void printRequest(PrepareSignatureRequest request) {
		LOG.debug("-------- PrepareSignatureRequest -------");
		LOG.debug("Document ID: " + request.getDocumentId());
		LOG.debug("Role/resolution: " + request.getManifest());
		LOG.debug("Country: " + request.getCountry());
		LOG.debug("State: " + request.getState());
		LOG.debug("City: " + request.getCity());
		LOG.debug("Zip: " + request.getZip());
		LOG.debug("----------------------------------------");
	}
}
