package ee.adit.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.cert.X509Certificate;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.DataFile;
import org.digidoc4j.DataToSign;
import org.digidoc4j.DigestAlgorithm;
import org.digidoc4j.EncryptionAlgorithm;
import org.digidoc4j.Signature;
import org.digidoc4j.SignatureBuilder;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.SignatureValidationResult;
import org.digidoc4j.ValidationResult;
import org.digidoc4j.X509Cert;
import org.digidoc4j.X509Cert.SubjectName;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import dvk.api.container.ArrayOfSignature;
import dvk.api.container.LetterMetaData;
import dvk.api.container.Metaxml;
import dvk.api.container.Person;
import dvk.api.container.v1.ContainerVer1;
import dvk.api.container.v2.ContainerVer2;
import dvk.api.container.v2.Fail;
import dvk.api.container.v2.FailideKonteiner;
import dvk.api.container.v2.MetaManual;
import dvk.api.container.v2.Metainfo;
import dvk.api.container.v2.Saaja;
import dvk.api.container.v2.Saatja;
import dvk.api.container.v2.SaatjaKontekst;
import dvk.api.container.v2.Transport;
import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.ml.PojoMessage;
import dvk.api.ml.PojoMessageRecipient;
import dvk.api.ml.PojoSettings;
import ee.adit.ContainerVer2_1Sender;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentFileDAO;
import ee.adit.dao.DocumentHistoryDAO;
import ee.adit.dao.DocumentSharingDAO;
import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.DocumentWfStatusDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dao.pojo.DocumentType;
import ee.adit.dto.DigitalSigningDTO;
import ee.adit.dvk.DvkReceiver;
import ee.adit.dvk.DvkReceiverFactory;
import ee.adit.dvk.DvkSender;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfDataFileHash;
import ee.adit.pojo.ArrayOfFileType;
import ee.adit.pojo.DataFileHash;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.PersonName;
import ee.adit.pojo.PrepareSignatureInternalResult;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.schedule.ScheduleClient;
import ee.adit.util.Configuration;
import ee.adit.util.DigiDocExtractionResult;
import ee.adit.util.SimplifiedDigiDocParser;
import ee.adit.util.StartEndOffsetPair;
import ee.adit.util.Util;


/**
 * Implements business logic for document processing. Provides methods for
 * processing documents (saving, retrieving, performing checks, etc.). Where
 * possible, the actual data queries are forwarded to DAO classes.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * @author Hendrik Pärna
 */
public class DocumentService {

    /**
     * Default MIME type to be used if MIME type cannot be determined.
     */
    public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";

    /**
     * Document sharing type code - sign.
     */
    public static final String SHARINGTYPE_SIGN = "sign";

    /**
     * Document sharing type code - share.
     */
    public static final String SHARINGTYPE_SHARE = "share";

    /**
     * Document sharing type code - send using DVK.
     */
    public static final String SHARINGTYPE_SEND_DVK = "send_dvk";

    /**
     * Document sharing type code - send using ADIT.
     */
    public static final String SHARINGTYPE_SEND_ADIT = "send_adit";

    /**
     * Document sharing type code - send using ADIT.
     */
    public static final String SHARINGTYPE_SEND_EMAIL = "send_email";

    /**
     * Document DVK status - missing.
     */
    public static final Long DVK_STATUS_MISSING = new Long(0);

    /**
     * Document DVK status - waiting.
     */
    public static final Long DVK_STATUS_WAITING = new Long(1);

    /**
     * Document DVK status - sending.
     */
    public static final Long DVK_STATUS_SENDING = new Long(2);

    /**
     * Document DVK status - sent.
     */
    public static final Long DVK_STATUS_SENT = new Long(3);

    /**
     * Document DVK status - aborted.
     */
    public static final Long DVK_STATUS_ABORTED = new Long(4);

    /**
     * Document DVK status - received.
     */
    public static final Long DVK_STATUS_RECEIVED = new Long(5);

    /**
     * DVK fault code used for deleted documents. Inserted to DVK when document
     * deleted.
     */
    public static final String DVK_FAULT_CODE_FOR_DELETED = "NO_FAULT: DELETED BY ADIT";

    /**
     * DVK message string for deleted documents. Inserted to DVK when document
     * deleted.
     */
    public static final String DVKBLOBMESSAGE_DELETE = "DELETED BY ADIT";

    /**
     * Document history type code - create.
     */
    public static final String HISTORY_TYPE_CREATE = "create";

    /**
     * Document history type code - modify.
     */
    public static final String HISTORY_TYPE_MODIFY = "modify";

    /**
     * Document history type code - add file.
     */
    public static final String HISTORY_TYPE_ADD_FILE = "add_file";

    /**
     * Document history type code - modify file.
     */
    public static final String HISTORY_TYPE_MODIFY_FILE = "modify_file";

    /**
     * Document history type code - delete file.
     */
    public static final String HISTORY_TYPE_DELETE_FILE = "delete_file";

    /**
     * Document history type code - modify status.
     */
    public static final String HISTORY_TYPE_MODIFY_STATUS = "modify_status";

    /**
     * Document history type code - send.
     */
    public static final String HISTORY_TYPE_SEND = "send";

    /**
     * Document history type code - share.
     */
    public static final String HISTORY_TYPE_SHARE = "share";

    /**
     * Document history type code - unshare.
     */
    public static final String HISTORY_TYPE_UNSHARE = "unshare";

    /**
     * Document history type code - lock.
     */
    public static final String HISTORY_TYPE_LOCK = "lock";

    /**
     * Document history type code - unlock.
     */
    public static final String HISTORY_TYPE_UNLOCK = "unlock";

    /**
     * Document history type code - deflate.
     */
    public static final String HISTORY_TYPE_DEFLATE = "deflate";

    /**
     * Document history type code - sign.
     */
    public static final String HISTORY_TYPE_SIGN = "sign";

    /**
     * Document history type code - delete.
     */
    public static final String HISTORY_TYPE_DELETE = "delete";

    /**
     * Document history type code - mark viewed.
     */
    public static final String HISTORY_TYPE_MARK_VIEWED = "mark_viewed";

    /**
     * Document history type code - extract file.
     */
    public static final String HISTORY_TYPE_EXTRACT_FILE = "extract_file";

    /**
     * DVK container version used when sending documents using DVK.
     */
    public static final int DVK_CONTAINER_VERSION = 2;

    /**
     * DVK response message title.
     */
    public static final String DVK_ERROR_RESPONSE_MESSAGE_TITLE = "ADIT vastuskiri";

    /**
     * DVK response message file name.
     */
    public static final String DVK_ERROR_RESPONSE_MESSAGE_FILENAME = "ADIT_vastuskiri.pdf";

    /**
     * Document type - letter.
     */
    public static final String DOCTYPE_LETTER = "letter";

    /**
     * Document type - application.
     */
    public static final String DOCTYPE_APPLICATION = "application";

    /**
     * DVK receive fail reason - user does not exist.
     */
    public static final Integer DVK_RECEIVE_FAIL_REASON_USER_DOES_NOT_EXIST = 1;

    /**
     * DVK receive fail reason - user uses DVK to exchange documents.
     */
    public static final Integer DVK_RECEIVE_FAIL_REASON_USER_USES_DVK = 2;

    // Document history description literals
    /**
     * Document history description - ADD FILE.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_ADDFILE = "Document file added";

    /**
     * Document history description - CREATE.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_CREATE = "Document created";

    /**
     * Document history description - LOCK.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_LOCK = "Document locked";

    /**
     * Document history description - UNLOCK.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_UNLOCK = "Document unlocked";

    /**
     * Document history description - DEFLATE.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_DEFLATE = "Document deflated";

    /**
     * Document history description - DELETE.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_DELETE = "Document deleted";

    /**
     * Document history description - DELETE FILE.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_DELETEFILE = "Document file deleted. ID: ";

    /**
     * Document history description - MODIFY STATUS.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_MODIFYSTATUS = "Document status modified to: ";

    /**
     * Document history description - MODIFY.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_MODIFY = "Document modified";

    /**
     * Document history description - MODIFY FILE.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_MODIFYFILE = "Document file modified. ID: ";

    /**
     * Document history type code - extract file.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE = "Files extracted from digital signature container";

    /**
     * Document history type code - mark viewed.
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_MARK_VIEWED = "Document viewed";

    /**
     * ID of file type "document file".
     */
    public static final long FILETYPE_DOCUMENT_FILE = 1L;

    /**
     * ID of file type "signature container".
     */
    public static final long FILETYPE_SIGNATURE_CONTAINER = 2L;

    /**
     * ID of file type "signature container draft".
     */
    public static final long FILETYPE_SIGNATURE_CONTAINER_DRAFT = 3L;
    
    /**
     * ID of file type "zip archive".
     */
    public static final long FILETYPE_ZIP_ARCHIVE = 4L;

    /**
     * Name of file type "document file".
     */
    public static final String FILETYPE_NAME_DOCUMENT_FILE = "document_file";

    /**
     * Name of file type "signature container".
     */
    public static final String FILETYPE_NAME_SIGNATURE_CONTAINER = "signature_container";

    /**
     * Name of file type "signature container draft".
     */
    public static final String FILETYPE_NAME_SIGNATURE_CONTAINER_DRAFT = "signature_container_draft";

    /**
     * Exception codes which digiDoc throws when certificate is revoked.
     */
    public static final Integer DIGIDOC_REVOKED_CERT_EXCPETION_CODE = 91;
    /**
     * Exception code which digiDoc throws when certificate is unknown.
     */
    public static final Integer DIGIDOC_UNKNOWN_CERT_EXCPETION_CODE = 92;

    
    /** RSA signatures have 128 bytes */
    public static final int SIGNATURE_VALUE_LENGTH = 128;

    /**
     * Name of file type "zip archive".
     */
    public static final String FILETYPE_NAME_ZIP_ARCHIVE = "zip_archive";

    private static Logger logger = Logger.getLogger(UserService.class);

    private MessageSource messageSource;

    private DocumentTypeDAO documentTypeDAO;

    private DocumentDAO documentDAO;

    private DocumentFileDAO documentFileDAO;

    private DocumentWfStatusDAO documentWfStatusDAO;

    private DocumentSharingDAO documentSharingDAO;

    private DocumentHistoryDAO documentHistoryDAO;

    private AditUserDAO aditUserDAO;

    private Configuration configuration;

    private DvkDAO dvkDAO;
    
    private NotificationService notificationService;

    /**
     * Methods verifies all signed documents using jDigiDoc and logs result.
     *
     * @param digidocConfigFile conf file
     * @throws Exception
     */
    public void verifySignedDocuments() throws Exception {
        logger.debug("Starting to check signed documents");
        
        List<Document> signedDocuments = getDocumentDAO().getSignedDocuments();
        logger.debug("Found signed documents: " + signedDocuments.size());
        
        for (Document document : signedDocuments) {
            logger.debug("Checking signed document with id: " + document.getId());
            
            //DocumentFile signatureContainer = findSignatureContainer(document);
            if ((document != null) && (document.getDocumentFiles() != null)) {
                for (DocumentFile file : document.getDocumentFiles()) {
                    if (file.getDocumentFileTypeId() == FILETYPE_SIGNATURE_CONTAINER && !file.getDeleted()) {
                        logger.debug("Signature documentFile id: " + file.getId());
                        
                        InputStream stream = null;
                        try {
                        	stream = new ByteArrayInputStream(file.getFileData());
                            
                            String containerType = Util.isBdocFile(file.getFileName()) ? ContainerBuilder.BDOC_CONTAINER_TYPE : ContainerBuilder.DDOC_CONTAINER_TYPE;
                            Container container = ContainerBuilder.aContainer(containerType).fromStream(stream).build();
                            
                            ValidationResult validationResult = container.validate();
                            if (validationResult.isValid()) {
                                List<Signature> signatures = container.getSignatures();
                                
                                if (signatures != null && signatures.size() > 0) {
                                    boolean hadTestSignature = false;
                                    
                                    for (Signature signature : signatures) {
                                    	X509Certificate cert = null;
                                        
                                        X509Cert signingSertificate = signature.getSigningCertificate();
                                        if ((signingSertificate != null) && (signingSertificate.getX509Certificate() != null)) {
                                            cert = signingSertificate.getX509Certificate();
                                        }
                                        
                                        if (Util.isTestCard(cert)) {
                                            hadTestSignature = true;
                                            logger.error("Signed document has test signature. DocumentId: " + document.getId() +
                                            			 "; Signature serialnumber: " + Util.getSubjectSerialNumberFromCert(cert));
                                        }
                                    }
                                    
                                    if (!hadTestSignature) {
                                        logger.debug("Signed document is OK. DocumentId: " + document.getId());
                                    }
                                }
                            } else {
                                for (DigiDoc4JException e : validationResult.getErrors()) {
                                    logger.error("Signed document with DocumentId: " + document.getId() + " has error. Error code: " + e.getErrorCode());
                                    logger.error("Signed document has errors. DocumentId: " + document.getId(), e);
                                }
                            }
                        } catch (Exception ex) {
                            logger.error("Error occured while checking signed document.", ex);
                        } finally {
                            Util.safeCloseStream(stream);
                        }
                    }
                }
            }
        }
        
        logger.debug("Signed documents check is finished");
    }
    
    
    /**
     * Checks if document metadata is sufficient and correct for creating a new document.
     *
     * @param document document metadata
     * @throws AditCodedException if metadata is insuffidient or incorrect
     */
    public void checkAttachedDocumentMetadataForNewDocument(SaveDocumentRequestAttachment document) throws AditCodedException {
        logger.debug("Checking attached document metadata for new document...");
        
        if (document != null) {

            logger.debug("Checking GUID: " + document.getGuid());
            // Check GUID
            if (document.getGuid() != null) {
                // Check GUID format
                try {
                    UUID.fromString(document.getGuid());
                } catch (Exception e) {
                    throw new AditCodedException("request.saveDocument.document.guid.wrongFormat");
                }
            }

            logger.debug("Checking title: " + document.getTitle());
            // Check title
            if (document.getTitle() == null || "".equalsIgnoreCase(document.getTitle())) {
                throw new AditCodedException("request.saveDocument.document.title.undefined");
            }

            logger.debug("Checking document type: " + document.getDocumentType());
            // Check document_type

            if (document.getDocumentType() != null && !"".equalsIgnoreCase(document.getDocumentType().trim())) {

                // Is the document type valid?
                logger.debug("Document type is defined. Checking if it is valid.");
                DocumentType documentType = this.getDocumentTypeDAO().getDocumentType(document.getDocumentType());

                if (documentType == null) {
                    logger.debug("Document type does not exist: " + document.getDocumentType());
                    String validDocumentTypes = getValidDocumentTypes();

                    AditCodedException aditCodedException = new AditCodedException(
                            "request.saveDocument.document.type.nonExistent");
                    aditCodedException.setParameters(new Object[]{validDocumentTypes});
                    throw aditCodedException;
                }

            } else {
                String validDocumentTypes = getValidDocumentTypes();
                AditCodedException aditCodedException = new AditCodedException(
                        "request.saveDocument.document.type.undefined");
                aditCodedException.setParameters(new Object[]{validDocumentTypes});
                throw aditCodedException;
            }

            logger.debug("Checking previous document ID: " + document.getPreviousDocumentID());
            // Check previous_document_id
            if (document.getPreviousDocumentID() != null && document.getPreviousDocumentID() != 0) {
                // Check if the document exists

                Document previousDocument = this.getDocumentDAO().getDocument(document.getPreviousDocumentID());

                if (previousDocument == null) {
                    AditCodedException aditCodedException = new AditCodedException(
                            "request.saveDocument.document.previousDocument.nonExistent");
                    aditCodedException.setParameters(new Object[]{document.getPreviousDocumentID().toString()});
                    throw aditCodedException;
                }
            }
        } else {
            throw new AditInternalException("Document not initialized.");
        }
    }

    
    /**
     * Retrieves a list of valid document types.
     *
     * @return List of valid document types as a comma separated list
     */
    public String getValidDocumentTypes() {
        StringBuffer result = new StringBuffer();
        List<DocumentType> documentTypes = this.getDocumentTypeDAO().listDocumentTypes();

        for (int i = 0; i < documentTypes.size(); i++) {
            DocumentType documentType = documentTypes.get(i);

            if (i > 0) {
                result.append(", ");
            }
            result.append(documentType.getShortName());

        }

        return result.toString();
    }

    /**
     * Defaltes document file. Replaces the data with MD5 hash. <br>
     * <br>
     * Returns one of the following result codes:<br>
     * "ok" - deflation succeeded<br>
     * "already_deleted" - specified file is already deleted<br>
     * "file_does_not_belong_to_document" - specified file does not belong to
     * specified document<br>
     * "file_does_not_exist" - specified file does not exist
     *
     * @param documentId      document ID
     * @param fileId          file ID
     * @param markDeleted     Add "deleted" flag to specified file
     * @param failIfSignature If true then method will throw an error while attempting to delete
     *                        digital signature container.
     * @return Deflation result code.
     */
    public String deflateDocumentFile(long documentId, long fileId, boolean markDeleted, boolean failIfSignature) {
        return this.getDocumentFileDAO().deflateDocumentFile(documentId, fileId, markDeleted, failIfSignature);
    }

