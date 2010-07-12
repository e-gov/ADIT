package ee.adit.ws.endpoint.document;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetDocumentListRequest;
import ee.adit.pojo.GetDocumentListResponse;
import ee.adit.pojo.GetDocumentListResponseAttachment;
import ee.adit.pojo.GetDocumentListResponseList;
import ee.adit.pojo.Message;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

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
	protected Object invokeInternal(Object requestObject) throws Exception {
		GetDocumentListResponse response = new GetDocumentListResponse();
		ArrayOfMessage messages = new ArrayOfMessage();

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
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid näha
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel < 1) {
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

			GetDocumentListResponseAttachment att = this.documentService.getDocumentDAO().getDocumentSearchResult(
					request,
					userCode,
					this.getConfiguration().getTempDir(),
					this.getMessageSource().getMessage("files.nonExistentOrDeleted", new Object[] { }, Locale.ENGLISH));
			
			if ((att.getDocumentList() != null) && !att.getDocumentList().isEmpty()) {
				// 1. Convert java list to XML string and output to file
				String xmlFile = marshal(att);
				
				// 2. GZip and Base64 encode the temporary file
				String gzipFileName = Util.gzipAndBase64Encode(xmlFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());

				// 3. Add as an attachment
				String contentID = addAttachment(gzipFileName);
				GetDocumentListResponseList responseList = new GetDocumentListResponseList();
				responseList.setHref("cid:" + contentID);
				response.setDocumentList(responseList);				
			} else {
				LOG.debug("No documents were found!");
			}
			
			
			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.getDocumentList.success",	new Object[] {}, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
			LOG.error("Exception: ", e);
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

		return response;
	}

	private void checkHeader(CustomXTeeHeader header) throws Exception {
		String errorMessage = null;
		if (header != null) {
			if ((header.getIsikukood() == null)	|| (header.getIsikukood().length() < 1)) {
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

	private void checkRequest(GetDocumentListRequest request) {
		String errorMessage = null;
		if (request == null) {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}

	private static void printRequest(GetDocumentListRequest request) {
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
