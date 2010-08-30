package ee.adit.ws.endpoint.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetDocumentListRequest;
import ee.adit.pojo.GetDocumentListResponse;
import ee.adit.pojo.GetDocumentListResponseAttachment;
import ee.adit.pojo.GetDocumentListResponseList;
import ee.adit.pojo.Message;
import ee.adit.pojo.OutputDocument;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "getDocumentList" web method (web service request).
 * Contains request input validation, request-specific workflow
 * and response composition.  
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "getDocumentList", version = "v1")
@Component
public class GetDocumentListEndpoint extends AbstractAditBaseEndpoint {
	
	private static Logger LOG = Logger.getLogger(GetDocumentListEndpoint.class);
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
		LOG.debug("getDocumentList invoked. Version: " + version);

		if (version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
	}
	
	/**
	 * Executes "V1" version of "getDocumentList" request.
	 * 
	 * @param requestObject		Request body object
	 * @return					Response body object
	 */
	protected Object v1(Object requestObject) {
		GetDocumentListResponse response = new GetDocumentListResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		List<Long> documentIdList = new ArrayList<Long>();

		try {
			LOG.debug("getDocumentList.v1 invoked.");
			GetDocumentListRequest request = (GetDocumentListRequest) requestObject;
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
			// andmeid näha
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel < 1) {
				AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.read");
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
			// read current user's data.
			int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
			if(applicationAccessLevelForUser < 1) {
				AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.forUser.read");
				aditCodedException.setParameters(new Object[] { applicationName, user.getUserCode() });
				throw aditCodedException;
			}

			GetDocumentListResponseAttachment att = this.documentService.getDocumentDAO().getDocumentSearchResult(
					request,
					userCode,
					this.getConfiguration().getTempDir(),
					this.getMessageSource().getMessage("files.nonExistentOrDeleted", new Object[] { }, Locale.ENGLISH),
					user.getUserCode());
			
			if ((att.getDocumentList() != null) && !att.getDocumentList().isEmpty()) {
				// Remember document ID-s for logging
				for (OutputDocument outputDoc : att.getDocumentList()) {
					documentIdList.add(outputDoc.getId());
				}
				
				// 1. Convert java list to XML string and output to file
				String xmlFile = marshal(att);
				
				// 2. GZip the temporary file
				// Base64 encoding will be done at SOAP envelope level
				String gzipFileName = Util.gzipFile(xmlFile, this.getConfiguration().getTempDir());

				// 3. Add as an attachment
				String contentID = addAttachment(gzipFileName);
				GetDocumentListResponseList responseList = new GetDocumentListResponseList();
				responseList.setHref("cid:" + contentID);
				response.setDocumentList(responseList);				
			} else {
				AditCodedException aditCodedException = new AditCodedException("request.getDocumentList.noDocumentsFound");
				aditCodedException.setParameters(new Object[] { userCode });
				throw aditCodedException;
			}
			
			
			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.getDocumentList.success",	new Object[] {}, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
			additionalInformationForLog = "Request failed: " + e.getMessage();
			LOG.error("Exception: ", e);
			super.logError(null, requestDate.getTime(), LogService.ErrorLogLevel_Error, e.getMessage());
			
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
		}

		super.logCurrentRequest(null, requestDate.getTime(), additionalInformationForLog);
		
		// Log metadata download
		if ((documentIdList == null) || (documentIdList.size() < 1)) {
			for (Long documentId : documentIdList) {
				super.logMetadataRequest(documentId, requestDate.getTime());
			}
		}
		
		return response;
	}

	@Override
	protected Object getResultForGenericException(Exception ex) {
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, ex.getMessage());
		GetDocumentListResponse response = new GetDocumentListResponse();
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
	 * @param request				Request body as {@link GetDocumentListRequest} object.
	 * @throws AditCodedException	Exception describing error found in requet body.
	 */
	private void checkRequest(GetDocumentListRequest request) throws AditCodedException {
		if (request != null) {
			if ((request.getFolder() != null) && (request.getFolder().length() > 0)) {
				if (!request.getFolder().equalsIgnoreCase("incoming")
					&& !request.getFolder().equalsIgnoreCase("outgoing")
					&& !request.getFolder().equalsIgnoreCase("local")) {
					throw new AditCodedException("request.getDocumentList.incorrectFolderName");
				}
			}
		} else {
			throw new AditCodedException("request.body.empty");
		}
	}

	/**
	 * Writes request parameters to application DEBUG log.
	 * 
	 * @param request	Request body as {@link GetDocumentListRequest} object.
	 */
	private void printRequest(GetDocumentListRequest request) {
		LOG.debug("-------- GetDocumentListRequest -------");
		LOG.debug("Folder: " + request.getFolder());
		if ((request.getDocumentTypes() != null) && (request.getDocumentTypes().getDocumentType() != null) && !request.getDocumentTypes().getDocumentType().isEmpty()) {
			for (String documentType : request.getDocumentTypes().getDocumentType()) {
				LOG.debug("Document type: " + documentType);
			}
		}
		if ((request.getDocumentDvkStatuses() != null) && (request.getDocumentDvkStatuses().getStatusId() != null) && !request.getDocumentDvkStatuses().getStatusId().isEmpty()) {
			for (Long dvkStatus : request.getDocumentDvkStatuses().getStatusId()) {
				LOG.debug("Document DVK status: " + dvkStatus);
			}
		}
		if ((request.getDocumentWorkflowStatuses() != null) && (request.getDocumentWorkflowStatuses().getStatusId() != null) && !request.getDocumentWorkflowStatuses().getStatusId().isEmpty()) {
			for (Long wfStatus : request.getDocumentWorkflowStatuses().getStatusId()) {
				LOG.debug("Document WF status: " + wfStatus);
			}
		}
		if ((request.getCreatorApplications() != null) && (request.getCreatorApplications().getCreatorApplication() != null) && !request.getCreatorApplications().getCreatorApplication().isEmpty()) {
			for (String app : request.getCreatorApplications().getCreatorApplication()) {
				LOG.debug("Creator applicatione: " + app);
			}
		}
		LOG.debug("Search phrase: " + request.getSearchPhrase());
		LOG.debug("Max results: " + request.getMaxResults());
		LOG.debug("Start indexr: " + request.getStartIndex());
		LOG.debug("---------------------------------------");
	}
}