    /**
     * Saves a document using the request attachment.
     *
     * @param attachmentDocument document as an attachment
     * @param creatorCode        document creator code
     * @param remoteApplication  remote application name
     * @param remainingDiskQuota Disk quota remaining for this user
     * @param creatorUserCode    Personal ID code of person who executed document save request
     * @param creatorUserName    Name of person who executed document save request
     * @param creatorName        The code of the user that saves the document (present time name)
     * @param digidocConfigFile  Full path to DigiDoc library configuration file.
     * @return save result
     * @throws FileNotFoundException
     */
    @Transactional
    public SaveItemInternalResult save(
            final SaveDocumentRequestAttachment attachmentDocument,
            final String creatorCode,
            final String remoteApplication,
            final long remainingDiskQuota,
            final String creatorUserCode,
            final String creatorUserName,
            final String creatorName,
            final String digidocConfigFile)
            throws FileNotFoundException, AditCodedException {

        final DocumentDAO docDao = this.getDocumentDAO();

        return (SaveItemInternalResult) this.getDocumentDAO().getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException, AditCodedException {
                boolean involvedSignatureContainerExtraction = false;
                Date creationDate = new Date();
                Document document = new Document();

                if ((attachmentDocument.getId() != null) && (attachmentDocument.getId() > 0)) {
                    document = (Document) session.get(Document.class, attachmentDocument.getId());
                    logger.debug("Document file count: " + document.getDocumentFiles().size());
                } else {
                    document.setCreationDate(creationDate);
                    document.setCreatorCode(creatorCode);
                    document.setRemoteApplication(remoteApplication);
                    document.setSignable(true);
                }

                document.setDocumentType(attachmentDocument.getDocumentType());
                if (attachmentDocument.getGuid() != null && !"".equalsIgnoreCase(attachmentDocument.getGuid().trim())) {
                    document.setGuid(attachmentDocument.getGuid());
                } else if ((document.getGuid() == null) || attachmentDocument.getGuid() == null
                        || "".equalsIgnoreCase(attachmentDocument.getGuid().trim())) {
                    // Generate new GUID
                    document.setGuid(Util.generateGUID());
                }

                document.setCreatorName(creatorName);
                document.setLastModifiedDate(creationDate);
                document.setTitle(attachmentDocument.getTitle());
                document.setCreatorUserCode(creatorUserCode);
                document.setCreatorUserName(creatorUserName);

                document.setEformUseId(attachmentDocument.getEformUseId());
                document.setContent(attachmentDocument.getContent());
                Document parent = null;
                if (attachmentDocument.getPreviousDocumentID() != null) {
                    parent = docDao.getDocument(attachmentDocument.getPreviousDocumentID());
                }
                document.setDocument(parent);

                if ((attachmentDocument.getFiles() != null) && (attachmentDocument.getFiles().size() == 1)
                        && ((document.getDocumentFiles() == null) || (document.getDocumentFiles().size() == 0))) {
                    OutputDocumentFile file = attachmentDocument.getFiles().get(0);

                    String extension = Util.getFileExtension(file.getName());

                    // If first added file happens to be a DigiDoc container then
                    // extract files and signatures from container. Otherwise add
                    // container as a regular file.
                    if (((file.getId() == null) || (file.getId() <= 0)) &&
                    		("ddoc".equalsIgnoreCase(extension) || Util.isBdocExtension(extension))) {
                        DigiDocExtractionResult extractionResult = extractDigiDocContainer(file.getSysTempFile(), digidocConfigFile);
                        if (extractionResult.isSuccess()) {
                            file.setFileType(FILETYPE_NAME_SIGNATURE_CONTAINER);
                            document.setSigned((extractionResult.getSignatures() != null) && (extractionResult.getSignatures().size() > 0));
                            involvedSignatureContainerExtraction = true;

                            for (int i = 0; i < extractionResult.getFiles().size(); i++) {
                                DocumentFile df = extractionResult.getFiles().get(i);
                                df.setDocument(document);
                                document.getDocumentFiles().add(df);
                            }

                            for (int i = 0; i < extractionResult.getSignatures().size(); i++) {
                                ee.adit.dao.pojo.Signature sig = extractionResult.getSignatures().get(i);
                                sig.setDocument(document);
                                document.getSignatures().add(sig);
                            }

                            //If it's a signed container, then document must be locked. That prevents users to modify files inside the signed container.
                            if (document.getSigned()) {
                                document.setLocked(true);
                                document.setLockingDate(new Date());
                            }
                        }
                    }
                }

                try {
                    SaveItemInternalResult result = docDao.save(document, attachmentDocument.getFiles(), remainingDiskQuota, session);
                    result.setInvolvedSignatureContainerExtraction(involvedSignatureContainerExtraction);
                    return result;
                } catch (Exception e) {
                    throw new HibernateException(e);
                }
            }
        });
    }

    /**
     * Saves document file to database.
     *
     * @param documentId         Document ID
     * @param file               File as {@link OutputDocumentFile} object
     * @param remainingDiskQuota Remaining disk quota of current user (in bytes)
     * @param temporaryFilesDir  Absolute path to temporary files directory
     * @param digidocConfigFile  Full path to DigiDoc library configuration file.
     * @return Result of save as {@link SaveItemInternalResult} object.
     */
    public SaveItemInternalResult saveDocumentFile(
            final long documentId,
            final OutputDocumentFile file,
            final long remainingDiskQuota,
            final String temporaryFilesDir,
            final String digidocConfigFile) throws HibernateException, SQLException, AditCodedException {

        final DocumentDAO docDao = this.getDocumentDAO();

        return (SaveItemInternalResult) this.getDocumentDAO().getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException, AditCodedException {
                SaveItemInternalResult result = new SaveItemInternalResult();

                Document document = (Document) session.get(Document.class, documentId);

                // Remember highest ID of existing files.
                // This is useful later to find out which file was added.
                long maxId = -1;
                if ((document != null) && (document.getDocumentFiles() != null)) {
                    Iterator<DocumentFile> it = document.getDocumentFiles().iterator();
                    if (it != null) {
                        while (it.hasNext()) {
                            DocumentFile f = it.next();
                            if (f.getId() > maxId) {
                                maxId = f.getId();
                            }
                        }
                    }
                }
                logger.debug("Highest existing file ID: " + maxId);

                List<OutputDocumentFile> filesList = new ArrayList<OutputDocumentFile>();
                String extension = Util.getFileExtension(file.getName());
                // If first added file happens to be a DigiDoc container then
                // extract files and signatures from container. Otherwise add
                // container as a regular file.
                boolean involvedSignatureContainerExtraction = false;
                if ((maxId <= 0) &&
                		("ddoc".equalsIgnoreCase(extension)|| Util.isBdocExtension(extension))) {
                    DigiDocExtractionResult extractionResult = extractDigiDocContainer(file.getSysTempFile(), digidocConfigFile);
                    if (extractionResult.isSuccess()) {
                        file.setFileType(FILETYPE_NAME_SIGNATURE_CONTAINER);
                        document.setSigned((extractionResult.getSignatures() != null) && (extractionResult.getSignatures().size() > 0));

                        filesList.add(file);
                        involvedSignatureContainerExtraction = true;

                        for (int i = 0; i < extractionResult.getFiles().size(); i++) {
                            DocumentFile df = extractionResult.getFiles().get(i);
                            df.setDocument(document);
                            document.getDocumentFiles().add(df);
                        }

                        for (int i = 0; i < extractionResult.getSignatures().size(); i++) {
                            ee.adit.dao.pojo.Signature sig = extractionResult.getSignatures().get(i);
                            sig.setDocument(document);
                            document.getSignatures().add(sig);
                        }

                        //if it's a signed container, then document must be locked. Otherwise users can change files after signing.
                        if (document.getSigned()) {
                            document.setLocked(true);
                            document.setLockingDate(new Date());
                        }
                    } else {
                        filesList.add(file);
                    }
                } else {
                    filesList.add(file);
                }

                // Document to database
                try {

                    // update doc last modified date
                    document.setLastModifiedDate(new Date());

                    result = docDao.save(document, filesList, remainingDiskQuota, session);
                } catch (Exception e) {
                    throw new HibernateException(e);
                }

                long fileId = 0;
                if ((file.getId() != null) && (file.getId() > 0)) {
                    fileId = file.getId();
                    logger.debug("Existing file saved with ID: " + fileId);
                } else if ((document != null) && (document.getDocumentFiles() != null)) {
                    Iterator<DocumentFile> it = document.getDocumentFiles().iterator();
                    if (it != null) {
                        while (it.hasNext()) {
                            DocumentFile f = it.next();
                            if (f.getId() > maxId) {
                                fileId = f.getId();
                                logger.debug("New file saved with ID: " + fileId);
                                break;
                            }
                        }
                    }
                }
                result.setItemId(fileId);
                result.setInvolvedSignatureContainerExtraction(involvedSignatureContainerExtraction);

                return result;
            }
        });
    }

    /**
     * Makes a document copy and saves it. Used for unSahring of signed document.
     *
     * @param doc
     * @param sharing
     * @param remainingDiskQuota
     * @return
     */
    public SaveItemInternalResult saveDocumentCopy(final Document doc, final DocumentSharing sharing, final long remainingDiskQuota) {

        final DocumentDAO docDao = this.getDocumentDAO();

        return (SaveItemInternalResult) this.getDocumentDAO().getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException, AditCodedException {
                boolean involvedSignatureContainerExtraction = false;
                Date creationDate = new Date();
                Document document = new Document();

                document.setCreationDate(doc.getCreationDate());
                document.setCreatorCode(sharing.getUserCode());
                document.setRemoteApplication(doc.getRemoteApplication());
                document.setSignable(doc.getSignable());

                document.setDocumentType(doc.getDocumentType());
                document.setGuid(Util.generateGUID());

                document.setCreatorName(sharing.getUserName());
                document.setLastModifiedDate(doc.getLastModifiedDate());
                document.setTitle(doc.getTitle());
                document.setCreatorUserCode(sharing.getUserCode());
                document.setCreatorUserName(sharing.getUserName());

                //Add original document as a Parent document of the copy.
                document.setDocument(doc);


                //Copy files
                if ((doc.getDocumentFiles() != null) && (doc.getDocumentFiles().size() > 0)) {

                    Iterator<DocumentFile> it = doc.getDocumentFiles().iterator();

                    while (it.hasNext()) {
                        DocumentFile origFile = it.next();
                        DocumentFile file = new DocumentFile();

                        file.setContentType(origFile.getContentType());
                        file.setDeleted(false);
                        file.setFileName(origFile.getFileName());
                        file.setDescription(origFile.getDescription());
                        file.setFileSizeBytes(origFile.getFileSizeBytes());
                        file.setLastModifiedDate(origFile.getLastModifiedDate());

                        file.setDocumentFileTypeId(origFile.getDocumentFileTypeId());

                        file.setFileDataInDdoc(origFile.getFileDataInDdoc());
                        file.setDdocDataFileId(origFile.getDdocDataFileId());
                        file.setDdocDataFileStartOffset(origFile.getDdocDataFileStartOffset());
                        file.setDdocDataFileEndOffset(origFile.getDdocDataFileEndOffset());
                        file.setFileData(origFile.getFileData());

                        file.setDocument(document);
                        document.getDocumentFiles().add(file);

                    }

                    //Copy signatures

                    if (doc.getSigned()) {

                        Iterator<ee.adit.dao.pojo.Signature> itr = doc.getSignatures().iterator();

                        while (itr.hasNext()) {
                            ee.adit.dao.pojo.Signature origSig = itr.next();
                            ee.adit.dao.pojo.Signature sig = new ee.adit.dao.pojo.Signature();
                            sig.setDocument(document);
                            sig.setUserCode(origSig.getUserCode());
                            sig.setSignerName(origSig.getSignerName());
                            sig.setSignerRole(origSig.getSignerRole());
                            sig.setResolution(origSig.getResolution());
                            sig.setCountry(origSig.getCountry());
                            sig.setCounty(origSig.getCounty());
                            sig.setCity(origSig.getCity());
                            sig.setPostIndex(origSig.getPostIndex());
                            sig.setSignerCode(origSig.getSignerCode());
                            sig.setSigningDate(origSig.getSigningDate());
                            sig.setUserName(origSig.getUserName());

                            document.getSignatures().add(sig);
                        }

                        document.setLocked(true);
                        document.setLockingDate(doc.getLockingDate());
                    }
                }

                try {
                    SaveItemInternalResult result = docDao.save(document, null, remainingDiskQuota, session);
                    result.setSuccess(true);
                    return result;
                } catch (Exception e) {
                    throw new HibernateException(e);
                }
            }
        });
    }

    /**
     * Extracts data files and signature data from DigiDoc container.
     *
     * @param pathToContainer   Absolute path of DigiDoc container
     * @param digidocConfigFile Absolute path of DigiDoc library configuration file
     * @return File and signature meta-data wrapped into {@link DigiDocExtractionResult} object.
     */
    public DigiDocExtractionResult extractDigiDocContainer(final String pathToContainer, final String digidocConfigFile)
            throws AditCodedException {

        DigiDocExtractionResult result = new DigiDocExtractionResult();
        final String tempDir = this.getConfiguration().getTempDir();
        
        if (!Util.isNullOrEmpty(pathToContainer)) {
            File digiDocContainer = new File(pathToContainer);
            if (digiDocContainer.exists()) {
                try {
                    logger.debug("pathToContainer: " + pathToContainer);
                    
                    Container container = ContainerBuilder.aContainer().fromExistingFile(pathToContainer).build();
                    Boolean isBdoc = false;
                    if (container.getType().equals(ContainerBuilder.BDOC_CONTAINER_TYPE)) {
                    	isBdoc = true;
                    }
                    
                    int dataFilesCount = Util.countElements(container.getDataFiles());
                    if (dataFilesCount > 0) {
                        Hashtable<String, StartEndOffsetPair> dataFileOffsets = SimplifiedDigiDocParser.findDigiDocDataFileOffsets(pathToContainer, isBdoc, tempDir);
                        
                        for (DataFile dataFile : container.getDataFiles()) {
                            DocumentFile localFile = new DocumentFile();
                            localFile.setDeleted(false);
                            localFile.setContentType(dataFile.getMediaType());
                            localFile.setFileName(dataFile.getName());
                            localFile.setFileSizeBytes(dataFile.getFileSize());
                            localFile.setLastModifiedDate(new Date());

                           // localFile.setFileData(Hibernate.createBlob(ddocDataFile.getBody()));
                            StartEndOffsetPair currentFileOffsets = dataFileOffsets.get(dataFile.getId());
                            if (currentFileOffsets != null) {
                                localFile.setFileDataInDdoc(true);
                                localFile.setDdocDataFileId(dataFile.getId());
                                localFile.setDdocDataFileStartOffset(currentFileOffsets.getStart());
                                localFile.setDdocDataFileEndOffset(currentFileOffsets.getEnd());
                                localFile.setFileData(currentFileOffsets.getDataMd5Hash());
                            } else {
                                logger.error("Failed to find DataFile offsets for data file " + dataFile.getId());
                                
                                AditCodedException aditCodedException = new AditCodedException("digidoc.extract.genericException");
                                aditCodedException.setParameters(new Object[]{});
                                
                                throw aditCodedException;
                            }

                            result.getFiles().add(localFile);
                        }
                    }

                    int signaturesCount = Util.countElements(container.getSignatures());
                    logger.info("Extracted file contains " + signaturesCount + " signatures.");
                    if (signaturesCount > 0) {
                        for (Signature signature : container.getSignatures()) {
                            // Convert DigiDoc signature to local signature
                            ee.adit.dao.pojo.Signature localSignature = convertDigiDocSignatureToLocalSignature(signature);
                            logger.info("Extracted signature of " + localSignature.getSignerName());

                            // Validate the signature
                            SignatureValidationResult signatureValidationResult = signature.validateSignature();
                            
                            if (signatureValidationResult.isValid()) {
                                result.getSignatures().add(localSignature);
                            } else {
                                logger.error("Signature given by " + localSignature.getSignerName() + " was found to be invalid.");
                                
                                logDigidocVerificationErrors(signatureValidationResult.getErrors());
                                AditCodedException aditCodedException = new AditCodedException("digidoc.extract.invalidSignature");
                                aditCodedException.setParameters(new Object[]{localSignature.getSignerName()});
                                
                                throw aditCodedException;
                            }
                        }
                    }
                    
                    result.setSuccess(true);
                    
                } catch (AditCodedException ex) {
                    throw ex;
                } catch (DigiDoc4JException ex) {
                    logger.error(ex, ex);
                    
                    AditCodedException aditCodedException = new AditCodedException("digidoc.extract.incorrectContainer", ex);
                    aditCodedException.setParameters(new Object[]{});
                    
                    throw aditCodedException;

                } catch (Exception ex) {
                    logger.error(ex);
                    
                    AditCodedException aditCodedException = new AditCodedException("digidoc.extract.genericException", ex);
                    aditCodedException.setParameters(new Object[]{});
                    
                    throw aditCodedException;
                }
            } else {
                logger.error("DigiDoc container extraction failed because container file \"" + pathToContainer + "\" does not exist!");
                
                AditCodedException aditCodedException = new AditCodedException("digidoc.extract.genericException");
                aditCodedException.setParameters(new Object[]{});
                
                throw aditCodedException;
            }
        } else {
            logger.error("DigiDoc container extraction failed because container file was not supplied!");
            
            AditCodedException aditCodedException = new AditCodedException("digidoc.extract.genericException");
            aditCodedException.setParameters(new Object[]{});
            
            throw aditCodedException;
        }

        return result;
    }

    /**
     * Logs DigiDoc verification errors to application's error log.
     *
     * @param verificationErrors a list containing verification errors of a DigiDoc file.
     */
    private void logDigidocVerificationErrors(List<DigiDoc4JException> verificationErrors) {
        if (verificationErrors != null) {
            for (int i = 0; i < verificationErrors.size(); i++) {
                try {
                    DigiDoc4JException ex = verificationErrors.get(i);
                    logger.error("Signature validation error " + i, ex);
                } catch (Exception ex) {
                    // Errors thrown by error logging are intentionally discarded
                }
            }
        }
    }

    /**
     * Saves document considering the disk quota for the user.
     *
     * @param doc                document
     * @param remainingDiskQuota remaining disk quota for user
     * @throws Exception
     */
    public void save(Document doc, long remainingDiskQuota) throws Exception {
        SaveItemInternalResult saveResult = this.getDocumentDAO().save(doc, null, remainingDiskQuota, null);

        if (doc.getCreatorCode() != null) {
            AditUser user = this.getAditUserDAO().getUserByID(doc.getCreatorCode());

            if (user != null) {
                Long usedDiskQuota = user.getDiskQuotaUsed();
                if (usedDiskQuota == null) {
                    usedDiskQuota = 0L;
                }
                user.setDiskQuotaUsed(usedDiskQuota + saveResult.getAddedFilesSize());
            }
        }
    }

    /**
     * Locks the document.
     *
     * @param document the document to be locked.
     * @throws Exception
     */
    public void lockDocument(Document document) throws Exception {
        if (document.getLocked() == null) {
            document.setLocked(new Boolean(false));
        }

        if (!document.getLocked()) {
            logger.debug("Locking document: " + document.getId());
            document.setLocked(true);
            document.setLockingDate(new Date());
            save(document, Long.MAX_VALUE);
            logger.info("Document locked: " + document.getId());
        }
    }

    /**
     * Sends document to the specified user.
     *
     * @param document  document
     * @param recipient user
     * @param dvkFolder DVK folder
     * @param dvkId dvkId
     * @param messageForRecipient message sent by the recipient
     * @return true, if sending succeeded
     */
    public boolean sendDocumentByEmail(Document document, String recipientEmail) {
        boolean result = false;

        DocumentSharing documentSharing = new DocumentSharing();
        documentSharing.setDocumentId(document.getId());
        documentSharing.setCreationDate(new Date());
        documentSharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SEND_EMAIL);

        documentSharing.setUserEmail(recipientEmail);

        this.getDocumentSharingDAO().save(documentSharing);

        if (documentSharing.getId() == 0) {
            throw new AditInternalException("Could not add document sharing information to database.");
        }

        return result;
    }

    /**
     * Sends document to the specified user.
     *
     * @param document
     *            document
     * @param recipient
     *            user
     * @param dvkFolder
     *            DVK folder
     * @return true, if sending succeeded
     */

    public boolean sendDocument(Document document, AditUser recipient, String dvkFolder, Long dvkId, String messageForRecipient) {
        DocumentSharing documentSharing = new DocumentSharing();
        documentSharing.setDocumentId(document.getId());
        documentSharing.setCreationDate(new Date());

        if (dvkFolder != null) {
            documentSharing.setDvkFolder(dvkFolder);
        }

        documentSharing.setDvkId(dvkId);

        if (recipient.getDvkOrgCode() != null && !"".equalsIgnoreCase(recipient.getDvkOrgCode().trim())) {
            documentSharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SEND_DVK);
        } else {
            documentSharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SEND_ADIT);
        }

        documentSharing.setUserCode(recipient.getUserCode());
        documentSharing.setUserName(recipient.getFullName());
        documentSharing.setComment(messageForRecipient);

        this.getDocumentSharingDAO().save(documentSharing);

        if (documentSharing.getId() == 0) {
            throw new AditInternalException("Could not add document sharing information to database.");
        }

        return true;
    }
    
    public void sendDocumentAndNotifyRecipient(Document document, AditUser sender, AditUser recipient, String dvkFolder, Long dvkId, String messageForRecipient) {
    	if (this.sendDocument(document, recipient, dvkFolder, dvkId, messageForRecipient)) {
    		this.notificationService.sendNotification(document, sender, recipient, ScheduleClient.NOTIFICATION_TYPE_SEND);
    	}
    }

    /**
     * Sends document to the specified user.
     *
     * @param document  document
     * @param recipient user
     * @return true, if sending succeeded
     */
    public boolean sendDocument(Document document, AditUser recipient) {
        return sendDocument(document, recipient, null, null, null);
    }

    /**
     * Adds a document history event.
     *
     * @param applicationName remote application short name
     * @param documentId      related document ID
     * @param userCode        user code - the user that caused this event
     * @param historyType     history event type name
     * @param xteeUserCode    X-Tee user code
     * @param xteeUserName    X-Tee user name
     * @param description     event description
     * @param userName        user name - the user that caused this event
     */
    public void addHistoryEvent(String applicationName, long documentId, String userCode, String historyType,
                                String xteeUserCode, String xteeUserName, String description, String userName, Date eventDate) {
        // Add history event
    	if (applicationName == null || applicationName.isEmpty()) {
    		applicationName = "Riigiportaal";
    	}
        DocumentHistory documentHistory = new DocumentHistory();
        documentHistory.setRemoteApplicationName(applicationName);
        documentHistory.setDocumentId(documentId);
        documentHistory.setDocumentHistoryType(historyType);
        documentHistory.setEventDate(eventDate);
        documentHistory.setUserCode(userCode);
        documentHistory.setUserName(userName);
        documentHistory.setXteeUserCode(xteeUserCode);
        documentHistory.setXteeUserName(xteeUserName);
        documentHistory.setDescription(description);
        this.getDocumentHistoryDAO().save(documentHistory);
    }

    /**
     * Fetches all the documents that are to be sent to DVK. The DVK documents
     * are recognized by the following: <br />
     * 1. The document has at least one DocumentSharing associated with it <br />
     * 2. That DocumentSharing must have the "documentSharingType" equal to
     * "send_dvk" <br />
     * 3. That DocumentSharing must have the "documentDvkStatus" not initialized
     * or set to "100"
     *
     * @return number of documents sent to DVK
     */
    public int sendDocumentsToDVKWithContainerVersion2() {
        int result = 0;
        Session session = null;

        try {
            final String sqlQuery = "select doc from Document doc, DocumentSharing docSharing where docSharing.documentSharingType = 'send_dvk' and (docSharing.documentDvkStatus is null or docSharing.documentDvkStatus = "
                    + DocumentService.DVK_STATUS_MISSING + ") and docSharing.documentId = doc.id";

            final String tempDir = this.getConfiguration().getTempDir();

            logger.info("Starting to send documents to DVK. Using org.code '" + this.getConfiguration().getDvkOrgCode() + "'");

            logger.debug("Fetching documents for sending to DVK...");
            session = this.getDocumentDAO().getSessionFactory().openSession();

            Query query = session.createQuery(sqlQuery);
            List<Document> documents = query.list();
            //Set<Document> documents = new HashSet<Document>(query.list());

            logger.info(documents.size() + " documents need to be sent to DVK.");

            Iterator<Document> i = documents.iterator();

            while (i.hasNext()) {

                try {

                    Document document = i.next();

                    ContainerVer2 dvkContainer = new ContainerVer2();
                    dvkContainer.setVersion(DVK_CONTAINER_VERSION);

                    Metainfo metainfo = new Metainfo();

                    MetaManual metaManual = new MetaManual();
                    metaManual.setAutoriIsikukood(null);
                    metaManual.setAutoriKontakt(null);
                    metaManual.setAutoriNimi(null);
                    metaManual.setAutoriOsakond(null);
                    metaManual.setDokumentGuid(document.getGuid());
                    metaManual.setDokumentKeel(null);
                    metaManual.setDokumentLiik(document.getDocumentType());
                    metaManual.setDokumentPealkiri(document.getTitle());
                    metaManual.setDokumentViit(null);
                    metaManual.setIpr(null);
                    metaManual.setJuurdepaasPiirang(null);

                    metainfo.setMetaManual(metaManual);
                    dvkContainer.setMetainfo(metainfo);

                    // Transport information
                    Transport transport = new Transport();
                    List<Saaja> saajad = new ArrayList<Saaja>();
                    List<Saatja> saatjad = new ArrayList<Saatja>();
                    Saatja saatja = new Saatja();
                    saatja.setRegNr(Util.removeCountryPrefix(getConfiguration().getDvkOrgCode()));
                    saatja.setIsikukood(Util.removeCountryPrefix(document.getCreatorCode()));
                    saatjad.add(saatja);
                    transport.setSaatjad(saatjad);

                    //dvkFolder value taken from documentSharing
                    String dvkFolder = null;

                    Iterator<DocumentSharing> documentSharings = document.getDocumentSharings().iterator();

                    Saaja firstRecipient = null;
                    while (documentSharings.hasNext()) {
                        DocumentSharing documentSharing = documentSharings.next();

                        if (DocumentService.SHARINGTYPE_SEND_DVK.equalsIgnoreCase(documentSharing.getDocumentSharingType())
                                && (DocumentService.DVK_STATUS_WAITING.equals(documentSharing.getDocumentDvkStatus())
                                || DocumentService.DVK_STATUS_MISSING.equals(documentSharing.getDocumentDvkStatus()) || documentSharing
                                .getDocumentDvkStatus() == null)) {

                            AditUser recipient = this.getAditUserDAO().getUserByID(documentSharing.getUserCode());

                            Saaja saaja = new Saaja();
                            saaja.setRegNr(recipient.getDvkOrgCode());

                            if (UserService.USERTYPE_PERSON.equalsIgnoreCase(recipient.getUsertype().getShortName())) {
                                saaja.setNimi(recipient.getFullName());
                                saaja.setIsikukood(recipient.getUserCode());
                            } else {
                                saaja.setAsutuseNimi(recipient.getFullName());
                            }

                            if (saajad.isEmpty()) {
                                firstRecipient = saaja;
                            }

                            saajad.add(saaja);

                            dvkFolder = documentSharing.getDvkFolder();
                        }

                    }

                    transport.setSaajad(saajad);
                    dvkContainer.setTransport(transport);

                    FailideKonteiner failideKonteiner = new FailideKonteiner();

                    Set<DocumentFile> aditFiles = document.getDocumentFiles();
                    List<Fail> dvkFiles = new ArrayList<Fail>();

                    Iterator<DocumentFile> aditFilesIterator = aditFiles.iterator();
                    short count = 0;

                    // Convert the adit file to dvk file
                    while (aditFilesIterator.hasNext()) {
                        DocumentFile f = aditFilesIterator.next();

                        if (((document.getSigned() != null) && document.getSigned() && (f.getDocumentFileTypeId() == FILETYPE_SIGNATURE_CONTAINER))
                                || (((document.getSigned() == null) || !document.getSigned()) && (f.getDocumentFileTypeId() == FILETYPE_DOCUMENT_FILE))) {
                            Fail dvkFile = new Fail();

                            logger.debug("FileName: " + f.getFileName());

                            // Create a temporary file from the ADIT file and
                            // add a reference to the DVK file
                            try {
                                InputStream inputStream = new ByteArrayInputStream(f.getFileData());
                                String temporaryFile = Util.createTemporaryFile(inputStream, tempDir);
                                dvkFile.setFile(new File(temporaryFile));
                                dvkFiles.add(dvkFile);

                            } catch (Exception e) {
                                throw new HibernateException("Unable to create temporary file: ", e);
                            }

                            dvkFile.setFailNimi(f.getFileName());
                            dvkFile.setFailPealkiri(null);
                            dvkFile.setFailSuurus(f.getFileSizeBytes());
                            dvkFile.setFailTyyp(f.getContentType());
                            dvkFile.setJrkNr(count++);
                        }
                    }

                    failideKonteiner.setKokku(count);
                    failideKonteiner.setFailid(dvkFiles);
                    dvkContainer.setFailideKonteiner(failideKonteiner);

                    // Save document to DVK Client database

                    SessionFactory sessionFactory = this.getDvkDAO().getSessionFactory();
                    Long dvkMessageID = null;
                    Session dvkSession = sessionFactory.openSession();
                    dvkSession.setFlushMode(FlushMode.COMMIT);
                    Transaction dvkTransaction = dvkSession.beginTransaction();

                    logger.info("DVK session flush mode: " + dvkSession.getFlushMode().toString());

                    try {
                        PojoMessage dvkMessage = new PojoMessage();
                        dvkMessage.setIsIncoming(false);
                        if (dvkFolder == null) {
                            if (document.getDocumentType().equals("letter") && this.getConfiguration().getDvkFolderForLetterType() != null && !this.getConfiguration().getDvkFolderForLetterType().isEmpty()) {
//                        		dvkMessage.setDhlFolderName(this.getConfiguration().getDvkSendFolder());
                                dvkFolder = this.getConfiguration().getDvkFolderForLetterType();
                            }
                            if (document.getDocumentType().equals("application") && this.getConfiguration().getDvkFolderForApplicationType() != null && !this.getConfiguration().getDvkFolderForApplicationType().isEmpty()) {
                                dvkFolder = this.getConfiguration().getDvkFolderForApplicationType();
                            }
                            if (dvkFolder == null) {
                                dvkFolder = this.getConfiguration().getDvkSendFolder();
                            }
                        }
//                        else {
//                        	dvkMessage.setDhlFolderName(dvkFolder);
//                        	logger.info("DVK folder name : " + dvkFolder);
//                        }
                        dvkMessage.setDhlFolderName(dvkFolder);
                        dvkMessage.setLocalItemId(document.getId());
                        dvkMessage.setTitle(document.getTitle());
                        dvkMessage.setDhlGuid(document.getGuid());
                        dvkMessage.setSendingStatusId(DocumentService.DVK_STATUS_WAITING);

                        // Get sender org code
                        String documentOwnerCode = document.getCreatorCode();
                        AditUser documentOwner = (AditUser) session.get(AditUser.class, documentOwnerCode);
                        dvkMessage.setSenderOrgCode(documentOwner.getDvkOrgCode());
                        dvkMessage.setSenderPersonCode(documentOwner.getUserCode());
                        dvkMessage.setSenderName(documentOwner.getFullName());

                        // Add first recipient data
                        if (firstRecipient != null) {
                            dvkMessage.setRecipientOrgCode(firstRecipient.getRegNr());
                            dvkMessage.setRecipientPersonCode(firstRecipient.getIsikukood());
                            if (Util.isNullOrEmpty(firstRecipient.getIsikukood())) {
                                dvkMessage.setRecipientOrgName(firstRecipient.getNimi());
                            } else {
                                dvkMessage.setRecipientName(firstRecipient.getNimi());
                            }
                        }

                        // Insert data as stream
                        dvkMessage.setData(" ");

                        logger.debug("Saving document to DVK database");
                        dvkMessageID = (Long) dvkSession.save(dvkMessage);

                        if (dvkMessageID == null || dvkMessageID.longValue() == 0) {
                            logger.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
                            throw new DataRetrievalFailureException(
                                    "Error while saving outgoing message to DVK database - no ID returned by save method.");
                        } else {
                            logger.info("Outgoing message saved to DVK database. ID: " + dvkMessageID);
                        }

                        logger.debug("DVK Message saved to client database. GUID: " + dvkMessage.getDhlGuid());
                        dvkTransaction.commit();
                    } catch (Exception e) {
                        dvkTransaction.rollback();
                        throw new DataRetrievalFailureException("Error while adding message to DVK Client database: ", e);
                    } finally {
                        if (dvkSession != null) {
                            dvkSession.close();
                        }
                    }

                    // Update CLOB
                    Session dvkSession2 = sessionFactory.openSession();
                    dvkSession2.setFlushMode(FlushMode.COMMIT);
                    Transaction dvkTransaction2 = dvkSession2.beginTransaction();

                    try {
                        // Select the record for update
                        PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2.load(PojoMessage.class, dvkMessageID, LockOptions.UPGRADE);

                        // Write the DVK Container to temporary file
                        String temporaryFile = this.getConfiguration().getTempDir() + File.separator
                                + Util.generateRandomFileName();
                        dvkContainer.save2File(temporaryFile);

                        // Write the temporary file to the database
                        InputStream is = new FileInputStream(temporaryFile);
                        Writer dataWriter = new StringWriter();

                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = is.read(buf)) > 0) {
                        	dataWriter.write(new String(buf, 0, len, "UTF-8"));
                        }
                        is.close();
                        dataWriter.close();
                        
                        dvkMessageToUpdate.setData(dataWriter.toString());

                        // Commit to DVK database
                        dvkTransaction2.commit();

                        // Save the document DVK_ID to ADIT database
                        document.setDvkId(dvkMessageID);
                        session.saveOrUpdate(document);

                        logger.debug("Starting to update document sharings");
                        Transaction aditTransaction = session.beginTransaction();
                        // Update document sharings status
                        Iterator<DocumentSharing> documentSharingUpdateIterator = document.getDocumentSharings().iterator();
                        while (documentSharingUpdateIterator.hasNext()) {
                            DocumentSharing documentSharing = documentSharingUpdateIterator.next();
                            if (DocumentService.SHARINGTYPE_SEND_DVK.equalsIgnoreCase(documentSharing
                                    .getDocumentSharingType())) {
                                documentSharing.setDocumentDvkStatus(DVK_STATUS_SENDING);
                                documentSharing.setDvkId(dvkMessageToUpdate.getDhlId());
                                // documentSharing.setDvkSendDate(new Date());
                                session.saveOrUpdate(documentSharing);
                                logger.debug("DocumentSharing status updated to: '" + DVK_STATUS_SENDING + "'.");
                            }
                        }

                        aditTransaction.commit();

                        result++;

                    } catch (Exception e) {
                        dvkTransaction2.rollback();

                        // Remove the document with empty clob from the database
                        Session dvkSession3 = sessionFactory.openSession();
                        dvkSession3.setFlushMode(FlushMode.COMMIT);
                        Transaction dvkTransaction3 = dvkSession3.beginTransaction();
                        try {
                            logger.debug("Starting to delete document from DVK Client database: " + dvkMessageID);
                            PojoMessage dvkMessageToDelete = (PojoMessage) dvkSession3
                                    .load(PojoMessage.class, dvkMessageID);
                            if (dvkMessageToDelete == null) {
                                logger.warn("DVK message to delete is not initialized.");
                            }
                            dvkSession3.delete(dvkMessageToDelete);
                            dvkTransaction3.commit();
                            logger.info("Empty DVK document deleted from DVK Client database. ID: " + dvkMessageID);
                        } catch (Exception dvkException) {
                            dvkTransaction3.rollback();
                            logger.error("Error deleting document from DVK database: ", dvkException);
                        } finally {
                            if (dvkSession3 != null) {
                                dvkSession3.close();
                            }
                        }

                        throw new DataRetrievalFailureException(
                                "Error while adding message to DVK Client database (CLOB update): ", e);
                    } finally {
                        if (dvkSession2 != null) {
                            dvkSession2.close();
                        }
                    }
                } catch (Exception e) {
                    throw new AditInternalException("Error while sending documents to DVK Client database: ", e);
                }
            }
        } finally {
            if ((session != null) && session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Fetches all the documents that are to be sent to DVK. The DVK documents
     * are recognized by the following: <br />
     * 1. The document has at least one DocumentSharing associated with it <br />
     * 2. That DocumentSharing must have the "documentSharingType" equal to
     * "send_dvk" <br />
     * 3. That DocumentSharing must have the "documentDvkStatus" not initialized
     * or set to "100"
     *
     * @return number of documents sent to DVK
     */
    public int sendDocumentsToDVK() {
        int result = 0;
        Session session = null;

        try {
            final String sqlQuery = "select distinct doc from Document doc, DocumentSharing docSharing where docSharing.documentSharingType = 'send_dvk' and (docSharing.documentDvkStatus is null or docSharing.documentDvkStatus = "
                    + DocumentService.DVK_STATUS_MISSING + ") and docSharing.documentId = doc.id";
            logger.info("Starting to send documents to DVK. Using org.code '" + this.getConfiguration().getDvkOrgCode() + "'");
            logger.debug("Fetching documents for sending to DVK...");
            session = this.getDocumentDAO().getSessionFactory().openSession();

            Query query = session.createQuery(sqlQuery);
            @SuppressWarnings("unchecked")
            List<Document> documents = query.list();

            logger.info(documents.size() + " documents need to be sent to DVK.");

            for (Document document: documents) {
            	logger.info("Sending document with ID " + document.getId() + " to DVK");
                try {
                    DvkSender dvkSender = new ContainerVer2_1Sender(this);

                    Long dvkMessageID = dvkSender.send(document);

                    // Save the document DVK_ID to ADIT database
                    document.setDvkId(dvkMessageID);
                    session.saveOrUpdate(document);

                    logger.debug("Starting to update document sharings");
                    Transaction aditTransaction = session.beginTransaction();
                    // Update document sharings status
                    for (DocumentSharing documentSharing: document.getDocumentSharings()) {
                        if (DocumentService.SHARINGTYPE_SEND_DVK.equalsIgnoreCase(documentSharing
                                .getDocumentSharingType())) {
                            documentSharing.setDocumentDvkStatus(DVK_STATUS_SENDING);
                            session.saveOrUpdate(documentSharing);
                            logger.debug("DocumentSharing status updated to: '" + DVK_STATUS_SENDING + "'.");
                        }
                    }
                    aditTransaction.commit();
                    result++;
                } catch (Exception e) {
                	logger.error("Error while sending document with ID " + document.getId() + " to DVK Client database: ", e);
                }
            }
        } finally {
            if ((session != null) && session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    public void addUserNameToDocumentSharings(AditUser user) throws AditInternalException {
        List<DocumentSharing> sharings = documentSharingDAO.getSharingsByUserCode(user.getUserCode());
        Iterator<DocumentSharing> itr = sharings.iterator();
        while (itr.hasNext()) {
            DocumentSharing sharing = itr.next();
            logger.debug("Adding user " + user.getUserCode() + " name " + user.getFullName() + "to document sharing of document " + sharing.getDocumentId());
            sharing.setUserName(user.getFullName());
            documentSharingDAO.update(sharing);
        }
    }

    /**
     * Creates metaxml part of outgoing DVK envelope.
     *
     * @param doc           Document to be sent over DVK
     * @param recipients    List of document recipients
     * @param documentOwner Document owner as {@link AditUser} object
     * @return Generated metaxml block
     */
    private Metaxml createDvkMetaxml(Document doc, List<dvk.api.container.v1.Saaja> recipients,
                                     AditUser documentOwner) {
        Metaxml result = new Metaxml();

        if (doc != null) {
            DocumentType docType = documentTypeDAO.getDocumentType(doc.getDocumentType());
            Calendar cal = Calendar.getInstance();
            Date documentSignatureDate = null;

            if ((doc.getSignatures() != null) && (doc.getSignatures().size() > 0)) {
                result.setSignatures(new ArrayOfSignature());
                result.getSignatures().setSignature(new ArrayList<dvk.api.container.Signature>());

                Iterator<ee.adit.dao.pojo.Signature> i = doc.getSignatures().iterator();
                while (i.hasNext()) {
                    ee.adit.dao.pojo.Signature aditSignature = i.next();
                    dvk.api.container.Signature dvkSignature = new dvk.api.container.Signature();

                    dvkSignature.setSignatureInfo(new dvk.api.container.SignatureInfo());
                    dvkSignature.getSignatureInfo().setSignatureDate(aditSignature.getSigningDate());
                    dvkSignature.getSignatureInfo().setSignatureTime(aditSignature.getSigningDate());

                    dvkSignature.setPerson(new dvk.api.container.Person());
                    PersonName signersName = Util.splitPersonName(aditSignature.getSignerName());
                    dvkSignature.getPerson().setFirstname(signersName.getFirstName());
                    dvkSignature.getPerson().setSurname(signersName.getSurname());
                    dvkSignature.getPerson().setJobtitle(aditSignature.getSignerRole());

                    result.getSignatures().getSignature().add(dvkSignature);

                    if (documentSignatureDate == null) {
                        documentSignatureDate = aditSignature.getSigningDate();
                    } else {
                        cal.setTime(documentSignatureDate);
                        if (cal.after(aditSignature.getSigningDate())) {
                            documentSignatureDate = aditSignature.getSigningDate();
                        }
                    }
                }
            }

            if ((recipients != null) && (recipients.size() > 0)) {
                result.setAddressees(new ArrayList<dvk.api.container.AddresseeInfo>());
                for (dvk.api.container.v1.Saaja recipient : recipients) {
                    dvk.api.container.AddresseeInfo addressee = new dvk.api.container.AddresseeInfo();

                    if (!Util.isNullOrEmpty(recipient.getRegNr())) {
                        addressee.setOrganisation(new dvk.api.container.Organisation());
                        addressee.getOrganisation().setOrganisationName(recipient.getAsutuseNimi());
                        addressee.getOrganisation().setDepartmentName(recipient.getAllyksuseNimetus());
                    }
                    if (!Util.isNullOrEmpty(recipient.getIsikukood())) {
                        addressee.setPerson(new dvk.api.container.v1.Addressee());
                        PersonName addresseeName = Util.splitPersonName(recipient.getNimi());
                        addressee.getPerson().setFirstname(addresseeName.getFirstName());
                        addressee.getPerson().setSurname(addresseeName.getSurname());
                    }

                    result.getAddressees().add(addressee);
                }
            } else {
                logger.warn("Could not fill \"Addressees\" part of \"metaxml\" block in outgoing DVK message. Supplied message recipients list was empty.");
            }

            result.setLetterMetaData(new LetterMetaData());
            result.getLetterMetaData().setSignDate(documentSignatureDate);
            result.getLetterMetaData().setTitle(doc.getTitle());

            if (docType != null) {
                result.getLetterMetaData().setType(docType.getDescription());
            }

            if (documentOwner != null) {
                result.setAuthorInfo(new dvk.api.container.AuthorInfo());
                result.setCompilators(new ArrayList<dvk.api.container.Compilator>());

                if (UserService.USERTYPE_PERSON.equalsIgnoreCase(documentOwner.getUsertype().getShortName())) {
                    PersonName ownersName = Util.splitPersonName(documentOwner.getFullName());
                    result.getAuthorInfo().setPerson(new Person());
                    result.getAuthorInfo().getPerson().setFirstname(ownersName.getFirstName());
                    result.getAuthorInfo().getPerson().setSurname(ownersName.getSurname());
                    result.getCompilators().add(new dvk.api.container.Compilator());
                    result.getCompilators().get(0).setFirstname(ownersName.getFirstName());
                    result.getCompilators().get(0).setSurname(ownersName.getSurname());
                } else {
                    result.getAuthorInfo().setOrganisation(new dvk.api.container.Organisation());
                    result.getAuthorInfo().getOrganisation().setOrganisationName(documentOwner.getFullName());

                    if (!Util.isNullOrEmpty(doc.getCreatorUserName())) {
                        PersonName creatorsName = Util.splitPersonName(doc.getCreatorUserName());
                        result.getCompilators().add(new dvk.api.container.Compilator());
                        result.getCompilators().get(0).setFirstname(creatorsName.getFirstName());
                        result.getCompilators().get(0).setSurname(creatorsName.getSurname());
                    }
                }
            }
        } else {
            logger.warn("Could not create \"metaxml\" block in outgoing DVK message. Supplied ADIT document was NULL.");
        }

        return result;
    }

    /**
     * Creates metainfo part of outgoing DVK v1 envelope.
     *
     * @param doc           Document to be sent over DVK
     * @param recipients    List of document recipients
     * @param documentOwner Document owner as {@link AditUser} object
     * @return Generated metainfo block
     */
    private dvk.api.container.v1.Metainfo createDvkV1Metainfo(Document doc,
                                                              List<dvk.api.container.v1.Saaja> recipients, AditUser documentOwner) {

        dvk.api.container.v1.Metainfo result = new dvk.api.container.v1.Metainfo();

        result.setKoostajaDokumendinimi(doc.getTitle());
        result.setKoostajaDokumendinr(String.valueOf(doc.getId()));
        result.setSaatjaDokumendinr(String.valueOf(doc.getId()));
        result.setKoostajaKuupaev(Util.dateToXMLDate(doc.getCreationDate()));
        result.setSaatjaKuupaev(doc.getCreationDate());

        DocumentType docType = documentTypeDAO.getDocumentType(doc.getDocumentType());
        if (docType != null) {
            result.setKoostajaDokumendityyp(docType.getDescription());
        }

        if (!UserService.USERTYPE_PERSON.equalsIgnoreCase(documentOwner.getUsertype().getShortName())) {
            result.setKoostajaAsutuseNr(Util.getPersonalIdCodeWithoutCountryPrefix(documentOwner.getUserCode()));
        } else {
            result.setAutoriIsikukood(Util.getPersonalIdCodeWithoutCountryPrefix(documentOwner.getUserCode()));
            result.setAutoriNimi(documentOwner.getFullName());
        }

        if ((recipients != null) && (recipients.size() > 0)) {
            dvk.api.container.v1.Saaja firstRecipient = recipients.get(0);
            result.setSaajaAsutuseNr(firstRecipient.getRegNr());
            result.setSaajaIsikukood(firstRecipient.getIsikukood());
            result.setSaajaNimi(Util.isNullOrEmpty(firstRecipient.getNimi()) ? firstRecipient.getAsutuseNimi() : firstRecipient.getNimi());
        }

        return result;
    }

    /**
     * Transfers incoming documents from DVK Client database to ADIT database.
     * Supports receiving container 1.0 and 2.1.
     * <p/>
     * Note: DVK stores the recipient status in the DHL_MESSAGE table (field
     * RECIPIENT_STATUS_ID). The situation where two recipients from the same
     * institution receive the same document, is not allowed (or at least DVK
     * stores only one status ID for this document).
     *
     * @param digidocConfigFile Full path to DigiDoc library configuration file.
     * @return the number of documents received
     */
    @Transactional
    public int receiveDocumentsFromDVK(String digidocConfigFile) {
        int result = 0;

        try {
            // Fetch all incoming documents from DVK Client database which
            // don't have local item ID set.
            logger.debug("Fetching documents from DVK Client database.");
            List<PojoMessage> dvkDocuments = this.getDvkDAO().getIncomingDocuments(this.getConfiguration().getDvkRecieveFolders());

            if (dvkDocuments != null && dvkDocuments.size() > 0) {
                logger.debug("Found " + dvkDocuments.size());

                Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments.iterator();
                while (dvkDocumentsIterator.hasNext()) {
                    PojoMessage dvkDocument = dvkDocumentsIterator.next();

                    DvkReceiverFactory receiverFactory = new DvkReceiverFactory(this, digidocConfigFile);
                    DvkReceiver receiver = receiverFactory.getReceiver(dvkDocument);

                    if (receiver.receive(dvkDocument)) {
                        result++;
                    }
                }
            } else {
                logger.info("No incoming messages found in DVK Client database.");
            }
        } catch (AditCodedException ex) {
            throw ex;
        } catch (Exception e) {
            throw new AditInternalException("Error while receiving documents from DVK Client database: ", e);
        }

        return result;
    }

    /**
     * Converts a single incoming DVK document to ADIT document.
     *
     * @param dvkDocument       Received DVK document
     * @param digidocConfigFile Full path to DigiDoc configuration file
     * @return {@code true} if receiving DVK document succeeded
     */
    public Boolean receiveSingleDocumentFromDVK(PojoMessage dvkDocument, String digidocConfigFile) {
        Boolean success = true;

        try {
            logger.debug("Starting to process incoming DVK message with DVK ID "
                    + dvkDocument.getDhlId() + " and DVK GUID " + dvkDocument.getDhlGuid());

            ContainerVer1 dvkContainer = this.getDVKContainerV1(dvkDocument);

            // Make sure that document sender exists as a user in ADIT
            AditUser senderUser = findAditUserByDvkSenderData(dvkContainer, dvkDocument.getDhlId(), dvkDocument.getDhlGuid());
            if (senderUser == null) {
                return false;
            }

            // Make sure that exactly the same document has not been received before.
            // This does not mean that future versions of the same document are blocked
            Boolean documentAlreadyReceived = this.getDocumentDAO().checkIfDocumentExists(dvkDocument.getDhlId(), senderUser.getDvkOrgCode());
            if (documentAlreadyReceived) {
                String errorMsg = "Unable to receive document from DVK because"
                        + " this document has been received before. Message DVK ID: "
                        + dvkDocument.getDhlId() + ". Message DVK GUID: "
                        + dvkDocument.getDhlGuid();
                logger.warn(errorMsg);
                return false;
            }

            // Get list of recipients and make sure that at least one recipient exists
            List<dvk.api.container.v1.Saaja> recipients = dvkContainer.getTransport().getSaajad();
            if ((recipients == null) || (recipients.size() < 1)) {
                String errorMsg = "Unable to receive document from DVK because DVK envelope"
                        + " does not contain recipient data. Message DVK ID: " + dvkDocument.getDhlId()
                        + ". Message DVK GUID: " + dvkDocument.getDhlGuid();
                logger.error(errorMsg);
                return false;
            }

            logger.debug("Recipients for this message: " + recipients.size());
            List<AditUser> allRecipients = new ArrayList<AditUser>();

            // For every recipient - check if registered in ADIT
            Iterator<dvk.api.container.v1.Saaja> recipientsIterator = recipients.iterator();
            while (recipientsIterator.hasNext()) {
                dvk.api.container.v1.Saaja recipient = recipientsIterator.next();

                // First of all make sure that this recipient is supposed to be found in ADIT
                if (!Util.isNullOrEmpty(recipient.getRegNr())
                        && getConfiguration().getDvkOrgCode().equalsIgnoreCase(recipient.getRegNr())) {

                    logger.info("Recipient: " + recipient.getRegNr() + " (" + recipient.getAsutuseNimi()
                            + "). Isikukood: '" + recipient.getIsikukood() + "'.");

                    // The ADIT internal recipient is always marked by
                    // the field <isikukood> in the DVK container,
                    // regardless if it is actually a person or an
                    // institution / company.
                    if (!Util.isNullOrEmpty(recipient.getRegNr())
                            && !Util.isNullOrEmpty(recipient.getIsikukood())) {
                        // The recipient is specified - check if it's a DVK user
                        String personalIdCodeWithCountryPrefix = recipient.getIsikukood().trim();
                        if (!personalIdCodeWithCountryPrefix.startsWith("EE")) {
                            personalIdCodeWithCountryPrefix = "EE" + personalIdCodeWithCountryPrefix;
                        }

                        logger.debug("Getting AditUser by personal code: " + personalIdCodeWithCountryPrefix);
                        AditUser user = this.getAditUserDAO().getUserByID(personalIdCodeWithCountryPrefix);

                        if (user != null && user.getActive()) {
                            // Check if user uses DVK
                            if (!Util.isNullOrEmpty(user.getDvkOrgCode())) {
                                // The user uses DVK - this is not allowed.
                                // Users that use DVK have to exchange documents with
                                // other users that use DVK, over DVK.
                                this.composeErrorResponse(DocumentService.DVK_RECEIVE_FAIL_REASON_USER_USES_DVK,
                                        dvkContainer, recipient.getIsikukood().trim(),
                                        dvkDocument.getReceivedDate(), recipient.getNimi());
                                logger.warn("User uses DVK - not allowed.");
                                return false;
                            } else {
                                allRecipients.add(user);
                            }
                        } else {
                            logger.error("User not found. Personal code: " + recipient.getIsikukood().trim());
                            this.composeErrorResponse(
                                    DocumentService.DVK_RECEIVE_FAIL_REASON_USER_DOES_NOT_EXIST, dvkContainer,
                                    recipient.getIsikukood().trim(), dvkDocument.getReceivedDate(),
                                    recipient.getNimi());
                            return false;
                        }
                    }
                }
            }

            if (allRecipients.size() > 0) {
                logger.debug("Constructing ADIT message");
                // Add document for this recipient to
                // ADIT database
                Document aditDocument = new Document();
                aditDocument.setCreationDate(new Date());
                aditDocument.setLastModifiedDate(new Date());
                aditDocument.setDocumentDvkStatusId(DVK_STATUS_SENT);
                aditDocument.setDvkId(dvkDocument.getDhlId());
                aditDocument.setGuid(dvkDocument.getDhlGuid());
                aditDocument.setLocked(true);
                aditDocument.setLockingDate(new Date());
                aditDocument.setSignable(true);
                aditDocument.setTitle(Util.isNullOrEmpty(dvkDocument.getTitle()) ? "-" : dvkDocument.getTitle());
                aditDocument.setDocumentType(DOCTYPE_LETTER);

                String seotudDokumendinrSaajal = dvkContainer.getMetainfo().getSeotudDokumendinrSaajal();
                if (seotudDokumendinrSaajal != null && seotudDokumendinrSaajal.length() > 0) {
                    try {
                        Document parent = this.getDocumentDAO().getDocument(Long.valueOf(seotudDokumendinrSaajal));

                        if (parent != null) {
                            aditDocument.setDocument(parent);
                        }
                    } catch (NumberFormatException ex) {
                        logger.info("Invalid related adit doc id in dhl message - " + dvkDocument.getDhlMessageId());
                    }
                }

                // The creator is the sender
                aditDocument.setCreatorCode(senderUser.getUserCode());
                aditDocument.setCreatorName(senderUser.getFullName());

                // Get document files from DVK container
                List<OutputDocumentFile> tempDocuments = this.getDocumentOutputFiles(dvkContainer);

                // Digital signature extraction
                boolean involvedSignatureContainerExtraction = false;
                if ((tempDocuments != null) && (tempDocuments.size() == 1)) {
                    OutputDocumentFile file = tempDocuments.get(0);
                    String extension = Util.getFileExtension(file.getName());

                    // If first added file happens to be a DigiDoc container then
                    // extract files and signatures from container. Otherwise add
                    // container as a regular file.
                    if (((file.getId() == null) || (file.getId() <= 0)) &&
                    		("ddoc".equalsIgnoreCase(extension)|| Util.isBdocExtension(extension))) {
                        DigiDocExtractionResult extractionResult = extractDigiDocContainer(file.getSysTempFile(), digidocConfigFile);
                        if (extractionResult.isSuccess()) {
                            file.setFileType(FILETYPE_NAME_SIGNATURE_CONTAINER);
                            aditDocument.setSigned((extractionResult.getSignatures() != null) && (extractionResult.getSignatures().size() > 0));
                            involvedSignatureContainerExtraction = true;

                            for (int i = 0; i < extractionResult.getFiles().size(); i++) {
                                DocumentFile df = extractionResult.getFiles().get(i);
                                df.setDocument(aditDocument);
                                aditDocument.getDocumentFiles().add(df);
                            }

                            for (int i = 0; i < extractionResult.getSignatures().size(); i++) {
                                ee.adit.dao.pojo.Signature sig = extractionResult.getSignatures().get(i);
                                sig.setDocument(aditDocument);
                                aditDocument.getSignatures().add(sig);
                            }
                        }
                    }
                }

                Session aditSession = null;
                //Transaction aditTransaction = null;
                try {
                    // Save the document
                    //aditSession = this.getDocumentDAO().getSessionFactory().openSession();
                    //aditTransaction = aditSession.beginTransaction();

                    // Save document
                    SaveItemInternalResult saveResult = getDocumentDAO().save(aditDocument, tempDocuments, Long.MAX_VALUE);

                    if (saveResult == null) {
                        logger.error("Document saving failed!");
                    }
                    if (saveResult.isSuccess()) {
                        logger.info("Document saved to ADIT database. ID: "
                                + saveResult.getItemId());

                        // Add signature container extraction history event
                        if (involvedSignatureContainerExtraction) {
                            addHistoryEvent("", saveResult.getItemId(), senderUser.getUserCode(),
                                    HISTORY_TYPE_EXTRACT_FILE, senderUser.getUserCode(), senderUser.getFullName(),
                                    DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE, senderUser.getFullName(),
                                    Calendar.getInstance().getTime());
                        }

                        // Add record to sending table to make document available to recipient.
                        for (AditUser recipient : allRecipients) {
                            this.sendDocumentAndNotifyRecipient(aditDocument, senderUser, recipient, null, dvkDocument.getDhlId(), null);
                        }

                        // Update user quota limit
                        Long usedDiskQuota = senderUser.getDiskQuotaUsed();
                        if (usedDiskQuota == null) {
                            usedDiskQuota = 0L;
                        }
                        senderUser.setDiskQuotaUsed(usedDiskQuota + saveResult.getAddedFilesSize());
                        this.getAditUserDAO().saveOrUpdate(senderUser, true);

                        // Add ID of created ADIT document to
                        // DVK buffer table.
                        //dvkDocument.setLocalItemId(saveResult.getItemId());
                        this.getDvkDAO().updateDocumentLocalId(saveResult.getItemId(), dvkDocument.getDhlMessageId());

                        // Finally commit
                        //aditTransaction.commit();
                        //aditSession.flush();
                        logger.info("DVK message " + dvkDocument.getDhlMessageId() + " was saved as ADIT document " + saveResult.getItemId());
                    } else {
                        success = false;
                        if ((saveResult.getMessages() != null) && (saveResult.getMessages().size() > 0)) {
                            logger.error(saveResult.getMessages().get(0).getValue());
                        } else {
                            logger.error("Document saving failed!");
                        }
                    }
                } catch (Exception e) {
                    success = false;
                    logger.warn("Error saving document to ADIT database: ", e);
                    //if (aditTransaction != null) {
                    //    aditTransaction.rollback();
                    //}
                } finally {
                    if (aditSession != null) {
                        aditSession.close();
                    }
                }
            }
        } catch (Exception ex) {
            success = false;
            logger.error(ex.getMessage(), ex);
        }

        return success;
    }

    private AditUser findAditUserByDvkSenderData(ContainerVer1 dvkContainer, Long docDvkId, String docDvkGuid) {
        AditUser result = null;
        List<dvk.api.container.v1.Saatja> senders = dvkContainer.getTransport().getSaatjad();

        if ((senders == null) || (senders.size() != 1)) {
            String errorMsg = "Unable to receive document from DVK because DVK envelope"
                    + " does not contain sender data. Message DVK ID: " + docDvkId
                    + ". Message DVK GUID: " + docDvkGuid;

            logger.error(errorMsg);
        } else {
            dvk.api.container.v1.Saatja sender = senders.get(0);
            if ((sender == null) || Util.isNullOrEmpty(sender.getRegNr())) {
                String errorMsg = "Unable to receive document from DVK because sender data"
                        + " is missing or unreadable. Message DVK ID: " + docDvkId
                        + ". Message DVK GUID: " + docDvkGuid;

                logger.error(errorMsg);
            } else {
                result = this.getAditUserDAO().getUserByID(sender.getRegNr());
                if ((result == null) || Util.isNullOrEmpty(result.getUserCode())) {
                    String senderCodeWithEePrefix = "EE" + sender.getRegNr();
                    result = this.getAditUserDAO().getUserByID(senderCodeWithEePrefix);

                    if ((result == null) || Util.isNullOrEmpty(result.getUserCode())) {
                        String errorMsg = "Unable to receive document from DVK because there is no"
                                + " known user that would match sender code \"" + sender.getRegNr()
                                + "\" or \"" + senderCodeWithEePrefix + "\"";

                        logger.error(errorMsg);
                        result = null;
                    }
                }
            }
        }
        return result;
    }


    /**
     * Converts the document object to {@code ContainerVer2} object.
     *
     * @param document DVK document
     * @return DVK container object
     * @throws AditInternalException
     */
    public ContainerVer2 getDVKContainer(PojoMessage document) throws AditInternalException {
        ContainerVer2 result = null;

        // Write the string data to a temporary file
        Reader dataReader  = null;
        FileWriter fileWriter = null;
        try {
        	dataReader  = new StringReader(document.getData());
            String tmpFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();
            fileWriter = new FileWriter(tmpFile);

            char[] cbuf = new char[1024];
            int readCount = 0;
            while ((readCount = dataReader .read(cbuf)) > 0) {
                fileWriter.write(cbuf, 0, readCount);
            }

            fileWriter.close();
            dataReader.close();

            result = ContainerVer2.parseFile(tmpFile);

            if (result == null) {
                throw new AditInternalException("DVK Container not initialized.");
            } else {
                if (result.getTransport() == null) {
                    throw new AditInternalException(
                            "DVK Container not properly initialized: <transport> section not initialized");
                }
            }

        } catch (Exception e) {
            throw new AditInternalException("Exception while reading DVK container from database: ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (Exception e) {
                    logger.warn("Error while closing file writer: ", e);
                }
            }

            if (dataReader != null) {
                try {
                	dataReader.close();
                } catch (Exception e) {
                    logger.warn("Error while closing Clob reader: ", e);
                }
            }
        }

        return result;
    }

    /**
     * Converts the document object to {@code ContainerVer2} object.
     *
     * @param document DVK document
     * @return DVK container object
     * @throws AditInternalException
     */
    public ContainerVer1 getDVKContainerV1(PojoMessage document) throws AditInternalException {
        ContainerVer1 result = null;

        StringBuilder sb = new StringBuilder(1024 * 1024 * 5);
        Reader dataReader = null;
        try {
        	dataReader = new StringReader(document.getData());

            char[] cbuf = new char[1024];
            int readCount = 0;
            while ((readCount = dataReader.read(cbuf)) > 0) {
                sb.append(cbuf, 0, readCount);
            }
            dataReader.close();

            result = ContainerVer1.parse(sb.toString());

            if (result == null) {
                throw new AditInternalException("DVK Container not initialized.");
            } else {
                if (result.getTransport() == null) {
                    throw new AditInternalException("DVK Container not properly initialized: <transport> section not initialized");
                }
            }
        } catch (Exception e) {
            logger.error("XML of container that could not be processed: " + sb.toString());
            throw new AditInternalException("Exception while reading DVK container from database: ", e);
        } finally {
            if (dataReader != null) {
                try {
                	dataReader.close();
                } catch (Exception e) {
                    logger.warn("Error while closing Clob reader: ", e);
                }
            }
        }

        return result;
    }

    /**
     * Get the 2.1 Container from dvkMessage.
     *
     * @param document {@link PojoMessage}
     * @return {@link ContainerVer2_1}
     */
    public ContainerVer2_1 getDVKContainer2_1(final PojoMessage document) {
        try {
            return ContainerVer2_1.parse(document.getData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Read string from clob.
     *
     * @param data {@link Clob}
     * @return string
     */
    public String readFromClob(final Clob data) {
        StringBuilder sb = new StringBuilder(1024 * 1024 * 5);
        Reader clobReader = null;
        try {
            clobReader = data.getCharacterStream();
            char[] cbuf = new char[1024];
            int readCount = 0;
            while ((readCount = clobReader.read(cbuf)) > 0) {
                sb.append(cbuf, 0, readCount);
            }
            clobReader.close();
        } catch (Exception e) {
            throw new AditInternalException("Unable to read from clob", e);
        } finally {
            if (clobReader != null) {
                try {
                    clobReader.close();
                } catch (Exception e) {
                    logger.warn("Error while closing Clob reader: ", e);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Extracts files from DVK container.
     *
     * @param dvkContainer DVK container
     * @return list of files extracted
     */
    public List<OutputDocumentFile> getDocumentOutputFiles(ContainerVer2 dvkContainer) {
        List<OutputDocumentFile> result = new ArrayList<OutputDocumentFile>();

        try {
            List<Fail> dvkFiles = dvkContainer.getFailideKonteiner().getFailid();
            Iterator<Fail> dvkFilesIterator = dvkFiles.iterator();

            logger.debug("Total number of files in DVK Container: " + dvkContainer.getFailideKonteiner().getKokku());

            while (dvkFilesIterator.hasNext()) {
                Fail dvkFile = dvkFilesIterator.next();
                logger.debug("Processing file nr.: " + dvkFile.getJrkNr());

                String fileContents = dvkFile.getZipBase64Sisu();
                InputStream inputStream = new StringBufferInputStream(fileContents);
                String tempFile = Util.createTemporaryFile(inputStream, this.getConfiguration().getTempDir());

                String decodedTempFile = Util.base64DecodeAndUnzip(tempFile, this.getConfiguration().getTempDir(), this
                        .getConfiguration().getDeleteTemporaryFilesAsBoolean());

                OutputDocumentFile tempDocument = new OutputDocumentFile();
                tempDocument.setSysTempFile(decodedTempFile);
                tempDocument.setContentType(dvkFile.getFailTyyp());
                tempDocument.setName(dvkFile.getFailNimi());
                tempDocument.setSizeBytes(dvkFile.getFailSuurus());

                // Add the temporary file to the list
                result.add(tempDocument);
            }

        } catch (Exception e) {
            if (result != null && result.size() > 0) {
                // Delete temporary files
                Iterator<OutputDocumentFile> documentsToDelete = result.iterator();
                while (documentsToDelete.hasNext()) {
                    OutputDocumentFile documentToDelete = documentsToDelete.next();
                    try {
                        if (documentToDelete.getSysTempFile() != null
                                && !documentToDelete.getSysTempFile().trim().equalsIgnoreCase("")) {
                            File f = new File(documentToDelete.getSysTempFile());
                            if (f.exists()) {
                                f.delete();
                            } else {
                                throw new FileNotFoundException("Could not find temporary file (to delete): "
                                        + documentToDelete.getSysTempFile());
                            }
                        }
                    } catch (Exception exc) {
                        logger.debug("Error while deleting temporary files: ", exc);
                    }
                }
                logger.info("Temporary files deleted.");
            }
            throw new AditInternalException("Error while saving files: ", e);
        }

        return result;
    }

    /**
     * Extracts files from DVK container.
     *
     * @param dvkContainer DVK container
     * @return list of files extracted
     */
    public List<OutputDocumentFile> getDocumentOutputFiles(ContainerVer1 dvkContainer) {
        List<OutputDocumentFile> result = new ArrayList<OutputDocumentFile>();

        try {
            List<dvk.api.container.v1.DataFile> dvkFiles = dvkContainer.getSignedDoc().getDataFiles();
            Iterator<dvk.api.container.v1.DataFile> dvkFilesIterator = dvkFiles.iterator();

            logger.debug("Total number of files in DVK Container: " + dvkFiles.size());

            while (dvkFilesIterator.hasNext()) {
                dvk.api.container.v1.DataFile dvkFile = dvkFilesIterator.next();
                logger.debug("Processing file with ID: " + dvkFile.getFileId());

                String fileContents = dvkFile.getFileBase64Content();
                InputStream inputStream = new StringBufferInputStream(fileContents);
                String tempFile = Util.createTemporaryFile(inputStream, this.getConfiguration().getTempDir());

                String decodedTempFile = Util.base64DecodeFile(tempFile, this.getConfiguration().getTempDir());

                OutputDocumentFile tempDocument = new OutputDocumentFile();
                tempDocument.setSysTempFile(decodedTempFile);
                tempDocument.setContentType(dvkFile.getFileMimeType());
                tempDocument.setName(dvkFile.getFileName());
                tempDocument.setSizeBytes(Long.parseLong(dvkFile.getFileSize()));

                // Add the temporary file to the list
                result.add(tempDocument);
            }

        } catch (Exception e) {
            if (result != null && result.size() > 0) {
                // Delete temporary files
                Iterator<OutputDocumentFile> documentsToDelete = result.iterator();
                while (documentsToDelete.hasNext()) {
                    OutputDocumentFile documentToDelete = documentsToDelete.next();
                    try {
                        if (documentToDelete.getSysTempFile() != null
                                && !documentToDelete.getSysTempFile().trim().equalsIgnoreCase("")) {
                            File f = new File(documentToDelete.getSysTempFile());
                            if (f.exists()) {
                                f.delete();
                            } else {
                                throw new FileNotFoundException("Could not find temporary file (to delete): "
                                        + documentToDelete.getSysTempFile());
                            }
                        }
                    } catch (Exception exc) {
                        logger.debug("Error while deleting temporary files: ", exc);
                    }
                }
                logger.info("Temporary files deleted.");
            }
            throw new AditInternalException("Error while saving files: ", e);
        }

        return result;
    }

    /**
     * Updates statuses for outgoing messages.
     *
     * @return number of messages updated.
     */
    @Transactional
    public int updateDocumentsFromDVK() {
        int result = 0;
        logger.info("Updating DVK statuses...");

        // 1. Võtame kõik dokumendid ADIT andmebaasist, millel DVK staatus ei ole "saadetud"
        List<Document> documents = this.getDocumentDAO().getDocumentsWithoutDVKStatus(DVK_STATUS_SENT);
        logger.info(documents.size() + " documents found that need their statuses updated.");

        Iterator<Document> documentsIterator = documents.iterator();
        while (documentsIterator.hasNext()) {
            Document document = documentsIterator.next();

            try {
                logger.info("Updating DVK status for document. DocumentID: " + document.getId());

                List<PojoMessageRecipient> messageRecipients = this.getDvkDAO().getMessageRecipients(
                        document.getDvkId(), false);

                PojoMessage dvkDocument = this.getDvkDAO().getMessage(
                        document.getDvkId());
                Iterator<PojoMessageRecipient> messageRecipientIterator = messageRecipients.iterator();

                List<DocumentSharing> documentSharings = this.getDocumentSharingDAO().getDVKSharings(document.getId());

                if (messageRecipients != null) {
                    logger.debug("messageRecipients.size: " + messageRecipients.size());
                }

                if (documentSharings != null) {
                    logger.debug("documentSharings.size: " + documentSharings.size());
                }

                while (messageRecipientIterator.hasNext()) {
                    PojoMessageRecipient messageRecipient = messageRecipientIterator.next();

                    logger.debug("Updating for messageRecipient: " + messageRecipient.getRecipientOrgCode());

                    boolean allDocumentSharingsSent = true;

                    // Compare the status with the status of the sharing in ADIT
                    for (int i = 0; i < documentSharings.size(); i++) {
                        DocumentSharing documentSharing = documentSharings.get(i);
                        logger.debug("Updating for documentSharing: " + documentSharing.getId());

                        String sharingUserCode = documentSharing.getUserCode();
                        String sharingUserCodeWithoutCountryPrefix = Util.removeCountryPrefix(sharingUserCode);
                        long sharingDvkStatus = (documentSharing.getDocumentDvkStatus() == null) ? 0 : documentSharing.getDocumentDvkStatus().longValue();
                                                
                        if (sharingUserCodeWithoutCountryPrefix.equalsIgnoreCase(messageRecipient.getRecipientOrgCode())
                                || sharingUserCode.equalsIgnoreCase(messageRecipient.getRecipientOrgCode())
                                || sharingUserCodeWithoutCountryPrefix.equalsIgnoreCase(messageRecipient.getRecipientPersonCode())
                                || sharingUserCode.equalsIgnoreCase(messageRecipient.getRecipientPersonCode())) {

                            // If the statuses differ or dvk id differ, update the one in ADIT
                            // database
                            boolean updateNeeded = false;
                            if (documentSharing.getDvkId() == null && dvkDocument.getDhlId() != null) {
                                updateNeeded = true;
                                documentSharing.setDvkId(dvkDocument.getDhlId());
                                logger.debug("DocumentSharing DVK id updated, DVK id: "
                                        + dvkDocument.getDhlId());
                            }
                            if (sharingDvkStatus != messageRecipient.getSendingStatusId()) {
                                documentSharing.setDocumentDvkStatus(messageRecipient.getSendingStatusId());
                                updateNeeded = true;
                                logger.debug("DocumentSharing DVK status updated: documentSharingID: "
                                        + documentSharing.getId() + ", DVK status: "
                                        + documentSharing.getDocumentDvkStatus());
                                result++;
                            }
                            if (updateNeeded) {
                                this.getDocumentSharingDAO().update(documentSharing, true);
                            }
                        }
                        
                        if (!documentSharing.getDocumentDvkStatus().equals(DocumentService.DVK_STATUS_SENT)) {
                            allDocumentSharingsSent = false;
                        }
                    }
                    
                    // If all documentSharings statuses are "sent" then update
                    // the document's dvk status
                    if (allDocumentSharingsSent) {
                        // Update document DVK status ID
                        document.setDocumentDvkStatusId(DocumentService.DVK_STATUS_SENT);
                        this.getDocumentDAO().update(document);
                        logger.debug("All DVK sharings for this document updated to 'sent'. Updating document DVK status.");
                    }
                }
            } catch (Exception e) {
                logger.error("Error while updating status from DVK. DocumentID: " + document.getId(), e);
                logger.info("Continue...");
            }
        }

        return result;
    }

    /**
     * Updates document statuses for incoming messages.
     *
     * @return number of messages updated.
     */
    @Transactional
    public int updateDocumentsToDVK() {

        int result = 0;
        try {
            List<PojoMessage> dvkDocuments = this.getDvkDAO().getIncomingDocumentsWithoutStatus(
                    DocumentService.DVK_STATUS_SENT);
            Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments.iterator();

            while (dvkDocumentsIterator.hasNext()) {
                PojoMessage dvkDocument = dvkDocumentsIterator.next();

                try {
                    // Find the message from ADIT database
                    Document document = this.getDocumentDAO().getDocumentByDVKID(dvkDocument.getDhlMessageId());

                    if (document != null) {

                        // Compare the statuses - ADIT status prevails
                        if (document.getDocumentDvkStatusId() != null) {
                            if (!document.getDocumentDvkStatusId().equals(dvkDocument.getRecipientStatusId())) {

                                // If the statuses do not match, update from
                                // ADIT to DVK
                                dvkDocument.setRecipientStatusId(document.getDocumentDvkStatusId());

                                // Update DVK document
                                this.getDvkDAO().updateDocument(dvkDocument);
                            }
                        } else {
                            throw new AditInternalException("Could not update document with DVK_ID: "
                                    + dvkDocument.getDhlMessageId() + ". Document's DVK status is not defined in ADIT.");
                        }
                    } else {
                        throw new AditInternalException("Could not find document with DVK_ID: "
                                + dvkDocument.getDhlMessageId());
                    }
                } catch (Exception e) {
                    logger.error("Error while updating DVK status for document. DVK_ID: " + dvkDocument.getDhlMessageId());
                    logger.error("Continue...");
                }
            }

        } catch (Exception e) {
            logger.error("Error while updating DVK statuses to DVK: ", e);
        }

        return result;
    }

    /**
     * Deletes documents from DVK, that have the status 'sent'.
     *
     * @return Number of documents deleted
     * @throws Exception
     */
    public int deleteSentDocumentsFromDVK() throws Exception {
        int result = 0;
        try {
            List<PojoMessage> dvkDocuments = this.getDvkDAO().getSentDocuments();

            logger.info("Documents (sent) fetched: " + dvkDocuments.size());

            Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments.iterator();

            Session dvkSession = null;
            Transaction transaction = null;

            try {
                dvkSession = this.getDvkDAO().getSessionFactory().openSession();
                transaction = dvkSession.beginTransaction();
                while (dvkDocumentsIterator.hasNext()) {
                    PojoMessage dvkDocument = dvkDocumentsIterator.next();
                    deleteDVKDocument(dvkDocument, dvkSession);
                    logger.info("Document deleted from DVK. DHL_ID: " + dvkDocument.getDhlId());
                    result++;
                }
                transaction.commit();

            } catch (Exception e) {
                logger.error("Error while deleting documents from DVK database: ", e);
                if (transaction != null) {
                    transaction.rollback();
                }
                result = 0;
                throw e;
            } finally {
                if (dvkSession != null) {
                    dvkSession.close();
                }
            }

        } catch (Exception e) {
            logger.error("Error while deleting documents from DVK: ", e);
            throw e;
        }

        return result;
    }

    /**
     * Deletes received documents from DVK client database. Deleting means the
     * DVK container XML is deleted (not the document metadata).
     *
     * @return Number of affected documents
     * @throws Exception
     */
    public int deleteReceivedDocumentsFromDVK() throws Exception {
        int result = 0;
        try {
            List<PojoMessage> dvkDocuments = this.getDvkDAO().getReceivedDocuments();

            logger.info("Documents (received / aborted) fetched: " + dvkDocuments.size());

            Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments.iterator();

            Session dvkSession = null;
            Transaction transaction = null;

            try {
                dvkSession = this.getDvkDAO().getSessionFactory().openSession();
                transaction = dvkSession.beginTransaction();
                while (dvkDocumentsIterator.hasNext()) {
                    PojoMessage dvkDocument = dvkDocumentsIterator.next();
                    deleteDVKDocument(dvkDocument, dvkSession);
                    logger.info("Document deleted from DVK. DHL_ID: " + dvkDocument.getDhlId());
                    result++;
                }
                transaction.commit();

            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                if (dvkSession != null) {
                    dvkSession.close();
                }
            }

        } catch (Exception e) {
            logger.error("Error while deleting documents from DVK: ", e);
            throw e;
        }

        return result;
    }

    /**
     * Deletes the messages content (data) and updates 'faultCode' to 'NO_FAULT:
     * DELETED BY ADIT'.
     *
     * @param message DVK document to be deleted.
     * @param session Current hibernate session.
     */
    public void deleteDVKDocument(PojoMessage message, Session session) {
        logger.debug("Deleting document. DHL_ID: " + message.getDhlId());
        message.setData(DocumentService.DVKBLOBMESSAGE_DELETE);
        message.setFaultCode(DocumentService.DVK_FAULT_CODE_FOR_DELETED);
        session.update(message);
    }

    /**
     * Kui dokumendi sidumisel kasutajaga ilmnes, et kasutajat pole ADIT
     * aktiivsete kasutajate hulgas, siis märgitakse dokument DVKs katkestatuks
     * ning algsele saatjale koostatakse automaatselt vastuskiri, milles on
     * toodud kaaskirja dokument (muudetav ADIT haldaja poolt) ning algne
     * dokument. Kui dokumendi adressaadiks on DVK kasutaja, siis talitatakse
     * sarnaselt eeltoodule, kuna DVK kasutajad peavad suhtlema otse omavahel,
     * mitte ADIT kaudu.
     *
     * @param reasonCode    reason code
     * @param dvkContainer  DVK container
     * @param recipientCode recipient code
     * @param receivedDate  date of retrieval
     * @param recipientName recipient name
     */
    public void composeErrorResponse(Integer reasonCode, ContainerVer2 dvkContainer, String recipientCode,
                                     Date receivedDate, String recipientName) throws AditInternalException {

        try {
            // 1. Gather data required for response message
            String xml = this.createErrorResponseDataXML(dvkContainer, recipientCode, receivedDate, recipientName);

            // 2. Transform to XSL-FO
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            String xmlTempFile = Util.createTemporaryFile(byteArrayInputStream, this.getConfiguration().getTempDir());
            String outputXslFoFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();

            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.getConfiguration().getDvkResponseMessageStylesheet());
            String styleSheet = Util.createTemporaryFile(input, this.getConfiguration().getTempDir());
            Util.applyXSLT(xmlTempFile, styleSheet, outputXslFoFile);

            // 3. Transform to PDF
            String outputPDFFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();
            Util.generatePDF(outputPDFFile, outputXslFoFile);

            // 4. Save the response message PDF to DVK
            logger.debug("DVK error response message composed. FileName: " + outputPDFFile);
            this.saveErrorResponseMessageToDVK(outputPDFFile, dvkContainer);

        } catch (Exception e) {
            logger.error("Error while composing DVK error response message: ", e);
            throw new AditInternalException("Error while composing DVK error response message: ", e);
        }
    }

    /**
     * Kui dokumendi sidumisel kasutajaga ilmnes, et kasutajat pole ADIT
     * aktiivsete kasutajate hulgas, siis märgitakse dokument DVKs katkestatuks
     * ning algsele saatjale koostatakse automaatselt vastuskiri, milles on
     * toodud kaaskirja dokument (muudetav ADIT haldaja poolt) ning algne
     * dokument. Kui dokumendi adressaadiks on DVK kasutaja, siis talitatakse
     * sarnaselt eeltoodule, kuna DVK kasutajad peavad suhtlema otse omavahel,
     * mitte ADIT kaudu.
     *
     * @param reasonCode    reason code
     * @param dvkContainer  DVK container
     * @param recipientCode recipient code
     * @param receivedDate  date of retrieval
     * @param recipientName recipient name
     */
    public void composeErrorResponse(Integer reasonCode, ContainerVer1 dvkContainer, String recipientCode,
                                     Date receivedDate, String recipientName) throws AditInternalException {

        try {
            // 1. Gather data required for response message
            String xml = this.createErrorResponseDataXML(dvkContainer, recipientCode, receivedDate, recipientName);

            // 2. Transform to XSL-FO
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            String xmlTempFile = Util.createTemporaryFile(byteArrayInputStream, this.getConfiguration().getTempDir());
            String outputXslFoFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();

            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.getConfiguration().getDvkResponseMessageStylesheet());
            String styleSheet = Util.createTemporaryFile(input, this.getConfiguration().getTempDir());
            Util.applyXSLT(xmlTempFile, styleSheet, outputXslFoFile);

            // 3. Transform to PDF
            String outputPDFFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();
            Util.generatePDF(outputPDFFile, outputXslFoFile);

            // 4. Save the response message PDF to DVK
            logger.debug("DVK error response message composed. FileName: " + outputPDFFile);
            this.saveErrorResponseMessageToDVK(outputPDFFile, dvkContainer);

        } catch (Exception e) {
            logger.error("Error while composing DVK error response message: ", e);
            throw new AditInternalException("Error while composing DVK error response message: ", e);
        }
    }

    /**
     * Creates DVK response message data XML string.
     *
     * @param dvkContainer  DVK container
     * @param recipientCode recipient code
     * @param receivedDate  receiving date
     * @param recipientName recipient name
     * @return XML string
     */
    public String createErrorResponseDataXML(ContainerVer2 dvkContainer, String recipientCode, Date receivedDate,
                                             String recipientName) {
        StringBuffer result = new StringBuffer();

        String senderOrgCode = null;
        String senderPersonCode = null;
        String senderOrgName = null;
        String senderName = null;
        String receiveDateTmp = null;
        String dhlId = null;
        String guid = null;
        String title = null;

        try {
            // DVK Container can contain only one sender
            Saatja sender = dvkContainer.getTransport().getSaatjad().get(0);
            senderOrgCode = sender.getRegNr();
            senderPersonCode = sender.getIsikukood();
            senderOrgName = sender.getAsutuseNimi();
            senderName = sender.getNimi();
        } catch (Exception e) {
            logger.error("Error while getting Sender information: ", e);
        }

        try {
            receiveDateTmp = Util.dateToXMLDate(receivedDate);
        } catch (Exception e) {
            try {
                receiveDateTmp = Util.dateToXMLDate(new Date());
            } catch (Exception exc) {
                logger.error("Error while parsing received date (system date): ", exc);
            }
            logger.error("Error while parsing received date: ", e);
        }

        try {
            dhlId = dvkContainer.getMetainfo().getMetaAutomatic().getDhlId();
        } catch (Exception e) {
            logger.error("Error while getting document DHL ID: ", e);
        }

        try {
            guid = dvkContainer.getMetainfo().getMetaManual().getDokumentGuid();
        } catch (Exception e) {
            logger.error("Error while getting document GUID: ", e);
        }

        try {
            title = dvkContainer.getMetainfo().getMetaManual().getDokumentPealkiri();
        } catch (Exception e) {
            logger.error("Error while getting document title: ", e);
        }

        result.append("<document>");

        result.append("<sender_org_code>");
        result.append(senderOrgCode);
        result.append("</sender_org_code>");

        result.append("<sender_person_code>");
        result.append(senderPersonCode);
        result.append("</sender_person_code>");

        result.append("<sender_org_name>");
        result.append(senderOrgName);
        result.append("</sender_org_name>");

        result.append("<sender_name>");
        result.append(senderName);
        result.append("</sender_name>");

        result.append("<receiving_date>");
        result.append(receiveDateTmp);
        result.append("</receiving_date>");

        result.append("<dhl_message_id>");
        result.append(dhlId);
        result.append("</dhl_message_id>");

        result.append("<guid>");
        result.append(guid);
        result.append("</guid>");

        result.append("<title>");
        result.append(title);
        result.append("</title>");

        result.append("<recipient_personal_code>");
        result.append(recipientCode);
        result.append("</recipient_personal_code>");

        result.append("<recipient_name>");
        result.append(recipientName);
        result.append("</recipient_name>");

        result.append("</document>");

        return result.toString();
    }

    /**
     * Creates DVK response message data XML string.
     *
     * @param dvkContainer  DVK container
     * @param recipientCode recipient code
     * @param receivedDate  receiving date
     * @param recipientName recipient name
     * @return XML string
     */
    public String createErrorResponseDataXML(ContainerVer1 dvkContainer, String recipientCode, Date receivedDate,
                                             String recipientName) {
        StringBuffer result = new StringBuffer();

        String senderOrgCode = null;
        String senderPersonCode = null;
        String senderOrgName = null;
        String senderName = null;
        String receiveDateTmp = null;
        String dhlId = null;
        String guid = null;
        String title = null;

        try {
            // DVK Container can contain only one sender
            dvk.api.container.v1.Saatja sender = dvkContainer.getTransport().getSaatjad().get(0);
            senderOrgCode = sender.getRegNr();
            senderPersonCode = sender.getIsikukood();
            senderOrgName = sender.getAsutuseNimi();
            senderName = sender.getNimi();
        } catch (Exception e) {
            logger.error("Error while getting Sender information: ", e);
        }

        try {
            receiveDateTmp = Util.dateToXMLDate(receivedDate);
        } catch (Exception e) {
            try {
                receiveDateTmp = Util.dateToXMLDate(new Date());
            } catch (Exception exc) {
                logger.error("Error while parsing received date (system date): ", exc);
            }
            logger.error("Error while parsing received date: ", e);
        }

        result.append("<document>");

        result.append("<sender_org_code>");
        result.append(senderOrgCode);
        result.append("</sender_org_code>");

        result.append("<sender_person_code>");
        result.append(senderPersonCode);
        result.append("</sender_person_code>");

        result.append("<sender_org_name>");
        result.append(senderOrgName);
        result.append("</sender_org_name>");

        result.append("<sender_name>");
        result.append(senderName);
        result.append("</sender_name>");

        result.append("<receiving_date>");
        result.append(receiveDateTmp);
        result.append("</receiving_date>");

        result.append("<dhl_message_id>");
        result.append(dhlId);
        result.append("</dhl_message_id>");

        result.append("<guid>");
        result.append(guid);
        result.append("</guid>");

        result.append("<title>");
        result.append(title);
        result.append("</title>");

        result.append("<recipient_personal_code>");
        result.append(recipientCode);
        result.append("</recipient_personal_code>");

        result.append("<recipient_name>");
        result.append(recipientName);
        result.append("</recipient_name>");

        result.append("</document>");

        return result.toString();
    }

    /**
     * Saves error response message to DVK client database.
     *
     * @param fileName          absolute path to the data file.
     * @param originalContainer original (request) DVK container
     * @throws Exception
     */
    public void saveErrorResponseMessageToDVK(String fileName, ContainerVer2 originalContainer) throws Exception {

        String guid = null;
        Long dvkMessageID = null;

        try {
            PojoSettings settings = this.getDvkDAO().getDVKSettings();

            // 1. Construct a DVK Container
            ContainerVer2 container = new ContainerVer2();
            container.setVersion(DVK_CONTAINER_VERSION);

            // Transport
            List<Saatja> senders = new ArrayList<Saatja>();
            Saatja sender = new Saatja();
            sender.setRegNr(settings.getInstitutionCode());
            sender.setAsutuseNimi(settings.getInstitutionName());
            senders.add(sender);

            List<Saaja> recipients = new ArrayList<Saaja>();
            Saaja recipient = new Saaja();
            Saatja originalSender = null;

            originalSender = originalContainer.getTransport().getSaatjad().get(0);

            if (originalSender != null) {
                recipient.setRegNr(originalSender.getRegNr());
                recipient.setAsutuseNimi(originalSender.getAsutuseNimi());
                recipient.setAllyksuseLyhinimetus(originalSender.getAllyksuseLyhinimetus());
                recipient.setAllyksuseNimetus(originalSender.getAllyksuseNimetus());
                recipient.setAmetikohaLyhinimetus(originalSender.getAmetikohaLyhinimetus());
                recipient.setAmetikohaNimetus(originalSender.getAmetikohaNimetus());
                recipient.setEpost(originalSender.getEpost());
                recipient.setIsikukood(originalSender.getIsikukood());
                recipient.setNimi(originalSender.getNimi());
                recipient.setOsakonnaKood(originalSender.getOsakonnaKood());
                recipient.setOsakonnaNimi(originalSender.getOsakonnaNimi());
            } else {
                throw new AditInternalException(
                        "Error while saving error message response to DVK: original sender not found.");
            }

            recipients.add(recipient);
            Transport transport = new Transport();
            transport.setSaatjad(senders);
            transport.setSaajad(recipients);
            container.setTransport(transport);

            // MetaManual
            Metainfo metainfo = new Metainfo();
            MetaManual metaManual = new MetaManual();

            guid = Util.generateGUID();

            metaManual.setDokumentGuid(guid);
            metaManual.setDokumentLiik(DOCTYPE_LETTER);
            metaManual.setDokumentPealkiri(DVK_ERROR_RESPONSE_MESSAGE_TITLE);

            SaatjaKontekst saatjaKontekst = new SaatjaKontekst();
            saatjaKontekst.setDokumentSaatjaGuid(originalContainer.getMetainfo().getMetaManual().getDokumentGuid());
            saatjaKontekst.setSeosviit(originalContainer.getMetainfo().getMetaManual().getDokumentViit());

            metaManual.setSaatjaKontekst(saatjaKontekst);
            metaManual.setSeotudDhlId(originalContainer.getMetainfo().getMetaAutomatic().getDhlId());
            metaManual.setSeotudDokumendinrSaajal(originalContainer.getMetainfo().getMetaManual().getDokumentViit());

            metainfo.setMetaManual(metaManual);
            container.setMetainfo(metainfo);
            FailideKonteiner failideKonteiner = new FailideKonteiner();
            failideKonteiner.setKokku((short) 1);

            List<Fail> files = new ArrayList<Fail>();

            Fail responseFile = new Fail();
            File dataFile = new File(fileName);
            responseFile.setFile(dataFile);
            responseFile.setFailNimi(DVK_ERROR_RESPONSE_MESSAGE_FILENAME);
            responseFile.setFailPealkiri(DVK_ERROR_RESPONSE_MESSAGE_TITLE);
            responseFile.setFailSuurus(dataFile.length());
            responseFile.setFailTyyp(MimeConstants.MIME_PDF);
            responseFile.setJrkNr((short) 1);
            responseFile.setKrypteering(false);
            responseFile.setPohiDokument(true);

            // Add the response document
            files.add(responseFile);

            // Add the original files
            FailideKonteiner originalFileContainer = originalContainer.getFailideKonteiner();
            List<Fail> originalFiles = originalFileContainer.getFailid();

            for (int i = 0; i < originalFiles.size(); i++) {
                Fail originalFile = originalFiles.get(i);
                files.add(originalFile);
            }

            failideKonteiner.setFailid(files);

            container.setFailideKonteiner(failideKonteiner);

            String temporaryFile = this.getConfiguration().getTempDir() + File.separator
                    + Util.generateRandomFileName();
            container.save2File(temporaryFile);

            logger.info("DVK error response message DVK container saved to temporary file: " + temporaryFile);

            // 2. Construct a DVK PojoMessage
            PojoMessage message = new PojoMessage();
            message.setDhlGuid(guid);
            message.setDhlFolderName(null);
            message.setIsIncoming(false);
            message.setSendingStatusId(DVK_STATUS_WAITING);
            message.setTitle(DVK_ERROR_RESPONSE_MESSAGE_TITLE);

            Session dvkSession = null;
            Transaction dvkTransaction = null;
            try {
                dvkSession = this.getDocumentDAO().getSessionFactory().openSession();
                dvkTransaction = dvkSession.beginTransaction();

                message.setData(" ");

                dvkMessageID = (Long) dvkSession.save(message);
                message.setData(null);

                if (dvkMessageID == null || dvkMessageID.longValue() == 0) {
                    logger.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
                    throw new DataRetrievalFailureException(
                            "Error while saving outgoing message to DVK database - no ID returned by save method.");
                } else {
                    logger.info("Outgoing message saved to DVK database. ID: " + dvkMessageID);
                }
            } catch (Exception e) {
                if (dvkTransaction != null) {
                    dvkTransaction.rollback();
                }
                throw new DataRetrievalFailureException("Error while adding message to DVK Client database: ", e);
            } finally {
                if (dvkSession != null) {
                    dvkSession.close();
                }
            }

            logger.debug("DVK Message saved to client database. GUID: " + message.getDhlGuid());
            dvkTransaction.commit();

            // Update CLOB
            Session dvkSession2 = this.getDocumentDAO().getSessionFactory().openSession();
            Transaction dvkTransaction2 = dvkSession2.beginTransaction();

            try {
                // Select the record for update
                PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2.load(PojoMessage.class, dvkMessageID,
                        LockMode.UPGRADE);

                // Write the temporary file to the database
                InputStream is = new FileInputStream(temporaryFile);
                Writer dataWriter = new StringWriter();

                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                	dataWriter.write(new String(buf, 0, len, "UTF-8"));
                }
                is.close();
                dataWriter.close();
                dvkMessageToUpdate.setData(dataWriter.toString());

                // Commit to DVK database
                dvkTransaction2.commit();
            } catch (Exception e) {
                dvkTransaction2.rollback();

                // Remove the document with empty clob from the database
                Session dvkSession3 = this.getDocumentDAO().getSessionFactory().openSession();
                Transaction dvkTransaction3 = dvkSession3.beginTransaction();
                try {
                    logger.debug("Starting to delete document from DVK Client database: " + dvkMessageID);
                    PojoMessage dvkMessageToDelete = (PojoMessage) dvkSession3.load(PojoMessage.class, dvkMessageID);
                    if (dvkMessageToDelete == null) {
                        logger.warn("DVK message to delete is not initialized.");
                    }
                    dvkSession3.delete(dvkMessageToDelete);
                    dvkTransaction3.commit();
                    logger.info("Empty DVK document deleted from DVK Client database. ID: " + dvkMessageID);
                } catch (Exception dvkException) {
                    dvkTransaction3.rollback();
                    logger.error("Error deleting document from DVK database: ", dvkException);
                } finally {
                    if (dvkSession3 != null) {
                        dvkSession3.close();
                    }
                }
                throw new DataRetrievalFailureException(
                        "Error while adding message to DVK Client database (CLOB update): ", e);
            } finally {
                if (dvkSession2 != null) {
                    dvkSession2.close();
                }
            }
        } catch (Exception e) {
            logger.error("Error while constructing DVK response message: ", e);
            throw e;
        }
    }

    /**
     * Saves error response message to DVK client database.
     *
     * @param fileName          absolute path to the data file.
     * @param originalContainer original (request) DVK container
     * @throws Exception
     */
    public void saveErrorResponseMessageToDVK(String fileName, ContainerVer1 originalContainer) throws Exception {
        String guid = null;
        Long dvkMessageID = null;

        try {
            PojoSettings settings = this.getDvkDAO().getDVKSettings();

            // 1. Construct a DVK Container
            ContainerVer2 container = new ContainerVer2();
            container.setVersion(DVK_CONTAINER_VERSION);

            // Transport
            List<Saatja> senders = new ArrayList<Saatja>();
            Saatja sender = new Saatja();
            sender.setRegNr(settings.getInstitutionCode());
            sender.setAsutuseNimi(settings.getInstitutionName());
            senders.add(sender);

            List<Saaja> recipients = new ArrayList<Saaja>();
            Saaja recipient = new Saaja();
            dvk.api.container.v1.Saatja originalSender = null;

            originalSender = originalContainer.getTransport().getSaatjad().get(0);

            if (originalSender != null) {
                recipient.setRegNr(originalSender.getRegNr());
                recipient.setAsutuseNimi(originalSender.getAsutuseNimi());
                //recipient.setAllyksuseLyhinimetus(originalSender.getAllyksuseLyhinimetus());
                recipient.setAllyksuseNimetus(originalSender.getAllyksuseNimetus());
                //recipient.setAmetikohaLyhinimetus(originalSender.getAmetikohaLyhinimetus());
                recipient.setAmetikohaNimetus(originalSender.getAmetikohaNimetus());
                recipient.setEpost(originalSender.getEpost());
                recipient.setIsikukood(originalSender.getIsikukood());
                recipient.setNimi(originalSender.getNimi());
                recipient.setOsakonnaKood(originalSender.getOsakonnaKood());
                recipient.setOsakonnaNimi(originalSender.getOsakonnaNimi());
            } else {
                throw new AditInternalException("Error while saving error message response to DVK: original sender not found.");
            }

            recipients.add(recipient);
            Transport transport = new Transport();
            transport.setSaatjad(senders);
            transport.setSaajad(recipients);
            container.setTransport(transport);

            // MetaManual
            Metainfo metainfo = new Metainfo();
            MetaManual metaManual = new MetaManual();

            guid = Util.generateGUID();

            metaManual.setDokumentGuid(guid);
            metaManual.setDokumentLiik(DOCTYPE_LETTER);
            metaManual.setDokumentPealkiri(DVK_ERROR_RESPONSE_MESSAGE_TITLE);

//            SaatjaKontekst saatjaKontekst = new SaatjaKontekst();
//            saatjaKontekst.setDokumentSaatjaGuid(originalContainer.getMetainfo().getMetaManual().getDokumentGuid());
//            saatjaKontekst.setSeosviit(originalContainer.getMetainfo().getMetaManual().getDokumentViit());
//
//            metaManual.setSaatjaKontekst(saatjaKontekst);
//            metaManual.setSeotudDhlId(originalContainer.getMetainfo().getMetaAutomatic().getDhlId());
//            metaManual.setSeotudDokumendinrSaajal(originalContainer.getMetainfo().getMetaManual().getDokumentViit());

            metainfo.setMetaManual(metaManual);
            container.setMetainfo(metainfo);
            FailideKonteiner failideKonteiner = new FailideKonteiner();
            failideKonteiner.setKokku((short) 1);

            List<Fail> files = new ArrayList<Fail>();

            Fail responseFile = new Fail();
            File dataFile = new File(fileName);
            responseFile.setFile(dataFile);
            responseFile.setFailNimi(DVK_ERROR_RESPONSE_MESSAGE_FILENAME);
            responseFile.setFailPealkiri(DVK_ERROR_RESPONSE_MESSAGE_TITLE);
            responseFile.setFailSuurus(dataFile.length());
            responseFile.setFailTyyp(MimeConstants.MIME_PDF);
            responseFile.setJrkNr((short) 1);
            responseFile.setKrypteering(false);
            responseFile.setPohiDokument(true);

            // Add the response document
            files.add(responseFile);

            // Add the original files
            List<dvk.api.container.v1.DataFile> originalFiles = originalContainer.getSignedDoc().getDataFiles();

            for (int i = 0; i < originalFiles.size(); i++) {
                dvk.api.container.v1.DataFile originalFile = originalFiles.get(i);
                String filePath = Util.createTemporaryFile(
                        Util.base64DecodeToByteArray(originalFile.getFileBase64Content()),
                        this.getConfiguration().getTempDir());
                File f = new File(filePath);

                Fail v2File = new Fail();
                v2File.setFile(f);
                v2File.setFailSuurus(f.length());
                v2File.setFailNimi(originalFile.getFileName());
                v2File.setFailTyyp(originalFile.getFileMimeType());
                v2File.setJrkNr((short) (i + 1));
                v2File.setKrypteering(false);
                v2File.setPohiDokument(false);

                files.add(v2File);
            }

            failideKonteiner.setFailid(files);

            container.setFailideKonteiner(failideKonteiner);

            String temporaryFile = this.getConfiguration().getTempDir() + File.separator
                    + Util.generateRandomFileName();
            container.save2File(temporaryFile);

            logger.info("DVK error response message DVK container saved to temporary file: " + temporaryFile);

            // 2. Construct a DVK PojoMessage
            PojoMessage message = new PojoMessage();
            message.setDhlGuid(guid);
            message.setDhlFolderName(null);
            message.setIsIncoming(false);
            message.setSendingStatusId(DVK_STATUS_WAITING);
            message.setTitle(DVK_ERROR_RESPONSE_MESSAGE_TITLE);

            Session dvkSession = null;
            Transaction dvkTransaction = null;
            try {
                dvkSession = this.getDocumentDAO().getSessionFactory().openSession();
                dvkTransaction = dvkSession.beginTransaction();

                message.setData(" ");

                dvkMessageID = (Long) dvkSession.save(message);
                message.setData(null);

                if (dvkMessageID == null || dvkMessageID.longValue() == 0) {
                    logger.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
                    throw new DataRetrievalFailureException(
                            "Error while saving outgoing message to DVK database - no ID returned by save method.");
                } else {
                    logger.info("Outgoing message saved to DVK database. ID: " + dvkMessageID);
                }
            } catch (Exception e) {
                if (dvkTransaction != null) {
                    dvkTransaction.rollback();
                }
                throw new DataRetrievalFailureException("Error while adding message to DVK Client database: ", e);
            } finally {
                if (dvkSession != null) {
                    dvkSession.close();
                }
            }

            logger.debug("DVK Message saved to client database. GUID: " + message.getDhlGuid());
            dvkTransaction.commit();

            // Update CLOB
            Session dvkSession2 = this.getDocumentDAO().getSessionFactory().openSession();
            Transaction dvkTransaction2 = dvkSession2.beginTransaction();

            try {
                // Select the record for update
                PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2.load(PojoMessage.class, dvkMessageID, LockMode.UPGRADE);

                // Write the temporary file to the database
                InputStream is = new FileInputStream(temporaryFile);
                Writer dataWriter = new StringWriter();

                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                	dataWriter.write(new String(buf, 0, len, "UTF-8"));
                }
                is.close();
                dataWriter.close();
                
                dvkMessageToUpdate.setData(dataWriter.toString());

                // Commit to DVK database
                dvkTransaction2.commit();
            } catch (Exception e) {
                dvkTransaction2.rollback();

                // Remove the document with empty clob from the database
                Session dvkSession3 = this.getDocumentDAO().getSessionFactory().openSession();
                Transaction dvkTransaction3 = dvkSession3.beginTransaction();
                try {
                    logger.debug("Starting to delete document from DVK Client database: " + dvkMessageID);
                    PojoMessage dvkMessageToDelete = (PojoMessage) dvkSession3.load(PojoMessage.class, dvkMessageID);
                    if (dvkMessageToDelete == null) {
                        logger.warn("DVK message to delete is not initialized.");
                    }
                    dvkSession3.delete(dvkMessageToDelete);
                    dvkTransaction3.commit();
                    logger.info("Empty DVK document deleted from DVK Client database. ID: " + dvkMessageID);
                } catch (Exception dvkException) {
                    dvkTransaction3.rollback();
                    logger.error("Error deleting document from DVK database: ", dvkException);
                } finally {
                    if (dvkSession3 != null) {
                        dvkSession3.close();
                    }
                }
                throw new DataRetrievalFailureException(
                        "Error while adding message to DVK Client database (String update): ", e);
            } finally {
                if (dvkSession2 != null) {
                    dvkSession2.close();
                }
            }
        } catch (Exception e) {
            logger.error("Error while constructing DVK response message: ", e);
            throw e;
        }
    }

    /**
     * Marks document matching the given ID as deleted. Document file contents
     * will be replaced with their MD5 hash codes. Document and individual files
     * will be marked as "deleted".
     *
     * @param documentId      ID of document to be deleted
     * @param userCode        Code of the user who executed current request
     * @param applicationName Short name of application that executed current request
     * @throws Exception
     */
    @Transactional
    public void deleteDocument(long documentId, String userCode, String applicationName) throws Exception {
        Document doc = this.getDocumentDAO().getDocument(documentId);
        long deletedFilesSize = 0;

        // Check whether or not the document exists
        if (doc == null) {
            AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
            aditCodedException.setParameters(new Object[]{new Long(documentId).toString()});
            throw aditCodedException;
        }

        // Make sure that the document is not already deleted
        // NB! doc.getDeleted() can be NULL
        if ((doc.getDeleted() != null) && doc.getDeleted()) {
            AditCodedException aditCodedException = new AditCodedException("request.deleteDocument.document.deleted");
            aditCodedException.setParameters(new Object[]{new Long(documentId).toString()});
            throw aditCodedException;
        }

        boolean saveDocument = false;

        // Check whether or not given document belongs to current user
        if ((userCode != null) && (userCode.equalsIgnoreCase(doc.getCreatorCode()))) {

            // If document has been sent then preserve contents and only mark
            // document as invisible to owner.
            // If document has been shared then cancel sharing and delete the document
            boolean hasBeenSent = false;
            boolean hasBeenSentToSelfOnly = false;
            if (doc.getDocumentSharings() != null) {
                int numberOfSendRecipients = 0;
                Iterator<DocumentSharing> it = doc.getDocumentSharings().iterator();
                while (it.hasNext()) {
                    DocumentSharing sharing = it.next();
                    if (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SHARE)
                            || sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SIGN)) {

                        it.remove();
                        sharing.setDocumentId(0);
                    } else {
                        hasBeenSent = true;
                        numberOfSendRecipients++;

                        if (userCode.equalsIgnoreCase(sharing.getUserCode())) {
                            sharing.setDeleted(true);
                            hasBeenSentToSelfOnly = true;
                        }
                    }
                }
                hasBeenSentToSelfOnly = hasBeenSentToSelfOnly && (numberOfSendRecipients == 1);
            }

            if (!Boolean.TRUE.equals(doc.getDeleted()) && !Boolean.TRUE.equals(doc.getInvisibleToOwner())) {
                if (doc.getDocumentFiles() != null) {
                    Iterator<DocumentFile> it = doc.getDocumentFiles().iterator();
                    while (it.hasNext()) {
                        DocumentFile docFile = it.next();
                        if ((docFile.getDeleted() == null) || !docFile.getDeleted()) {
                            if (!hasBeenSent || hasBeenSentToSelfOnly) {
                                // Replace file contents with MD5 hash of original contents
                                String resultCode = this.deflateDocumentFile(doc.getId(), docFile.getId(), true, false);
                                // Make sure no relevant error code was returned
                                if (resultCode.equalsIgnoreCase("file_does_not_exist")) {
                                    AditCodedException aditCodedException = new AditCodedException("file.nonExistent");
                                    aditCodedException.setParameters(new Object[]{new Long(docFile.getId())
                                            .toString()});
                                    throw aditCodedException;
                                } else if (resultCode.equalsIgnoreCase("file_does_not_belong_to_document")) {
                                    AditCodedException aditCodedException = new AditCodedException(
                                            "file.doesNotBelongToDocument");
                                    aditCodedException.setParameters(new Object[]{
                                            new Long(docFile.getId()).toString(), new Long(doc.getId()).toString()});
                                    throw aditCodedException;
                                }
                            }
                            deletedFilesSize = deletedFilesSize + docFile.getFileSizeBytes();
                        }
                    }
                }
                if (!hasBeenSent || hasBeenSentToSelfOnly) {
                    doc.setDeleted(true);
                } else {
                    doc.setInvisibleToOwner(true);
                }
                saveDocument = true;
            } else {
                AditCodedException aditCodedException = new AditCodedException(
                        "request.deleteDocument.document.deleted");
                aditCodedException.setParameters(new Object[]{new Long(documentId).toString()});
                throw aditCodedException;
            }
        } else if (doc.getDocumentSharings() != null) {
            // Check whether or not the document has been shared to current user
            Iterator<DocumentSharing> it = doc.getDocumentSharings().iterator();
            while (it.hasNext()) {
                DocumentSharing sharing = it.next();
                if (sharing.getUserCode() != null && sharing.getUserCode().equalsIgnoreCase(userCode)) {
                    if (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SHARE)
                            || sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SIGN)) {
                        // doc.getDocumentSharings().remove(sharing); // NB! DO NOT
                        // DO THAT - can throw ConcurrentModificationException
                        it.remove();
                        sharing.setDocumentId(0);
                    } else {
                        sharing.setDeleted(true);
                    }
                    saveDocument = true;
                }
            }
            if (!saveDocument) {
                AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
                aditCodedException.setParameters(new Object[]{new Long(documentId).toString(), userCode});
                throw aditCodedException;
            }
        } else {
            AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            aditCodedException.setParameters(new Object[]{new Long(documentId).toString(), userCode});
            throw aditCodedException;
        }

        // Save changes to database
        if (saveDocument) {
            // Using Long.MAX_VALUE for disk quota because it is not possible to
            // exceed disk quota by deleting files. Therefore it does not make much
            // sense to calculate the actual disk quota here.
            this.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);

            if (deletedFilesSize > 0) {
                AditUser user = this.getAditUserDAO().getUserByID(userCode);
                if (user != null) {
                    Long usedDiskQuota = user.getDiskQuotaUsed();
                    if (usedDiskQuota == null) {
                        usedDiskQuota = 0L;
                    }

                    // Re-calculate used disk quota and prevent the result
                    // from being negative.
                    long newUsedDiskQuota = usedDiskQuota - deletedFilesSize;
                    if (newUsedDiskQuota < 0) {
                        newUsedDiskQuota = 0;
                    }

                    user.setDiskQuotaUsed(newUsedDiskQuota);
                    this.getAditUserDAO().saveOrUpdate(user, true);
                }
            }

        }
    }

    /**
     * Deflates document matching the given ID. Deflation means that document
     * file contents will be replaced with their MD5 hash codes. Document and
     * individual files will be marked as "deflated".
     *
     * @param documentId      ID of document to be deflated
     * @param userCode        Code of the user who executed current request
     * @param applicationName Short name of application that executed current request
     * @throws Exception
     */
    @Transactional
    public void deflateDocument(long documentId, String userCode, String applicationName) throws Exception {
        Document doc = this.getDocumentDAO().getDocument(documentId);
        long deflatedFilesSize = 0;

        // Check whether or not the document exists
        if (doc == null) {
            AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
            aditCodedException.setParameters(new Object[]{new Long(documentId).toString()});
            throw aditCodedException;
        }

        // Make sure that the document is not deleted
        // NB! doc.getDeleted() can be NULL
        if ((doc.getDeleted() != null) && doc.getDeleted()) {
            AditCodedException aditCodedException = new AditCodedException("document.deleted");
            aditCodedException.setParameters(new Object[]{new Long(documentId).toString()});
            throw aditCodedException;
        }

        // Make sure that the document is not already deflated
        // NB! doc.getDeflated() can be NULL
        if ((doc.getDeflated() != null) && doc.getDeflated()) {
            AditCodedException aditCodedException = new AditCodedException("document.deflated");
            aditCodedException.setParameters(new Object[]{Util.dateToEstonianDateString(doc.getDeflateDate())});
            throw aditCodedException;
        }

        // Check whether or not the document belongs to user
        if (!doc.getCreatorCode().equalsIgnoreCase(userCode)) {
            AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            aditCodedException.setParameters(new Object[]{new Long(documentId).toString(), userCode});
            throw aditCodedException;

        }

        // Replace file contents with their MD5 hash codes
        Iterator<DocumentFile> it = doc.getDocumentFiles().iterator();
        while (it.hasNext()) {
            DocumentFile docFile = it.next();

            if ((docFile.getDeleted() == null) || !docFile.getDeleted()) {
                String resultCode = deflateDocumentFile(doc.getId(), docFile.getId(), false, false);

                if (resultCode.equalsIgnoreCase("file_does_not_exist")) {
                    AditCodedException aditCodedException = new AditCodedException("file.nonExistent");
                    aditCodedException.setParameters(new Object[]{new Long(docFile.getId()).toString()});
                    throw aditCodedException;
                } else if (resultCode.equalsIgnoreCase("file_does_not_belong_to_document")) {
                    AditCodedException aditCodedException = new AditCodedException("file.doesNotBelongToDocument");
                    aditCodedException.setParameters(new Object[]{new Long(docFile.getId()).toString(), new Long(doc.getId()).toString()});
                    throw aditCodedException;
                }

                deflatedFilesSize = deflatedFilesSize + docFile.getFileSizeBytes();
            }
        }

        // Mark document as deflated, locked and not signable
        doc.setDeflated(true);
        doc.setDeflateDate(new Date());
        doc.setLocked(true);
        doc.setLockingDate(new Date());
        doc.setSignable(false);

        // Save changes to database
        //
        // Using Long.MAX_VALUE for disk quota because it is not possible to
        // exceed disk quota by deleting files. Therefore it does not make much
        // sense to calculate the actual disk quota here.
        this.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);

        if (deflatedFilesSize > 0) {
            AditUser user = this.getAditUserDAO().getUserByID(userCode);
            if (user != null) {
                Long usedDiskQuota = user.getDiskQuotaUsed();
                if (usedDiskQuota == null) {
                    usedDiskQuota = 0L;
                }

                // Re-calculate used disk quota and prevent the result
                // from being negative.
                long newUsedDiskQuota = usedDiskQuota - deflatedFilesSize;
                if (newUsedDiskQuota < 0) {
                    newUsedDiskQuota = 0;
                }

                user.setDiskQuotaUsed(newUsedDiskQuota);
                this.getAditUserDAO().saveOrUpdate(user, true);
            }
        }
    }

    /**
     * Initializes signing of specified document.<br>
     * Adds a pending signature to documents signature container and returns hash code of added signature.
     * Returned hash code can be signed using ID-card in any user interface.
     *
     * @param documentId        Document ID specifying which document should be signed
     * @param manifest          Role or resolution of signer
     * @param country           Country part of signers address
     * @param state             County/state part of signers address
     * @param city              City/town/village part of signers address
     * @param zip               Postal code of signers address
     * @param certFile          Absolute path to signers signing certificate file
     * @param digidocConfigFile Absolute path to DigiDoc configuration file
     * @param temporaryFilesDir Absolute path to applications temporary files directory
     * @param xroadUser         {@link AditUser} who executed current request
     * @return {@link PrepareSignatureInternalResult} that contains hash code of
     *         added signature and indication whether or not adding new signature succeeded.
     * @throws Exception
     */
    public PrepareSignatureInternalResult prepareSignature(final long documentId, final String manifest,
                                                           final String country, final String state, final String city, final String zip,
                                                           final String certFile, final AditUser xroadUser, final Boolean doPreferBdoc) throws Exception {

        PrepareSignatureInternalResult result = new PrepareSignatureInternalResult();
        result.setSuccess(true);
        
        // TODO We should always prefer BDOC. Read more here: http://www.id.ee/index.php?id=37370
        // First of all put isBdoc equal input parameter,
        // but later we will change it according to existing container if necessary.
        Boolean isBdoc = doPreferBdoc;
        
        Session session = null;
        Transaction tx = null;
        try {
            session = this.getDocumentDAO().getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Load certificate from file
            X509Certificate cert = Util.readCertificate(certFile);

            // Remove country prefix from request user code, so it can be
            // compared to certificate personal id code more reliably

            // Determine if certificate belongs to same person who executed current query
            String certPersonalIdCode = Util.getSubjectSerialNumberFromCert(cert);
            String userCodeWithoutCountryPrefix = Util.getPersonalIdCodeWithoutCountryPrefix(xroadUser.getUserCode());
            if (!userCodeWithoutCountryPrefix.equalsIgnoreCase(certPersonalIdCode)) {
                logger.info("Attempted to sign document " + documentId + " by person \"" + certPersonalIdCode
                        + "\" while logged in as person \"" + userCodeWithoutCountryPrefix + "\"");
                
                result.setSuccess(false);
                result.setErrorCode("request.prepareSignature.signer.notCurrentUser");
                
                return result;
            }
            
            if (getConfiguration().getDoCheckTestCert()) {
                Boolean isTest = Util.isTestCard(cert);
                if (isTest) {
                    logger.info("Attempted to sign document " + documentId + " by person \"" + certPersonalIdCode + " using test certificate");
                    
                    result.setSuccess(false);
                    result.setErrorCode("request.saveDocument.testcertificate");
                    
                    return result;
                }
            }
            
            // Load document
            Document doc = (Document) session.get(Document.class, documentId);

            // Find signature container (if exists)
            boolean usingContainerDraft = false;
            boolean readExistingContainer = false;          
            
            DocumentFile documentFileWithContainerDraft = findSignatureContainerDraft(doc);
            DocumentFile documentFileWithExistingContainer = findSignatureContainer(doc);
            
            DigitalSigningDTO storedDraftData = null;
            if ((documentFileWithContainerDraft != null) && (documentFileWithContainerDraft.getFileData() != null)
            		&& !isSignatureContainerDraftExpired(documentFileWithContainerDraft)) {
                try {
                	storedDraftData = (DigitalSigningDTO) SerializationUtils.deserialize(documentFileWithContainerDraft.getFileData());
            	} catch (SerializationException e) {
            		logger.error("Could not deserialize stored digital signing draft data", e);
            	}
                
                if (storedDraftData != null && storedDraftData.isDataStored()) {
                	logger.debug("Stored draft data was successfully retrieved");
                	
                	usingContainerDraft = true;
                	readExistingContainer = true;
                }
            } else if ((documentFileWithExistingContainer != null) && (documentFileWithExistingContainer.getFileData() != null)) {
                readExistingContainer = true;
            }

            Container container = null;
            if (!readExistingContainer) {
                logger.debug("Creating new signature container.");
                
                // Using singleton configuration to speed-up signing process
                org.digidoc4j.Configuration configuration = org.digidoc4j.Configuration.getInstance();
                
                if (isBdoc) {
                	container = ContainerBuilder.aContainer(ContainerBuilder.BDOC_CONTAINER_TYPE).withConfiguration(configuration).build();
                } else {
                	container = ContainerBuilder.aContainer(ContainerBuilder.DDOC_CONTAINER_TYPE).withConfiguration(configuration).build();
                }
            } else {
                logger.debug("Loading existing signature container");
                
                if (usingContainerDraft) {
                	container = storedDraftData.getContainer();
                	
                	isBdoc = container.getType().equals(ContainerBuilder.BDOC_CONTAINER_TYPE) ? true : false;
                } else {
                	InputStream containerAsStream = null;
                	try {
                        containerAsStream = new ByteArrayInputStream(documentFileWithExistingContainer.getFileData());
                        isBdoc = Util.isBdocFile(documentFileWithExistingContainer.getFileName());
                        
                        String containerType = isBdoc ? ContainerBuilder.BDOC_CONTAINER_TYPE : ContainerBuilder.DDOC_CONTAINER_TYPE;
                        container = ContainerBuilder.aContainer(containerType).fromStream(containerAsStream).build();
                    } finally {
                    	Util.safeCloseStream(containerAsStream);
                    }
                } 

                // Make sure that document is not already signed by the same person.
                Signature signatureToRemove = null;
                if (container.getSignatures() != null) {
                	for (Signature existingSignature : container.getSignatures()) {
                		if (isPossibleToRemovePendingSignature(existingSignature, userCodeWithoutCountryPrefix)) {
                			signatureToRemove = existingSignature;
                			
                			break;
                		}
                	}
                }

                // If the same person has already given an unconfirmed
                // and now attempts to prepare another signature then
                // let's remove the user's earlier signature.
                if (signatureToRemove != null) {
                    container.removeSignature(signatureToRemove);
                } else if (usingContainerDraft) {
                	DataToSign dataToSign = storedDraftData.getDataToSign();
                	String personIdCode = Util.getSubjectSerialNumberFromCert(dataToSign.getSignatureParameters().getSigningCertificate());
                	
                	boolean sameUserPreparesSignature = userCodeWithoutCountryPrefix.equalsIgnoreCase(personIdCode);
                	if (!sameUserPreparesSignature) {
                		// If someone else has a pending signature then lets break signing
                		// until the other person has completed his/her signing process.
                		long containerDraftRemainingLifetime = getContainerDraftRemainingLifetimeSeconds(documentFileWithContainerDraft);
                		if (containerDraftRemainingLifetime <= 0L) {
                			throw new AditCodedException("request.prepareSignature.documentIsBeingSignedByAnotherUser");
                		} else {
                			if (containerDraftRemainingLifetime > 60) {
                				long minutes = (long) Math.floor((double) containerDraftRemainingLifetime / 60);
                				long seconds = (containerDraftRemainingLifetime - (minutes * 60L));
                				
                				AditCodedException aditCodedException = new AditCodedException(
                						"request.prepareSignature.documentIsBeingSignedByAnotherUser.withEstimate");
                				aditCodedException.setParameters(new Object[]{minutes, seconds});
                				
                				throw aditCodedException;
                			} else {
                				AditCodedException aditCodedException = new AditCodedException(
                						"request.prepareSignature.documentIsBeingSignedByAnotherUser.withEstimateSecondsOnly");
                				aditCodedException.setParameters(new Object[]{containerDraftRemainingLifetime});
                				
                				throw aditCodedException;
                			}
                		}
                	}
                }
            }
            
            if (container.getType().equals(ContainerBuilder.DDOC_CONTAINER_TYPE) && Util.countElements(container.getSignatures()) > 0) {
            	AditCodedException aditCodedException = new AditCodedException("request.prepareSignature.notAllowedToAddSignaturesToDdocContainer");
            	
            	throw aditCodedException;
            }
     
            if ((Util.countElements(container.getDataFiles()) < 1) && (Util.countElements(container.getSignatures()) < 1)) {
            	populateContainerWithDataFiles(container, doc);
            }
            
            if (logger.isDebugEnabled()) {
            	logger.debug("public key algorithm: " + cert.getPublicKey().getAlgorithm());
            }
            
            EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.RSA;
            if (cert.getPublicKey().getAlgorithm().equals("EC") || cert.getPublicKey().getAlgorithm().equals("ECC")) {
            	encryptionAlgorithm = EncryptionAlgorithm.ECDSA;
            }
            
            String signatureId = Util.getNewSignatureId(container);
            
            SignatureBuilder signatureBuilder  = SignatureBuilder.
            		aSignature(container).
            		withSignatureProfile(SignatureProfile.LT_TM).
            		withSignatureId(signatureId).
            		withSigningCertificate(cert).
            		withSignatureDigestAlgorithm(DigestAlgorithm.SHA256).
            		withEncryptionAlgorithm(encryptionAlgorithm).
            		withCountry(country).
            		withStateOrProvince(state).
            		withCity(city).
            		withPostalCode(zip);
            if ((manifest != null) && (manifest.length() > 0)) {
                signatureBuilder.withRoles(manifest);
            }
            
            DataToSign dataToSign = signatureBuilder.buildDataToSign();
            byte[] digest = dataToSign.getDigestToSign();
            
            result.setSignatureHash(Util.convertToHexString(digest));
            result.setSignatureId(dataToSign.getSignatureParameters().getSignatureId());
            result.setDataFileHashes(getListOfDataFileDigests(container));
            
            DigitalSigningDTO digitalSigningDTO = new DigitalSigningDTO();
            digitalSigningDTO.setContainer(container);
            digitalSigningDTO.setDataToSign(dataToSign);
            
            byte[] serializedDigitalSigningData = SerializationUtils.serialize(digitalSigningDTO);
            if (documentFileWithContainerDraft == null) {
            	logger.debug("Creating new document file with container draft");
            	
            	documentFileWithContainerDraft = new DocumentFile();
            	documentFileWithContainerDraft.setContentType(UNKNOWN_MIME_TYPE);
            	documentFileWithContainerDraft.setDeleted(false);
            	documentFileWithContainerDraft.setDocument(doc);
            	documentFileWithContainerDraft.setDocumentFileTypeId(FILETYPE_SIGNATURE_CONTAINER_DRAFT);
            	
            	doc.getDocumentFiles().add(documentFileWithContainerDraft);
            }
            documentFileWithContainerDraft.setFileData(serializedDigitalSigningData);
            documentFileWithContainerDraft.setFileSizeBytes((long)serializedDigitalSigningData.length);
            documentFileWithContainerDraft.setLastModifiedDate(new Date());
            documentFileWithContainerDraft.setGuid(UUID.randomUUID().toString());
            documentFileWithContainerDraft.setFileName(
            		Util.convertToLegalFileName(doc.getTitle(),
            		(isBdoc ? Util.BDOC_PRIMARY_EXTENSION : Util.DDOC_FILE_EXTENSION),
            		null));

            doc.setLocked(true);
            doc.setLockingDate(new Date());

            session.update(doc);

            tx.commit();
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
            }
            
            throw ex;
        } finally {
            if ((session != null) && session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Replaces in file name symbols that can crash DigiDoc library with
     * symbols that will not cause anything to crash.
     *
     * @param originalFileName Original name of file that will be added to DigiDoc container.
     * @return File name with unsafe symbols replaced.
     */
    public static String makeFileNameSafeForDigiDocLibrary(String originalFileName) {
        String result = "";
        if (!Util.isNullOrEmpty(originalFileName)) {
            result = originalFileName.replace("&", "_");
        }
        return result;
    }

    private ArrayOfDataFileHash getListOfDataFileDigests(Container container) throws Exception {
        ArrayOfDataFileHash result = new ArrayOfDataFileHash();
        
        if (container != null && container.getDataFiles() != null) {
            for (DataFile df: container.getDataFiles()) {
                if (df != null) {
                    byte[] fileDigest = df.calculateDigest();
                    String digestAsHexString = Util.convertToHexString(fileDigest);
                    
                    result.getDataFileHash().add(new DataFileHash(df.getId(), digestAsHexString));
                } else {
                    logger.warn("Cannot calculate DataFile hash because DataFile is empty (NULL).");
                }
            }
        } else {
            logger.warn("Cannot build list of DataFile hashes because the supplied DigiDoc container is empty (NULL).");
        }
        
        return result;
    }

    /**
     * Detects if given file can be signed.
     *
     * @param file File to be checked
     * @return {@code true} if given container can be signed
     */
    public boolean isPossibleToSignFile(DocumentFile file) {
        if (file == null) {
            return false;
        }

        if (file.getDocumentFileTypeId() != FILETYPE_DOCUMENT_FILE) {
            return false;
        }

        if ((file.getDeleted() != null) && file.getDeleted()) {
            return false;
        }

        return true;
    }

    /**
     * Returns remaining lifetime of given signature container draft in seconds.
     *
     * @param containerDraft Signature container draft
     * @return Remaining lifetime of given signature container draft in seconds
     */
    public long getContainerDraftRemainingLifetimeSeconds(DocumentFile containerDraft) {
        long result = 0;
        if ((this.configuration.getUnfinishedSignatureLifetimeSeconds() != null)
                && (this.configuration.getUnfinishedSignatureLifetimeSeconds() > 0L)
                && (containerDraft != null)) {

            long containerAgeInSeconds = Util.getDateDiffInMilliseconds(containerDraft.getLastModifiedDate(), new Date()) / 1000L;
            result = this.configuration.getUnfinishedSignatureLifetimeSeconds() - containerAgeInSeconds;
        }
        return result;
    }

    /**
     * Detects if a given signature container draft is expired.
     * Expiration means that signature has been prepared but not confirmed
     * within a time period configured in adit-configuration.xml file.
     *
     * @param containerDraft Signature container draft
     * @return {@code true} if given container is expired
     */
    public boolean isSignatureContainerDraftExpired(DocumentFile containerDraft) {
        long containerAgeInSeconds = Util.getDateDiffInMilliseconds(containerDraft.getLastModifiedDate(), new Date()) / 1000L;
        boolean result =
                (FILETYPE_SIGNATURE_CONTAINER_DRAFT == containerDraft.getDocumentFileTypeId())
                        && (this.configuration.getUnfinishedSignatureLifetimeSeconds() != null)
                        && (this.configuration.getUnfinishedSignatureLifetimeSeconds() > 0L)
                        && ((containerAgeInSeconds < 0)
                        || (containerAgeInSeconds > this.configuration.getUnfinishedSignatureLifetimeSeconds()));
        return result;
    }

    /**
     * Detects if given signature can be safely removed from signature container draft.
     *
     * @param signature Signature to be checked
     * @param userCodeWithoutCountryPrefix Personal ID code of current user without country prefix
     * @return {@code true} if given signature can be safely removed
     */
    private boolean isPossibleToRemovePendingSignature(Signature signature, String userCodeWithoutCountryPrefix) {
        boolean result = false;

        if (signature != null) {
        	if (signature.getSigningCertificate() != null && signature.getSigningCertificate().getX509Certificate() != null) {
        		boolean pendingSignatureBelongsToCurrentUser = userCodeWithoutCountryPrefix.equalsIgnoreCase(
        				Util.getSubjectSerialNumberFromCert(signature.getSigningCertificate().getX509Certificate()));
        		
        		if (pendingSignatureBelongsToCurrentUser) {
        			if (signature.getOCSPCertificate() != null) {
        				throw new AditCodedException("request.prepareSignature.signer.hasAlreadySigned");
        			} else {
        				result = true;
        			}
        		}
        	}
        }

        return result;
    }
    
    
    /**
     * Adds user signature data to pending signature in specified documents
     * signature container. After adding signature to container, gets a
     * confirmation for signature from OCSP service.
     *
     * @param documentId          Document ID specifying which document the users signature
     *                            belongs to
     * @param signatureFileName   Absolute path to file containing users signature
     * @param requestPersonalCode Personal ID code of the person who executed current request
     * @param currentUser         Active user (person or organization)
     * @param digidocConfigFile   Absolute path to DigiDoc configuration file
     * @param temporaryFilesDir   Absolute path to applications temporary files directory
     * @throws Exception
     */
    public void confirmSignature(final long documentId, final String signatureFileName,
                                 final String requestPersonalCode, final AditUser currentUser,
                                 final String temporaryFilesDir) throws Exception {

        Session session = null;
        Transaction tx = null;
        try {
            session = this.getDocumentDAO().getSessionFactory().openSession();
            tx = session.beginTransaction();

            File signatureFile = new File(signatureFileName);
            if (!signatureFile.exists()) {
            	AditCodedException aditCodedException = new AditCodedException("request.confirmSignature.missingSignature");
            	aditCodedException.setParameters(new Object[]{});
            	
            	throw aditCodedException;
            }
            
            Document doc = (Document) session.get(Document.class, documentId);
            
            DigitalSigningDTO storedDraftData = null;
            DocumentFile documentFileWithContainerDraft = findSignatureContainerDraft(doc);
            if (documentFileWithContainerDraft != null && documentFileWithContainerDraft.getFileData() != null) {
            	try {
            		storedDraftData = (DigitalSigningDTO) SerializationUtils.deserialize(documentFileWithContainerDraft.getFileData());
            	} catch (SerializationException e) {
            		logger.error("Could not deserialize stored digital signing draft data", e);
            	}
            }
            
            if (storedDraftData == null || !storedDraftData.isDataStored()) {
                AditCodedException aditCodedException = new AditCodedException("request.confirmSignature.signatureNotPrepared");
                aditCodedException.setParameters(new Object[]{});
                
                throw aditCodedException;
            }
            
            Container container = storedDraftData.getContainer();
            
            boolean isBdoc = container.getType().equals(ContainerBuilder.BDOC_CONTAINER_TYPE) ? true : false;

            byte[] sigValue = new byte[(int) signatureFile.length()];
            FileInputStream fs = null;
            try {
                fs = new FileInputStream(signatureFileName);
                fs.read(sigValue, 0, sigValue.length);
            } catch (IOException ex) {
                logger.error(ex);
                
                AditCodedException aditCodedException = new AditCodedException("request.confirmSignature.errorReadingSignatureFile");
                aditCodedException.setParameters(new Object[]{});
                
                throw aditCodedException;
            } finally {
                Util.safeCloseStream(fs);
            }
            
            // Incoming signature value can be of following 3 types (at least):
            // a) binary signature value
            // b) hex-encoded signature value
            // c) base64 encoded <Signature> element 
            String signatureValueAsString = new String(sigValue, "UTF-8");
            if (!Util.isNullOrEmpty(signatureValueAsString)
                    && !signatureValueAsString.startsWith("<Signature") /*ddoc signature*/
                    && !signatureValueAsString.startsWith("<?xml") /*bdoc signature*/) {
            	
            	// Decode signature value if it is HEX encoded
            	sigValue = convertSignatureValueToByteArray(sigValue);
            }
            
            DataToSign dataToSign = storedDraftData.getDataToSign();
            Signature sig = null;
            try {
            	sig = dataToSign.finalize(sigValue);
            } catch(Exception e) {
            	logger.error("Error finalizing the signature", e);
            	
            	throw new AditCodedException("request.confirmSignature.errorFinalizingSignature");
            }
            
			container.addSignature(sig);
			
			String containerFileName = Util.generateRandomFileNameWithoutExtension();
			containerFileName = temporaryFilesDir + File.separator + containerFileName + "_CSv1.adit";
			
			container.saveAsFile(containerFileName);
            
            // Add signature container to document table
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(containerFileName);
            } catch (FileNotFoundException e) {
                logger.error("Error reading digidoc container file: ", e);
            }
            
            long length = (new File(containerFileName)).length();
            byte[] containerData = new byte[fileInputStream.available()];
            fileInputStream.read(containerData, 0, fileInputStream.available());
            
            
            boolean wasSignedBefore = true;
            DocumentFile signatureContainer = findSignatureContainer(doc);
            if (signatureContainer == null) {
                wasSignedBefore = false;
                
                signatureContainer = new DocumentFile();
                signatureContainer.setContentType(UNKNOWN_MIME_TYPE);
                signatureContainer.setDeleted(false);
                signatureContainer.setDocument(doc);
                signatureContainer.setDocumentFileTypeId(FILETYPE_SIGNATURE_CONTAINER);
                signatureContainer.setGuid(UUID.randomUUID().toString());
                
                String extension = null;
                if (isBdoc) {
                	extension = Util.BDOC_PRIMARY_EXTENSION;
                } else {
                	extension = Util.DDOC_FILE_EXTENSION;
                }
                signatureContainer.setFileName(Util.convertToLegalFileName(doc.getTitle(), extension, null));
                
                doc.getDocumentFiles().add(signatureContainer);
            }
            signatureContainer.setFileSizeBytes(length);
            signatureContainer.setFileData(containerData);
            signatureContainer.setLastModifiedDate(new Date());
            
            doc.setSigned(true);
            // update doc last modified date
            doc.setLastModifiedDate(new Date());

            // Remove container draft contents
            documentFileWithContainerDraft.setFileData(null);
            documentFileWithContainerDraft.setFileSizeBytes(0L);

            // Update document
            session.update(doc);

            // Add signature metadata to signature table
            ee.adit.dao.pojo.Signature aditSig = convertDigiDocSignatureToLocalSignature(sig);
            
            // Verify the signature
            SignatureValidationResult signatureValidationResult = sig.validateSignature();
            if (!signatureValidationResult.isValid()) {
                logger.error("Signature given by " + aditSig.getSignerName() + " was found to be invalid.");
                
                logDigidocVerificationErrors(signatureValidationResult.getErrors());
                AditCodedException aditCodedException = new AditCodedException("digidoc.extract.invalidSignature");
                aditCodedException.setParameters(new Object[]{aditSig.getSignerName()});
                
                throw aditCodedException;
            }
            
            aditSig.setUserCode(currentUser.getUserCode());
            aditSig.setUserName(currentUser.getFullName());
            aditSig.setDocument(doc);
            
            session.save(aditSig);

            // Remove file contents and calculate offsets
            if (!wasSignedBefore) {
                Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc =
                        SimplifiedDigiDocParser.findDigiDocDataFileOffsets(containerFileName, isBdoc, temporaryFilesDir);

                for (DocumentFile file : doc.getDocumentFiles()) {
                    if (isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc)) {
                        removeFileContents(doc.getId(), file, fileOffsetsInDdoc);
                    }
                }
            }

            tx.commit();
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
            }
            
            throw ex;
        } finally {
            if ((session != null) && session.isOpen()) {
                session.close();
            }
        }
    }

	/**
     * Makes sure that given signature value is an unencoded byte array
     * (in contrary to a byte array that represents a HEX string).
     *
     * @param signatureValue Signature value from input
     * @return Normalized signature value
     */
    public byte[] convertSignatureValueToByteArray(byte[] signatureValue) {
        String signatureValueAsString = new String(signatureValue);
        if (Util.isHexString(signatureValueAsString)) {
            return Util.convertHexStringToByteArray(signatureValueAsString);
        } else {
            return signatureValue;
        }
    }

    /**
     * Replaces Signature XML block with given XML text in DigiDoc container.
     *
     * @param ddocContainerFullPath Full path to DigiDoc container to be modified.
     * @param signatureId Id of signature to be replaced
     * @param signatureXml New Signature block
     * @throws IOException Exception is thrown if digidoc container file cannot be found
     *                     or application does not have sufficient rights to read from or write to the file.
     */
    public void replaceSignatureInDigiDocContainer(String ddocContainerFullPath, String signatureId, String signatureXml) throws IOException {

        boolean changesMade = false;
        String containerWorkingCopyFullPath = ddocContainerFullPath + ".tmp";

        BufferedWriter containerWriter = null;
        BufferedReader originalContainerReader = null;
        try {
            containerWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(containerWorkingCopyFullPath), "UTF-8"));
            originalContainerReader = new BufferedReader(new InputStreamReader(new FileInputStream(ddocContainerFullPath), "UTF-8"));

            boolean inTag = false;
            boolean ignoreInput = false;
            char[] currentChar = new char[1];
            StringBuilder tagText = new StringBuilder();

            while (originalContainerReader.read(currentChar, 0, 1) > 0) {
                switch (currentChar[0]) {
                    case '<':
                        tagText.append(currentChar[0]);
                        inTag = true;
                        break;
                    case '>':
                        tagText.append(currentChar[0]);
                        inTag = false;
                        String tagTextString = tagText.toString();
                        if (tagTextString.startsWith("<Signature ")) {
                            String sigId = Util.getAttributeValueFromTag(tagTextString, "Id");
                            if (signatureId.equalsIgnoreCase(sigId)) {
                                containerWriter.write(signatureXml);
                                ignoreInput = true;
                                changesMade = true;
                            } else if (!ignoreInput) {
                                containerWriter.write(tagTextString);
                            }
                        } else if ("</Signature>".equalsIgnoreCase(tagTextString)) {
                            if (ignoreInput) {
                                ignoreInput = false;
                            } else {
                                containerWriter.write(tagTextString);
                            }
                        } else if (!ignoreInput) {
                            containerWriter.write(tagTextString);
                        }
                        tagText = null;
                        tagText = new StringBuilder();
                        break;
                    default:
                        if (inTag) {
                            tagText.append(currentChar[0]);
                        } else if (!ignoreInput) {
                            containerWriter.write(currentChar[0]);
                        }
                        break;
                }
            }
        } finally {
        	Util.safeCloseWriter(containerWriter);
            Util.safeCloseReader(originalContainerReader);
        }

        if (changesMade) {
            Util.deleteFile(ddocContainerFullPath, true);
            
            File oldContainer = new File(ddocContainerFullPath);
            File newContainer = new File(containerWorkingCopyFullPath);
            newContainer.renameTo(oldContainer);
        }
    }

    /**
     * Removes file contents from file record.
     * After removal file contents can only be found from DigiDoc container,
     * using file data offset numbers from file record.
     *
     * @param documentId        ID of document, to which the file belongs to
     * @param file              Document file
     * @param fileOffsetsInDdoc Table containing start and end markers of all known files in
     *                          current DigiDoc container
     * @throws Exception
     */
    private void removeFileContents(long documentId, DocumentFile file,
                                    Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc) throws Exception {

        StartEndOffsetPair offsets = fileOffsetsInDdoc.get(file.getDdocDataFileId());

        String resultMsg = documentFileDAO.removeSignedFileContents(
                documentId, file.getId(), offsets.getStart(), offsets.getEnd());

        Exception fileContentRemovalException = null;
        if ("file_data_already_moved".equalsIgnoreCase(resultMsg)) {
            fileContentRemovalException = new Exception("Cannot remove signed file contents because file contents have already been moved!");
        } else if ("file_is_deleted".equalsIgnoreCase(resultMsg)) {
            fileContentRemovalException = new Exception("Cannot remove signed file contents because file is deleted!");
        } else if ("file_does_not_belong_to_document".equalsIgnoreCase(resultMsg)) {
            fileContentRemovalException = new Exception("Cannot remove signed file contents because file does not belong to current document!");
        } else if ("file_does_not_exist".equalsIgnoreCase(resultMsg)) {
            fileContentRemovalException = new Exception("Cannot remove signed file contents because file does not exist!");
        }

        if (fileContentRemovalException != null) {
            logger.error("Failed removing contents of following file:");
            logger.error("Document ID: " + documentId);
            logger.error("File ID: " + file.getId());
            logger.error("File name: " + file.getFileName());
            logger.error("Is the file deleted? " + file.getDeleted());
            logger.error("File type ID: " + file.getDocumentFileTypeId());
            logger.error("DigiDoc DataFile ID: " + file.getDdocDataFileId());
            logger.error("DigiDoc DataFile start offset: " + file.getDdocDataFileStartOffset());
            logger.error("DigiDoc DataFile end offset: " + file.getDdocDataFileEndOffset());
            logger.error("Is file data in DigiDoc container? " + file.getFileDataInDdoc());
            throw fileContentRemovalException;
        }
    }

    /**
     * Determines if file contents should be removed after document has
     * been successfully digitally signed.
     *
     * @param file              File thats properties are examined to determine if content
     *                          removal is necessary (or even possible)
     * @param fileOffsetsInDdoc Offsets of all known data files in documents DigiDoc container
     * @return {@code true} if given files contents should be removed after
     *         document has been suvvessfully digitally signed
     */
    public static boolean isNecessaryToRemoveFileContentsAfterSigning(
            DocumentFile file, Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc) {

        // Do not allow to delete anything if input parameters are empty.
        if ((file == null) || (fileOffsetsInDdoc == null)) {
            return false;
        }

        // Contents of deleted files are already removed and no attempts
        // should be made to remove them again.
        if ((file.getDeleted() != null) && file.getDeleted()) {
            return false;
        }

        // Only contents of regular files may be removed (as an opposite to
        // contents of signature containers).
        if (file.getDocumentFileTypeId() != FILETYPE_DOCUMENT_FILE) {
            return false;
        }

        // File contents should not be removed if we don't know the files
        // ID in DigiDoc container (and therefore had no clue where to
        // find the contents after they get removed from file record)
        if (Util.isNullOrEmpty(file.getDdocDataFileId())) {
            return false;
        }

        // File contents should not be removed if file data offsets
        // are already set (indicating that contents have already been removed).
        if (((file.getDdocDataFileStartOffset() != null) && (file.getDdocDataFileStartOffset() > 0L))
                || ((file.getDdocDataFileEndOffset() != null) && (file.getDdocDataFileEndOffset() > 0L))) {
            return false;
        }

        // It is safe to remove file contents if we know file data offsets
        // in DigiDoc container.
        if (!fileOffsetsInDdoc.containsKey(file.getDdocDataFileId())) {
            return false;
        }

        // As a precaution - removal of file contents should not be
        // attempted, should the file data offsets be messed up in some
        // obvious way.
        StartEndOffsetPair offsets = fileOffsetsInDdoc.get(file.getDdocDataFileId());
        //in case of BDOC offsets not needed.
        if (offsets.getBdocOrigin()==null || offsets.getBdocOrigin()==false) {
	        if ((offsets == null) || (offsets.getStart() > offsets.getEnd())
	                || (offsets.getStart() <= 0)) {
	            return false;
	        }
        }

        return true;
    }

    /**
     * Creates unsigned DDOC container from given documents files.
     *
     * @param doc               Document the files of which will be added to DDOC container
     * @param digidocConfigFile Full path to DigiDoc library configuration file
     * @param temporaryFilesDir Path to directory that is used to store temporary files
     * @return DDOC container as {@link OutputDocumentFile} instance
     * @throws DigiDoc4JException Will be thrown if DigiDoc library initialization or container
     *                          manipulation fails
     * @throws SQLException     Will be thrown if reading file contents from database fails
     * @throws IOException      Will be thrown if saving DigiDoc container fails
     */
	public static OutputDocumentFile createSignatureContainerFromDocumentFiles(
            final Document doc,
            final String digidocConfigFile,
            final String temporaryFilesDir) throws DigiDoc4JException, SQLException, IOException {

        Container container = ContainerBuilder.aContainer(ContainerBuilder.BDOC_CONTAINER_TYPE).build();

        // Create unique subdirectory for files
        File uniqueDir = new File(temporaryFilesDir + File.separator + doc.getId());
        int uniqueCounter = 0;
        while (uniqueDir.exists()) {
            uniqueDir = new File(temporaryFilesDir + File.separator + doc.getId() + "_" + (++uniqueCounter));
        }
        uniqueDir.mkdir();

        List<DocumentFile> filesList = new ArrayList<DocumentFile>(doc.getDocumentFiles());
        for (DocumentFile docFile : filesList) {
            if (((docFile.getDeleted() == null) || !docFile.getDeleted())
                    && (docFile.getDocumentFileTypeId() == FILETYPE_DOCUMENT_FILE)) {

                String outputFileName = uniqueDir.getAbsolutePath()
                        + File.separator
                        + makeFileNameSafeForDigiDocLibrary(docFile.getFileName());

                InputStream blobDataStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    blobDataStream = new ByteArrayInputStream(docFile.getFileData());
                    fileOutputStream = new FileOutputStream(outputFileName);

                    byte[] buffer = new byte[10240];
                    int len = 0;
                    while ((len = blobDataStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                } finally {
                    Util.safeCloseStream(blobDataStream);
                    Util.safeCloseStream(fileOutputStream);
                }

                // Add file to signature container
               container.addDataFile(new File(outputFileName), docFile.getContentType());
            }
        }

        // Save container to file.
        String containerFileName = Util.generateRandomFileNameWithoutExtension();
        containerFileName = temporaryFilesDir + File.separator + containerFileName + ".adit";
        uniqueCounter = 0;
        while (new File(containerFileName).exists()) {
            containerFileName = temporaryFilesDir + File.separator + containerFileName + uniqueCounter + ".adit";
        }
        
        container.saveAsFile(containerFileName);
        
        long length = (new File(containerFileName)).length();

        OutputDocumentFile result = new OutputDocumentFile();
        result.setContentType(UNKNOWN_MIME_TYPE);
        result.setName(Util.convertToLegalFileName(doc.getTitle(), "bdoc", null));
        result.setFileType(FILETYPE_NAME_SIGNATURE_CONTAINER);
        result.setSizeBytes(length);
        result.setSysTempFile(containerFileName);

        return result;
    }

    /**
     * Creates a ZIP archive from given documents files.
     *
     * @param doc               Document the files of which will be added to ZIP archive
     * @param filesList         List of files to be added to ZIP archive
     * @param temporaryFilesDir Path to directory that is used to store temporary files
     * @return ZIP archive as {@link OutputDocumentFile} instance
     * @throws IOException will be thrown if writing ZIP archive fails for any reason
     */
    public static OutputDocumentFile createZipArchiveFromDocumentFiles(
            final Document doc,
            final List<OutputDocumentFile> filesList,
            final String temporaryFilesDir) throws IOException {

        // Create unique subdirectory for files
        File uniqueDir = new File(temporaryFilesDir + File.separator + doc.getId());
        int uniqueCounter = 0;
        while (uniqueDir.exists()) {
            uniqueDir = new File(temporaryFilesDir + File.separator + doc.getId() + "_" + (++uniqueCounter));
        }
        uniqueDir.mkdir();

        // Create name for ZIP archive
        String archiveFileName = Util.generateRandomFileNameWithoutExtension();
        archiveFileName = temporaryFilesDir + File.separator + archiveFileName + ".adit";
        uniqueCounter = 0;
        while (new File(archiveFileName).exists()) {
            archiveFileName = temporaryFilesDir + File.separator + archiveFileName + uniqueCounter + ".adit";
        }

        FileOutputStream archiveStream = null;
        ZipArchiveOutputStream zipStream = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedFileStream = null;

        try {
            archiveStream = new FileOutputStream(archiveFileName);
            zipStream = new ZipArchiveOutputStream(new BufferedOutputStream(archiveStream));
            zipStream.setEncoding("Cp775");
            zipStream.setFallbackToUTF8(true);
            zipStream.setUseLanguageEncodingFlag(true);
            zipStream.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

            byte[] data = new byte[1024];
            List<String> usedEntryNames = new ArrayList<String>();
            for (OutputDocumentFile docFile : filesList) {
                if (FILETYPE_NAME_DOCUMENT_FILE.equalsIgnoreCase(docFile.getFileType())) {
                    try {
                        fileInputStream = new FileInputStream(Util.base64DecodeFile(docFile.getSysTempFile(), temporaryFilesDir));
                        bufferedFileStream = new BufferedInputStream(fileInputStream, data.length);

                        String extension = Util.getFileExtension(docFile.getName());
                        String fileNameWithoutExtension = Util.getFileNameWithoutExtension(docFile.getName());
                        String entryName = Util.convertToLegalFileName(fileNameWithoutExtension, extension, null);

                        uniqueCounter = 0;
                        while (usedEntryNames.contains(entryName)) {
                            uniqueCounter++;
                            entryName = Util.convertToLegalFileName(fileNameWithoutExtension, extension, String.valueOf(uniqueCounter));
                        }

                        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
                        zipStream.putArchiveEntry(entry);
                        usedEntryNames.add(entryName);

                        int readLength;
                        while ((readLength = bufferedFileStream.read(data, 0, data.length)) != -1) {
                            zipStream.write(data, 0, readLength);
                        }

                        zipStream.closeArchiveEntry();
                    } finally {
                        Util.safeCloseStream(bufferedFileStream);
                        Util.safeCloseStream(fileInputStream);
                    }
                }
            }

            zipStream.finish();
        } finally {
            Util.safeCloseStream(zipStream);
            Util.safeCloseStream(archiveStream);
        }

        long length = (new File(archiveFileName)).length();

        OutputDocumentFile result = new OutputDocumentFile();
        result.setContentType("application/zip");
        result.setName(Util.convertToLegalFileName(doc.getTitle(), "zip", null));
        result.setFileType(FILETYPE_NAME_ZIP_ARCHIVE);
        result.setSizeBytes(length);
        result.setSysTempFile(archiveFileName);

        return result;
    }

    /**
     * Creates instance of {@link ee.adit.dao.pojo.Signature} and populates it
     * with data from given DigiDoc Signature object.
     *
     * @param digiDocSignature DigiDoc4j Signature object
     * @return Local signature object that can be added to document and saved to database
     */
    public ee.adit.dao.pojo.Signature convertDigiDocSignatureToLocalSignature(Signature digiDocSignature) throws AditCodedException {
        ee.adit.dao.pojo.Signature result = new ee.adit.dao.pojo.Signature();

        result.setCity(digiDocSignature.getCity());
        result.setCountry(digiDocSignature.getCountryName());
        result.setCounty(digiDocSignature.getStateOrProvince());
        result.setPostIndex(digiDocSignature.getPostalCode());
        
        if (digiDocSignature.getSignerRoles() != null && digiDocSignature.getSignerRoles().size() > 0) {
        	result.setSignerRole(digiDocSignature.getSignerRoles().get(0));
        }
        
        result.setSigningDate(digiDocSignature.getClaimedSigningTime());

        X509Cert signingCertificate = digiDocSignature.getSigningCertificate();
        if ((signingCertificate != null) && (signingCertificate.getX509Certificate() != null)) {
            X509Certificate cert = signingCertificate.getX509Certificate();
            
            String signerCode = Util.getSubjectSerialNumberFromCert(cert);
            logger.debug("signerCode: " + signerCode);
            
            String signerCountryCode = getSubjectCountryCode(cert);
            logger.debug("signerCountryCode: " + signerCountryCode);
            
            String signerCodeWithCountryPrefix = "EE" + signerCode;
            if (!Util.isNullOrEmpty(signerCode) && !Util.isNullOrEmpty(signerCountryCode)) {
                signerCodeWithCountryPrefix = (signerCode.startsWith(signerCountryCode)) ? signerCode : signerCountryCode + signerCode;
            }
            
			if (getConfiguration().getDoCheckTestCert()) {
			    Boolean isTest = Util.isTestCard(cert);
			    if (isTest) {
			        logger.info("Attempted to sign document by person \"" + signerCodeWithCountryPrefix + " using test certificate");
			        
			        throw new AditCodedException("request.saveDocument.testcertificate");
			    }
			}
            
            result.setSignerCode(signerCodeWithCountryPrefix);
            result.setSignerName(signingCertificate.getSubjectName(SubjectName.SURNAME) + ", " + signingCertificate.getSubjectName(SubjectName.GIVENNAME));

            // Add reference to ADIT user if signer happens to be registered user
            AditUser user = this.getAditUserDAO().getUserByID(signerCodeWithCountryPrefix);
            if (user != null) {
                result.setUserCode(user.getUserCode());
                result.setUserName(user.getFullName());
            }
        }

        return result;
    }

    
    /**
     * Finds signature container from document files list.
     *
     * @param doc Document instance
     * @return Signature container as {@link DocumentFile}. Will return
     *         {@code null} if signature container is not found.
     */
    private DocumentFile findSignatureContainer(Document doc) {
        DocumentFile result = null;

        if ((doc != null) && (doc.getDocumentFiles() != null)) {
            for (DocumentFile file : doc.getDocumentFiles()) {
                if (file.getDocumentFileTypeId() == FILETYPE_SIGNATURE_CONTAINER) {
                    result = file;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Finds signature container draft from document files list.
     *
     * @param doc Document instance
     * @return Signature container draft as {@link DocumentFile}. Will return
     *         {@code null} if signature container draft is not found.
     */
    private DocumentFile findSignatureContainerDraft(Document doc) {
        DocumentFile result = null;

        if ((doc != null) && (doc.getDocumentFiles() != null)) {
            for (DocumentFile file : doc.getDocumentFiles()) {
                if (file.getDocumentFileTypeId() == FILETYPE_SIGNATURE_CONTAINER_DRAFT) {
                    result = file;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Finds certificate subjects country code from X509 certificate.
     *
     * @param cert Certificate
     * @return Country code of certificate subject. Will return {@code null} if
     *         country code is not found in subject data.
     */
    private String getSubjectCountryCode(X509Certificate cert) {
        String result = null;
        if (cert != null) {
            String dn = cert.getSubjectDN().getName();
            int idx1 = dn.indexOf("C=");
            if (idx1 >= 0) {
                idx1 += 2;
                while (idx1 < dn.length() && !Character.isLetter(dn.charAt(idx1))) {
                    idx1++;
                }
                int idx2 = idx1;
                while (idx2 < dn.length() && dn.charAt(idx2) != ',' && dn.charAt(idx2) != '/') {
                    idx2++;
                }
                result = dn.substring(idx1, idx2);
            }
        }
        return result;
    }

    /**
     * Gets Document File ID by document and file guid
     *
     * @param doc
     * @param documentFileGuid
     * @return document file ID
     */
    public long getDocumentFileIdByGuid(Document doc, String documentFileGuid) {
        return this.getDocumentFileDAO().getDocumentFileIdByGuid(doc, documentFileGuid).getId();
    }

    /**
     * Translates file type name to file type ID.
     *
     * @param fileTypeName Name of file type
     * @return ID of file type
     */
    public static long resolveFileTypeId(String fileTypeName) {
        long result = FILETYPE_DOCUMENT_FILE;

        if (FILETYPE_NAME_SIGNATURE_CONTAINER.equalsIgnoreCase(fileTypeName)) {
            result = FILETYPE_SIGNATURE_CONTAINER;
        } else if (FILETYPE_NAME_SIGNATURE_CONTAINER_DRAFT.equalsIgnoreCase(fileTypeName)) {
            result = FILETYPE_SIGNATURE_CONTAINER_DRAFT;
        } else if (FILETYPE_NAME_ZIP_ARCHIVE.equalsIgnoreCase(fileTypeName)) {
            result = FILETYPE_ZIP_ARCHIVE;
        }

        return result;
    }

    /**
     * Translates file type ID to file type name.
     *
     * @param fileTypeId ID of file type
     * @return Name of file type
     */
    public static String resolveFileTypeName(Long fileTypeId) {
        String result = FILETYPE_NAME_DOCUMENT_FILE;

        if (FILETYPE_SIGNATURE_CONTAINER == fileTypeId) {
            result = FILETYPE_NAME_SIGNATURE_CONTAINER;
        } else if (FILETYPE_SIGNATURE_CONTAINER_DRAFT == fileTypeId) {
            result = FILETYPE_NAME_SIGNATURE_CONTAINER_DRAFT;
        }

        return result;
    }

    /**
     * Helper method to determine if document has been shared to given user.
     *
     * @param documentSharings List of documents sharing records
     * @param userCode         Code of user
     * @return {@code true} if document has been shared to given person
     */
    public static boolean documentSharingExists(Set<DocumentSharing> documentSharings, String userCode) {
        boolean result = false;

        if ((documentSharings != null) && (!documentSharings.isEmpty())) {
            Iterator<DocumentSharing> it = documentSharings.iterator();
            while (it.hasNext()) {
                DocumentSharing sharing = it.next();
                if (sharing.getUserCode() != null && userCode.equalsIgnoreCase(sharing.getUserCode())
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
     * Helper method to determine if document has been signed by given user.
     *
     * @param doc     Document
     * @param sharing Document sharing record
     * @return {@code true} if document has been signed by person in the sharing object.
     */
    public static boolean documentSignedBySharing(Set<ee.adit.dao.pojo.Signature> documentSignatures, DocumentSharing sharing) {
        boolean result = false;

        Iterator<ee.adit.dao.pojo.Signature> it = documentSignatures.iterator();
        while (it.hasNext()) {
            ee.adit.dao.pojo.Signature signature = it.next();
            if (sharing.getUserCode().equalsIgnoreCase(signature.getUserCode()) && sharing.getCreationDate().compareTo(signature.getSigningDate()) < 0) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    
    /**
     * Helper method to determine if document has been sent to given user.
     *
     * @param documentSharings List of documents sharing records
     * @param userCode         Code of user
     * @return {@code true} if document has been sent to given person
     */
    public static boolean documentSendingExists(Set<DocumentSharing> documentSharings, String userCode) {
        boolean result = false;

        if ((documentSharings != null) && (!documentSharings.isEmpty())) {
            Iterator<DocumentSharing> it = documentSharings.iterator();
            while (it.hasNext()) {
                DocumentSharing sharing = it.next();
                if (userCode.equalsIgnoreCase(sharing.getUserCode())
                        && (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SEND_ADIT)
                        || sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SEND_DVK))) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
    
    
    /**
     * Helper method to determine if file with given type ID should be returned
     * when given list of file types was requested.
     *
     * @param fileTypeId     ID of file type
     * @param requestedTypes List of file types that were requested by user
     * @return {@code true} if file having given type ID should be returned
     */
    public static boolean fileIsOfRequestedType(long fileTypeId, ArrayOfFileType requestedTypes) {
        String fileTypeName = resolveFileTypeName(fileTypeId);
        return ((requestedTypes == null)
                || (requestedTypes.getFileType() == null)
                || (requestedTypes.getFileType().size() < 1)
                || (requestedTypes.getFileType().contains(fileTypeName)));
    }

    public Long findDocumentDvkIdForUser(Document doc, AditUser user) {
        Long result = null;
        if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
            Iterator<DocumentSharing> it = doc.getDocumentSharings().iterator();
            while (it.hasNext()) {
                DocumentSharing sharing = it.next();
                if (sharing.getUserCode().equalsIgnoreCase(user.getUserCode())) {
                    // Check whether the document is marked as deleted by recipient
                    if ((sharing.getDeleted() == null) || !sharing.getDeleted()) {
                        result = sharing.getDvkId();
                        break;
                    }

                }
            }
        }
        return result;
    }
    
    private void populateContainerWithDataFiles(Container container, Document doc) {
    	if (container == null) {
    		throw new IllegalArgumentException("Container must not be null");
    	}
    	
    	if (doc.getDocumentFiles() != null) {
    		Map <String, String> documentFileNames = new HashMap<String, String>();
    		
    		for (DocumentFile documentFile : doc.getDocumentFiles()) {
    			if (isPossibleToSignFile(documentFile)) {
    				if (container.getType().equals(ContainerBuilder.BDOC_CONTAINER_TYPE) && documentFileNames.get(documentFile.getFileName()) != null) {
                		String fileName = Util.getFileNameWithoutExtension(documentFile.getFileName());
                		String extension = Util.getFileExtension(documentFile.getFileName());
                		documentFile.setFileName(fileName + "_" + documentFile.getId() + "." + extension);
                	}
    				documentFileNames.put(documentFile.getFileName(), documentFile.getFileName());
    				
    				DataFile dataFile = new DataFile(documentFile.getFileData(), documentFile.getFileName(), documentFile.getContentType());
    				documentFile.setDdocDataFileId(dataFile.getId());
    				
    				container.addDataFile(dataFile);
    			}
    		}
    	}
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public DocumentTypeDAO getDocumentTypeDAO() {
        return documentTypeDAO;
    }

    public void setDocumentTypeDAO(DocumentTypeDAO documentTypeDAO) {
        this.documentTypeDAO = documentTypeDAO;
    }

    public DocumentDAO getDocumentDAO() {
        return documentDAO;
    }

    public void setDocumentDAO(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    public DocumentFileDAO getDocumentFileDAO() {
        return documentFileDAO;
    }

    public void setDocumentFileDAO(DocumentFileDAO documentFileDAO) {
        this.documentFileDAO = documentFileDAO;
    }

    public DocumentWfStatusDAO getDocumentWfStatusDAO() {
        return documentWfStatusDAO;
    }

    public void setDocumentWfStatusDAO(DocumentWfStatusDAO documentWfStatusDAO) {
        this.documentWfStatusDAO = documentWfStatusDAO;
    }

    public DocumentSharingDAO getDocumentSharingDAO() {
        return documentSharingDAO;
    }

    public void setDocumentSharingDAO(DocumentSharingDAO documentSharingDAO) {
        this.documentSharingDAO = documentSharingDAO;
    }

    public DocumentHistoryDAO getDocumentHistoryDAO() {
        return documentHistoryDAO;
    }

    public void setDocumentHistoryDAO(DocumentHistoryDAO documentHistoryDAO) {
        this.documentHistoryDAO = documentHistoryDAO;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public AditUserDAO getAditUserDAO() {
        return aditUserDAO;
    }

    public void setAditUserDAO(AditUserDAO aditUserDAO) {
        this.aditUserDAO = aditUserDAO;
    }

    public DvkDAO getDvkDAO() {
        return dvkDAO;
    }

    public void setDvkDAO(DvkDAO dvkDAO) {
        this.dvkDAO = dvkDAO;
    }


	public NotificationService getNotificationService() {
		return notificationService;
	}


	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

}
