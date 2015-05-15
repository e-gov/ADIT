package ee.adit.ws.endpoint.document;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.ws.mime.Attachment;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.exception.AditMultipleException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.SaveDocumentRequest;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveDocumentRequestDocument;
import ee.adit.pojo.SaveDocumentResponse;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.FileSplitResult;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "saveDocument" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "saveDocument", version = "v1")
@Component
public class SaveDocumentEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(SaveDocumentEndpoint.class);

    private UserService userService;

    private DocumentService documentService;

    private String digidocConfigurationFile;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("saveDocument invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "saveDocument" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    @SuppressWarnings("unchecked")
    protected Object v1(Object requestObject) {
        SaveDocumentResponse response = new SaveDocumentResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;
        boolean updatedExistingDocument = false;

        try {
            logger.debug("SaveDocumentEndpoint.v1 invoked.");
            CustomXTeeHeader header = this.getHeader();
            String applicationName = header.getInfosysteem();
            SaveDocumentRequest request = (SaveDocumentRequest) requestObject;

            // Log request
            Util.printHeader(header);

            // Check header for required fields
            checkHeader(header);

            // Check whether or not the user who executed current query is registered.
            AditUser user = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());
            AditUser xroadRequestUser = Util.getXroadUserFromXroadHeader(user, this.getHeader(), this.getUserService());

            checkRights(request, applicationName, user);

            String attachmentID = null;
            // Check if the attachment ID is specified
            if (request.getDocument() != null && request.getDocument().getHref() != null
                    && !request.getDocument().getHref().trim().equals("")) {
                attachmentID = Util.extractContentID(request.getDocument().getHref());
            } else {
                throw new AditCodedException("request.saveDocument.attachment.id.notSpecified");
            }

            // All primary checks passed.
            logger.debug("Processing attachment with id: '" + attachmentID + "'");
            // Extract the SOAP message to a temporary file
            String base64EncodedFile = extractAttachmentXML(this.getRequestMessage(), attachmentID);

            // Base64 decode and unzip the temporary file
            String xmlFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this
                    .getConfiguration().getDeleteTemporaryFilesAsBoolean());
            logger.debug("Attachment unzipped to temporary file: " + xmlFile);

            // Extract large files from main document
            FileSplitResult splitResult = Util.splitOutTags(xmlFile, "data", false, false, true, true);

            // Decode base64-encoded files
            if ((splitResult.getSubFiles() != null) && (splitResult.getSubFiles().size() > 0)) {
                for (String fileName : splitResult.getSubFiles()) {
                    String resultFile = Util.base64DecodeFile(fileName, this.getConfiguration().getTempDir());
                    // Replace encoded file with decoded file
                    (new File(fileName)).delete();
                    (new File(resultFile)).renameTo(new File(fileName));
                }
            }

            // Unmarshal the XML from the temporary file
            Object unmarshalledObject = null;
            try {
                unmarshalledObject = unMarshal(xmlFile);
            } catch (Exception e) {
                logger.error("Error while unmarshalling SOAP attachment: ", e);
                AditCodedException aditCodedException = new AditCodedException("request.attachments.invalidFormat");
                throw aditCodedException;
            }

            boolean involvedSignatureContainerExtraction = false;

            // Check if the marshalling result is what we expected
            if (unmarshalledObject != null) {
                logger.debug("XML unmarshalled to type: " + unmarshalledObject.getClass());
                if (unmarshalledObject instanceof SaveDocumentRequestAttachment) {
                    SaveDocumentRequestAttachment document = (SaveDocumentRequestAttachment) unmarshalledObject;

                    // Check document metadata
                    this.getDocumentService().checkAttachedDocumentMetadataForNewDocument(document);

                    // Kas kasutajal on piisavalt vaba kettaruumi
                    long remainingDiskQuota = this.getUserService().getRemainingDiskQuota(user,
                            this.getConfiguration().getGlobalDiskQuota());

                    String creatorUserCode = null;
                    String creatorUserName = null;
                    // Set the person who made the query (necessary for
                    if (xroadRequestUser != null && xroadRequestUser.getUserCode() != null) {
                        creatorUserCode = xroadRequestUser.getUserCode();
                        creatorUserName = xroadRequestUser.getFullName();
                    } else {
                        creatorUserCode = header.getIsikukood();
                    }

                    InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(getDigidocConfigurationFile());
                    String jdigidocCfgTmpFile = Util.createTemporaryFile(input, getConfiguration().getTempDir());

                    if (document.getId() != null && document.getId() != 0) {
                        updatedExistingDocument = true;

                        // Determine whether or not this document can be
                        // modified
                        Document doc = this.documentService.getDocumentDAO().getDocument(document.getId());
                        runExistingDocumentChecks(doc, user.getUserCode());

                        logger.debug("Modifying document. ID: " + document.getId());

                        // Document to database
                        SaveItemInternalResult saveResult = this.getDocumentService().save(document,
                                user.getUserCode(), applicationName, remainingDiskQuota, creatorUserCode,
                                creatorUserName, user.getFullName(), jdigidocCfgTmpFile);
                        if (saveResult.isSuccess()) {
                            documentId = saveResult.getItemId();
                            logger.debug("Document saved with ID: " + documentId.toString());
                            response.setDocumentId(documentId);

                            // Update user disk quota (used)
                            logger.info("User disk quota shrinked by: " + saveResult.getAddedFilesSize());
                            Long usedDiskQuota = user.getDiskQuotaUsed();
                            if (usedDiskQuota == null) {
                            	usedDiskQuota = 0L;
                            }
                            user.setDiskQuotaUsed(usedDiskQuota + saveResult.getAddedFilesSize());
                            this.getUserService().getAditUserDAO().saveOrUpdate(user);

                            involvedSignatureContainerExtraction = saveResult.isInvolvedSignatureContainerExtraction();
                        } else {
                            if ((saveResult.getMessages() != null) && (saveResult.getMessages().size() > 0)) {
                                AditMultipleException aditMultipleException = new AditMultipleException(
                                        "MultiException");
                                aditMultipleException.setMessages(saveResult.getMessages());
                                throw aditMultipleException;
                            } else {
                                throw new AditException("Document saving failed!");
                            }
                        }
                    } else {
                        logger.debug("Adding new document. GUID: " + document.getGuid());

                        // Document to database
                        SaveItemInternalResult saveResult = this.getDocumentService().save(document,
                                user.getUserCode(), applicationName, remainingDiskQuota, creatorUserCode,
                                creatorUserName, user.getFullName(), jdigidocCfgTmpFile);
                        if (saveResult.isSuccess()) {
                            documentId = saveResult.getItemId();
                            logger.debug("Document saved with ID: " + documentId.toString());
                            response.setDocumentId(documentId);

                            // Update user disk quota (used)
                            logger.info("User disk quota shrinked by: " + saveResult.getAddedFilesSize());
                            Long usedDiskQuota = user.getDiskQuotaUsed();
                            if (usedDiskQuota == null) {
                            	usedDiskQuota = 0L;
                            }
                            user.setDiskQuotaUsed(usedDiskQuota + saveResult.getAddedFilesSize());
                            this.getUserService().getAditUserDAO().saveOrUpdate(user);

                            involvedSignatureContainerExtraction = saveResult.isInvolvedSignatureContainerExtraction();
                        } else {
                            if ((saveResult.getMessages() != null) && (saveResult.getMessages().size() > 0)) {
                                AditMultipleException aditMultipleException = new AditMultipleException(
                                        "MultiException");
                                aditMultipleException.setMessages(saveResult.getMessages());
                                throw aditMultipleException;
                            } else {
                                throw new AditException("Document saving failed!");
                            }
                        }
                    }

                } else {
                    throw new AditInternalException("Unmarshalling returned wrong type. Expected "
                            + SaveDocumentRequestAttachment.class + ", got " + unmarshalledObject.getClass());
                }
            } else {
                throw new AditInternalException("Unmarshalling failed for XML in file: " + xmlFile);
            }

            if (involvedSignatureContainerExtraction) {
                DocumentHistory signatureContainerExtractionEvent = new DocumentHistory(
             		 DocumentService.HISTORY_TYPE_EXTRACT_FILE, documentId,
             		 requestDate.getTime(), user, xroadRequestUser, header);
               signatureContainerExtractionEvent.setDescription(DocumentService.DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE);
               this.getDocumentService().getDocumentHistoryDAO().save(signatureContainerExtractionEvent);
            }

            // If saving was successful then add history event
            DocumentHistory historyEvent = new DocumentHistory(
                    (updatedExistingDocument ? DocumentService.HISTORY_TYPE_MODIFY : DocumentService.HISTORY_TYPE_CREATE),
                    documentId, requestDate.getTime(), user, xroadRequestUser, header);
            if (updatedExistingDocument) {
                historyEvent.setDescription(DocumentService.DOCUMENT_HISTORY_DESCRIPTION_MODIFY);
            } else {
                historyEvent.setDescription(DocumentService.DOCUMENT_HISTORY_DESCRIPTION_CREATE);
            }
            this.getDocumentService().getDocumentHistoryDAO().save(historyEvent);

            // Set response messages
            response.setSuccess(new Success(true));
            messages.setMessage(this.getMessageService().getMessages("request.saveDocument.success", new Object[] {}));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.saveDocument.success",
                    new Object[] {}, Locale.ENGLISH);
            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

        } catch (Exception e) {
            String errorMessage = null;
            logger.error("Exception: ", e);
            response.setSuccess(new Success(false));
            ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

            if (e instanceof AditCodedException) {
                logger.debug("Adding exception messages to response object.");
                arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
                errorMessage = this.getMessageService().getMessage(e.getMessage(),
                        ((AditCodedException) e).getParameters(), Locale.ENGLISH);
                errorMessage = "ERROR: " + errorMessage;
            } else if (e instanceof AditMultipleException) {
                AditMultipleException aditMultipleException = (AditMultipleException) e;
                arrayOfMessage.setMessage(aditMultipleException.getMessages());
                if (aditMultipleException.getMessages() != null && aditMultipleException.getMessages().size() > 0) {
                    Message englishMessage = Util.getMessageByLocale(aditMultipleException.getMessages(), Locale.ENGLISH);
                    if (englishMessage != null) {
                    	errorMessage = "ERROR: " + englishMessage.getValue();
                    }
                }
            } else if (e instanceof AditException) {
                logger.debug("Adding exception message to response object.");
                arrayOfMessage.getMessage().add(new Message("en", e.getMessage()));
                errorMessage = "ERROR: " + e.getMessage();
            } else {
            	arrayOfMessage.setMessage(this.getMessageService().getMessages(MessageService.GENERIC_ERROR_CODE, new Object[]{}));
                errorMessage = "ERROR: " + e.getMessage();
            }

            logger.debug("Adding exception messages to response object.");
            response.setMessages(arrayOfMessage);

            additionalInformationForLog = errorMessage;
            super.logError(documentId, requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);
        }

        logger.debug("Adding request attachments to response object.");
        try {
            super.setIgnoreAttachmentHeaders(true);
            boolean cidAdded = false;
            Iterator<Attachment> i = this.getRequestMessage().getAttachments();
            while (i.hasNext()) {
                Attachment attachment = i.next();
                String contentId = attachment.getContentId();
                if ((contentId == null) || (contentId.length() < 1)) {
                    contentId = Util.generateRandomID();
                } else {
                    contentId = Util.stripContentID(contentId);
                }
                this.getResponseMessage().addAttachment(contentId, attachment.getDataHandler());
                if (!cidAdded) {
                    response.setDocument(new SaveDocumentRequestDocument("cid:" + contentId));
                    cidAdded = true;
                }
            }
        } catch (Exception ex) {
            logger.error("Failed sending request attachments back within response object!", ex);
        }

        super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object getResultForGenericException(Exception ex) {
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: "
                + ex.getMessage());
        SaveDocumentResponse response = new SaveDocumentResponse();
        response.setSuccess(new Success(false));
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        logger.debug("Adding request attachments to response object.");
        try {
            super.setIgnoreAttachmentHeaders(true);
            boolean cidAdded = false;
            Iterator<Attachment> i = this.getRequestMessage().getAttachments();
            while (i.hasNext()) {
                Attachment attachment = i.next();
                String contentId = attachment.getContentId();
                if ((contentId == null) || (contentId.length() < 1)) {
                    contentId = Util.generateRandomID();
                }
                this.getResponseMessage().addAttachment(contentId, attachment.getDataHandler());
                if (!cidAdded) {
                    response.setDocument(new SaveDocumentRequestDocument("cid:" + contentId));
                    cidAdded = true;
                }
            }
        } catch (Exception e) {
            logger.error("Failed sending request attachments back within response object!", ex);
        }
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
     */
    private void checkRights(
    	final SaveDocumentRequest request, final String applicationName,
    	final AditUser user) {

        // Check whether or not the application that executed
        // current query is registered.
        boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
        if (!applicationRegistered) {
            AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }

        // Check whether or not the application is allowed
        // to view or modify data.
        int accessLevel = this.getUserService().getAccessLevel(applicationName);
        if (accessLevel != 2) {
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.write");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }

        // Check whether or not the user is active (account not deleted)
        if ((user.getActive() == null) || !user.getActive()) {
            AditCodedException aditCodedException = new AditCodedException("user.inactive");
            aditCodedException.setParameters(new Object[] {user.getUserCode()});
            throw aditCodedException;
        }

        // Check whether or not the application has rights to
        // modify current user's data.
        int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
        if (applicationAccessLevelForUser != 2) {
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.forUser.write");
            aditCodedException.setParameters(new Object[] {applicationName, user.getUserCode() });
            throw aditCodedException;
        }
    }

    /**
     * Checks the specified document data and throws an error if data is
     * incorrect.
     *
     * @param existingDoc
     *     Document to be checked
     * @param userCode
     *     Code of user who executed saveDocument request
     * @throws AditException
     */
    protected void runExistingDocumentChecks(Document existingDoc, String userCode) throws AditCodedException {
        if (!userCode.equalsIgnoreCase(existingDoc.getCreatorCode())) {
            AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            aditCodedException.setParameters(new Object[] {new Long(existingDoc.getId()).toString(), userCode });
            throw aditCodedException;
        }

        // Null checks in following statements need to be there
        // (i.e. don't remove them).
        if ((existingDoc.getLocked() != null) && existingDoc.getLocked()) {
            AditCodedException aditCodedException = new AditCodedException("request.saveDocument.document.locked");
            aditCodedException.setParameters(new Object[] {existingDoc.getLockingDate(), userCode });
            throw aditCodedException;
        }
        if ((existingDoc.getDeflated() != null) && existingDoc.getDeflated()) {
            AditCodedException aditCodedException = new AditCodedException("request.saveDocument.document.deflated");
            aditCodedException
                    .setParameters(new Object[] {Util.dateToEstonianDateString(existingDoc.getDeflateDate()) });
            throw aditCodedException;
        }
        if ((existingDoc.getDeleted() != null) && existingDoc.getDeleted()) {
            throw new AditCodedException("request.saveDocument.document.deleted");
        }
        if ((existingDoc.getInvisibleToOwner() != null) && existingDoc.getInvisibleToOwner()) {
            throw new AditCodedException("request.saveDocument.document.deleted");
        }
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
