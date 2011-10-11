package ee.adit.ws.endpoint.document;

import java.io.InputStream;
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
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "getDocumentList" web method (web service request).
 * Contains request input validation, request-specific workflow and response
 * composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "getDocumentList", version = "v1")
@Component
public class GetDocumentListEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(GetDocumentListEndpoint.class);

    private UserService userService;
    private DocumentService documentService;
    private String digidocConfigurationFile;


    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("getDocumentList invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "getDocumentList" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        GetDocumentListResponse response = new GetDocumentListResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        List<Long> documentIdList = new ArrayList<Long>();

        try {
            logger.debug("getDocumentList.v1 invoked.");
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
                aditCodedException.setParameters(new Object[] {applicationName });
                throw aditCodedException;
            }

            // Kontrollime, kas päringu käivitanud infosüsteem tohib
            // andmeid näha
            int accessLevel = this.getUserService().getAccessLevel(applicationName);
            if (accessLevel < 1) {
                AditCodedException aditCodedException = new AditCodedException(
                        "application.insufficientPrivileges.read");
                aditCodedException.setParameters(new Object[] {applicationName });
                throw aditCodedException;
            }

            // Kontrollime, kas päringus märgitud isik on teenuse kasutaja
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());

            // Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja
            // lahkunud)
            if ((user.getActive() == null) || !user.getActive()) {
                AditCodedException aditCodedException = new AditCodedException("user.inactive");
                aditCodedException.setParameters(new Object[] {user.getUserCode() });
                throw aditCodedException;
            }

            // Check whether or not the application has rights to
            // read current user's data.
            int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
            if (applicationAccessLevelForUser < 1) {
                AditCodedException aditCodedException = new AditCodedException(
                        "application.insufficientPrivileges.forUser.read");
                aditCodedException.setParameters(new Object[] {applicationName, user.getUserCode()});
                throw aditCodedException;
            }

            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(getDigidocConfigurationFile());
            String jdigidocCfgTmpFile = Util.createTemporaryFile(input, getConfiguration().getTempDir());

            GetDocumentListResponseAttachment att = this.documentService.getDocumentDAO().getDocumentSearchResult(
                    request, user.getUserCode(), this.getConfiguration().getTempDir(),
                    this.getMessageSource().getMessage("files.nonExistentOrDeleted", new Object[] {}, Locale.ENGLISH),
                    user.getUserCode(), getConfiguration().getDocumentRetentionDeadlineDays(), jdigidocCfgTmpFile);

            if (att != null) {
                // Remember document ID-s for logging
                if (att.getDocumentList() != null) {
	            	for (OutputDocument outputDoc : att.getDocumentList()) {
	                    documentIdList.add(outputDoc.getId());
	                }
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
            }

            // Set response messages
            response.setSuccess(true);
            if ((att != null) && (att.getTotal() > 0)) {
	            messages.setMessage(this.getMessageService().getMessages("request.getDocumentList.success", new Object[] {}));
	            response.setMessages(messages);
            } else {
	            messages.setMessage(this.getMessageService().getMessages("request.getDocumentList.noDocumentsFound", new Object[] {user.getUserCode()}));
	            response.setMessages(messages);
            }

            if ((att != null) && (att.getTotal() > 0)) {
	            String additionalMessage = this.getMessageService().getMessage("request.getDocumentList.success", new Object[] {}, Locale.ENGLISH);
	            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;
            } else {
	            String additionalMessage = this.getMessageService().getMessage("request.getDocumentList.noDocumentsFound", new Object[] {user.getUserCode()}, Locale.ENGLISH);
	            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;
            }

        } catch (Exception e) {
            String errorMessage = null;
            logger.error("Exception: ", e);
            response.setSuccess(false);
            ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

            if (e instanceof AditCodedException) {
                logger.debug("Adding exception messages to response object.");
                arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
                errorMessage = this.getMessageService().getMessage(e.getMessage(),
                        ((AditCodedException) e).getParameters(), Locale.ENGLISH);
                errorMessage = "ERROR: " + errorMessage;
            } else if (e instanceof AditException) {
                logger.debug("Adding exception message to response object.");
                arrayOfMessage.getMessage().add(new Message("en", e.getMessage()));
                errorMessage = "ERROR: " + e.getMessage();
            } else {
            	arrayOfMessage.setMessage(this.getMessageService().getMessages(MessageService.GENERIC_ERROR_CODE, new Object[]{}));
                errorMessage = "ERROR: " + e.getMessage();
            }

            additionalInformationForLog = errorMessage;
            super.logError(null, requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);

            logger.debug("Adding exception messages to response object.");
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
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: "
                + ex.getMessage());
        GetDocumentListResponse response = new GetDocumentListResponse();
        response.setSuccess(false);
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        return response;
    }

    /**
     * Validates request body and makes sure that all required fields exist and
     * are not empty. <br>
     * <br>
     * Throws {@link AditCodedException} if any errors in request data are
     * found.
     *
     * @param request
     *            Request body as {@link GetDocumentListRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in requet body.
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
     * @param request
     *            Request body as {@link GetDocumentListRequest} object.
     */
    private void printRequest(GetDocumentListRequest request) {
        logger.debug("-------- GetDocumentListRequest -------");
        logger.debug("Folder: " + request.getFolder());
        if ((request.getDocumentTypes() != null) && (request.getDocumentTypes().getDocumentType() != null)
                && !request.getDocumentTypes().getDocumentType().isEmpty()) {
            for (String documentType : request.getDocumentTypes().getDocumentType()) {
                logger.debug("Document type: " + documentType);
            }
        }
        if ((request.getFileTypes() != null) && (request.getFileTypes().getFileType() != null)) {
        	for (String fileType : request.getFileTypes().getFileType()) {
        		logger.debug("File type: " + fileType);
        	}
        }
        if ((request.getDocumentDvkStatuses() != null) && (request.getDocumentDvkStatuses().getStatusId() != null)
                && !request.getDocumentDvkStatuses().getStatusId().isEmpty()) {
            for (Long dvkStatus : request.getDocumentDvkStatuses().getStatusId()) {
                logger.debug("Document DVK status: " + dvkStatus);
            }
        }
        if ((request.getDocumentWorkflowStatuses() != null)
                && (request.getDocumentWorkflowStatuses().getStatusId() != null)
                && !request.getDocumentWorkflowStatuses().getStatusId().isEmpty()) {
            for (Long wfStatus : request.getDocumentWorkflowStatuses().getStatusId()) {
                logger.debug("Document WF status: " + wfStatus);
            }
        }
        if ((request.getCreatorApplications() != null)
                && (request.getCreatorApplications().getCreatorApplication() != null)
                && !request.getCreatorApplications().getCreatorApplication().isEmpty()) {
            for (String app : request.getCreatorApplications().getCreatorApplication()) {
                logger.debug("Creator applicatione: " + app);
            }
        }
        logger.debug("Search phrase: " + request.getSearchPhrase());
        logger.debug("Max results: " + request.getMaxResults());
        logger.debug("Start indexr: " + request.getStartIndex());
        logger.debug("---------------------------------------");
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

    public String getDigidocConfigurationFile() {
        return digidocConfigurationFile;
    }

    public void setDigidocConfigurationFile(String digidocConfigurationFile) {
        this.digidocConfigurationFile = digidocConfigurationFile;
    }
}
