package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Date;
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
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.MarkDocumentViewedRequest;
import ee.adit.pojo.MarkDocumentViewedResponse;
import ee.adit.pojo.Message;
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
 * Implementation of "markDocumentViewed" web method (web service request).
 * Contains request input validation, request-specific workflow and response
 * composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "markDocumentViewed", version = "v1")
@Component
public class MarkDocumentViewedEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = LogManager.getLogger(MarkDocumentViewedEndpoint.class);

    private UserService userService;

    private DocumentService documentService;

    private ScheduleClient scheduleClient;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("markDocumentViewed invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "markDocumentViewed" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        MarkDocumentViewedResponse response = new MarkDocumentViewedResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;

        try {
            logger.debug("markDocumentViewed.v1 invoked.");
            MarkDocumentViewedRequest request = (MarkDocumentViewedRequest) requestObject;
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
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

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
                AditCodedException aditCodedException = new AditCodedException(
                        "application.insufficientPrivileges.forUser.read");
                aditCodedException.setParameters(new Object[] {applicationName, user.getUserCode() });
                throw aditCodedException;
            }

            Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());

            // Kontrollime, kas ID-le vastav dokument on olemas
            if (doc != null) {
                if ((doc.getDeleted() == null) || (!doc.getDeleted())) {
                    if ((doc.getDeflated() == null) || (!doc.getDeflated())) {
                        boolean saveDocument = false;

                        // Document can be marked as viewed only if:
                        // a) document belongs to user
                        // b) document is sent or shared to user
                        boolean userIsDocOwner = false;
                        if (doc.getCreatorCode().equalsIgnoreCase(user.getUserCode())) {
                            // Check whether the document is marked as invisible to owner
                            if ((doc.getInvisibleToOwner() != null) && doc.getInvisibleToOwner()) {
                                AditCodedException aditCodedException = new AditCodedException("document.deleted");
                                aditCodedException.setParameters(new Object[] {documentId.toString() });
                                throw aditCodedException;
                            }

                            userIsDocOwner = true;
                        } else {
                            if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
                                Iterator<DocumentSharing> it = doc.getDocumentSharings().iterator();
                                while (it.hasNext()) {
                                    DocumentSharing sharing = it.next();
                                    if (sharing.getUserCode() != null && sharing.getUserCode().equalsIgnoreCase(user.getUserCode())) {
                                        // Check whether the document is marked as deleted by recipient
                                        if ((sharing.getDeleted() != null) && sharing.getDeleted()) {
                                            AditCodedException aditCodedException = new AditCodedException("document.deleted");
                                            aditCodedException.setParameters(new Object[] {documentId.toString() });
                                            throw aditCodedException;
                                        }

                                        userIsDocOwner = true;

                                        if (sharing.getFirstAccessDate() == null) {
                                            sharing.setFirstAccessDate(new Date());
                                            saveDocument = true;
                                        }

                                        break;
                                    }
                                }
                            }
                        }

                        // Kui kasutaja tohib dokumendile ligi pääseda, siis
                        // tagastame failid
                        if (userIsDocOwner) {
                        	// If document has not been viewed by current user
                            // before then mark it viewed.
                            boolean isViewed = this.getDocumentService().getDocumentHistoryDAO()
                            	.checkIfHistoryEventExists(DocumentService.HISTORY_TYPE_MARK_VIEWED, doc.getId(), user.getUserCode());

                            if (!isViewed) {
                                // Add first viewing history event
                                this.getDocumentService().addHistoryEvent(applicationName, documentId, user.getUserCode(),
                                    DocumentService.HISTORY_TYPE_MARK_VIEWED, xroadRequestUser.getUserCode(),
                                    xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_MARK_VIEWED,
                                    user.getFullName(), requestDate.getTime());
                            }

                            if (saveDocument) {
                                this.documentService.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);
                            }

                            // If it was the first time for this particular user to
                            // view the document then send scheduler
                            // notification to document owner.
                            // Notification does not need to be sent if user
                            // viewed his/her own document.
                            if (!user.getUserCode().equalsIgnoreCase(doc.getCreatorCode())) {
                                AditUser docCreator = this.getUserService().getUserByID(doc.getCreatorCode());
                                if (!isViewed
                                    && (docCreator != null)
                                    && (userService.findNotification(docCreator.getUserNotifications(),
                                            ScheduleClient.NOTIFICATION_TYPE_VIEW) != null)) {
                                	
                                	String xroadRequestUserInfo = xroadRequestUser.getFullName() != null && !xroadRequestUser.getFullName().trim().isEmpty() ?
                                					xroadRequestUser.getFullName() : xroadRequestUser.getUserCode();

                                	List<Message> messageInAllKnownLanguages = this.getMessageService().getMessages(
                                			"scheduler.message.view", new Object[] {doc.getTitle(), xroadRequestUserInfo});
                                	String eventText = Util.joinMessages(messageInAllKnownLanguages, "<br/>");

                                    getScheduleClient().addEvent(
                                        docCreator, eventText,
                                        this.getConfiguration().getSchedulerEventTypeName(), requestDate,
                                        ScheduleClient.NOTIFICATION_TYPE_VIEW, doc.getId(), this.userService);
                                }
                            }
                        } else {
                            logger.debug("Requested document does not belong to user. Document ID: "
                                    + request.getDocumentId() + ", User ID: " + user.getUserCode());
                            AditCodedException aditCodedException = new AditCodedException(
                                    "document.doesNotBelongToUser");
                            aditCodedException
                                    .setParameters(new Object[] {request.getDocumentId().toString(), user.getUserCode()});
                            throw aditCodedException;
                        }
                    } else {
                        logger.debug("Requested document is deflated. Document ID: " + request.getDocumentId());
                        AditCodedException aditCodedException = new AditCodedException("document.deflated");
                        aditCodedException.setParameters(new Object[] {Util.dateToEstonianDateString(doc
                                .getDeflateDate()) });
                        throw aditCodedException;
                    }
                } else {
                    logger.debug("Requested document is deleted. Document ID: " + request.getDocumentId());
                    AditCodedException aditCodedException = new AditCodedException("document.deleted");
                    aditCodedException.setParameters(new Object[] {request.getDocumentId().toString() });
                    throw aditCodedException;
                }
            } else {
                logger.debug("Requested document does not exist. Document ID: " + request.getDocumentId());
                AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
                aditCodedException.setParameters(new Object[] {request.getDocumentId().toString() });
                throw aditCodedException;
            }

            // Set response messages
            response.setSuccess(true);
            messages.setMessage(this.getMessageService().getMessages("request.markDocumentViewed.success",
                    new Object[] {request.getDocumentId().toString(), user.getUserCode() }));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.markDocumentViewed.success",
                    new Object[] {request.getDocumentId().toString(), user.getUserCode() }, Locale.ENGLISH);
            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

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
        MarkDocumentViewedResponse response = new MarkDocumentViewedResponse();
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
     *            Request body as {@link MarkDocumentViewedRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in requet body.
     */
    private void checkRequest(MarkDocumentViewedRequest request) throws AditCodedException {
        if (request != null) {
            if ((request.getDocumentId() == null) || (request.getDocumentId() <= 0)) {
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
     *            Request body as {@link MarkDocumentViewedRequest} object.
     */
    private void printRequest(MarkDocumentViewedRequest request) {
        logger.debug("-------- MarkDocumentViewedRequest -------");
        logger.debug("Document ID: " + String.valueOf(request.getDocumentId()));
        logger.debug("------------------------------------------");
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
