package ee.adit.ws.endpoint.document;

import java.io.File;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.pojo.DocumentSendStatus;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetDocumentRequest;
import ee.adit.pojo.GetSendStatusRequest;
import ee.adit.pojo.GetSendStatusRequestAttachment;
import ee.adit.pojo.GetSendStatusResponse;
import ee.adit.pojo.GetSendStatusResponseAttachment;
import ee.adit.pojo.GetSendStatusResponseDocument;
import ee.adit.pojo.Message;
import ee.adit.pojo.OutputDocument;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.schedule.ScheduleClient;
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
 * Implementation of "getSendStatus" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 *
 * @author Aleksei Kokarev, BPW Consulting OÜ, aleksei.kokarev@bpw-consulting.com
 */
@XTeeService(name = "getSendStatus", version = "v1")
@Component
public class GetSendStatusEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(GetSendStatusEndpoint.class);

    private UserService userService;
    private DocumentService documentService;
    private ScheduleClient scheduleClient;
    private String digidocConfigurationFile;


    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("getSendStatus invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "getSendStatus" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        GetSendStatusResponse response = new GetSendStatusResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;
        
        try {
            logger.debug("getSendStatus.v1 invoked.");
            GetSendStatusRequest request = (GetSendStatusRequest) requestObject;
            String attachmentID = null;
            // Check if the attachment ID is specified
            if (request.getDocument() != null && request.getDocument().getHref() != null
                    && !request.getDocument().getHref().trim().equals("")) {
                attachmentID = Util.extractContentID(request.getDocument().getHref());
            } else {
                throw new AditCodedException("request.getSendStatus.attachment.id.notSpecified");
            }

            // All primary checks passed.
            logger.info("Processing attachment with id: '" + attachmentID + "'");
            // Extract the SOAP message to a temporary file
            String base64EncodedFile = extractAttachmentXML(this.getRequestMessage(), attachmentID);

            // Base64 decode and unzip the temporary file
            String xmlFileInput = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this
                    .getConfiguration().getDeleteTemporaryFilesAsBoolean());
            logger.info("Attachment unzipped to temporary file: " + xmlFileInput);

            // Extract large files from main document
            FileSplitResult splitResult = Util.splitOutTags(xmlFileInput, "data", false, false, true, true);

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
                unmarshalledObject = unMarshal(xmlFileInput);
            } catch (Exception e) {
                logger.error("Error while unmarshalling SOAP attachment: ", e);
                AditCodedException aditCodedException = new AditCodedException("request.attachments.invalidFormat");
                throw aditCodedException;
            }

            boolean involvedSignatureContainerExtraction = false;
            boolean saveDocument = false;
            // Check if the marshalling result is what we expected
            if (unmarshalledObject != null) {
                logger.info("XML unmarshalled to type: " + unmarshalledObject.getClass());
                if (unmarshalledObject instanceof GetSendStatusRequestAttachment) {
                	GetSendStatusRequestAttachment requestAttachment = (GetSendStatusRequestAttachment) unmarshalledObject;
                	 InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(getDigidocConfigurationFile());
                     String jdigidocCfgTmpFile = Util.createTemporaryFile(input, getConfiguration().getTempDir());
                	 CustomXTeeHeader header = this.getHeader();
                     String applicationName = header.getInfosysteem(this.getConfiguration().getXteeProducerName());

                     // Log request
                     Util.printHeader(header, this.getConfiguration());

                     // Check header for required fields
                     checkHeader(header);

                     // Kontrollime, kas päringu käivitanud infosüsteem on ADITis
                     // registreeritud
                     this.getUserService().checkApplicationRegistered(applicationName);

                     // Kontrollime, kas päringu käivitanud infosüsteem tohib
                     // andmeid näha
                     this.getUserService().checkApplicationReadPrivilege(applicationName);
                     
                	 List<DocumentSendStatus> documentSendStatuses = this.documentService.getDocumentDAO().getDocumentsForSendStatus(requestAttachment.getDhlIds());
                     if (documentSendStatuses != null) {
                             // 1. Convert java list to XML string and output
                             // to file
                             GetSendStatusResponseAttachment attachment = new GetSendStatusResponseAttachment();
                            /* List<OutputDocument> outputDocuments = new ArrayList<OutputDocument>();
                             outputDocuments.add(resultDoc);*/
                             attachment.setDocuments(documentSendStatuses);
                             String xmlFile = marshal(attachment);
                             Util.joinSplitXML(xmlFile, "data");

                             // 2. GZip the temporary file Base64 encoding
                             // will be done at SOAP envelope level
                             String gzipFileName = Util.gzipFile(xmlFile, this.getConfiguration().getTempDir());

                             // 3. Add as an attachment
                             String contentID = addAttachment(gzipFileName);
                             GetSendStatusResponseDocument responseDoc = new GetSendStatusResponseDocument();
                             responseDoc.setHref("cid:" + contentID);
                             response.setDocument(responseDoc);
                     } else {
                         logger.debug("Document has no files!");
                     }
                	 // Set response messages
                     response.setSuccess(true);
                     messages.setMessage(this.getMessageService().getMessages("request.getSendStatus.success", new Object[] {}));
                     response.setMessages(messages);

                     String additionalMessage = this.getMessageService().getMessage("request.getDocument.success",
                             new Object[] {}, Locale.ENGLISH);
                     additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

                   /*  if (request != null && request.isIncludeFileContents()) {
                         additionalInformationForLog = additionalInformationForLog + ("(Including files)");
                     }*/
                }
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
        GetSendStatusResponse response = new GetSendStatusResponse();
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

    public String getDigidocConfigurationFile() {
        return digidocConfigurationFile;
    }

    public void setDigidocConfigurationFile(String digidocConfigurationFile) {
        this.digidocConfigurationFile = digidocConfigurationFile;
    }
}
