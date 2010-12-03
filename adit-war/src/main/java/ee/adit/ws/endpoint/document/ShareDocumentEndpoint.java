package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.ArrayOfRecipientStatus;
import ee.adit.pojo.Message;
import ee.adit.pojo.RecipientStatus;
import ee.adit.pojo.ShareDocumentRequest;
import ee.adit.pojo.ShareDocumentResponse;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "shareDocument" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "shareDocument", version = "v1")
@Component
public class ShareDocumentEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(ShareDocumentEndpoint.class);
    private UserService userService;
    private DocumentService documentService;
    private ScheduleClient scheduleClient;

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
        logger.debug("shareDocument invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "shareDocument" request.
     * 
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        ShareDocumentResponse response = new ShareDocumentResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        ArrayOfRecipientStatus statusArray = new ArrayOfRecipientStatus();
        String additionalInformationForLog = "";
        Long documentId = null;

        // Get a fixed request date
        // This becomes useful if we later have to compare
        // records in different database tables by date
        Calendar requestDate = Calendar.getInstance();

        try {
            logger.debug("shareDocument.v1 invoked.");
            ShareDocumentRequest request = (ShareDocumentRequest) requestObject;
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
            String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this
                    .getHeader().getAllasutus()
                    : this.getHeader().getIsikukood();
            AditUser user = this.getUserService().getUserByID(userCode);
            if (user == null) {
                AditCodedException aditCodedException = new AditCodedException("user.nonExistent");
                aditCodedException.setParameters(new Object[] {userCode });
                throw aditCodedException;
            }
            AditUser xroadRequestUser = null;
            if (user.getUsertype().getShortName().equalsIgnoreCase("person")) {
                xroadRequestUser = user;
            } else {
                try {
                    xroadRequestUser = this.getUserService().getUserByID(header.getIsikukood());
                } catch (Exception ex) {
                    logger
                            .debug("Error when attempting to find local user matchinig the person that executed a company request.");
                }
            }

            // Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja
            // lahkunud)
            if ((user.getActive() == null) || !user.getActive()) {
                AditCodedException aditCodedException = new AditCodedException("user.inactive");
                aditCodedException.setParameters(new Object[] {userCode });
                throw aditCodedException;
            }

            // Check whether or not the application has rights to
            // read current user's data.
            int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
            if (applicationAccessLevelForUser < 1) {
                AditCodedException aditCodedException = new AditCodedException(
                        "application.insufficientPrivileges.forUser.read");
                aditCodedException.setParameters(new Object[] {applicationName, user.getUserCode() });
                throw aditCodedException;
            }

            // Now it is safe to load the document from database
            // (and even necessary to do all the document-specific checks)
            Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());

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
            if (!doc.getCreatorCode().equalsIgnoreCase(userCode)) {
                logger.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId()
                        + ", User ID: " + userCode);
                AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
                aditCodedException.setParameters(new Object[] {request.getDocumentId().toString(), userCode });
                throw aditCodedException;
            }

            // All checks are successfully passed
            boolean saveDocument = false;
            boolean completeSuccess = true;
            for (String recipientCode : request.getRecipientList().getCode()) {
                boolean isSuccess = false;
                ArrayOfMessage statusMessages = new ArrayOfMessage();

                AditUser recipient = this.getUserService().getUserByID(recipientCode);
                if (recipient == null) {
                    statusMessages.setMessage(this.getMessageService().getMessages(
                            "request.shareDocument.recipientStatus.recipient.nonExistant", new Object[] {}));
                } else if (!recipient.getActive()) {
                    statusMessages.setMessage(this.getMessageService().getMessages(
                            "request.shareDocument.recipientStatus.recipient.inactive", new Object[] {}));
                } else if (sharingExists(doc.getDocumentSharings(), recipientCode)) {
                    statusMessages
                            .setMessage(this.getMessageService().getMessages(
                                    "request.shareDocument.recipientStatus.alreadySharedToUser",
                                    new Object[] {recipientCode }));
                } else if (recipient.getDvkOrgCode() != null && !"".equalsIgnoreCase(recipient.getDvkOrgCode().trim())) {
                    statusMessages.setMessage(this.getMessageService().getMessages(
                            "request.shareDocument.recipient.usesDVK", new Object[] {recipientCode }));
                } else {
                    DocumentSharing sharing = new DocumentSharing();
                    sharing.setCreationDate(requestDate.getTime());
                    sharing.setDocumentId(doc.getId());

                    if (request.getSharedForSigning()) {
                        sharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SIGN);
                    } else {
                        sharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SHARE);
                    }

                    sharing.setTaskDescription(request.getReasonForSharing());
                    sharing.setUserCode(recipient.getUserCode());
                    sharing.setUserName(recipient.getFullName());
                    doc.getDocumentSharings().add(sharing);

                    isSuccess = true;
                    saveDocument = true;
                    statusMessages.setMessage(this.getMessageService().getMessages(
                            "request.shareDocument.recipientStatus.success", new Object[] {}));
                    if (additionalInformationForLog != null && additionalInformationForLog.trim() != "") {
                        additionalInformationForLog = additionalInformationForLog + ",";
                    }
                    additionalInformationForLog = additionalInformationForLog + " shared to: " + recipientCode;
                }

                // Create response object
                RecipientStatus status = new RecipientStatus();
                status.setSuccess(isSuccess);
                status.setCode(recipientCode);
                status.setMessages(statusMessages);
                statusArray.addRecipient(status);

                completeSuccess = (completeSuccess && isSuccess);
            }

            if (saveDocument) {
                doc.setLocked(true);
                if (doc.getLockingDate() == null) {
                    doc.setLockingDate(requestDate.getTime());
                }
                doc.setSignable(true);

                // Lisame jagamise ajaloosündmuse
                DocumentHistory sharingEvent = new DocumentHistory(DocumentService.HISTORY_TYPE_SHARE, documentId,
                        requestDate.getTime(), user, xroadRequestUser, header);
                doc.getDocumentHistories().add(sharingEvent);

                // Lisame lukustamise ajaloosündmuse
                DocumentHistory lockEvent = new DocumentHistory(DocumentService.HISTORY_TYPE_LOCK, documentId,
                        requestDate.getTime(), user, xroadRequestUser, header);
                doc.getDocumentHistories().add(lockEvent);

                this.documentService.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);

                // Send notification to every user the document was shared to
                // (assuming they have requested such notifications)
                for (RecipientStatus status : statusArray.getRecipient()) {
                    if ((status != null) && status.isSuccess()) {
                        AditUser recipient = this.getUserService().getUserByID(status.getCode());
                        if ((recipient != null)
                                && (userService.findNotification(recipient.getUserNotifications(),
                                        ScheduleClient.NOTIFICATION_TYPE_SHARE) != null)) {
                            getScheduleClient().addEvent(
                                    recipient,
                                    this.getMessageSource().getMessage("scheduler.message.share",
                                            new Object[] {doc.getTitle(), userCode }, Locale.ENGLISH),
                                    this.getConfiguration().getSchedulerEventTypeName(), requestDate,
                                    ScheduleClient.NOTIFICATION_TYPE_SHARE, doc.getId(), this.userService);
                        }
                    }
                }
            } else {
                response.setMessages(messages);
                response.setRecipientList(statusArray);

                AditCodedException aditCodedException = new AditCodedException(
                        "request.shareDocument.recipients.noneSucceeded");
                aditCodedException.setParameters(new Object[] {request.getDocumentId().toString(), userCode });
                throw aditCodedException;
            }

            // Set response messages
            response.setSuccess(completeSuccess);

            if (completeSuccess) {
                // messages.addMessage(new Message("en",
                // this.getMessageSource().getMessage("request.shareDocument.success",
                // new Object[] { request.getDocumentId() }, Locale.ENGLISH)));
                String additionalMessage = this.getMessageService().getMessage("request.shareDocument.success",
                        new Object[] {request.getDocumentId().toString() }, Locale.ENGLISH);
                additionalMessage = additionalInformationForLog;
                additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ":" + additionalMessage;
                messages.setMessage(this.getMessageService().getMessages("request.shareDocument.success",
                        new Object[] {request.getDocumentId().toString() }));
            } else {
                // messages.addMessage(new Message("en",
                // this.getMessageSource().getMessage("request.shareDocument.fail",
                // new Object[] { request.getDocumentId() }, Locale.ENGLISH)));
                String additionalMessage = this.getMessageService().getMessage("request.shareDocument.fail",
                        new Object[] {request.getDocumentId().toString() }, Locale.ENGLISH);
                additionalMessage = additionalInformationForLog;
                additionalInformationForLog = LogService.REQUEST_LOG_FAIL + additionalMessage;
                messages.setMessage(this.getMessageService().getMessages("request.shareDocument.fail",
                        new Object[] {request.getDocumentId().toString() }));
            }

            response.setMessages(messages);
            response.setRecipientList(statusArray);
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
                arrayOfMessage.getMessage().add(new Message("en", "Service error"));
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
        ShareDocumentResponse response = new ShareDocumentResponse();
        response.setSuccess(false);
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        return response;
    }

    @SuppressWarnings("unchecked")
    private boolean sharingExists(Set documentSharings, String userCode) {
        boolean result = false;
        if ((documentSharings != null) && (!documentSharings.isEmpty())) {
            Iterator it = documentSharings.iterator();
            while (it.hasNext()) {
                DocumentSharing sharing = (DocumentSharing) it.next();
                if (userCode.equalsIgnoreCase(sharing.getUserCode())
                        && (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SHARE) || sharing
                                .getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SIGN))) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Validates request body and makes sure that all required fields exist and
     * are not empty. <br>
     * <br>
     * Throws {@link AditCodedException} if any errors in request data are
     * found.
     * 
     * @param request
     *            Request body as {@link ShareDocumentRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in requet body.
     */
    private void checkRequest(ShareDocumentRequest request) throws AditCodedException {
        if (request != null) {
            if ((request.getDocumentId() == null) || (request.getDocumentId() <= 0)) {
                throw new AditCodedException("request.body.undefined.documentId");
            } else if ((request.getRecipientList() == null) || (request.getRecipientList().getCode() == null)
                    || request.getRecipientList().getCode().isEmpty()) {
                throw new AditCodedException("request.shareDocument.recipients.unspecified");
            }
        } else {
            throw new AditCodedException("request.body.empty");
        }
    }

    /**
     * Writes request parameters to application DEBUG log.
     * 
     * @param request
     *            Request body as {@link ShareDocumentRequest} object.
     */
    private void printRequest(ShareDocumentRequest request) {
        logger.debug("-------- ShareDocumentRequest -------");
        logger.debug("Document ID: " + String.valueOf(request.getDocumentId()));
        if ((request.getRecipientList() != null) && (request.getRecipientList().getCode() != null)) {
            for (String userCode : request.getRecipientList().getCode()) {
                logger.debug("User code: " + userCode);
            }
        }
        logger.debug("Reason for sharing: " + request.getReasonForSharing());
        logger.debug("Shared for signing: " + request.getSharedForSigning());
        logger.debug("-------------------------------------");
    }

    public ScheduleClient getScheduleClient() {
        return scheduleClient;
    }

    public void setScheduleClient(ScheduleClient scheduleClient) {
        this.scheduleClient = scheduleClient;
    }
}
