package ee.adit.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.Writer;
import java.security.cert.X509Certificate;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import dvk.api.container.v2.ContainerVer2;
import dvk.api.container.v2.Fail;
import dvk.api.container.v2.FailideKonteiner;
import dvk.api.container.v2.MetaManual;
import dvk.api.container.v2.Metainfo;
import dvk.api.container.v2.Saaja;
import dvk.api.container.v2.Saatja;
import dvk.api.container.v2.SaatjaKontekst;
import dvk.api.container.v2.Transport;
import dvk.api.ml.PojoMessage;
import dvk.api.ml.PojoMessageRecipient;
import dvk.api.ml.PojoSettings;
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
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfFileType;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.PrepareSignatureInternalResult;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.util.Configuration;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.DigiDocExtractionResult;
import ee.adit.util.SimplifiedDigiDocParser;
import ee.adit.util.StartEndOffsetPair;
import ee.adit.util.Util;
import ee.sk.digidoc.CertValue;
import ee.sk.digidoc.DataFile;
import ee.sk.digidoc.Signature;
import ee.sk.digidoc.SignatureProductionPlace;
import ee.sk.digidoc.SignatureValue;
import ee.sk.digidoc.SignedDoc;
import ee.sk.digidoc.factory.SAXDigiDocFactory;
import ee.sk.utils.ConfigManager;

