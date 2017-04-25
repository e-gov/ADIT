package ee.adit.ws.endpoint.document;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.ejb.criteria.CriteriaQueryCompiler.RenderedCriteriaQuery;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetDocumentRequest;
import ee.adit.pojo.GetDocumentResponse;
import ee.adit.pojo.GetDocumentResponseAttachment;
import ee.adit.pojo.GetDocumentResponseAttachmentV2;
import ee.adit.pojo.GetDocumentResponseAttachmentV3;
import ee.adit.pojo.GetDocumentResponseDocument;
import ee.adit.pojo.Message;
import ee.adit.pojo.OutputDocument;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.Util;
import ee.adit.util.xroad.CustomXRoadHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.ria.dhx.util.StringUtil;
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

    private static Logger logger = LogManager.getLogger(GetDocumentEndpoint.class);

    private UserService userService;
    private DocumentService documentService;
    private ScheduleClient scheduleClient;
    private String digidocConfigurationFile;


    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("getDocument invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        }
        else if (version == 2) {
            return v2(requestObject);
        } else if (version == 3) {
            return v3(requestObject);
        }
        else {
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
        String documentGuid = null;
        List<Long> fileIdList = new ArrayList<Long>();
        boolean includeFileContents = false;

        try {
            logger.debug("getDocument.v1 invoked.");
            GetDocumentRequest request = (GetDocumentRequest) requestObject;

            //that parameter is not used in this version
            request.setDvkId(null);
            request.setDhxReceiptId(null);
            
            if (request != null) {
                documentId = request.getDocumentId();
                if (documentId == null) {
                	documentGuid = request.getDocumentGuid();
                }
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
            // andmeid näha
            this.getUserService().checkApplicationReadPrivilege(applicationName);

            // Kontrollime, kas päringus märgitud isik on teenuse kasutaja
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

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
                    if (documentId != null) {
                    	aditCodedException.setParameters(new Object[] {documentId.toString() });
                    } else {
                    	aditCodedException.setParameters(new Object[] {documentGuid.toString() });
                    }
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
                                if (documentId != null) {
                                	aditCodedException.setParameters(new Object[] {documentId.toString() });
                                } else {
                                	aditCodedException.setParameters(new Object[] {documentGuid.toString() });
                                }
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
                    jdigidocCfgTmpFile, true, false);

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
                    boolean isViewed = this.getDocumentService().getDocumentHistoryDAO()
                		.checkIfHistoryEventExists(DocumentService.HISTORY_TYPE_MARK_VIEWED, doc.getId(), user.getUserCode());

                    if (!isViewed && documentId != null) {
                        // Add first viewing history event
                        this.getDocumentService().addHistoryEvent(applicationName, documentId, user.getUserCode(),
                            DocumentService.HISTORY_TYPE_MARK_VIEWED, xroadRequestUser.getUserCode(),
                            xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_MARK_VIEWED,
                            user.getFullName(), requestDate.getTime());
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
                            ScheduleClient.NOTIFICATION_TYPE_VIEW) != null) && documentId != null) {

                        	String userInfo = user.getFullName() != null && !user.getFullName().trim().isEmpty() ?
											user.getFullName() : user.getUserCode();
                        	
                        	List<Message> messageInAllKnownLanguages = this.getMessageService().getMessages(
                        			"scheduler.message.view", new Object[] {doc.getTitle(), userInfo});
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
            	AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            	if (request.getDocumentId() != null) {
	                logger.debug("Requested document does not belong to user. Document ID: "
	                        + request.getDocumentId() + ", User ID: " + user.getUserCode());                
	                aditCodedException.setParameters(new Object[] {request.getDocumentId().toString(), user.getUserCode()});
            	} else {
            		logger.debug("Requested document does not belong to user. Document GUID: "
	                        + request.getDocumentGuid() + ", User ID: " + user.getUserCode());                
	                aditCodedException.setParameters(new Object[] {request.getDocumentGuid().toString(), user.getUserCode()});
            	}
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

    /**
     * Executes "V2" version of "getDocument" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v2(Object requestObject) {
        GetDocumentResponse response = new GetDocumentResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;
        String documentGuid = null;
        List<Long> fileIdList = new ArrayList<Long>();
        boolean includeFileContents = false;

        try {
            logger.debug("getDocument.v2 invoked.");
            GetDocumentRequest request = (GetDocumentRequest) requestObject;
            if (request != null) {
                documentId = request.getDocumentId();
                if (documentId == null) {
                	documentGuid = request.getDocumentGuid();
                }
            }
            request.setDhxReceiptId(null);
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
            // andmeid näha
            this.getUserService().checkApplicationReadPrivilege(applicationName);

            // Kontrollime, kas päringus märgitud isik on teenuse kasutaja
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

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
                    if (documentId != null) {
                    	aditCodedException.setParameters(new Object[] {documentId.toString() });
                    } else {
                    	aditCodedException.setParameters(new Object[] {documentGuid.toString() });
                    }
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
                                if (documentId != null) {
                                	aditCodedException.setParameters(new Object[] {documentId.toString() });
                                } else {
                                	aditCodedException.setParameters(new Object[] {documentGuid.toString() });
                                }
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
                    jdigidocCfgTmpFile, true, false);
                Long dvkId = this.documentService.findDocumentDvkIdForUser(doc, user);
                resultDoc.setDvkId(dvkId);		
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
                    GetDocumentResponseAttachmentV2 attachment = new GetDocumentResponseAttachmentV2();
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
                    boolean isViewed = this.getDocumentService().getDocumentHistoryDAO()
                		.checkIfHistoryEventExists(DocumentService.HISTORY_TYPE_MARK_VIEWED, doc.getId(), user.getUserCode());

                    if (!isViewed && documentId != null) {
                        // Add first viewing history event
                        this.getDocumentService().addHistoryEvent(applicationName, documentId, user.getUserCode(),
                            DocumentService.HISTORY_TYPE_MARK_VIEWED, xroadRequestUser.getUserCode(),
                            xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_MARK_VIEWED,
                            user.getFullName(), requestDate.getTime());
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
                            ScheduleClient.NOTIFICATION_TYPE_VIEW) != null) && documentId != null) {

                        	String userInfo = user.getFullName() != null && !user.getFullName().trim().isEmpty() ?
											user.getFullName() : user.getUserCode();
                        	
                        	List<Message> messageInAllKnownLanguages = this.getMessageService().getMessages(
                        			"scheduler.message.view", new Object[] {doc.getTitle(), userInfo});
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
            	AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            	if (request.getDocumentId() != null) {
	                logger.debug("Requested document does not belong to user. Document ID: "
	                        + request.getDocumentId() + ", User ID: " + user.getUserCode());                
	                aditCodedException.setParameters(new Object[] {request.getDocumentId().toString(), user.getUserCode()});
            	} else if (request.getDocumentGuid() != null) {
            		logger.debug("Requested document does not belong to user. Document GUID: "
	                        + request.getDocumentGuid() + ", User ID: " + user.getUserCode());                
	                aditCodedException.setParameters(new Object[] {request.getDocumentGuid().toString(), user.getUserCode()});
            	}else {
            		logger.debug("Requested document does not belong to user. Document GUID: "
	                        + request.getDvkId() + ", User ID: " + user.getUserCode());                
	                aditCodedException.setParameters(new Object[] {request.getDvkId().toString(), user.getUserCode()});
            	}
                throw aditCodedException;
            }

            // Set response messages
            response.setSuccess(true);
            messages.setMessage(this.getMessageService().getMessages("request.getDocument.success", new Object[] {}));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.getDocument.success",
                    new Object[] {}, Locale.ENGLISH);
            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

            if (request != null && (request.isIncludeFileContents() != null && request.isIncludeFileContents())) {
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
    

    
    /**
     * Executes "V3" version of "getDocument" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v3(Object requestObject) {
        GetDocumentResponse response = new GetDocumentResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;
        String documentGuid = null;
        List<Long> fileIdList = new ArrayList<Long>();
        boolean includeFileContents = false;

        try {
            logger.debug("getDocument.v3 invoked.");
            GetDocumentRequest request = (GetDocumentRequest) requestObject;
            if (request != null) {
                documentId = request.getDocumentId();
                if (documentId == null) {
                	documentGuid = request.getDocumentGuid();
                }
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
            // andmeid näha
            this.getUserService().checkApplicationReadPrivilege(applicationName);

            // Kontrollime, kas päringus märgitud isik on teenuse kasutaja
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

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
                    if (documentId != null) {
                    	aditCodedException.setParameters(new Object[] {documentId.toString() });
                    } else {
                    	aditCodedException.setParameters(new Object[] {documentGuid.toString() });
                    }
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
                                if (documentId != null) {
                                	aditCodedException.setParameters(new Object[] {documentId.toString() });
                                } else {
                                	aditCodedException.setParameters(new Object[] {documentGuid.toString() });
                                }
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
                    jdigidocCfgTmpFile, false, true);
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
                    GetDocumentResponseAttachmentV3 attachment = new GetDocumentResponseAttachmentV3();
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
                    boolean isViewed = this.getDocumentService().getDocumentHistoryDAO()
                		.checkIfHistoryEventExists(DocumentService.HISTORY_TYPE_MARK_VIEWED, doc.getId(), user.getUserCode());

                    if (!isViewed && documentId != null) {
                        // Add first viewing history event
                        this.getDocumentService().addHistoryEvent(applicationName, documentId, user.getUserCode(),
                            DocumentService.HISTORY_TYPE_MARK_VIEWED, xroadRequestUser.getUserCode(),
                            xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_MARK_VIEWED,
                            user.getFullName(), requestDate.getTime());
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
                            ScheduleClient.NOTIFICATION_TYPE_VIEW) != null) && documentId != null) {

                        	String userInfo = user.getFullName() != null && !user.getFullName().trim().isEmpty() ?
											user.getFullName() : user.getUserCode();
                        	
                        	List<Message> messageInAllKnownLanguages = this.getMessageService().getMessages(
                        			"scheduler.message.view", new Object[] {doc.getTitle(), userInfo});
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
            	AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            	if (request.getDocumentId() != null) {
	                logger.debug("Requested document does not belong to user. Document ID: "
	                        + request.getDocumentId() + ", User ID: " + user.getUserCode());                
	                aditCodedException.setParameters(new Object[] {request.getDocumentId().toString(), user.getUserCode()});
            	} else if (request.getDocumentGuid() != null) {
            		logger.debug("Requested document does not belong to user. Document GUID: "
	                        + request.getDocumentGuid() + ", User ID: " + user.getUserCode());                
	                aditCodedException.setParameters(new Object[] {request.getDocumentGuid().toString(), user.getUserCode()});
            	}else {
            		logger.debug("Requested document does not belong to user. Document dhxReceiptId: "
	                        + request.getDhxReceiptId() + ", User ID: " + user.getUserCode());                
	                aditCodedException.setParameters(new Object[] {request.getDhxReceiptId(), user.getUserCode()});
            	}
                throw aditCodedException;
            }

            // Set response messages
            response.setSuccess(true);
            messages.setMessage(this.getMessageService().getMessages("request.getDocument.success", new Object[] {}));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.getDocument.success",
                    new Object[] {}, Locale.ENGLISH);
            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

            if (request != null && (request.isIncludeFileContents() != null && request.isIncludeFileContents())) {
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
        Document doc = null;
        if (request.getDocumentId() != null) {
        	doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());
        } else if (request.getDocumentGuid() != null) {
        	doc = this.documentService.getDocumentDAO().getDocumentByGuid(request.getDocumentGuid());
        }else if (request.getDvkId() != null) {
        	doc = this.documentService.getDocumentDAO().getDocumentByDhlId(request.getDvkId());
        }  
        else if (request.getDhxReceiptId() != null) {
        	doc = this.documentService.getDocumentDAO().getDocumentByDhxReceiptId(request.getDhxReceiptId());
        } 
        else {
        	logger.debug("Document ID nor document GUID are not defined.");
        	AditCodedException aditCodedException = new AditCodedException("document.idNotDefined");
        	throw aditCodedException;
        }
        
        // Check whether the document exists
        if (doc == null) {
        	if (request.getDocumentId() != null) {
        		logger.debug("Requested document does not exist. Document ID: " + request.getDocumentId());
        	} else if (request.getDocumentGuid() != null) {
        		logger.debug("Requested document does not exist. Document GUID: " + request.getDocumentGuid());
        	}
            AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
            if (request.getDocumentId() != null) {
            	aditCodedException.setParameters(new Object[] {request.getDocumentId().toString() });
            } else if (request.getDocumentGuid() != null) {
            	aditCodedException.setParameters(new Object[] {request.getDocumentGuid().toString() });
            }else if (request.getDvkId() != null) {
            	aditCodedException.setParameters(new Object[] {request.getDvkId().toString() });
            }
            else if (request.getDhxReceiptId() != null) {
            	aditCodedException.setParameters(new Object[] {request.getDhxReceiptId().toString() });
            }
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
            if ((request.getDocumentId() == null || request.getDocumentId() <= 0) 
            		&& (request.getDocumentGuid() == null || request.getDocumentGuid().isEmpty())
            		&& (request.getDvkId() == null || request.getDvkId()<=0)
            		&& (StringUtil.isNullOrEmpty(request.getDhxReceiptId()))) {
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
        logger.debug("DVK ID: " + String.valueOf(request.getDvkId()));
        logger.debug("DHX receipt ID: " + request.getDhxReceiptId());
        logger.debug("Document GUID: " + String.valueOf(request.getDocumentGuid()));
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
