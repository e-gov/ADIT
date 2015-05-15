package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.DeleteDocumentFileRequest;
import ee.adit.pojo.DeleteDocumentFileResponse;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "deleteDocumentFile" web method (web service request).
 * Contains request input validation, request-specific workflow and response
 * composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "deleteDocumentFile", version = "v1")
@Component
public class DeleteDocumentFileEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(DeleteDocumentFileEndpoint.class);

    private UserService userService;

    private DocumentService documentService;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("deleteDocumentFile invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "deleteDocumentFile" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        DeleteDocumentFileResponse response = new DeleteDocumentFileResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;

        try {
            logger.debug("deleteDocumentFile.v1 invoked.");
            DeleteDocumentFileRequest request = (DeleteDocumentFileRequest) requestObject;
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

            Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());

            // Kontrollime, kas ID-le vastav dokument on olemas
            if (doc != null) {
                // Kontrollime, kas dokument kuulub päringu käivitanud
                // kasutajale
                if (doc.getCreatorCode().equalsIgnoreCase(user.getUserCode())) {
                    // Make sure that the document is not deleted
                    // NB! doc.getDeleted() can be NULL
                    if ((doc.getDeleted() != null) && doc.getDeleted()) {
                        AditCodedException aditCodedException = new AditCodedException("document.deleted");
                        aditCodedException.setParameters(new Object[] {documentId.toString() });
                        throw aditCodedException;
                    }

                    // Check whether the document is marked as invisible to owner
                    if ((doc.getInvisibleToOwner() != null) && doc.getInvisibleToOwner()) {
                        AditCodedException aditCodedException = new AditCodedException("document.deleted");
                        aditCodedException.setParameters(new Object[] {documentId.toString() });
                        throw aditCodedException;
                    }

                    // Make sure that the document is not locked
                    // NB! doc.getLocked() can be NULL
                    if ((doc.getLocked() != null) && doc.getLocked()) {
                        AditCodedException aditCodedException = new AditCodedException(
                                "request.deleteDocumentFile.document.locked");
                        aditCodedException.setParameters(new Object[] {doc.getLockingDate()});
                        throw aditCodedException;
                    }

                    String resultCode = this.documentService.deflateDocumentFile(request.getDocumentId(), request
                            .getFileId(), true, true);
                    if (resultCode.equalsIgnoreCase("already_deleted")) {
                        AditCodedException aditCodedException = new AditCodedException("file.isDeleted");
                        aditCodedException.setParameters(new Object[] {new Long(request.getFileId()).toString() });
                        throw aditCodedException;
                    } else if (resultCode.equalsIgnoreCase("file_does_not_exist")) {
                        AditCodedException aditCodedException = new AditCodedException("file.nonExistent");
                        aditCodedException.setParameters(new Object[] {new Long(request.getFileId()).toString() });
                        throw aditCodedException;
                    } else if (resultCode.equalsIgnoreCase("file_does_not_belong_to_document")) {
                        AditCodedException aditCodedException = new AditCodedException("file.doesNotBelongToDocument");
                        aditCodedException.setParameters(new Object[] {new Long(request.getFileId()).toString(),
                                new Long(request.getDocumentId()).toString() });
                        throw aditCodedException;
                    } else if (resultCode.equalsIgnoreCase("cannot_delete_signature_container")) {
                        AditCodedException aditCodedException = new AditCodedException("file.nonExistent");
                        aditCodedException.setParameters(new Object[] {new Long(request.getFileId()).toString() });
                        throw aditCodedException;
                    }
                } else {
                    AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
                    aditCodedException.setParameters(new Object[] {new Long(request.getDocumentId()).toString(),
                            user.getUserCode() });
                    throw aditCodedException;
                }
            } else {
                AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
                aditCodedException.setParameters(new Object[] {new Long(request.getDocumentId()).toString() });
                throw aditCodedException;
            }

            // If deletion was successful then add history event
            DocumentHistory historyEvent = new DocumentHistory(DocumentService.HISTORY_TYPE_DELETE_FILE, documentId,
                    requestDate.getTime(), user, xroadRequestUser, header);
            historyEvent.setDescription(DocumentService.DOCUMENT_HISTORY_DESCRIPTION_DELETEFILE + request.getFileId());
            this.getDocumentService().getDocumentHistoryDAO().save(historyEvent);

            // Set response messages
            response.setSuccess(new Success(true));
            messages.setMessage(this.getMessageService().getMessages("request.deleteDocumentFile.success",
                    new Object[] {}));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.deleteDocumentFile.success",
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
        DeleteDocumentFileResponse response = new DeleteDocumentFileResponse();
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
     *            Request body as {@link DeleteDocumentFileRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in requet body.
     */
    private void checkRequest(DeleteDocumentFileRequest request) throws AditCodedException {
        if (request != null) {
            if (request.getDocumentId() <= 0) {
                throw new AditCodedException("request.body.undefined.documentId");
            } else if (request.getDocumentId() <= 0) {
                throw new AditCodedException("request.body.undefined.fileId");
            }
        } else {
            throw new AditCodedException("request.body.empty");
        }
    }

    /**
     * Writes request parameters to application DEBUG log.
     *
     * @param request
     *            Request body as {@link DeleteDocumentFileRequest} object.
     */
    private void printRequest(DeleteDocumentFileRequest request) {
        logger.debug("-------- DeleteDocumentFileRequest -------");
        logger.debug("Document ID: " + String.valueOf(request.getDocumentId()));
        logger.debug("File ID: " + String.valueOf(request.getFileId()));
        logger.debug("------------------------------------------");
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