/**
 * Implements business logic for document processing. Provides methods for
 * processing documents (saving, retrieving, performing checks, etc.). Where
 * possible, the actual data queries are forwarded to DAO classes.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentService {

    /**
     * Document sharing type code - sign
     */
    public static final String SHARINGTYPE_SIGN = "sign";

    /**
     * Document sharing type code - share
     */
    public static final String SHARINGTYPE_SHARE = "share";

    /**
     * Document sharing type code - send using DVK
     */
    public static final String SHARINGTYPE_SEND_DVK = "send_dvk";

    /**
     * Document sharing type code - send using ADIT
     */
    public static final String SHARINGTYPE_SEND_ADIT = "send_adit";

    /**
     * Document DVK status - missing
     */
    public static final Long DVK_STATUS_MISSING = new Long(0);

    /**
     * Document DVK status - waiting
     */
    public static final Long DVK_STATUS_WAITING = new Long(1);

    /**
     * Document DVK status - sending
     */
    public static final Long DVK_STATUS_SENDING = new Long(2);

    /**
     * Document DVK status - sent
     */
    public static final Long DVK_STATUS_SENT = new Long(3);

    /**
     * Document DVK status - aborted
     */
    public static final Long DVK_STATUS_ABORTED = new Long(4);

    /**
     * Document DVK status - received
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
     * Document history type code - create
     */
    public static final String HISTORY_TYPE_CREATE = "create";

    /**
     * Document history type code - modify
     */
    public static final String HISTORY_TYPE_MODIFY = "modify";

    /**
     * Document history type code - add file
     */
    public static final String HISTORY_TYPE_ADD_FILE = "add_file";

    /**
     * Document history type code - modify file
     */
    public static final String HISTORY_TYPE_MODIFY_FILE = "modify_file";

    /**
     * Document history type code - delete file
     */
    public static final String HISTORY_TYPE_DELETE_FILE = "delete_file";

    /**
     * Document history type code - modify status
     */
    public static final String HISTORY_TYPE_MODIFY_STATUS = "modify_status";

    /**
     * Document history type code - send
     */
    public static final String HISTORY_TYPE_SEND = "send";

    /**
     * Document history type code - share
     */
    public static final String HISTORY_TYPE_SHARE = "share";

    /**
     * Document history type code - unshare
     */
    public static final String HISTORY_TYPE_UNSHARE = "unshare";

    /**
     * Document history type code - lock
     */
    public static final String HISTORY_TYPE_LOCK = "lock";

    /**
     * Document history type code - unlock
     */
    public static final String HISTORY_TYPE_UNLOCK = "unlock";

    /**
     * Document history type code - deflate
     */
    public static final String HISTORY_TYPE_DEFLATE = "deflate";

    /**
     * Document history type code - sign
     */
    public static final String HISTORY_TYPE_SIGN = "sign";

    /**
     * Document history type code - delete
     */
    public static final String HISTORY_TYPE_DELETE = "delete";

    /**
     * Document history type code - mark viewed
     */
    public static final String HISTORY_TYPE_MARK_VIEWED = "mark_viewed";
    
    /**
     * Document history type code - extract file
     */
    public static final String HISTORY_TYPE_EXTRACT_FILE = "extract_file";

    /**
     * DVK container version used when sending documents using DVK
     */
    public static final int DVK_CONTAINER_VERSION = 2;

    /**
     * DVK response message title
     */
    public static final String DVK_ERROR_RESPONSE_MESSAGE_TITLE = "ADIT vastuskiri";

    /**
     * DVK response message file name
     */
    public static final String DVK_ERROR_RESPONSE_MESSAGE_FILENAME = "ADIT_vastuskiri.pdf";

    /**
     * Document type - letter
     */
    public static final String DOCTYPE_LETTER = "letter";

    /**
     * Document type - application
     */
    public static final String DOCTYPE_APPLICATION = "application";

    /**
     * DVK receive fail reason - user does not exist
     */
    public static final Integer DVK_RECEIVE_FAIL_REASON_USER_DOES_NOT_EXIST = 1;

    /**
     * DVK receive fail reason - user uses DVK to exchange documents
     */
    public static final Integer DVK_RECEIVE_FAIL_REASON_USER_USES_DVK = 2;

    // Document history description literals
    /**
     * Document history description - CREATE
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_CREATE = "Document created";
    
    /**
     * Document history description - LOCK
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_LOCK = "Document locked";
    
    /**
     * Document history description - DEFLATE
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_DEFLATE = "Document deflated";
    
    /**
     * Document history description - DELETE
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_DELETE = "Document deleted";
    
    /**
     * Document history description - DELETE FILE
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_DELETEFILE = "Document file deleted. ID: ";
    
    /**
     * Document history description - MODIFY STATUS
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_MODIFYSTATUS = "Document status modified to: ";
    
    /**
     * Document history description - MODIFY
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_MODIFY = "Document modified";
    
    /**
     * Document history description - MODIFY FILE
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_MODIFYFILE = "Document file modified. ID: ";
    
    /**
     * Document history type code - extract file
     */
    public static final String DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE = "Files extracted from digital signature container";
    
	public static final long FILETYPE_DOCUMENT_FILE = 1L;
	public static final long FILETYPE_SIGNATURE_CONTAINER = 2L;
	public static final long FILETYPE_SIGNATURE_CONTAINER_DRAFT = 3L;
	public static final String FILETYPE_NAME_DOCUMENT_FILE = "document_file";
	public static final String FILETYPE_NAME_SIGNATURE_CONTAINER = "signature_container";
	public static final String FILETYPE_NAME_SIGNATURE_CONTAINER_DRAFT = "signature_container_draft";

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

    /**
     * Checks if document metadata is sufficient and correct for creating a new
     * document.
     * 
     * @param document
     *            document metadata
     * @throws AditCodedException
     *             if metadata is insuffidient or incorrect
     */
    public void checkAttachedDocumentMetadataForNewDocument(SaveDocumentRequestAttachment document)
            throws AditCodedException {
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
                    aditCodedException.setParameters(new Object[] {validDocumentTypes });
                    throw aditCodedException;
                }

            } else {
                String validDocumentTypes = getValidDocumentTypes();
                AditCodedException aditCodedException = new AditCodedException(
                        "request.saveDocument.document.type.undefined");
                aditCodedException.setParameters(new Object[] {validDocumentTypes });
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
                    aditCodedException.setParameters(new Object[] {document.getPreviousDocumentID().toString() });
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
     * @param documentId
     *            document ID
     * @param fileId
     *            file ID
     * @param markDeleted
     * @param failIfSignature
     * 		If true then method will throw an error while attempting to delete
     * 		digital signature container.
     * @return Deflation result code.
     */
    public String deflateDocumentFile(long documentId, long fileId, boolean markDeleted, boolean failIfSignature) {
        return this.getDocumentFileDAO().deflateDocumentFile(documentId, fileId, markDeleted, failIfSignature);
    }

    /**
     * Saves a document using the request attachment.
     * 
     * @param attachmentDocument
     *            document as an attachment
     * @param creatorCode
     *            document creator code
     * @param remoteApplication
     *            remote application name
     * @param remainingDiskQuota
     *            disk quota remaining for this user
     * @param creatorName
     *            The code of the user that saves the document (present time name)
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
            throws FileNotFoundException {
        
    	final DocumentDAO docDao = this.getDocumentDAO();

        return (SaveItemInternalResult) this.getDocumentDAO().getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
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
                
                if ((attachmentDocument.getFiles() != null) && (attachmentDocument.getFiles().size() == 1)
                	&& ((document.getDocumentFiles() == null) || (document.getDocumentFiles().size() == 0))) {
                	OutputDocumentFile file = attachmentDocument.getFiles().get(0);
                	
                    String extension = Util.getFileExtension(file.getName()); 
                    
                    // If first added file happens to be a DigiDoc container then
                    // extract files and signatures from container. Otherwise add
                    // container as a regular file.
                    if (((file.getId() == null) || (file.getId() <= 0)) && "ddoc".equalsIgnoreCase(extension)) {
                    	DigiDocExtractionResult extractionResult = extractDigiDocContainer(file.getSysTempFile(), digidocConfigFile);
                    	if (extractionResult.isSuccess()) {
                    		file.setFileType(FILETYPE_NAME_SIGNATURE_CONTAINER);
                    		document.setSigned(true);
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
     * @param documentId
     *            Document ID
     * @param file
     *            File as {@link OutputDocumentFile} object
     * @param remainingDiskQuota
     *            Remaining disk quota of current user (in bytes)
     * @param temporaryFilesDir
     *            Absolute path to temporary files directory
     * @return Result of save as {@link SaveItemInternalResult} object.
     */
    public SaveItemInternalResult saveDocumentFile(
    		final long documentId,
    		final OutputDocumentFile file,
            final long remainingDiskQuota,
            final String temporaryFilesDir,
            final String digidocConfigFile) {
        
    	final DocumentDAO docDao = this.getDocumentDAO();

        return (SaveItemInternalResult) this.getDocumentDAO().getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                SaveItemInternalResult result = new SaveItemInternalResult();
                
                Document document = (Document) session.get(Document.class, documentId);
                
                // Remember highest ID of existing files.
                // This is useful later to find out which file was added.
                long maxId = -1;
                if ((document != null) && (document.getDocumentFiles() != null)) {
                    Iterator it = document.getDocumentFiles().iterator();
                    if (it != null) {
                        while (it.hasNext()) {
                            DocumentFile f = (DocumentFile) it.next();
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
                if ((maxId <= 0) && "ddoc".equalsIgnoreCase(extension)) {
                	 DigiDocExtractionResult extractionResult = extractDigiDocContainer(file.getSysTempFile(), digidocConfigFile);
                	 if (extractionResult.isSuccess()) {
                		 file.setFileType(FILETYPE_NAME_SIGNATURE_CONTAINER);
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
                	 } else {
                		 filesList.add(file);
                	 }
                } else {
                	filesList.add(file);
                }
                
                // Document to database
                try {
                    result = docDao.save(document, filesList, remainingDiskQuota, session);
                } catch (Exception e) {
                    throw new HibernateException(e);
                }

                long fileId = 0;
                if ((file.getId() != null) && (file.getId() > 0)) {
                    fileId = file.getId();
                    logger.debug("Existing file saved with ID: " + fileId);
                } else if ((document != null) && (document.getDocumentFiles() != null)) {
                    Iterator it = document.getDocumentFiles().iterator();
                    if (it != null) {
                        while (it.hasNext()) {
                            DocumentFile f = (DocumentFile) it.next();
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
     * Extracts data files and signature data from DigiDoc container. 
     * 
     * @param pathToContainer
     * 		Absolute path of DigiDoc container
     * @param digidocConfigFile
     * 		Absolute path of DigiDoc library configuration file
     * @return
     * 		File and signature meta-data wrapped into
     * 		{@link DigiDocExtractionResult} object.
     */
    private DigiDocExtractionResult extractDigiDocContainer(final String pathToContainer, final String digidocConfigFile) {
    	DigiDocExtractionResult result = new DigiDocExtractionResult();
    	
    	if (!Util.isNullOrEmpty(pathToContainer)) {
    		File digiDocContainer = new File(pathToContainer);
    		if (digiDocContainer.exists()) {
    			try {
	    			ConfigManager.init(digidocConfigFile);
	    			SAXDigiDocFactory factory = new SAXDigiDocFactory();
	                SignedDoc ddocContainer = factory.readSignedDoc(pathToContainer);
	                
	                int dataFilesCount = ddocContainer.countDataFiles();
	                if (dataFilesCount > 0) {
	                	Hashtable<String, StartEndOffsetPair> dataFileOffsets = SimplifiedDigiDocParser.findDigiDocDataFileOffsets(pathToContainer);
		                for (int i = 0; i < dataFilesCount; i++) {
		                	DataFile ddocDataFile = ddocContainer.getDataFile(i);
		                	DocumentFile localFile = new DocumentFile();
		                	localFile.setDeleted(false);
		                	localFile.setContentType(ddocDataFile.getMimeType());
		                	localFile.setFileName(ddocDataFile.getFileName());
		                	localFile.setFileSizeBytes(ddocDataFile.getSize());
		                	//localFile.setFileData();  // TODO: MD5 hash
		                	
		                	StartEndOffsetPair currentFileOffsets = dataFileOffsets.get(ddocDataFile.getId());
		                	if (currentFileOffsets != null) {
		                		localFile.setDdocDataFileId(ddocDataFile.getId());
		                		localFile.setDdocDataFileStartOffset(currentFileOffsets.getStart());
		                		localFile.setDdocDataFileEndOffset(currentFileOffsets.getEnd());
		                	} else {
		                		// TODO: Veateade?
		                	}
		                	
		                	result.getFiles().add(localFile);
		                }
	                }
	                
	                int signaturesCount = ddocContainer.countSignatures();
	                logger.info("Extracted file contains "+ signaturesCount +" signatures.");
	                if (signaturesCount > 0) {
	                	for (int i = 0; i < signaturesCount; i++) {
	                		Signature ddocSignature = ddocContainer.getSignature(i);
	                        ee.adit.dao.pojo.Signature localSignature = convertDigiDocSignatureToLocalSignature(ddocSignature);
	                        logger.info("Extracted signature of " + localSignature.getSignerName());
	                        result.getSignatures().add(localSignature);
	                	}
	                }
	                result.setSuccess(true);
    			} catch (Exception ex) {
    				logger.error(ex);
    				result.setSuccess(false);
    				result.getFiles().clear();
    				result.getSignatures().clear();
    			}
    		} else {
    			logger.warn("DigiDoc container extraction failed because container file \""+ pathToContainer +"\" does not exist!");
    		}
    	} else {
    		logger.warn("DigiDoc container extraction failed because container file was not supplied!");
    	}
    	
    	return result;
    }

    /**
     * Saves document considering the disk quota for the user.
     * 
     * @param doc
     *            document
     * @param remainingDiskQuota
     *            remaining disk quota for user
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
     * @param document
     *            the document to be locked.
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
     * @param document
     *            document
     * @param recipient
     *            user
     * @return true, if sending succeeded
     */
    public boolean sendDocument(Document document, AditUser recipient) {
        boolean result = false;

        DocumentSharing documentSharing = new DocumentSharing();
        documentSharing.setDocumentId(document.getId());
        documentSharing.setCreationDate(new Date());

        if (recipient.getDvkOrgCode() != null && !"".equalsIgnoreCase(recipient.getDvkOrgCode().trim())) {
            documentSharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SEND_DVK);
        } else {
            documentSharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SEND_ADIT);
        }

        documentSharing.setUserCode(recipient.getUserCode());
        documentSharing.setUserName(recipient.getFullName());

        this.getDocumentSharingDAO().save(documentSharing);

        if (documentSharing.getId() == 0) {
            throw new AditInternalException("Could not add document sharing information to database.");
        }

        return result;
    }

    /**
     * Adds a document history event.
     * 
     * @param applicationName
     *            remote application short name
     * @param doc
     *            document
     * @param userCode
     *            user code - the user that caused this event
     * @param historyType
     *            history event type name
     * @param xteeUserCode
     *            X-Tee user code
     * @param xteeUserName
     *            X-Tee user name
     * @param description
     *            event description
     */
    public void addHistoryEvent(String applicationName, Document doc, String userCode, String historyType,
            String xteeUserCode, String xteeUserName, String description, String userName) {
        // Add history event
        DocumentHistory documentHistory = new DocumentHistory();
        documentHistory.setRemoteApplicationName(applicationName);
        documentHistory.setDocumentId(doc.getId());
        documentHistory.setDocumentHistoryType(historyType);
        documentHistory.setEventDate(new Date());
        documentHistory.setUserCode(userCode);
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
    @SuppressWarnings("unchecked")
    public int sendDocumentsToDVK() {
        int result = 0;

        final String sqlQuery = "select doc from Document doc, DocumentSharing docSharing where docSharing.documentSharingType = 'send_dvk' and (docSharing.documentDvkStatus is null or docSharing.documentDvkStatus = "
                + DocumentService.DVK_STATUS_MISSING + ") and docSharing.documentId = doc.id";

        final String tempDir = this.getConfiguration().getTempDir();

        logger.info("Starting to send documents to DVK. Using org.code '" + this.getConfiguration().getDvkOrgCode() + "'");

        logger.debug("Fetching documents for sending to DVK...");
        Session session = this.getDocumentDAO().getSessionFactory().openSession();

        Query query = session.createQuery(sqlQuery);
        List<Document> documents = query.list();

        logger.debug("Documents fetched successfully (" + documents.size() + ")");

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

                Iterator<DocumentSharing> documentSharings = document.getDocumentSharings().iterator();

                while (documentSharings.hasNext()) {
                    DocumentSharing documentSharing = documentSharings.next();

                    if (DocumentService.SHARINGTYPE_SEND_DVK.equalsIgnoreCase(documentSharing.getDocumentSharingType())
                            && (DocumentService.DVK_STATUS_WAITING.equals(documentSharing.getDocumentDvkStatus())
                                    || DocumentService.DVK_STATUS_MISSING.equals(documentSharing.getDocumentDvkStatus()) || documentSharing
                                    .getDocumentDvkStatus() == null)) {

                        AditUser recipient = this.getAditUserDAO().getUserByID(documentSharing.getUserCode());

                        Saaja saaja = new Saaja();
                        saaja.setRegNr(recipient.getDvkOrgCode());
                        saaja.setIsikukood(recipient.getUserCode());
                        saaja.setNimi(recipient.getFullName());

                        saajad.add(saaja);
                    }

                }

                transport.setSaajad(saajad);
                dvkContainer.setTransport(transport);

                FailideKonteiner failideKonteiner = new FailideKonteiner();

                Set aditFiles = document.getDocumentFiles();
                List dvkFiles = new ArrayList();

                Iterator aditFilesIterator = aditFiles.iterator();
                short count = 0;

                // Convert the adit file to dvk file
                while (aditFilesIterator.hasNext()) {
                    DocumentFile f = (DocumentFile) aditFilesIterator.next();
                    
                    if (((document.getSigned() != null) && document.getSigned() && (f.getDocumentFileTypeId() == FILETYPE_SIGNATURE_CONTAINER))
                    	|| (((document.getSigned() == null) || !document.getSigned()) && (f.getDocumentFileTypeId() == FILETYPE_DOCUMENT_FILE))) {
	                    Fail dvkFile = new Fail();
	
	                    logger.debug("FileName: " + f.getFileName());
	
	                    // Create a temporary file from the ADIT file and
	                    // add a reference to the DVK file
	                    try {
	                    	InputStream inputStream = f.getFileData().getBinaryStream();
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
                Transaction dvkTransaction = dvkSession.beginTransaction();

                try {
                    PojoMessage dvkMessage = new PojoMessage();
                    dvkMessage.setIsIncoming(false);
                    dvkMessage.setTitle(document.getTitle());

                    // Get sender org code
                    String documentOwnerCode = document.getCreatorCode();

                    AditUser documentOwner = (AditUser) session.get(AditUser.class, documentOwnerCode);

                    dvkMessage.setSenderOrgCode(documentOwner.getDvkOrgCode());
                    dvkMessage.setSenderPersonCode(documentOwner.getUserCode());
                    dvkMessage.setSenderName(documentOwner.getFullName());
                    dvkMessage.setDhlGuid(document.getGuid());
                    dvkMessage.setSendingStatusId(DocumentService.DVK_STATUS_WAITING);

                    // Insert data as stream
                    Clob clob = Hibernate.createClob(" ", dvkSession);
                    dvkMessage.setData(clob);

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
                Transaction dvkTransaction2 = dvkSession2.beginTransaction();

                try {
                    // Select the record for update
                    PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2.load(PojoMessage.class, dvkMessageID, LockMode.UPGRADE);

                    // Write the DVK Container to temporary file
                    String temporaryFile = this.getConfiguration().getTempDir() + File.separator
                            + Util.generateRandomFileName();
                    dvkContainer.save2File(temporaryFile);

                    // Write the temporary file to the database
                    InputStream is = new FileInputStream(temporaryFile);
                    Writer clobWriter = dvkMessageToUpdate.getData().setCharacterStream(1);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        clobWriter.write(new String(buf, 0, len, "UTF-8"));
                    }
                    is.close();
                    clobWriter.close();

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

        if (session != null) {
            session.close();
        }

        return result;
    }

    /**
     * Transfers incoming documents from DVK Client database to ADIT database.
     * 
     * Note: DVK stores the recipient status in the DHL_MESSAGE table (field
     * RECIPIENT_STATUS_ID). The situation where two recipients from the same
     * institution receive the same document, is not allowed (or at least DVK
     * stores only one status ID for this document).
     * 
     * @return the number of documents received
     */
    @Transactional
    public int receiveDocumentsFromDVK(String digidocConfigFile) {
        int result = 0;

        try {

            // Fetch all incoming documents from DVK Client database which have
            // the required status - "sending" (recipient_status_id = "101" or
            // "1");
            logger.info("Fetching documents from DVK Client database.");
            List<PojoMessage> dvkDocuments = this.getDvkDAO().getIncomingDocuments();

            if (dvkDocuments != null && dvkDocuments.size() > 0) {

                logger.debug("Found " + dvkDocuments.size());
                Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments.iterator();

                while (dvkDocumentsIterator.hasNext()) {
                    PojoMessage dvkDocument = dvkDocumentsIterator.next();

                    // Get the DVK Container
                    ContainerVer2 dvkContainer = this.getDVKContainer(dvkDocument);

                    List<Saaja> recipients = dvkContainer.getTransport().getSaajad();

                    if (recipients != null && recipients.size() > 0) {
                        logger.debug("Recipients for this message: " + recipients.size());

                        // For every recipient - check if registered in ADIT
                        Iterator<Saaja> recipientsIterator = recipients.iterator();

                        while (recipientsIterator.hasNext()) {
                            Saaja recipient = recipientsIterator.next();

                            logger.info("Recipient: " + recipient.getRegNr() + " (" + recipient.getAsutuseNimi()
                                    + "). Isikukood: '" + recipient.getIsikukood() + "'.");

                            // The ADIT internal recipient is always marked by
                            // the field <isikukood> in the DVK container,
                            // regardless if it is actually a person or an
                            // institution / company.
                            if (recipient.getRegNr() != null && !recipient.getRegNr().equalsIgnoreCase("")) {
                                if (recipient.getIsikukood() != null && !recipient.getIsikukood().equals("")) {
                                    // The recipient is specified - check if
                                    // it's a DVK user

                                    logger.debug("Getting AditUser by personal code: " + recipient.getIsikukood().trim());
                                    AditUser user = this.getAditUserDAO().getUserByID(recipient.getIsikukood().trim());

                                    if (user != null && user.getActive()) {

                                        // Check if user uses DVK
                                        if (user.getDvkOrgCode() != null && !user.getDvkOrgCode().equalsIgnoreCase("")) {
                                            // The user uses DVK - this is not
                                            // allowed. Users that use DVK have
                                            // to exchange
                                            // documents with other users that
                                            // use DVK, over DVK.
                                            this.composeErrorResponse(DocumentService.DVK_RECEIVE_FAIL_REASON_USER_USES_DVK,
                                                    dvkContainer, recipient.getIsikukood().trim(), dvkDocument
                                                            .getReceivedDate(), recipient.getNimi());
                                            throw new AditInternalException("User uses DVK - not allowed.");
                                        }

                                        logger.debug("Constructing ADIT message");
                                        // Add document for this recipient to
                                        // ADIT database
                                        Document aditDocument = new Document();
                                        aditDocument.setCreationDate(new Date());
                                        aditDocument.setDocumentDvkStatusId(DVK_STATUS_SENT);
                                        aditDocument.setDvkId(dvkDocument.getDhlId());
                                        aditDocument.setGuid(dvkDocument.getDhlGuid());
                                        aditDocument.setLocked(true);
                                        aditDocument.setLockingDate(new Date());
                                        aditDocument.setSignable(true);
                                        aditDocument.setTitle(dvkDocument.getTitle());
                                        aditDocument.setDocumentType(DOCTYPE_LETTER);

                                        // The creator is the recipient
                                        aditDocument.setCreatorCode(user.getUserCode());
                                        aditDocument.setCreatorName(user.getFullName());

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
                                            if (((file.getId() == null) || (file.getId() <= 0)) && "ddoc".equalsIgnoreCase(extension)) {
                                            	DigiDocExtractionResult extractionResult = extractDigiDocContainer(file.getSysTempFile(), digidocConfigFile);
                                            	if (extractionResult.isSuccess()) {
                                            		file.setFileType(FILETYPE_NAME_SIGNATURE_CONTAINER);
                                            		aditDocument.setSigned(true);
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
                                        Transaction aditTransaction = null;
                                        try {

                                            // Save the document
                                            aditSession = this.getDocumentDAO().getSessionFactory().openSession();
                                            aditTransaction = aditSession.beginTransaction();

                                            // Before we save the document to
                                            // ADIT, check if the recipient has
                                            // already received a document
                                            // with the same GUID or DVK ID.
                                            if (this.getDocumentDAO().checkIfDocumentExists(dvkDocument, recipient)) {
                                                throw new AditInternalException(
                                                        "Document already sent to user. DVK ID: "
                                                                + dvkDocument.getDhlId() + ", recipient: "
                                                                + recipient.getIsikukood().trim());
                                            }

                                            // Save document
                                            SaveItemInternalResult saveResult = this.getDocumentDAO().save(
                                                    aditDocument, tempDocuments, Long.MAX_VALUE, aditSession);
                                            if (saveResult == null) {
                                                throw new AditInternalException("Document saving failed!");
                                            }
                                            if (saveResult.isSuccess()) {
                                                logger.info("Document saved to ADIT database. ID: "
                                                        + saveResult.getItemId());

                                                // Add signature container extraction history event
                                                if (involvedSignatureContainerExtraction) {
	                                        		CustomXTeeHeader dummyHeader = new CustomXTeeHeader();
	                                        		dummyHeader.addElement(CustomXTeeHeader.ISIKUKOOD, user.getUserCode());
	                                        		dummyHeader.addElement(CustomXTeeHeader.INFOSYSTEEM, "");
	                                        		
	                                                DocumentHistory historyEvent = new DocumentHistory(
	                                               		 HISTORY_TYPE_EXTRACT_FILE, saveResult.getItemId(),
	                                               		 Calendar.getInstance().getTime(), user,
	                                               		 user, dummyHeader);
	                                                historyEvent.setDescription(DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE);
	                                                this.getDocumentHistoryDAO().save(historyEvent);
                                                }
                                                
                                                // Update user quota limit
                                                Long usedDiskQuota = user.getDiskQuotaUsed();
                                                if (usedDiskQuota == null) {
                                                	usedDiskQuota = 0L;
                                                }
                                                user.setDiskQuotaUsed(usedDiskQuota + saveResult.getAddedFilesSize());
                                                this.getAditUserDAO().saveOrUpdate(user);

                                            } else {
                                                if ((saveResult.getMessages() != null)
                                                        && (saveResult.getMessages().size() > 0)) {
                                                    throw new AditInternalException(saveResult.getMessages().get(0)
                                                            .getValue());
                                                } else {
                                                    throw new AditInternalException("Document saving failed!");
                                                }
                                            }

                                            // Update document status to "sent"
                                            // (recipient_status_id = "102") in
                                            // DVK Client database
                                            dvkDocument.setRecipientStatusId(DVK_STATUS_RECEIVED);
                                            this.getDvkDAO().updateDocument(dvkDocument);

                                            // Finally commit
                                            aditTransaction.commit();

                                        } catch (Exception e) {
                                            logger.debug("Error saving document to ADIT database: ", e);
                                            if (aditTransaction != null) {
                                                aditTransaction.rollback();
                                            }
                                        } finally {
                                            if (aditSession != null) {
                                                aditSession.close();
                                            }
                                        }
                                    } else {
                                        logger.error("User not found. Personal code: " + recipient.getIsikukood().trim());
                                        this.composeErrorResponse(
                                                DocumentService.DVK_RECEIVE_FAIL_REASON_USER_DOES_NOT_EXIST, dvkContainer,
                                                recipient.getIsikukood().trim(), dvkDocument.getReceivedDate(),
                                                recipient.getNimi());
                                    }
                                }
                            }
                        }
                    } else {
                        logger.warn("No recipients found for this message: " + dvkDocument.getDhlGuid());
                    }
                }
            } else {
                logger.info("No incoming messages found in DVK Client database.");
            }
        } catch (Exception e) {
            throw new AditInternalException("Error while receiving documents from DVK Client database: ", e);
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

        // Write the clob data to a temporary file
        Reader clobReader = null;
        FileWriter fileWriter = null;
        try {
            clobReader = document.getData().getCharacterStream();
            String tmpFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();
            fileWriter = new FileWriter(tmpFile);

            char[] cbuf = new char[1024];
            int readCount = 0;
            while ((readCount = clobReader.read(cbuf)) > 0) {
                fileWriter.write(cbuf, 0, readCount);
            }

            fileWriter.close();
            clobReader.close();

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

            if (clobReader != null) {
                try {
                    clobReader.close();
                } catch (Exception e) {
                    logger.warn("Error while closing Clob reader: ", e);
                }
            }
        }

        return result;
    }

    /**
     * Extracts files from DVK container.
     * 
     * @param dvkContainer
     *            DVK container
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
     * Updates statuses for outgoing messages.
     * 
     * @return number of messages updated.
     */
    @Transactional
    public int updateDocumentsFromDVK() {
        int result = 0;

        logger.info("Updating DVK statuses...");

        // 1. Võtame kõik dokumendid ADIT andmebaasist, millel DVK staatus ei
        // ole "saadetud"
        List<Document> documents = this.getDocumentDAO().getDocumentsWithoutDVKStatus(DVK_STATUS_SENT);
        Iterator<Document> documentsIterator = documents.iterator();

        while (documentsIterator.hasNext()) {
            Document document = documentsIterator.next();

            try {
                logger.info("Updating DVK status for document. DocumentID: " + document.getId());

                List<PojoMessageRecipient> messageRecipients = this.getDvkDAO().getMessageRecipients(
                        document.getDvkId(), false);
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

                        if (documentSharing.getUserCode().equalsIgnoreCase(messageRecipient.getRecipientPersonCode())
                                || documentSharing.getUserCode().equalsIgnoreCase(
                                        messageRecipient.getRecipientOrgCode())) {

                            // If the statuses differ, update the one in ADIT
                            // database
                            if (!documentSharing.getDocumentDvkStatus().equals(messageRecipient.getSendingStatusId())) {
                                documentSharing.setDocumentDvkStatus(messageRecipient.getSendingStatusId());
                                this.getDocumentSharingDAO().update(documentSharing);
                                logger.debug("DocumentSharing DVK status updated: documentSharingID: "
                                        + documentSharing.getId() + ", DVK status: "
                                        + documentSharing.getDocumentDvkStatus());
                                result++;
                            }

                            if (messageRecipient.getSendingStatusId() != DocumentService.DVK_STATUS_SENT) {
                                allDocumentSharingsSent = false;
                            }
                        }
                    }

                    // If all documentSharings statuses are "sent" then update
                    // the document's dvk status
                    if (allDocumentSharingsSent) {
                        // Update document DVK status ID
                        document.setDocumentDvkStatusId(DocumentService.DVK_STATUS_SENT);
                        this.getDocumentDAO().update(document);
                        logger
                                .debug("All DVK sharings for this document updated to 'sent'. Updating document DVK status.");
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
                                // ADIT to
                                // DVK
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
     * Deletes documents from DVK, that have the status 'sent'
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
     * @return
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
     * DELETED BY ADIT'
     * 
     * @param message
     *            DVK document to be deleted.
     * @param session
     *            Current hibernate session.
     */
    public void deleteDVKDocument(PojoMessage message, Session session) {
        logger.debug("Deleting document. DHL_ID: " + message.getDhlId());
        message.setData(Hibernate.createClob(DocumentService.DVKBLOBMESSAGE_DELETE, session));
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
     * @param reasonCode reason code
     * @param dvkContainer DVK container
     * @param recipientCode recipient code
     * @param receivedDate date of retrieval
     * @param recipientName recipient name
     * 
     */
    public void composeErrorResponse(Integer reasonCode, ContainerVer2 dvkContainer, String recipientCode,
            Date receivedDate, String recipientName) throws AditInternalException {

        try {
            // 1. Gather data required for response message
            String xml = this.createErrorResponseDataXML(dvkContainer, recipientCode, receivedDate, recipientName);

            // 2. Transform to XSL-FO
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            String xmlTempFile = Util.createTemporaryFile(byteArrayInputStream, this.getConfiguration().getTempDir());
            String outputXslFoFile = Util.generateRandomFileName();
            Util.applyXSLT(xmlTempFile, this.getConfiguration().getDvkResponseMessageStylesheet(), outputXslFoFile);

            // 3. Transform to PDF
            String outputPDFFile = Util.generateRandomFileName();
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
     * @param dvkContainer
     *            DVK container
     * @param recipientCode
     *            recipient code
     * @param receivedDate
     *            receiving date
     * @param recipientName
     *            recipient name
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
     * Saves error response message to DVK client database.
     * 
     * @param fileName
     *            absolute path to the data file.
     * @param originalContainer
     *            original (request) DVK container
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

                Clob clob = Hibernate.createClob(" ", dvkSession);
                message.setData(clob);

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
                Writer clobWriter = dvkMessageToUpdate.getData().setCharacterStream(1);

                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    clobWriter.write(new String(buf, 0, len, "UTF-8"));
                }
                is.close();
                clobWriter.close();

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
     * Marks document matching the given ID as deleted. Document file contents
     * will be replaced with their MD5 hash codes. Document and individual files
     * will be marked as "deleted".
     * 
     * @param documentId
     *            ID of document to be deleted
     * @param userCode
     *            Code of the user who executed current request
     * @param applicationName
     *            Short name of application that executed current request
     * @throws Exception
     */
    @Transactional
    public void deleteDocument(long documentId, String userCode, String applicationName) throws Exception {
        Document doc = this.getDocumentDAO().getDocument(documentId);
        long deletedFilesSize = 0;

        // Check whether or not the document exists
        if (doc == null) {
            AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
            aditCodedException.setParameters(new Object[] {new Long(documentId).toString() });
            throw aditCodedException;
        }

        // Make sure that the document is not already deleted
        // NB! doc.getDeleted() can be NULL
        if ((doc.getDeleted() != null) && doc.getDeleted()) {
            AditCodedException aditCodedException = new AditCodedException("request.deleteDocument.document.deleted");
            aditCodedException.setParameters(new Object[] {new Long(documentId).toString() });
            throw aditCodedException;
        }

        boolean saveDocument = false;

        // Check whether or not given document belongs to current user
        if ((userCode != null) && (userCode.equalsIgnoreCase(doc.getCreatorCode()))) {
        	
        	// If document has been sent then preserve contents and only mark
        	// document as invisible to owner.
        	// If document has been shared then cancel sharing and delete the document
        	boolean hasBeenSent = false;
        	if (doc.getDocumentSharings() != null) {
                Iterator it = doc.getDocumentSharings().iterator();
                while (it.hasNext()) {
                    DocumentSharing sharing = (DocumentSharing) it.next();
                    if (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SHARE)
                        || sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SIGN)) {

                        it.remove();
                        sharing.setDocumentId(0);
                    } else {
                    	hasBeenSent = true;
                    }
                }
        	}
        	
            // Replace file contents with MD5 hash of original contents
            if (!hasBeenSent) {
                if (doc.getDocumentFiles() != null) {
	            	Iterator it = doc.getDocumentFiles().iterator();
	                while (it.hasNext()) {
	                    DocumentFile docFile = (DocumentFile) it.next();
	
	                    if ((docFile.getDeleted() == null) || !docFile.getDeleted()) {
	                        String resultCode = this.deflateDocumentFile(doc.getId(), docFile.getId(), true, false);
	
	                        // Make sure no relevant error code was returned
	                        if (resultCode.equalsIgnoreCase("file_does_not_exist")) {
	                            AditCodedException aditCodedException = new AditCodedException("file.nonExistent");
	                            aditCodedException.setParameters(new Object[] {new Long(docFile.getId()).toString() });
	                            throw aditCodedException;
	                        } else if (resultCode.equalsIgnoreCase("file_does_not_belong_to_document")) {
	                            AditCodedException aditCodedException = new AditCodedException("file.doesNotBelongToDocument");
	                            aditCodedException.setParameters(new Object[] {new Long(docFile.getId()).toString(), new Long(doc.getId()).toString() });
	                            throw aditCodedException;
	                        }
	                        
	                        deletedFilesSize = deletedFilesSize + docFile.getFileSizeBytes();
	                    }
	                }
                }
                doc.setDeleted(true);
                saveDocument = true;
            } else {
                // Make sure that the document is not already deleted by owner
                if ((doc.getInvisibleToOwner() != null) && doc.getInvisibleToOwner()) {
                    AditCodedException aditCodedException = new AditCodedException("request.deleteDocument.document.deleted");
                    aditCodedException.setParameters(new Object[] {new Long(documentId).toString() });
                    throw aditCodedException;
                } else {
                	doc.setInvisibleToOwner(true);
                	saveDocument = true;
                }
            }
        } else if (doc.getDocumentSharings() != null) {
            // Check whether or not the document has been shared to current user
            Iterator it = doc.getDocumentSharings().iterator();
            while (it.hasNext()) {
                DocumentSharing sharing = (DocumentSharing) it.next();
                if (sharing.getUserCode().equalsIgnoreCase(userCode)) {
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
                aditCodedException.setParameters(new Object[] {new Long(documentId).toString(), userCode });
                throw aditCodedException;
            }
        } else {
            AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            aditCodedException.setParameters(new Object[] {new Long(documentId).toString(), userCode });
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
     * @param documentId
     *            ID of document to be deflated
     * @param userCode
     *            Code of the user who executed current request
     * @param applicationName
     *            Short name of application that executed current request
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public void deflateDocument(long documentId, String userCode, String applicationName) throws Exception {
        Document doc = this.getDocumentDAO().getDocument(documentId);
        long deflatedFilesSize = 0;

        // Check whether or not the document exists
        if (doc == null) {
            AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
            aditCodedException.setParameters(new Object[] {new Long(documentId).toString() });
            throw aditCodedException;
        }

        // Make sure that the document is not deleted
        // NB! doc.getDeleted() can be NULL
        if ((doc.getDeleted() != null) && doc.getDeleted()) {
            AditCodedException aditCodedException = new AditCodedException("document.deleted");
            aditCodedException.setParameters(new Object[] {new Long(documentId).toString() });
            throw aditCodedException;
        }

        // Make sure that the document is not already deflated
        // NB! doc.getDeflated() can be NULL
        if ((doc.getDeflated() != null) && doc.getDeflated()) {
            AditCodedException aditCodedException = new AditCodedException("document.deflated");
            aditCodedException.setParameters(new Object[] {Util.dateToEstonianDateString(doc.getDeflateDate()) });
            throw aditCodedException;
        }

        // Check whether or not the document belongs to user
        if (!doc.getCreatorCode().equalsIgnoreCase(userCode)) {
            AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
            aditCodedException.setParameters(new Object[] {new Long(documentId).toString(), userCode });
            throw aditCodedException;

        }

        // Replace file contents with their MD5 hash codes
        Iterator it = doc.getDocumentFiles().iterator();
        while (it.hasNext()) {
            DocumentFile docFile = (DocumentFile) it.next();
            
            if ((docFile.getDeleted() == null) || !docFile.getDeleted()) {
	            String resultCode = deflateDocumentFile(doc.getId(), docFile.getId(), false, false);
	
                if (resultCode.equalsIgnoreCase("file_does_not_exist")) {
                    AditCodedException aditCodedException = new AditCodedException("file.nonExistent");
                    aditCodedException.setParameters(new Object[] {new Long(docFile.getId()).toString() });
                    throw aditCodedException;
                } else if (resultCode.equalsIgnoreCase("file_does_not_belong_to_document")) {
                    AditCodedException aditCodedException = new AditCodedException("file.doesNotBelongToDocument");
                    aditCodedException.setParameters(new Object[] {new Long(docFile.getId()).toString(), new Long(doc.getId()).toString() });
                    throw aditCodedException;
                }
	            
	            deflatedFilesSize = deflatedFilesSize + docFile.getFileSizeBytes();
            }
        }

        // Mark document as deflated, locked and unsignable
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
     * Adds a pending signature to documents signature container and returns
     * hash code of added signature. Returned hash code can be signed using
     * ID-card in any user interface.
     * 
     * @param documentId
     *            Document ID specifying which document should be signed
     * @param manifest
     *            Role or resolution of signer
     * @param country
     *            Country part of signers address
     * @param state
     *            County/state part of signers address
     * @param city
     *            City/town/village part of signers address
     * @param zip
     *            Postal code of signers address
     * @param certFile
     *            Absolute path to signers signing certificate file
     * @param digidocConfigFile
     *            Absolute path to digidoc configuration file
     * @param temporaryFilesDir
     *            Absolute path to applications temporary files directory
     * @param xroadUser
     *            {@link AditUser} who executed current request
     * @return {@link PrepareSignatureInternalResult} that contains hash code of
     *         added signature and indication whether or not adding new
     *         signature succeeded.
     * @throws Exception
     */
    public PrepareSignatureInternalResult prepareSignature(final long documentId, final String manifest,
            final String country, final String state, final String city, final String zip, final String certFile,
            final String digidocConfigFile, final String temporaryFilesDir, final AditUser xroadUser) throws Exception {

        PrepareSignatureInternalResult result = new PrepareSignatureInternalResult();
        result.setSuccess(true);

        Session session = null;
        Transaction tx = null;
        try {
            session = this.getDocumentDAO().getSessionFactory().openSession();
            tx = session.beginTransaction();

            File uniqueDir = null;
            try {
                ConfigManager.init(digidocConfigFile);

                // Load certificate from file
                X509Certificate cert = SignedDoc.readCertificate(certFile);

                // Remove country prefix from request user code, so it can be
                // compared to certificate personal id code more reliably
                String userCodeWithoutCountryPrefix = Util.getPersonalIdCodeWithoutCountryPrefix(xroadUser.getUserCode());

                // Determine if certificate belongs to same person
                // who executed current query
                String certPersonalIdCode = SignedDoc.getSubjectPersonalCode(cert);
                if (!userCodeWithoutCountryPrefix.equalsIgnoreCase(certPersonalIdCode)) {
                    logger.info("Attempted to sign document " + documentId + " by person \"" + certPersonalIdCode
                            + "\" while logged in as person \"" + userCodeWithoutCountryPrefix + "\"");
                    result.setSuccess(false);
                    result.setErrorCode("request.prepareSignature.signer.notCurrentUser");
                    return result;
                }

                // Load document
                Document doc = (Document) session.get(Document.class, documentId);
                
                // Find signature container (if exists)
                DocumentFile signatureContainer = findSignatureContainerDraft(doc);
                if ((signatureContainer == null) || (signatureContainer.getFileData() == null)) {
                	signatureContainer = findSignatureContainer(doc);
                }

                SignedDoc sdoc = null;
                if (signatureContainer == null) {
                    logger.debug("Creating new signature container.");
                    sdoc = new SignedDoc(SignedDoc.FORMAT_DIGIDOC_XML, SignedDoc.VERSION_1_3);
                } else {
                    logger.debug("Loading existing signature container");
                    SAXDigiDocFactory factory = new SAXDigiDocFactory();
                    sdoc = factory.readSignedDoc(signatureContainer.getFileData().getBinaryStream());

                    // Make sure that document is not already signed
                    // by the same person.
                    int removeSignatureAtIndex = -1;
                    int sigCount = sdoc.countSignatures();
                    for (int i = 0; i < sigCount; i++) {
                        Signature existingSig = sdoc.getSignature(i);
                        if (existingSig != null) {
                            int certCount = existingSig.countCertValues();
                            for (int j = 0; j < certCount; j++) {
                                if ((existingSig.getCertValue(j) != null)
                                        && (existingSig.getCertValue(j).getCert() != null)) {
                                    if (userCodeWithoutCountryPrefix.equalsIgnoreCase(SignedDoc.getSubjectPersonalCode(existingSig
                                            .getCertValue(j).getCert()))) {
                                    	if (existingSig.findResponderCert() != null) {
                                    		throw new AditCodedException("request.prepareSignature.signer.hasAlreadySigned");
                                    	} else {
                                    		removeSignatureAtIndex = i;
                                    	}
                                    }
                                }
                            }
                        }
                    }
                    
                    // If the same person has already given an unconfirmed
                    // and now attempts to prepare another signature then
                    // lets remove the users earlier signature.
                    if (removeSignatureAtIndex >= 0) {
                    	sdoc.removeSignature(removeSignatureAtIndex);
                    }
                }

                String[] claimedRoles = null;
                if ((manifest != null) && (manifest.length() > 0)) {
                    claimedRoles = new String[] {manifest };
                }
                SignatureProductionPlace address = null;
                if (((country != null) && (country.length() > 0)) || ((state != null) && (state.length() > 0))
                        || ((city != null) && (city.length() > 0)) || ((zip != null) && (zip.length() > 0))) {

                    address = new SignatureProductionPlace();
                    address.setCountryName(country);
                    address.setStateOrProvince(state);
                    address.setCity(city);
                    address.setPostalCode(zip);
                }

                if ((sdoc.countDataFiles() < 1) && (sdoc.countSignatures() < 1)) {
	                // Create unique subdirectory for files
	                uniqueDir = new File(temporaryFilesDir + File.separator + documentId);
	                int uniqueCounter = 0;
	                while (uniqueDir.exists()) {
	                    uniqueDir = new File(temporaryFilesDir + File.separator + documentId + "_" + (++uniqueCounter));
	                }
	                uniqueDir.mkdir();
	
	                List<DocumentFile> filesList = new ArrayList<DocumentFile>(doc.getDocumentFiles());
	                for (DocumentFile docFile : filesList) {
	                    if ((docFile.getDeleted() == null) || !docFile.getDeleted()) {
	                        String outputFileName = uniqueDir.getAbsolutePath() + File.separator + docFile.getFileName();
	
	                        InputStream blobDataStream = null;
	                        FileOutputStream fileOutputStream = null;
	                        try {
	                            byte[] buffer = new byte[10240];
	                            int len = 0;
	                            blobDataStream = docFile.getFileData().getBinaryStream();
	                            fileOutputStream = new FileOutputStream(outputFileName);
	                            while ((len = blobDataStream.read(buffer)) > 0) {
	                                fileOutputStream.write(buffer, 0, len);
	                            }
	
	                            // Add file to signature container
	                            DataFile df = sdoc.addDataFile(new File(outputFileName), docFile.getContentType(), DataFile.CONTENT_EMBEDDED_BASE64);
	                            docFile.setDdocDataFileId(df.getId());
	                        } catch (IOException ex) {
	                            throw new HibernateException(ex);
	                        } finally {
	                            try {
	                                if (blobDataStream != null) {
	                                    blobDataStream.close();
	                                }
	                                blobDataStream = null;
	                            } catch (Exception ex) {
	                                logger.error("Exception: ", ex);
	                            }
	
	                            try {
	                                if (fileOutputStream != null) {
	                                    fileOutputStream.close();
	                                }
	                                fileOutputStream = null;
	                            } catch (Exception ex) {
	                                logger.error("Exception: ", ex);
	                            }
	                        }
	
	                    }
	                }
                }

                // Add signature and calculate digest
                Signature sig = sdoc.prepareSignature(cert, claimedRoles, address);
                byte[] digest = sig.calculateSignedInfoDigest();
                result.setSignatureHash(Util.convertToHexString(digest));

                // Create a dummy signature.
                // Otherwise it will not be possible to save signature container
                byte[] dummySignature = new byte[128];
                for (int i = 0; i < dummySignature.length; i++) {
                    dummySignature[i] = 0;
                }
                sig.setSignatureValue(dummySignature);

                // Save container to file.
                String containerFileName = Util.generateRandomFileNameWithoutExtension();
                containerFileName = temporaryFilesDir + File.separator + containerFileName + "_PSv1.adit";
                sdoc.writeToFile(new File(containerFileName));

                // Add signature container to document table
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(containerFileName);
                } catch (FileNotFoundException e) {
                    logger.error("Error reading digidoc container file: ", e);
                }
                long length = (new File(containerFileName)).length();
                // Blob containerData = Hibernate.createBlob(fileInputStream,
                // length, session);
                Blob containerData = Hibernate.createBlob(fileInputStream, length);
                
                if (signatureContainer == null) {
                	signatureContainer = new DocumentFile();
                	signatureContainer.setContentType("application/octet-stream");
                	signatureContainer.setDeleted(false);
                	signatureContainer.setDocument(doc);
                	signatureContainer.setDocumentFileTypeId(FILETYPE_SIGNATURE_CONTAINER_DRAFT);
                	signatureContainer.setFileName(Util.convertToLegalFileName(doc.getTitle(), "ddoc"));
                	signatureContainer.setFileSizeBytes(length);
                	doc.getDocumentFiles().add(signatureContainer);
                }
                signatureContainer.setFileData(containerData);
                
                doc.setLocked(true);
                doc.setLockingDate(new Date());

                // Update document
                session.update(doc);
            } finally {
                // Delete temporary directory that was created only for this method.
                try {
                    Util.deleteDir(uniqueDir);
                } catch (Exception ex) {
                    logger.error("Exception: ", ex);
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

        return result;
    }

    /**
     * Adds user signature data to pending signature in specified documents
     * signature container. After adding signature to container, gets a
     * confirmation for signature from OCSP service.
     * 
     * @param documentId
     *            Document ID specifying which document the users signature
     *            belongs to
     * @param signatureFileName
     *            Absolute path to file containing users signature
     * @param requestPersonalCode
     *            Personal ID code of the person who executed current request
     * @param digidocConfigFile
     *            Absolute path to digidoc configuration file
     * @param temporaryFilesDir
     *            Absolute path to applications temporary files directory
     * @throws Exception
     */
    public void confirmSignature(final long documentId, final String signatureFileName,
            final String requestPersonalCode, final String digidocConfigFile, final String temporaryFilesDir)
            throws Exception {

        Session session = null;
        Transaction tx = null;
        try {
            session = this.getDocumentDAO().getSessionFactory().openSession();
            tx = session.beginTransaction();

            Document doc = (Document) session.get(Document.class, documentId);
            DocumentFile signatureContainerDraft = findSignatureContainerDraft(doc);
            DocumentFile signatureContainer = findSignatureContainer(doc);
            
            if ((signatureContainerDraft == null) || (signatureContainerDraft.getFileData() == null)) {
            	throw new HibernateException("Cannot comfirm signature because no unconfirmed signatures exist!");
            }

            ConfigManager.init(digidocConfigFile);
            SAXDigiDocFactory factory = new SAXDigiDocFactory();
            SignedDoc sdoc = factory.readSignedDoc(signatureContainerDraft.getFileData().getBinaryStream());

            File signatureFile = new File(signatureFileName);
            if (!signatureFile.exists()) {
                throw new HibernateException("Signature file does not exist!");
            }

            byte[] sigValue = new byte[(int) signatureFile.length()];
            FileInputStream fs = null;
            try {
                fs = new FileInputStream(signatureFileName);
                fs.read(sigValue, 0, sigValue.length);
            } catch (IOException ex) {
                throw new HibernateException(ex);
            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (Exception ex1) {
                        logger.error("Exception: ", ex1);
                    }
                }
            }

            Signature sig = null;
            for (int i = 0; i < sdoc.countSignatures(); i++) {
                String signerPersonalCode = SignedDoc.getSubjectPersonalCode(sdoc.getSignature(i).getLastCertValue().getCert());
                if (requestPersonalCode.endsWith(signerPersonalCode)) {
                    sig = sdoc.getSignature(i);
                    break;
                }
            }

            if (sig != null) {
                sig.setOrigContent(null);
            	sig.setSignatureValue(sigValue);
                sig.getConfirmation();

                // Save container to file.
                String containerFileName = Util.generateRandomFileNameWithoutExtension();
                containerFileName = temporaryFilesDir + File.separator + containerFileName + "_CSv1.adit";
                sdoc.writeToFile(new File(containerFileName));

                // Add signature container to document table
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(containerFileName);
                } catch (FileNotFoundException e) {
                    logger.error("Error reading digidoc container file: ", e);
                }
                long length = (new File(containerFileName)).length();
                // Blob containerData = Hibernate.createBlob(fileInputStream,
                // length, session);
                Blob containerData = Hibernate.createBlob(fileInputStream, length);
                
                boolean wasSignedBefore = true;
                if (signatureContainer == null) {
                	wasSignedBefore = false;
                	signatureContainer = new DocumentFile();
                	signatureContainer.setContentType("application/octet-stream");
                	signatureContainer.setDeleted(false);
                	signatureContainer.setDocument(doc);
                	signatureContainer.setDocumentFileTypeId(FILETYPE_SIGNATURE_CONTAINER);
                	signatureContainer.setFileName(Util.convertToLegalFileName(doc.getTitle(), "ddoc"));
                	signatureContainer.setFileSizeBytes(length);
                	doc.getDocumentFiles().add(signatureContainer);
                }
                signatureContainer.setFileData(containerData);
                doc.setSigned(true);
                
                // Remove container draft contents
                signatureContainerDraft.setFileData(null);

                // Update document
                session.update(doc);

                // Add signature metadata to signature table
                ee.adit.dao.pojo.Signature aditSig = convertDigiDocSignatureToLocalSignature(sig);
                aditSig.setUserCode(requestPersonalCode);
                
                aditSig.setDocument(doc);
                session.save(aditSig);
                
                // Remove file contents and calculate offsets
                if (!wasSignedBefore) {
	                Hashtable<String, StartEndOffsetPair> offsetData = SimplifiedDigiDocParser.findDigiDocDataFileOffsets(containerFileName);
	                for (DocumentFile file : doc.getDocumentFiles()) {
	                	if ((file != null) && ((file.getDeleted() == null) || !file.getDeleted())
	                		&& (file.getDocumentFileTypeId() == FILETYPE_DOCUMENT_FILE)
	                		&& !Util.isNullOrEmpty(file.getDdocDataFileId())
	                		&& offsetData.containsKey(file.getDdocDataFileId())) {
	                		
	                		StartEndOffsetPair offsets = offsetData.get(file.getDdocDataFileId());
	                		
	                		String resultMsg = documentFileDAO.removeSignedFileContents(
	                			doc.getId(), file.getId(), offsets.getStart(), offsets.getEnd());
	                		
	                		if ("file_data_already_moved".equalsIgnoreCase(resultMsg)) {
	                			throw new Exception("Cannot remove signed file contents because file contents have already been moved!");
	                		} else if ("file_is_deleted".equalsIgnoreCase(resultMsg)) {
	                			throw new Exception("Cannot remove signed file contents because file is deleted!");
	                		} else if ("file_does_not_belong_to_document".equalsIgnoreCase(resultMsg)) {
	                			throw new Exception("Cannot remove signed file contents because file does not belong to current document!");
	                		} else if ("file_does_not_exist".equalsIgnoreCase(resultMsg)) {
	                			throw new Exception("Cannot remove signed file contents because file does not exist!");
	                		}
	                	}
	                }
                }
            } else {
                throw new Exception("Could not find pending signature given by user: " + requestPersonalCode);
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
    
    public ee.adit.dao.pojo.Signature convertDigiDocSignatureToLocalSignature(Signature digiDocSignature) {
    	ee.adit.dao.pojo.Signature result = new ee.adit.dao.pojo.Signature();
    	
        if (digiDocSignature.getSignedProperties() != null) {
            if (digiDocSignature.getSignedProperties().getSignatureProductionPlace() != null) {
                ee.sk.digidoc.SignatureProductionPlace location = digiDocSignature.getSignedProperties().getSignatureProductionPlace();
                result.setCity(location.getCity());
                result.setCountry(location.getCountryName());
                result.setCounty(location.getStateOrProvince());
                result.setPostIndex(location.getPostalCode());
            }
            if ((digiDocSignature.getSignedProperties().countClaimedRoles() > 0) && (digiDocSignature.getSignedProperties().getClaimedRole(0) != null)) {
                result.setSignerRole(digiDocSignature.getSignedProperties().getClaimedRole(0));
            }
            result.setSigningDate(digiDocSignature.getSignedProperties().getSigningTime());
        }
        
        CertValue signerCertificate = digiDocSignature.getCertValueOfType(CertValue.CERTVAL_TYPE_SIGNER);
        if ((signerCertificate != null) && (signerCertificate.getCert() != null)) {
            X509Certificate cert = signerCertificate.getCert();
            result.setSignerCode(SignedDoc.getSubjectPersonalCode(cert));
            result.setSignerName(SignedDoc.getSubjectLastName(cert) + ", " + SignedDoc.getSubjectFirstName(cert));
            
            // Add reference to ADIT user if signer happens to be registered user
            String signerCode = result.getSignerCode();
            String signerCountryCode = getSubjectCountryCode(cert);
            if (!Util.isNullOrEmpty(signerCode) && !Util.isNullOrEmpty(signerCountryCode)) {
            	String signerCodeWithCountryPrefix = (signerCode.startsWith(signerCountryCode)) ? signerCode : signerCountryCode + signerCode;
            	AditUser user = this.getAditUserDAO().getUserByID(signerCodeWithCountryPrefix);
	            if (user != null) {
	            	result.setUserCode(signerCodeWithCountryPrefix);
	            }
            }
        }
        
    	return result;
    }
    
    private DocumentFile findSignatureContainer(Document doc) {
    	DocumentFile result = null;
    	
    	if ((doc != null) && (doc.getSigned() != null) && doc.getSigned() && (doc.getDocumentFiles() != null)) {
    		for (DocumentFile file : doc.getDocumentFiles()) {
    			if (file.getDocumentFileTypeId() == FILETYPE_SIGNATURE_CONTAINER) {
    				result = file;
    				break;
    			}
    		}
    	}
    	
    	return result;
    }
    
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
    
    private String getSubjectCountryCode(X509Certificate cert) {
        String result = null;
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
        return result;
    }
    
    public static long resolveFileTypeId(String fileTypeName) {
    	long result = FILETYPE_DOCUMENT_FILE;
    	
    	if (FILETYPE_NAME_SIGNATURE_CONTAINER.equalsIgnoreCase(fileTypeName)) {
    		result = FILETYPE_SIGNATURE_CONTAINER;
    	} else if (FILETYPE_NAME_SIGNATURE_CONTAINER_DRAFT.equalsIgnoreCase(fileTypeName)) {
    		result = FILETYPE_SIGNATURE_CONTAINER_DRAFT;
    	}
    	
    	return result;
    }
    
    public static String resolveFileTypeName(Long fileTypeId) {
    	String result = FILETYPE_NAME_DOCUMENT_FILE;
    	
    	if (FILETYPE_SIGNATURE_CONTAINER == fileTypeId) {
    		result = FILETYPE_NAME_SIGNATURE_CONTAINER;
    	} else if (FILETYPE_SIGNATURE_CONTAINER_DRAFT == fileTypeId) {
    		result = FILETYPE_NAME_SIGNATURE_CONTAINER_DRAFT;
    	}
    	
    	return result;
    }
    
    public static boolean documentSharingExists(Set documentSharings, String userCode) {
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
    
    public static boolean documentSendingExists(Set documentSharings, String userCode) {
        boolean result = false;
        
        if ((documentSharings != null) && (!documentSharings.isEmpty())) {
            Iterator it = documentSharings.iterator();
            while (it.hasNext()) {
                DocumentSharing sharing = (DocumentSharing) it.next();
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
    
    public static boolean fileIsOfRequestedType(long fileTypeId, ArrayOfFileType requestedTypes) {
    	String fileTypeName = resolveFileTypeName(fileTypeId);
    	return ((requestedTypes == null)
    		|| (requestedTypes.getFileType() == null)
			|| (requestedTypes.getFileType().size() < 1)
			|| (requestedTypes.getFileType().contains(fileTypeName)));
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

}
