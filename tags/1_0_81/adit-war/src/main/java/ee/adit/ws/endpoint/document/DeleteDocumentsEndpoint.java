package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfDocumentActionStatus;
import ee.adit.pojo.ArrayOfDocumentId;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.DeleteDocumentRequest;
import ee.adit.pojo.DeleteDocumentResponse;
import ee.adit.pojo.DeleteDocumentsRequest;
import ee.adit.pojo.DeleteDocumentsResponse;
import ee.adit.pojo.DocumentActionStatus;
import ee.adit.pojo.Message;
import ee.adit.pojo.RecipientStatus;
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
 * Implementation of "deleteDocument" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "deleteDocuments", version = "v1")
@Component
public class DeleteDocumentsEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(DeleteDocumentsEndpoint.class);

    private UserService userService;

    private DocumentService documentService;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("deleteDocuments invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "deleteDocuments" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        DeleteDocumentsResponse response = new DeleteDocumentsResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        ArrayOfDocumentActionStatus documentStatuses = new ArrayOfDocumentActionStatus();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        //Long documentId = null;
        ArrayOfDocumentId documents = null;        
        boolean success = true;

        try {
            logger.debug("deleteDocument.v1 invoked.");
            DeleteDocumentsRequest request = (DeleteDocumentsRequest) requestObject;
            if (request != null) {
            	documents = request.getDocuments();
            }
            CustomXTeeHeader header = this.getHeader();
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
            
           
            for (Long documentId: request.getDocuments().getDocumentId()){
            	
                DocumentActionStatus documentStatus = new DocumentActionStatus();
                documentStatus.setSuccess(true);
                documentStatus.setDocumentId(documentId);
                
                try {
	            this.getDocumentService().deleteDocument(documentId, user.getUserCode(), applicationName);
	
	            // If deletion was successful then add history event
	            this.getDocumentService().addHistoryEvent(applicationName, documentId, user.getUserCode(),
	                DocumentService.HISTORY_TYPE_DELETE, xroadRequestUser.getUserCode(),
	                xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_DELETE,
	                user.getFullName(), requestDate.getTime());

                } catch (Exception e) {
                	
                    logger.error("Exception while deleting document: ", e);
                    documentStatus.setSuccess(false);
                    String errorMessage = null;
                    List<Message> errorMessages = this.getMessageService().getMessages("service.error",
                            new Object[] {});
                    
                    ArrayOfMessage documentMessages = new ArrayOfMessage();
                    documentMessages.setMessage(errorMessages);
                    
                    if (e instanceof AditCodedException) {
                        logger.debug("Adding exception messages to response object.");
                        documentMessages.setMessage(this.getMessageService().getMessages((AditCodedException) e));
                        errorMessage = this.getMessageService().getMessage(e.getMessage(),
                                ((AditCodedException) e).getParameters(), Locale.ENGLISH);
                        errorMessage = "ERROR: " + errorMessage;
                    } else {
                    	documentMessages.setMessage(this.getMessageService().getMessages(MessageService.GENERIC_ERROR_CODE, new Object[]{}));
                        errorMessage = "ERROR: " + e.getMessage();
                    }                    
                    
                    documentStatus.setMessages(documentMessages);
                    success = false;
                    additionalInformationForLog = errorMessage;
                    super.logError(documentId, requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);

                    logger.debug("Adding exception messages to response object.");
                	
                	/*##############
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
                    */
                }
                
                documentStatuses.addDocument(documentStatus);
            }
	            

            
            // Set response messages
            response.setSuccess(new Success(success));
            response.setDocuments(documentStatuses);

            if (success) {
                messages.setMessage(this.getMessageService().getMessages("request.deleteDocuments.success", new Object[] {}));
                response.setMessages(messages);

                String additionalMessage = this.getMessageService().getMessage("request.deleteDocuments.success",
                        new Object[] {}, Locale.ENGLISH);
                additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

            } else {
                messages.setMessage(this.getMessageService().getMessages("request.deleteDocuments.fail", new Object[] {}));
                response.setMessages(messages);

                String additionalMessage = this.getMessageService().getMessage("request.deleteDocuments.fail",
                        new Object[] {}, Locale.ENGLISH);
                additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;
            }

            response.setMessages(messages);
            

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
            super.logError(null, requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);

            logger.debug("Adding exception messages to response object.");
            response.setMessages(arrayOfMessage);
        }
        
        super.logCurrentRequest(null, requestDate.getTime(), additionalInformationForLog);
        
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
     *            Request body as {@link DeleteDocumentsRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in request body.
     */
    private void checkRequest(DeleteDocumentsRequest request) throws AditCodedException {
        if (request !=null) {
	        if ((request.getDocuments() == null) 
	            	|| (request.getDocuments().getDocumentId() == null)
	            	|| request.getDocuments().getDocumentId().isEmpty()){
	        	throw new AditCodedException("request.deleteDocuments.documents.notSpecified");
	        }
	        else if (request != null && request.getDocuments().getDocumentId().isEmpty()) {
	        	for (Long documentId: request.getDocuments().getDocumentId()) {
	                if (documentId <= 0) {
	                    throw new AditCodedException("request.body.undefined.documentId");
	                }
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
     *            Request body as {@link DeleteDocumentRequest} object.
     */
    private void printRequest(DeleteDocumentsRequest request) {
        logger.debug("-------- DeleteDocumentRequest -------");
        logger.debug("Documents: " + String.valueOf(request.getDocuments().toString()));
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
