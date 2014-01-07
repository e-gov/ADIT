package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.ArrayOfRecipientStatus;
import ee.adit.pojo.ArrayOfUserCode;
import ee.adit.pojo.Message;
import ee.adit.pojo.RecipientStatus;
import ee.adit.pojo.SendDocumentRequest;
import ee.adit.pojo.SendDocumentResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "sendDocument" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "sendDocument", version = "v1")
@Component
public class SendDocumentEndpoint extends AbstractAditBaseEndpoint {
    private static Logger logger = Logger.getLogger(SendDocumentEndpoint.class);
    private UserService userService;
    private DocumentService documentService;
    private ScheduleClient scheduleClient;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("sendDocument invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "sendDocument" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        SendDocumentResponse response = new SendDocumentResponse();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = "";
        boolean success = true;
        ArrayOfRecipientStatus reponseStatuses = new ArrayOfRecipientStatus();
        String description = "Recipients: ";
        int successCount = 0;
        ArrayOfMessage messages = new ArrayOfMessage();
        SendDocumentRequest request = null;

        try {

            logger.debug("SendDocumentEndpoint.v1 invoked.");
            request = (SendDocumentRequest) requestObject;
            CustomXTeeHeader header = this.getHeader();
            String applicationName = header.getInfosysteem(this.getConfiguration().getXteeProducerName());

            // Log request
            Util.printHeader(header, this.getConfiguration());

            // Check header for required fields
            checkHeader(header);

            // Check request body
            checkRequest(request);

            // Check if the user is registered
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

            Document doc = checkRightsAndGetDocument(request, applicationName, user);

            ArrayOfUserCode recipientList = request.getRecipientList();

            if (recipientList != null && recipientList.getCode() != null && recipientList.getCode().size() > 0) {
                Iterator<String> i = recipientList.getCode().iterator();
                while (i.hasNext()) {
                    String recipientCode = i.next();

                    RecipientStatus recipientStatus = new RecipientStatus();
                    recipientStatus.setSuccess(true);
                    recipientStatus.setCode(recipientCode);

                    // Check if the user is registered
                    AditUser recipient = this.getUserService().getUserByID(recipientCode);
                    if ((recipient == null) && !Util.codeStartsWithCountryPrefix(recipientCode)) {
                    	String recipientCodeWithDefaultCountryPrefix = "EE" + recipientCode;
                    	recipient = this.getUserService().getUserByID(recipientCodeWithDefaultCountryPrefix);
                    }

                    if (recipient == null || !recipient.getActive()) {
                        logger.error("User is not registered or inactive.");
                        String messageCode = "user.nonExistent";
                        boolean inactive = false;

                        if (recipient != null) {
                            messageCode = "user.inactive";
                            inactive = true;
                        }

                        recipientStatus.setSuccess(false);
                        List<Message> errorMessages = this.getMessageService().getMessages(messageCode, new Object[] {recipientCode });
                        ArrayOfMessage recipientMessages = new ArrayOfMessage();
                        recipientMessages.setMessage(errorMessages);
                        recipientStatus.setMessages(recipientMessages);
                        success = false;

                        String localErrorMessage = "";
                        if (inactive) {
                            localErrorMessage = "User account deleted (inactive) for user ";
                        } else {
                            localErrorMessage = "User does not exist: ";
                        }
                        super.logError(request.getDocumentId(), requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR,
                                LogService.REQUEST_LOG_FAIL + localErrorMessage + recipientCode);
                        if (additionalInformationForLog != null && !additionalInformationForLog.trim().equals("")) {
                            additionalInformationForLog = additionalInformationForLog + ", ";
                        }
                        additionalInformationForLog = additionalInformationForLog + localErrorMessage + recipientCode + " ";
                    } else if (DocumentService.documentSendingExists(doc.getDocumentSharings(), recipientCode)) {
                        recipientStatus.setSuccess(false);
                        List<Message> errorMessages = this.getMessageService().getMessages("request.sendDocument.recipientStatus.alreadySentToUser", new Object[] {recipientCode });
                        ArrayOfMessage recipientMessages = new ArrayOfMessage();
                        recipientMessages.setMessage(errorMessages);
                        recipientStatus.setMessages(recipientMessages);
                        success = false;

                        String localErrorMessage = "Document has already been sent to user: ";
                        super.logError(request.getDocumentId(), requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR,
                                LogService.REQUEST_LOG_FAIL + localErrorMessage + recipientCode);
                        if (additionalInformationForLog != null && !additionalInformationForLog.trim().equals("")) {
                            additionalInformationForLog += ", ";
                        }
                        additionalInformationForLog += localErrorMessage + recipientCode + " ";
                    } else {
                        try {
                            // Lock the document
                            this.getDocumentService().lockDocument(doc);

                            // Add locking history event
                            this.getDocumentService().addHistoryEvent(applicationName, doc.getId(), user.getUserCode(),
                                DocumentService.HISTORY_TYPE_LOCK, xroadRequestUser.getUserCode(),
                                xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_LOCK,
                                user.getFullName(), requestDate.getTime());

                            String dvkFolder = request.getDvkFolder();

                            // Add sharing information to database
                            this.getDocumentService().sendDocument(doc, recipient, dvkFolder, null);


                            //TODO: it is better to add dvkFolder to the Document object here
                            //instead of adding it in sendDocument Method to DocumentSharing objects


                            // Send notification to every user the document was shared to
                            // (assuming they have requested such notifications)
                            if ((userService.findNotification(recipient.getUserNotifications(), ScheduleClient.NOTIFICATION_TYPE_SEND) != null)) {
                            	List<Message> messageInAllKnownLanguages = this.getMessageService().getMessages("scheduler.message.send", new Object[] {doc.getTitle(), user.getUserCode()});
                            	String eventText = Util.joinMessages(messageInAllKnownLanguages, "<br/>");

                            	this.scheduleClient.addEvent(recipient, eventText,
                                    this.getConfiguration().getSchedulerEventTypeName(), requestDate,
                                    ScheduleClient.NOTIFICATION_TYPE_SEND, doc.getId(), this.userService);
                            }

                            // Add success message to response
                            recipientStatus.setSuccess(true);

                            if (successCount > 0) {
                                description = description + ", ";
                            }
                            description = description + recipient.getUserCode();
                            successCount++;

                            if (additionalInformationForLog != null && additionalInformationForLog.trim() != "") {
                                additionalInformationForLog = additionalInformationForLog + ",";
                            }
                            additionalInformationForLog = additionalInformationForLog + " Document sent to: " + recipientCode;

                            // Add recipient to user contacts
                            userService.addUserContact(user, recipient);

                        } catch (Exception e) {
                            logger.error("Exception while sharing document: ", e);
                            recipientStatus.setSuccess(false);
                            List<Message> errorMessages = this.getMessageService().getMessages("service.error",
                                    new Object[] {});
                            ArrayOfMessage recipientMessages = new ArrayOfMessage();
                            recipientMessages.setMessage(errorMessages);
                            recipientStatus.setMessages(recipientMessages);
                            success = false;
                            additionalInformationForLog = "Exception while sharing document: " + e.getMessage() + " ";
                        }
                    }
                    reponseStatuses.addRecipient(recipientStatus);
                }

            } else {
                throw new NullPointerException("Recipient list is empty or null.");
            }

            response.setSuccess(success);
            response.setRecipientList(reponseStatuses);

            if (success) {
                String additionalMessage = this.getMessageService().getMessage("request.sendDocument.success",
                        new Object[] {}, Locale.ENGLISH);
                additionalMessage = additionalInformationForLog;
                additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;
                messages.setMessage(this.getMessageService().getMessages("request.sendDocument.success",
                        new Object[] {}));
    			// update doc last modified date
    			doc.setLastModifiedDate(new Date());
    			this.documentService.save(doc, Long.MAX_VALUE);
            } else {
                String additionalMessage = this.getMessageService().getMessage("request.sendDocument.fail",
                        new Object[] {}, Locale.ENGLISH);
                additionalMessage = additionalInformationForLog;
                additionalInformationForLog = LogService.REQUEST_LOG_FAIL + additionalMessage;
                messages.setMessage(this.getMessageService().getMessages("request.sendDocument.fail", new Object[] {}));
            }

            response.setMessages(messages);

            if (successCount > 0) {
                this.getDocumentService().addHistoryEvent(applicationName, doc.getId(), user.getUserCode(),
                        DocumentService.HISTORY_TYPE_SEND, xroadRequestUser.getUserCode(),
                        xroadRequestUser.getFullName(), description, user.getFullName(), requestDate.getTime());
            }

        } catch (Exception e) {
            String errorMessage = null;
            success = false;
            logger.error("Exception: ", e);
            response.setSuccess(success);
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
            super.logError(request.getDocumentId(), requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);

            logger.debug("Adding exception messages to response object.");
            response.setMessages(arrayOfMessage);
        }

        super.logCurrentRequest(request.getDocumentId(), requestDate.getTime(), additionalInformationForLog);

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
    	final SendDocumentRequest request, final String applicationName,
    	final AditUser user) {

        // Check if the application is registered
        boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
        if (!applicationRegistered) {
            logger.error("Application is not registered.");
            AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
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

        // Check application access level for user
        int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
        if (applicationAccessLevelForUser < 2) {
            logger.error("Application has insufficient privileges for user: " + user.getUserCode());
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.read");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }

        // Check if the document exists
        Document doc = this.getDocumentService().getDocumentDAO().getDocument(request.getDocumentId());

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
            aditCodedException.setParameters(new Object[] {request.getDocumentId().toString() });
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
     *            Request body as {@link SendDocumentRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in request body.
     */
    private void checkRequest(SendDocumentRequest request) throws AditCodedException {
        if (request != null) {
            if (request.getDocumentId() <= 0) {
                throw new AditCodedException("request.body.undefined.documentId");
            } else if ((request.getRecipientList() == null)
            	|| (request.getRecipientList().getCode() == null)
            	|| request.getRecipientList().getCode().isEmpty()) {
            	throw new AditCodedException("request.sendDocument.recipients.unspecified");
            }
        } else {
            throw new AditCodedException("request.body.empty");
        }
    }

    @Override
    protected Object getResultForGenericException(Exception ex) {
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: "
                + ex.getMessage());
        SendDocumentResponse response = new SendDocumentResponse();
        response.setSuccess(false);
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        return response;
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