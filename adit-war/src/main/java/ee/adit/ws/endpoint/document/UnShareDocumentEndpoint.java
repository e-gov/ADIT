package ee.adit.ws.endpoint.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.exception.AditMultipleException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.ArrayOfRecipientStatus;
import ee.adit.pojo.Message;
import ee.adit.pojo.RecipientStatus;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.pojo.UnShareDocumentRequest;
import ee.adit.pojo.UnShareDocumentResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.Util;
import ee.adit.util.xroad.CustomXRoadHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "unShareDocument" web method (web service request).
 * Contains request input validation, request-specific workflow and response
 * composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "unShareDocument", version = "v1")
@Component
public class UnShareDocumentEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = LogManager.getLogger(UnShareDocumentEndpoint.class);

    private UserService userService;

    private DocumentService documentService;

    private ScheduleClient scheduleClient;

    /**
     * Executes "V1" version of "unShareDocument" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        UnShareDocumentResponse response = new UnShareDocumentResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        ArrayOfRecipientStatus statusArray = new ArrayOfRecipientStatus();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = "";
        Long documentId = null;
        boolean summarySuccess = true;

        try {
            logger.debug("unShareDocument.v1 invoked.");
            UnShareDocumentRequest request = (UnShareDocumentRequest) requestObject;
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

            // Kontrollime, kas päringus märgitud isik on teenuse kasutaja
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

            // Check user rights for document
            Document doc = checkRightsAndGetDocument(request, applicationName, user);


            // All checks are successfully passed
            boolean saveDocument = false;
            List<String> userCodes = new ArrayList<String>();
            if ((request.getRecipientList() != null) && (request.getRecipientList().getCode() != null)) {
                userCodes.addAll(request.getRecipientList().getCode());
            }

            if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
                Iterator<DocumentSharing> it = doc.getDocumentSharings().iterator();
                while (it.hasNext()) {
                    DocumentSharing sharing = (DocumentSharing) it.next();
                    if ((request.getRecipientList() == null) || (request.getRecipientList().getCode() == null)
                            || (request.getRecipientList().getCode().size() < 1)
                            || (sharing.getUserCode() != null && (request.getRecipientList().getCode().contains(sharing.getUserCode())))) {

                        if (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SHARE)
                                || sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SIGN)) {
                        	
                        	//Control if document is signed by user and make a copy of this document for him.
                        	
                        	boolean documentSignedByUser = false;
                        	if (doc.getSigned() != null){
                        		documentSignedByUser = DocumentService.documentSignedBySharing(doc.getSignatures(), sharing);
                        	}
                        	
                        	if (documentSignedByUser) {
                        		logger.debug("user " + sharing.getUserCode() + " has signed document " + doc.getId() + " so he will get a copy of this document after sharing is finished.");
                                
                        		// Kas kasutajal on piisavalt vaba kettaruumi
                        		AditUser sharingUser = userService.getUserByID(sharing.getUserCode());
                        		
                                long remainingDiskQuota = this.getUserService().getRemainingDiskQuota(sharingUser,
                                        this.getConfiguration().getGlobalDiskQuota());
                                
                                SaveItemInternalResult saveResult = this.getDocumentService().saveDocumentCopy(doc, sharing, remainingDiskQuota);
                                
                                this.getDocumentService().addHistoryEvent(applicationName, saveResult.getItemId(), sharing.getUserCode(),
                                        DocumentService.HISTORY_TYPE_CREATE, xroadRequestUser.getUserCode(),
                                        xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_CREATE, sharing.getUserName(), requestDate.getTime());
                                
                                if (saveResult.isSuccess()) {
                                    documentId = saveResult.getItemId();
                                    logger.info("Signed document copy with ID: " + documentId.toString() + " saved for user: " + sharing.getUserCode());

                                    // Update user disk quota (used)
                                    logger.info("User disk quota shrinked by: " + saveResult.getAddedFilesSize());
                                    Long usedDiskQuota = user.getDiskQuotaUsed();
                                    if (usedDiskQuota == null) {
                                    	usedDiskQuota = 0L;
                                    }
                                    user.setDiskQuotaUsed(usedDiskQuota + saveResult.getAddedFilesSize());
                                    this.getUserService().getAditUserDAO().saveOrUpdate(user);

                                } else { 
                                    if ((saveResult.getMessages() != null) && (saveResult.getMessages().size() > 0)) {
                                        AditMultipleException aditMultipleException = new AditMultipleException(
                                                "MultiException");
                                        aditMultipleException.setMessages(saveResult.getMessages());
                                        throw aditMultipleException;
                                    } else {
                                        throw new AditException("Document copy saving failed!");
                                    }
                                }
//                        		throw new Exception ("Just to prevent document unSharing FOR DEVELOPMENT PURPOSES ONLY!");
                        	} 
                        	
                        /*	else {
                        		logger.debug("user " + sharing.getUserCode() + " has NOT signed document " + doc.getId() + " so he will get a copy of this document after sharing is finished. Just for the purpose of testing the document copy functionality.");	
                        	}*/
                        		
                            // Remove sharing
                            // doc.getDocumentSharings().remove(sharing);// DO
                            // NOT DO THAT - causes
                            // ConcurrentModificationException
                            it.remove();
                            sharing.setDocumentId(0);
                            saveDocument = true;

                            // Create response object
                            RecipientStatus status = new RecipientStatus();
                            status.setSuccess(true);
                            status.setCode(sharing.getUserCode());
                            ArrayOfMessage statusMessages = new ArrayOfMessage();
                            statusMessages.setMessage(this.getMessageService().getMessages(
                                    "request.unShareDocument.recipientStatus.success", new Object[] {}));
                            status.setMessages(statusMessages);
                            statusArray.addRecipient(status);

                            if (additionalInformationForLog != null && additionalInformationForLog.trim() != "") {
                                additionalInformationForLog = additionalInformationForLog + ",";
                            }
                            additionalInformationForLog = additionalInformationForLog + " unshared to: "
                                    + sharing.getUserCode();

                            if (userCodes.contains(sharing.getUserCode())) {
                                userCodes.remove(sharing.getUserCode());
                            }
                        }
                    }
                }
            } else {
                summarySuccess = false;
                AditCodedException aditCodedException = new AditCodedException(
                        "request.unShareDocument.document.notShared");
                aditCodedException.setParameters(new Object[] {request.getDocumentId().toString() });
                throw aditCodedException;
            }

            // If the document was not shared to some users in request's
            // user list then compose corresponding error messages
            int localErrorCount = 0;
            if (!userCodes.isEmpty()) {
                summarySuccess = false;
                for (String code : userCodes) {
                    RecipientStatus status = new RecipientStatus();
                    status.setSuccess(false);
                    status.setCode(code);
                    ArrayOfMessage statusMessages = new ArrayOfMessage();
                    statusMessages.setMessage(this.getMessageService().getMessages(
                            "request.unShareDocument.recipientStatus.notShared", new Object[] {}));
                    status.setMessages(statusMessages);
                    statusArray.addRecipient(status);

                    if (localErrorCount > 0) {
                        additionalInformationForLog = additionalInformationForLog + ", ";
                    }
                    additionalInformationForLog = additionalInformationForLog
                            + this.getMessageService().getMessage("request.unShareDocument.recipientStatus.notShared",
                                    new Object[] {}, Locale.ENGLISH);
                }
            }

            // Add history event about unsharing
            this.getDocumentService().addHistoryEvent(applicationName, doc.getId(), user.getUserCode(),
                DocumentService.HISTORY_TYPE_UNSHARE, xroadRequestUser.getUserCode(),
                xroadRequestUser.getFullName(), null, user.getFullName(), requestDate.getTime());

            if (saveDocument) {
                // If all sharings are removed and not signed then remove locking
                if (doc.getDocumentSharings().isEmpty() && ((doc.getSigned() == null) || !doc.getSigned())) {
                    doc.setLocked(false);
                    doc.setLockingDate(null);

                    // Lisame lukustamise ajaloosündmuse
                    this.getDocumentService().addHistoryEvent(applicationName, doc.getId(), user.getUserCode(),
                        DocumentService.HISTORY_TYPE_UNLOCK, xroadRequestUser.getUserCode(),
                        xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_UNLOCK, user.getFullName(), requestDate.getTime());
                }

                this.documentService.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);

                // Send notification to every user who was removed from
                // sharing recipients list (assuming they have requested such
                // notifications)
                for (RecipientStatus status : statusArray.getRecipient()) {
                    if (status != null) {
                        summarySuccess = summarySuccess && status.isSuccess();
                    }
                    if ((status != null) && status.isSuccess()) {
                        AditUser recipient = this.getUserService().getUserByID(status.getCode());
                        if ((recipient != null)
                            && (userService.findNotification(recipient.getUserNotifications(),
                                    ScheduleClient.NOTIFICATION_TYPE_SHARE) != null)) {
                        	
                        	String userInfo = user.getFullName() != null && !user.getFullName().trim().isEmpty() ?
											user.getFullName() : user.getUserCode();

                        	List<Message> messageInAllKnownLanguages = this.getMessageService().getMessages(
                        			"scheduler.message.unShare", new Object[] {doc.getTitle(), userInfo});
                        	String eventText = Util.joinMessages(messageInAllKnownLanguages, "<br/>");

                            getScheduleClient().addEvent(
                                recipient, eventText,
                                this.getConfiguration().getSchedulerEventTypeName(), requestDate,
                                ScheduleClient.NOTIFICATION_TYPE_SHARE, doc.getId(), this.userService);
                        }
                    }
                }
            }

            // Set response messages
            if (summarySuccess) {
                // messages.addMessage(new Message("en",
                // this.getMessageSource().getMessage("request.unShareDocument.success",
                // new Object[] { request.getDocumentId() }, Locale.ENGLISH)));
                String additionalMessage = this.getMessageService().getMessage("request.unShareDocument.success",
                        new Object[] {request.getDocumentId().toString() }, Locale.ENGLISH);
                // additionalMessage = additionalInformationForLog;
                additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;
                messages.setMessage(this.getMessageService().getMessages("request.unShareDocument.success",
                        new Object[] {request.getDocumentId().toString() }));
            } else {
                String additionalMessage = this.getMessageService().getMessage("request.unShareDocument.fail",
                        new Object[] {request.getDocumentId().toString() }, Locale.ENGLISH);
                additionalMessage = additionalInformationForLog;
                additionalInformationForLog = LogService.REQUEST_LOG_FAIL + ": " + additionalMessage;
                String errorMessage = this.getMessageService().getMessage("request.unShareDocument.fail",
                        new Object[] {request.getDocumentId().toString() }, Locale.ENGLISH);
                // Add error log
                super.logError(request.getDocumentId(), requestDate.getTime(), LogService.ERROR_LOG_LEVEL_WARN,
                        errorMessage);
                messages.setMessage(this.getMessageService().getMessages("request.unShareDocument.fail",
                        new Object[] {request.getDocumentId().toString() }));
            }
            response.setSuccess(summarySuccess);
            response.setMessages(messages);
            response.setRecipientList(statusArray);

        } catch (Exception e) {
            String errorMessage = null;
            logger.error("Exception: ", e);
            response.setSuccess(false);
            response.setRecipientList(statusArray);
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
                response.setRecipientList(null);
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
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("unShareDocument invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    @Override
    protected Object getResultForGenericException(Exception ex) {
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: "
                + ex.getMessage());
        UnShareDocumentResponse response = new UnShareDocumentResponse();
        response.setSuccess(false);
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        return response;
    }

    /**
     * Checks users rights for document.
     *
     * @param request
     *     Current request
     * @param applicationName
     *     Name of application that was used to execute current request
     * @param user
     *     User who executed current request
     * @return
     *     Requested document if user has necessary rights for it (or
     *     {@code null} otherwise).
     */
    private Document checkRightsAndGetDocument(
    	final UnShareDocumentRequest request, final String applicationName,
    	final AditUser user) {

    	Document doc;

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
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.read");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }

        // Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja
        // lahkunud)
        if ((user.getActive() == null) || !user.getActive()) {
            AditCodedException aditCodedException = new AditCodedException("user.inactive");
            aditCodedException.setParameters(new Object[] {user.getUserCode()});
            throw aditCodedException;
        }

        // Check whether or not the application has rights to
        // read current user's data.
        int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
        if (applicationAccessLevelForUser < 1) {
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.forUser.read");
            aditCodedException.setParameters(new Object[] {applicationName, user.getUserCode() });
            throw aditCodedException;
        }

        // Now it is safe to load the document from database
        // (and even necessary to do all the document-specific checks)        
        try {
			doc = this.documentService.getDocumentDAO().getDocumentWithSignaturesAndFiles(request.getDocumentId());
		} catch (Exception e) {
			doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());
		}
        
        
        // Check whether the document exists
        if (doc == null) {
            logger.debug("Requested document does not exist. Document ID: " + request.getDocumentId());
            AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
            aditCodedException.setParameters(new Object[] {request.getDocumentId().toString() });
            throw aditCodedException;
        }

        // Check whether the document is marked as deleted
        if ((doc.getDeleted() != null) && doc.getDeleted()) {
            logger.debug("Requested document is deleted. Document ID: " + request.getDocumentId());
            AditCodedException aditCodedException = new AditCodedException("document.deleted");
            aditCodedException.setParameters(new Object[] {request.getDocumentId().toString() });
            throw aditCodedException;
        }

        // Check whether the document is marked as deflated
        if ((doc.getDeflated() != null) && doc.getDeflated()) {
            logger.debug("Requested document is deflated. Document ID: " + request.getDocumentId());
            AditCodedException aditCodedException = new AditCodedException("document.deflated");
            aditCodedException.setParameters(new Object[] {Util.dateToEstonianDateString(doc.getDeflateDate()) });
            throw aditCodedException;
        }

        // Check whether the document belongs to user
        if (!doc.getCreatorCode().equalsIgnoreCase(user.getUserCode())) {
            logger.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId()
                    + ", User ID: " + user.getUserCode());
            AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            aditCodedException.setParameters(new Object[] {request.getDocumentId().toString(), user.getUserCode()});
            throw aditCodedException;
        }

        // Check whether the document is marked as invisible to owner
        if ((doc.getInvisibleToOwner() != null) && doc.getInvisibleToOwner()) {
            AditCodedException aditCodedException = new AditCodedException("document.deleted");
            aditCodedException.setParameters(new Object[] {doc.getId()});
            throw aditCodedException;
        }

        return doc;
    }

    /**
     * Validates request body and makes sure that all required fields exist and
     * are not empty. <br>
     * <br>
     * Throws {@link AditCodedException} if any errors in request data are
     * found.
     *
     * @param request
     *            Request body as {@link UnShareDocumentRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in requet body.
     */
    private void checkRequest(UnShareDocumentRequest request) throws AditCodedException {
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
     *            Request body as {@link UnShareDocumentRequest} object.
     */
    private void printRequest(UnShareDocumentRequest request) {
        logger.debug("-------- UnShareDocumentRequest -------");
        logger.debug("Document ID: " + String.valueOf(request.getDocumentId()));
        if ((request.getRecipientList() != null) && (request.getRecipientList().getCode() != null)) {
            for (String userCode : request.getRecipientList().getCode()) {
                logger.debug("User code: " + userCode);
            }
        }
        logger.debug("---------------------------------------");
    }

    public ScheduleClient getScheduleClient() {
        return scheduleClient;
    }

    public void setScheduleClient(ScheduleClient scheduleClient) {
        this.scheduleClient = scheduleClient;
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
