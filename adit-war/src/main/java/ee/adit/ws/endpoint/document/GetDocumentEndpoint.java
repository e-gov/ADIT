package ee.adit.ws.endpoint.document;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetDocumentRequest;
import ee.adit.pojo.GetDocumentResponse;
import ee.adit.pojo.GetDocumentResponseAttachment;
import ee.adit.pojo.GetDocumentResponseDocument;
import ee.adit.pojo.Message;
import ee.adit.pojo.OutputDocument;
import ee.adit.pojo.OutputDocumentFile;
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
 * Implementation of "getDocument" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "getDocument", version = "v1")
@Component
public class GetDocumentEndpoint extends AbstractAditBaseEndpoint {
    
    private static Logger logger = Logger.getLogger(GetDocumentEndpoint.class);

    private UserService userService;
    private DocumentService documentService;
    private ScheduleClient scheduleClient;
    private String digidocConfigurationFile;

    
    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("getDocument invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "getDocument" request.
     * 
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        GetDocumentResponse response = new GetDocumentResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;
        List<Long> fileIdList = new ArrayList<Long>();
        boolean includeFileContents = false;

        try {
            logger.debug("getDocument.v1 invoked.");
            GetDocumentRequest request = (GetDocumentRequest) requestObject;
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
            // andmeid näha
            this.getUserService().checkApplicationReadPrivilege(applicationName);

            // Kontrollime, kas päringus märgitud isik on teenuse kasutaja
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());

            Document doc = checkRightsAndGetDocument(request, applicationName, user);

            boolean saveDocument = false;

            // Dokumenti saab alla laadida, kui dokument:
            // a) kuulub päringu käivitanud kasutajale
            // b) on päringu käivitanud kasutajale välja jagatud
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
                        if (sharing.getUserCode().equalsIgnoreCase(user.getUserCode())) {
                            // Check whether the document is marked as deleted by recipient
                            if ((sharing.getDeleted() != null) && sharing.getDeleted()) {
                                AditCodedException aditCodedException = new AditCodedException("document.deleted");
                                aditCodedException.setParameters(new Object[] {documentId.toString() });
                                throw aditCodedException;
                            }
                        	
                            userIsDocOwner = true;

                            if (sharing.getLastAccessDate() == null) {
                                sharing.setLastAccessDate(new Date());
                                saveDocument = true;
                            }

                            break;
                        }
                    }
                }
            }

            // Kui kasutaja tohib dokumendile ligi pääseda, siis
            // tagastame dokumendi
            if (userIsDocOwner) {
                InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(getDigidocConfigurationFile());
                String jdigidocCfgTmpFile = Util.createTemporaryFile(input, getConfiguration().getTempDir());
            	
            	includeFileContents = (request.isIncludeFileContents() == null) ? false : request.isIncludeFileContents();
                OutputDocument resultDoc = this.documentService.getDocumentDAO().getDocumentWithFiles(
                    doc.getId(), null, true, true, includeFileContents,
                    request.getFileTypes(),
                    this.getConfiguration().getTempDir(),
                    this.getMessageSource().getMessage("files.nonExistentOrDeleted", new Object[] {},
                    Locale.ENGLISH), user.getUserCode(), getConfiguration().getDocumentRetentionDeadlineDays(),
                    jdigidocCfgTmpFile);

                if (resultDoc != null) {
                    // Remember file IDs for logging later on.
                    List<OutputDocumentFile> docFiles = resultDoc.getFiles().getFiles();
                    if ((docFiles != null) && (docFiles.size() > 0)) {
                        for (OutputDocumentFile file : docFiles) {
                            fileIdList.add(file.getId());
                        }
                    }
                    
                    // 1. Convert java list to XML string and output
                    // to file
                    GetDocumentResponseAttachment attachment = new GetDocumentResponseAttachment();
                    attachment.setDocument(resultDoc);
                    String xmlFile = marshal(attachment);
                    Util.joinSplitXML(xmlFile, "data");

                    // 2. GZip the temporary file Base64 encoding
                    // will be done at SOAP envelope level
                    String gzipFileName = Util.gzipFile(xmlFile, this.getConfiguration().getTempDir());

                    // 3. Add as an attachment
                    String contentID = addAttachment(gzipFileName);
                    GetDocumentResponseDocument responseDoc = new GetDocumentResponseDocument();
                    responseDoc.setHref("cid:" + contentID);
                    response.setDocument(responseDoc);

                    // If document has not been viewed by current
                    // user before then mark it viewed.
                    boolean isViewed = false;
                    if ((doc.getDocumentHistories() != null) && (!doc.getDocumentHistories().isEmpty())) {
                        Iterator<DocumentHistory> it = doc.getDocumentHistories().iterator();
                        while (it.hasNext()) {
                            DocumentHistory event = it.next();
                            if (event.getDocumentHistoryType().equalsIgnoreCase(
                                    DocumentService.HISTORY_TYPE_MARK_VIEWED)
                                    && event.getUserCode().equalsIgnoreCase(user.getUserCode())) {
                                isViewed = true;
                                break;
                            }
                        }
                    }

                    if (!isViewed) {
                        // Add first viewing history event
                        DocumentHistory historyEvent = new DocumentHistory();
                        historyEvent.setRemoteApplicationName(applicationName);
                        historyEvent.setDocumentId(doc.getId());
                        historyEvent.setDocumentHistoryType(DocumentService.HISTORY_TYPE_MARK_VIEWED);
                        historyEvent.setEventDate(new Date());
                        historyEvent.setUserCode(user.getUserCode());
                        doc.getDocumentHistories().add(historyEvent);
                        saveDocument = true;
                    }

                    if (saveDocument) {
                        this.documentService.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);
                    }

                    // If it was the first time for this particular
                    // user to view the document then send scheduler
                    // notification to document owner.
                    // Notification does not need to be sent if user
                    // viewed his/her own document.
                    if (!user.getUserCode().equalsIgnoreCase(doc.getCreatorCode())) {
                        AditUser docCreator = this.getUserService().getUserByID(doc.getCreatorCode());
                        if (!isViewed && (docCreator != null)
                            && (userService.findNotification(docCreator.getUserNotifications(),
                            ScheduleClient.NOTIFICATION_TYPE_VIEW) != null)) {
                            
                        	List<Message> messageInAllKnownLanguages = this.getMessageService().getMessages("scheduler.message.view", new Object[] {doc.getTitle(), user.getUserCode()});
                        	String eventText = Util.joinMessages(messageInAllKnownLanguages, "<br/>");
                        	
                        	getScheduleClient().addEvent(
                                docCreator, eventText,
                                this.getConfiguration().getSchedulerEventTypeName(), requestDate,
                                ScheduleClient.NOTIFICATION_TYPE_VIEW, doc.getId(), this.userService);
                        }
                    }
                } else {
                    logger.debug("Document has no files!");
                }
            } else {
                logger.debug("Requested document does not belong to user. Document ID: "
                        + request.getDocumentId() + ", User ID: " + user.getUserCode());
                AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
                aditCodedException.setParameters(new Object[] {request.getDocumentId().toString(), user.getUserCode()});
                throw aditCodedException;
            }

            // Set response messages
            response.setSuccess(true);
            messages.setMessage(this.getMessageService().getMessages("request.getDocument.success", new Object[] {}));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.getDocument.success",
                    new Object[] {}, Locale.ENGLISH);
            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

            if (request != null && request.isIncludeFileContents()) {
                additionalInformationForLog = additionalInformationForLog + ("(Including files)");
            }

        } catch (Exception e) {
            logger.error("Exception: ", e);
            String errorMessage = null;
            response.setSuccess(false);
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

        // Log document/file download
        if (includeFileContents) {
            if ((fileIdList == null) || (fileIdList.size() < 1)) {
                super.logDownloadRequest(documentId, null, requestDate.getTime());
            } else {
                for (Long fileId : fileIdList) {
                    super.logDownloadRequest(documentId, fileId, requestDate.getTime());
                }
            }
        } else {
            super.logMetadataRequest(documentId, requestDate.getTime());
        }

        return response;
    }

    @Override
    protected Object getResultForGenericException(Exception ex) {
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: "
                + ex.getMessage());
        GetDocumentResponse response = new GetDocumentResponse();
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
    	final GetDocumentRequest request, final String applicationName,
    	final AditUser user) {
    	
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
     *            Request body as {@link GetDocumentRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in request body.
     */
    private void checkRequest(GetDocumentRequest request) throws AditCodedException {
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
     *            Request body as {@link GetDocumentRequest} object.
     */
    private void printRequest(GetDocumentRequest request) {
        logger.debug("-------- GetDocumentRequest -------");
        logger.debug("Document ID: " + String.valueOf(request.getDocumentId()));
        logger.debug("Include file contents: " + String.valueOf(request.isIncludeFileContents()));
        if ((request.getFileTypes() != null) && (request.getFileTypes().getFileType() != null)) {
        	for (String fileType : request.getFileTypes().getFileType()) {
        		logger.debug("Requested file type: " + fileType);
        	}
        }
        logger.debug("-----------------------------------");
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
    
    public String getDigidocConfigurationFile() {
        return digidocConfigurationFile;
    }

    public void setDigidocConfigurationFile(String digidocConfigurationFile) {
        this.digidocConfigurationFile = digidocConfigurationFile;
    }
}
