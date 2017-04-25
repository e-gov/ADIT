package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Locale;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.DeleteDocumentRequest;
import ee.adit.pojo.DeleteDocumentResponse;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.Util;
import ee.adit.util.xroad.CustomXRoadHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "deleteDocument" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "deleteDocument", version = "v1")
@Component
public class DeleteDocumentEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = LogManager.getLogger(DeleteDocumentEndpoint.class);

    private UserService userService;

    private DocumentService documentService;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("deleteDocument invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "deleteDocument" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        DeleteDocumentResponse response = new DeleteDocumentResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;

        try {
            logger.debug("deleteDocument.v1 invoked.");
            DeleteDocumentRequest request = (DeleteDocumentRequest) requestObject;
            if (request != null) {
                documentId = request.getDocumentId();
            }
            CustomXRoadHeader header = this.getHeader();
            String applicationName = header.getInfosysteem(this.getConfiguration().getXteeProducerName());

            // Log request
            Util.printHeader(header, this.getConfiguration());
            printRequest(request);

            // Check header for required fields
            checkHeader(header);

            // Check request body
            checkRequest(request);

            // Kontrollime, kas päringu käivitanud infosüsteem on ADITis
            // registreeritud
            this.getUserService().checkApplicationRegistered(applicationName);

            // Kontrollime, kas päringu käivitanud infosüsteem tohib
            // andmeid muuta (või üldse näha)
            this.getUserService().checkApplicationWritePrivilege(applicationName);

            // Kontrollime, kas päringus märgitud isik on teenuse kasutaja
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

            // Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja
            // lahkunud)
            if ((user.getActive() == null) || !user.getActive()) {
                AditCodedException aditCodedException = new AditCodedException("user.inactive");
                aditCodedException.setParameters(new Object[] {user.getUserCode()});
                throw aditCodedException;
            }

            // Check whether or not the application has rights to
            // modify current user's data.
            int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
            if (applicationAccessLevelForUser != 2) {
                AditCodedException aditCodedException = new AditCodedException(
                        "application.insufficientPrivileges.forUser.write");
                aditCodedException.setParameters(new Object[] {applicationName, user.getUserCode() });
                throw aditCodedException;
            }

            this.getDocumentService().deleteDocument(request.getDocumentId(), user.getUserCode(), applicationName);

            // If deletion was successful then add history event
            this.getDocumentService().addHistoryEvent(applicationName, documentId, user.getUserCode(),
                DocumentService.HISTORY_TYPE_DELETE, xroadRequestUser.getUserCode(),
                xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_DELETE,
                user.getFullName(), requestDate.getTime());

            // Set response messages
            response.setSuccess(new Success(true));
            messages.setMessage(this.getMessageService().getMessages("request.deleteDocument.success", new Object[] {}));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.deleteDocument.success",
                    new Object[] {}, Locale.ENGLISH);
            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

        } catch (Exception e) {
            logger.error("Exception: ", e);
            String errorMessage = null;
            response.setSuccess(new Success(false));
            ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

            if (e instanceof AditCodedException) {
                logger.debug("Adding exception messages to response object.");
                arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
                errorMessage = this.getMessageService().getMessage(e.getMessage(),
                        ((AditCodedException) e).getParameters(), Locale.ENGLISH);
                errorMessage = "ERROR: " + errorMessage;
            } else {
            	arrayOfMessage.setMessage(this.getMessageService().getMessages(MessageService.GENERIC_ERROR_CODE, new Object[]{}));
                errorMessage = "ERROR: " + e.getMessage();
            }

            additionalInformationForLog = errorMessage;
            super.logError(documentId, requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);

            logger.debug("Adding exception messages to response object.");
            response.setMessages(arrayOfMessage);
        }

        super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
        return response;
    }

    @Override
    protected Object getResultForGenericException(Exception ex) {
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: "
                + ex.getMessage());
        DeleteDocumentResponse response = new DeleteDocumentResponse();
        response.setSuccess(new Success(false));
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
     *            Request body as {@link DeleteDocumentRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in requet body.
     */
    private void checkRequest(DeleteDocumentRequest request) throws AditCodedException {
        if (request != null) {
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
     * @param request
     *            Request body as {@link DeleteDocumentRequest} object.
     */
    private void printRequest(DeleteDocumentRequest request) {
        logger.debug("-------- DeleteDocumentRequest -------");
        logger.debug("Document ID: " + String.valueOf(request.getDocumentId()));
        logger.debug("--------------------------------------");
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
