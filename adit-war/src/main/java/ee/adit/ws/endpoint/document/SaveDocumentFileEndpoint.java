package ee.adit.ws.endpoint.document;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.digidoc4j.exceptions.CertificateNotFoundException;
import org.digidoc4j.exceptions.CertificateRevokedException;
import org.digidoc4j.exceptions.DigiDoc4JException;

import org.apache.logging.log4j.LogManager; 
import org.apache.logging.log4j.Logger;

import org.springframework.stereotype.Component;
import org.springframework.ws.mime.Attachment;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.exception.AditMultipleException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.SaveDocumentFileRequest;
import ee.adit.pojo.SaveDocumentFileRequestAttachment;
import ee.adit.pojo.SaveDocumentFileRequestFile;
import ee.adit.pojo.SaveDocumentFileResponse;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.service.DocumentService;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.FileSplitResult;
import ee.adit.util.Util;
import ee.adit.util.xroad.CustomXRoadHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "saveDocumentFile" web method (web service request).
 * Contains request input validation, request-specific workflow and response
 * composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "saveDocumentFile", version = "v1")
@Component
public class SaveDocumentFileEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = LogManager.getLogger(SaveDocumentFileEndpoint.class);

    private UserService userService;

    private DocumentService documentService;

    private String digidocConfigurationFile;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("saveDocumentFile invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "saveDocumentFile" request.
     *
     * @param requestObject Request body object
     * @return Response body object
     */
    @SuppressWarnings("unchecked")
    protected Object v1(Object requestObject) {
        SaveDocumentFileResponse response = new SaveDocumentFileResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        Long documentId = null;
        boolean updatedExistingFile = false;
        long documentFileId = 0;

        try {
            logger.debug("saveDocumentFile.v1 invoked.");
            SaveDocumentFileRequest request = (SaveDocumentFileRequest) requestObject;
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

            // Check user's disk quota
            long remainingDiskQuota = this.getUserService().getRemainingDiskQuota(user,
                    this.getConfiguration().getGlobalDiskQuota());

            Document doc = checkRightsAndGetDocument(request, applicationName, user);

            String attachmentID = null;
            // Check if the attachment ID is specified
            if (request.getFile() != null && !Util.isNullOrEmpty(request.getFile().getHref())) {
                attachmentID = Util.extractContentID(request.getFile().getHref());
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
            Object unmarshalledObject = unMarshal(xmlFile);

            boolean involvedSignatureContainerExtraction = false;

            // Check if the marshalling result is what we expected
            if (unmarshalledObject != null) {
                logger.debug("XML unmarshalled to type: " + unmarshalledObject.getClass());
                if (unmarshalledObject instanceof SaveDocumentFileRequestAttachment) {
                    OutputDocumentFile docFile = ((SaveDocumentFileRequestAttachment) unmarshalledObject).getFile();
                    updatedExistingFile = ((docFile.getId() != null) && (docFile.getId() > 0));
                    documentFileId = (docFile.getId() == null) ? 0L : docFile.getId();

                    InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(getDigidocConfigurationFile());
                    String jdigidocCfgTmpFile = Util.createTemporaryFile(input, getConfiguration().getTempDir());

                    SaveItemInternalResult saveResult = this.getDocumentService().saveDocumentFile(doc.getId(),
                            docFile, remainingDiskQuota, this.getConfiguration().getTempDir(), jdigidocCfgTmpFile);

                    if (saveResult.isSuccess()) {
                        long fileId = saveResult.getItemId();
                        documentFileId = fileId;
                        logger.debug("File saved with ID: " + fileId);
                        response.setFileId(fileId);
                        involvedSignatureContainerExtraction = saveResult.isInvolvedSignatureContainerExtraction();
                    } else {
                        if ((saveResult.getMessages() != null) && (saveResult.getMessages().size() > 0)) {
                            AditMultipleException aditMultipleException = new AditMultipleException("MultiException");
                            aditMultipleException.setMessages(saveResult.getMessages());
                            throw aditMultipleException;
                        } else {
                            throw new AditInternalException("File saving failed!");
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
                this.getDocumentService().addHistoryEvent(applicationName, documentId, user.getUserCode(),
                    DocumentService.HISTORY_TYPE_EXTRACT_FILE, xroadRequestUser.getUserCode(),
                    xroadRequestUser.getFullName(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE,
                    user.getFullName(), requestDate.getTime());
            }

            // If saving was successful then add history event
            this.getDocumentService().addHistoryEvent(applicationName, documentId, user.getUserCode(),
                (updatedExistingFile ? DocumentService.HISTORY_TYPE_MODIFY_FILE : DocumentService.HISTORY_TYPE_ADD_FILE),
                xroadRequestUser.getUserCode(), xroadRequestUser.getFullName(),
                (updatedExistingFile ? (DocumentService.DOCUMENT_HISTORY_DESCRIPTION_MODIFYFILE + documentFileId) : DocumentService.DOCUMENT_HISTORY_DESCRIPTION_ADDFILE),
                user.getFullName(), requestDate.getTime());

			// update doc last modified date
			doc.setLastModifiedDate(new Date());
			this.documentService.save(doc, Long.MAX_VALUE);

            // Set response messages
            response.setSuccess(true);
            messages.setMessage(this.getMessageService().getMessages("request.saveDocumentFile.success",
                    new Object[] {}));
            response.setMessages(messages);

            String additionalMessage = this.getMessageService().getMessage("request.saveDocumentFile.success",
                    new Object[] {}, Locale.ENGLISH);
            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

        } catch (Exception e) {
            logger.error("Exception: ", e);
            response.setSuccess(false);
            
            ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
            String errorMessage = null;

            if (e instanceof AditCodedException) {
                logger.debug("Adding exception messages to response object.");
                arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
                
                errorMessage = this.getMessageService().getMessage(e.getMessage(), ((AditCodedException) e).getParameters(), Locale.ENGLISH);
                if (errorMessage == null) {
                	errorMessage = "ERROR: " + errorMessage;
                }
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
            } else if (e instanceof DigiDoc4JException) { 
	        	if (e instanceof CertificateRevokedException) {
	        		arrayOfMessage.setMessage(this.getMessageService().getMessages("request.saveDocument.revokedcertificate", new Object[]{}));
	        	} else if (e instanceof CertificateNotFoundException){
	        		arrayOfMessage.setMessage(this.getMessageService().getMessages("request.saveDocument.unknowncertificate", new Object[]{}));  
	        	}
	        	errorMessage = "ERROR: " + e.getMessage();
	        } else {
            	arrayOfMessage.setMessage(this.getMessageService().getMessages(MessageService.GENERIC_ERROR_CODE, new Object[]{}));
                errorMessage = "ERROR: " + e.getMessage();
            }

            additionalInformationForLog = errorMessage;
            super.logError(documentId, requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);

            logger.debug("Adding exception messages to response object.");
            response.setMessages(arrayOfMessage);

            logger.debug("Adding request attachments to response object.");
            try {
                super.setIgnoreAttachmentHeaders(true);
                boolean cidAdded = false;
                
                Iterator<Attachment> it = this.getRequestMessage().getAttachments();
                while (it.hasNext()) {
                    Attachment attachment = it.next();
                    
                    String contentId = attachment.getContentId();
                    if ((contentId == null) || (contentId.length() < 1)) {
                        contentId = Util.generateRandomID();
                    } else {
                        contentId = Util.stripContentID(contentId);
                    }
                    
                    this.getResponseMessage().addAttachment(contentId, attachment.getDataHandler());
                    if (!cidAdded) {
                        response.setFile(new SaveDocumentFileRequestFile("cid:" + contentId));
                        cidAdded = true;
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed sending request attachments back within response object!", ex);
            }
        }

        super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
        
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object getResultForGenericException(Exception ex) {
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: " + ex.getMessage());
        SaveDocumentFileResponse response = new SaveDocumentFileResponse();
        response.setSuccess(false);
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
                    response.setFile(new SaveDocumentFileRequestFile("cid:" + contentId));
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
     * @param request Current request
     * @param applicationName Name of application that was used to execute current request
     * @param user User who executed current request
     * @return Requested document if user has necessary rights for it (or {@code null} otherwise).
     */
    private Document checkRightsAndGetDocument(
    	final SaveDocumentFileRequest request, final String applicationName,
    	final AditUser user) {

        // Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
        boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
        if (!applicationRegistered) {
            AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }

        // Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid muuta
        int accessLevel = this.getUserService().getAccessLevel(applicationName);
        if (accessLevel != 2) {
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.write");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }

        // Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
        if ((user.getActive() == null) || !user.getActive()) {
            AditCodedException aditCodedException = new AditCodedException("user.inactive");
            aditCodedException.setParameters(new Object[] {user.getUserCode()});
            throw aditCodedException;
        }

        // Check whether or not the application has rights to modify current user's data.
        int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
        if (applicationAccessLevelForUser != 2) {
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.forUser.write");
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
            aditCodedException.setParameters(new Object[] {request.getDocumentId().toString()});
            throw aditCodedException;
        }

        // Check whether the document is marked as deleted
        if ((doc.getDeleted() != null) && doc.getDeleted()) {
            logger.debug("Requested document is deleted. Document ID: " + request.getDocumentId());
            AditCodedException aditCodedException = new AditCodedException("document.deleted");
            aditCodedException.setParameters(new Object[] {request.getDocumentId().toString()});
            throw aditCodedException;
        }

        // Check whether the document is marked as deflated
        if ((doc.getDeflated() != null) && doc.getDeflated()) {
            logger.debug("Requested document is deflated. Document ID: " + request.getDocumentId());
            AditCodedException aditCodedException = new AditCodedException("document.deflated");
            aditCodedException.setParameters(new Object[] {Util.dateToEstonianDateString(doc.getDeflateDate()) });
            throw aditCodedException;
        }

        // Check whether the document is locked
        if ((doc.getLocked() != null) && doc.getLocked()) {
            logger.debug("Requested document is locked. Document ID: " + request.getDocumentId());
            AditCodedException aditCodedException = new AditCodedException("request.saveDocumentFile.document.locked");
            aditCodedException.setParameters(new Object[] {doc.getLockingDate()});
            throw aditCodedException;
        }

        // File can be added to document only if:
        // a) document belongs to user
        // b) document is sent or shared to user
        boolean isOwner = false;
        if (doc.getCreatorCode().equalsIgnoreCase(user.getUserCode())) {
            // Check whether the document is marked as invisible to owner
            if ((doc.getInvisibleToOwner() != null) && doc.getInvisibleToOwner()) {
                AditCodedException aditCodedException = new AditCodedException("document.deleted");
                aditCodedException.setParameters(new Object[] {doc.getId()});
                throw aditCodedException;
            }

            isOwner = true;
        } else {
            if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
                Iterator<DocumentSharing> it = doc.getDocumentSharings().iterator();
                while (it.hasNext()) {
                    DocumentSharing sharing = it.next();
                    if (sharing.getUserCode() != null && sharing.getUserCode().equalsIgnoreCase(user.getUserCode())) {
                        // Check whether the document is marked as deleted by recipient
                        if ((sharing.getDeleted() != null) && sharing.getDeleted()) {
                            AditCodedException aditCodedException = new AditCodedException("document.deleted");
                            aditCodedException.setParameters(new Object[] {doc.getId()});
                            throw aditCodedException;
                        }

                        isOwner = true;
                        break;
                    }
                }
            }
        }

        if (!isOwner) {
            logger.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId()
                    + ", User ID: " + user.getUserCode());
            AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            aditCodedException.setParameters(new Object[] {request.getDocumentId().toString(), user.getUserCode()});
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
     * @param request Request body as {@link SaveDocumentFileRequest} object.
     * @throws AditCodedException Exception describing error found in requet body.
     */
    private void checkRequest(SaveDocumentFileRequest request) throws AditCodedException {
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
     *            Request body as {@link SaveDocumentFileRequest} object.
     */
    private void printRequest(SaveDocumentFileRequest request) {
        logger.debug("-------- SaveDocumentFileRequest -------");
        logger.debug("Document ID: " + request.getDocumentId());
        logger.debug("----------------------------------------");
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
