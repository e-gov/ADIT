package ee.adit.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfFileType;
import ee.adit.pojo.DocumentSendStatus;
import ee.adit.pojo.DocumentSendingData;
import ee.adit.pojo.DocumentSendingRecipient;
import ee.adit.pojo.DocumentSharingData;
import ee.adit.pojo.DocumentSharingRecipient;
import ee.adit.pojo.DocumentSharingRecipientStatus;
import ee.adit.pojo.DocumentSignatureList;
import ee.adit.pojo.GetDocumentListRequest;
import ee.adit.pojo.GetDocumentListResponseAttachment;
import ee.adit.pojo.OutputDocument;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.OutputDocumentFilesList;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.service.DocumentService;
import ee.adit.service.MessageService;
import ee.adit.util.SimplifiedDigiDocParser;
import ee.adit.util.Util;
import ee.sk.digidoc.DigiDocException;
import ee.sk.utils.ConfigManager;

/**
 * Document data access class. Provides methods for retrieving and manipulating
 * document data.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentDAO extends HibernateDaoSupport implements IDocumentDao {

    private static Logger logger = Logger.getLogger(DocumentDAO.class);

    private MessageSource messageSource;

    private MessageService messageService;

    /**
     * Fetches document by ID.
     *
     * @param id document ID
     * @return document
     */
    public Document getDocument(long id) {
        logger.debug("Attempting to load document from database. Document id: " + String.valueOf(id));
        return (Document) this.getHibernateTemplate().get(Document.class, id);
    }

    /**
     * Fetches document by GUID.
     *
     * @param documentGuid document GUID
     * @return document
     */
    @SuppressWarnings("unchecked")
    public Document getDocumentByGuid(String documentGuid) {
        logger.debug("Attempting to load document from database. Document GUID: " + String.valueOf(documentGuid));

        List<Document> result;
        DetachedCriteria dt = DetachedCriteria.forClass(Document.class, "document");
        dt.add(Property.forName("document.guid").eq(documentGuid));
        result = (List<Document>) this.getHibernateTemplate().findByCriteria(dt);

        return (result.isEmpty() ? null : result.get(0));
    }
    /**
     * Fetches document with Signatures by document ID.
     * Main goal is to initialize signatures collection which is lazy initialized by default.
     * @param documentId
     * @return
     * @throws Exception
     */
    public Document getDocumentWithSignaturesAndFiles(final long documentId)
            throws Exception {
        if (documentId <= 0) {
            throw new IllegalArgumentException("Document ID must be a positive integer. Currently supplied ID was "
                    + documentId + ".");
        }

        Document result = null;

        try {
            logger.debug("Attempting to load document files for document " + documentId);
            result = (Document) getHibernateTemplate().execute(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    Document doc = (Document) session.get(Document.class, documentId);

                    if (doc.getSigned()) {
                    	Set<ee.adit.dao.pojo.Signature> signatures =  doc.getSignatures();
                    	logger.debug("While fetching document for unsharing, initialized set of signatures. Set size - " + signatures.size());
                    }
                    Set<DocumentFile> docFiles = doc.getDocumentFiles();
                    logger.debug("While fetching document for unsharing, initialized set of document files. Set size - " + docFiles.size());
                    return doc;
                }
            });
        } catch (DataAccessException ex) {
            logger.error("Error while fetching document data from database: ", ex);
            if (ex.getRootCause() instanceof AditException) {
                throw (AditException) ex.getRootCause();
            } else {
                throw ex;
            }
        }
        return result;
    }

    /**
     * Fetch document with files.
     *
     * @param documentId document ID
     * @param fileIdList document file ID list
     * @param includeSignatures do signatures have to be included in the result
     * @param includeSharings do sharings have to be included in the result
     * @param includeFileContents do file contents have to be included in the result
     * @param fileTypes List of file types requested
     * @param temporaryFilesDir temporary directory
     * @param filesNotFoundMessageBase exception message base
     * @param currentRequestUserCode user code (the user that activated the request)
     * @param documentRetentionDeadlineDays
     * 		Document retention deadline from application configuration. Will be
     * 		used to calculate estimated remove date of document.
     * @param digidocConfigFile
     *     Full path to DigiDoc library configuration file.
     *
     * @return document
     * @throws Exception if any sort of exception occurred
     */
    public OutputDocument getDocumentWithFiles(final long documentId, final List<Long> fileIdList,
            final boolean includeSignatures, final boolean includeSharings, final boolean includeFileContents,
            final ArrayOfFileType fileTypes, final String temporaryFilesDir, final String filesNotFoundMessageBase,
            final String currentRequestUserCode, final Long documentRetentionDeadlineDays, final String digidocConfigFile)
            throws Exception {
        if (documentId <= 0) {
            throw new IllegalArgumentException("Document ID must be a positive integer. Currently supplied ID was "
                    + documentId + ".");
        }

        OutputDocument result = null;

        try {
            logger.debug("Attempting to load document files for document " + documentId);
            result = (OutputDocument) getHibernateTemplate().execute(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    Document doc = (Document) session.get(Document.class, documentId);
                    return dbDocumentToOutputDocument(doc, fileIdList, includeSignatures, includeSharings,
                            includeFileContents, fileTypes, temporaryFilesDir, filesNotFoundMessageBase,
                            currentRequestUserCode, documentRetentionDeadlineDays, digidocConfigFile);
                }
            });
        } catch (DataAccessException ex) {
            logger.error("Error while fetching document data from database: ", ex);
            if (ex.getRootCause() instanceof AditException) {
                throw (AditException) ex.getRootCause();
            } else {
                throw ex;
            }
        }
        return result;
    }

    /**
     * Converts database document to output document.
     *
     * @param doc document to be converted
     * @param fileIdList document files
     * @param includeSignatures do signatures have to be included in the result
     * @param includeSharings do sharings have to be included in the result
     * @param includeFileContents do file contents have to be included in the result
     * @param fileTypes List of file types requested
     * @param temporaryFilesDir temporary directory
     * @param filesNotFoundMessageBase exception message base
     * @param currentRequestUserCode user code (the user that activated the request)
     * @param documentRetentionDeadlineDays
     * 		Document retention deadline from application configuration. Will be
     * 		used to calculate estimated remove date of document.
     * @param digidocConfigFile
     *     Full path to DigiDoc library configuration file.
     *
     * @return document in output format
     * @throws SQLException
     */
    private OutputDocument dbDocumentToOutputDocument(Document doc, final List<Long> fileIdList,
            final boolean includeSignatures, final boolean includeSharings, final boolean includeFileContents,
            final ArrayOfFileType fileTypes, final String temporaryFilesDir, final String filesNotFoundMessageBase,
            final String currentRequestUserCode, final Long documentRetentionDeadlineDays, final String digidocConfigFile)
            throws SQLException {

        long totalBytes = 0;
        OutputDocument result = new OutputDocument();
        List<OutputDocumentFile> outputFilesList = new ArrayList<OutputDocumentFile>();
        List<DocumentFile> filesList = new ArrayList<DocumentFile>(doc.getDocumentFiles());

        // Check if all requested files exist
        if ((fileIdList != null) && !fileIdList.isEmpty()) {
            List<Long> internalIdList = new ArrayList<Long>();
            internalIdList.addAll(fileIdList);

            for (DocumentFile docFile : filesList) {
                if (((docFile.getDeleted() == null) || !docFile.getDeleted())
                	&& internalIdList.contains(docFile.getId())) {
                    internalIdList.remove(docFile.getId());
                }
            }

            // If some files did not exist or were deleted
            // then return error message
            if (!internalIdList.isEmpty()) {
                String idListString = "";
                for (Long id : internalIdList) {
                    idListString += " " + id;
                }
                idListString = idListString.trim().replaceAll(" ", ",");
                AditCodedException aditCodedException = new AditCodedException("files.nonExistentOrDeleted");
                aditCodedException.setParameters(new Object[] {idListString });
                throw aditCodedException;
            }
        }


        boolean buildZipArchive = (includeFileContents && (fileTypes != null)
            && (fileTypes.getFileType() != null) && (fileTypes.getFileType().size() > 0)
            && (fileTypes.getFileType().contains(DocumentService.FILETYPE_NAME_ZIP_ARCHIVE)));

        int itemIndex = 0;
        DocumentFile signatureContainerFile = null;
        boolean resultContainsSignatureContainer = false;
        for (DocumentFile docFile : filesList) {
            if (((docFile.getDeleted() == null) || !docFile.getDeleted())
            	&& (docFile.getDocumentFileTypeId() != DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT)) {

            	if (docFile.getDocumentFileTypeId() == DocumentService.FILETYPE_SIGNATURE_CONTAINER) {
                	signatureContainerFile = docFile;
                }
            	
            	boolean fileMatchesRequestedId = (fileIdList == null) || fileIdList.isEmpty() || fileIdList.contains(docFile.getId());
            	boolean fileTypeWasRequested = DocumentService.fileIsOfRequestedType(docFile.getDocumentFileTypeId(), fileTypes);
            	boolean fileIsNeededForZipArchive = buildZipArchive && (docFile.getDocumentFileTypeId() == DocumentService.FILETYPE_DOCUMENT_FILE);

            	if ((fileMatchesRequestedId && fileTypeWasRequested) || fileIsNeededForZipArchive) {
            		OutputDocumentFile f = new OutputDocumentFile();
                    f.setContentType(docFile.getContentType());
                    f.setDescription(docFile.getDescription());
                    f.setId(docFile.getId());
                    f.setGuid(docFile.getGuid());
                    f.setName(docFile.getFileName());
                    f.setSizeBytes(docFile.getFileSizeBytes());
                    f.setFileType(DocumentService.resolveFileTypeName(docFile.getDocumentFileTypeId()));

                    f.setDdocDataFileId(docFile.getDdocDataFileId());
                    f.setDdocDataFileStartOffset(docFile.getDdocDataFileStartOffset());
                    f.setDdocDataFileEndOffset(docFile.getDdocDataFileEndOffset());

                    totalBytes += ((docFile.getFileSizeBytes() == null) ? 0L : docFile.getFileSizeBytes().longValue());

                    // Read file data from BLOB and write it to temporary file.
                    // This is necessary to avoid storing potentially large
                    // amounts of binary data in server memory.
                    if (includeFileContents) {
                    	logger.debug("Attempting to retreive file contents because contents of file " + docFile.getId() + " were requested.");

                    	if ((docFile.getFileDataInDdoc() == null) || !docFile.getFileDataInDdoc()) {
	                        itemIndex++;
	                        String outputFileName = Util.generateRandomFileNameWithoutExtension();
	                        outputFileName = temporaryFilesDir + File.separator + outputFileName + "_" + itemIndex + "_GDFv1.adit";

	                        InputStream blobDataStream = null;
	                        FileOutputStream fileOutputStream = null;
	                        try {
	                        	byte[] buffer = new byte[10240];
	                            int len = 0;
	                            int currentFileBytes = 0;
	                            logger.debug("Opening BLOB stream to retrieve data from database.");
	                            blobDataStream = new ByteArrayInputStream(docFile.getFileData());
	                            fileOutputStream = new FileOutputStream(outputFileName);
	                            logger.debug("BLOB stream opened. Starting to read data");
	                            while ((len = blobDataStream.read(buffer)) > 0) {
	                                fileOutputStream.write(buffer, 0, len);
	                                currentFileBytes += len;
	                            }
	                            logger.debug("Successfully retreived " + currentFileBytes + " bytes. Was expecting " + docFile.getFileSizeBytes() + " bytes.");
	                        } catch (IOException ex) {
	                        	logger.debug("Exception occured while reading file " + docFile.getId() + " from database.", ex);
	                            throw new HibernateException(ex);
	                        } finally {
	                        	Util.safeCloseStream(blobDataStream);
	                        	Util.safeCloseStream(fileOutputStream);
	                        	blobDataStream = null;
	                        	fileOutputStream = null;
	                        }

	                        // Base64 encode file
	                        try {
	                        	logger.debug("Base64 encoding contents of file " + docFile.getId() + " ad writing encoded data to disk.");
	                            f.setSysTempFile(Util.base64EncodeFile(outputFileName, temporaryFilesDir));
	                            logger.debug("Base64 encoded data was successfully written to " + f.getSysTempFile());
	                        } catch (IOException ex) {
	                        	logger.debug("Exception occured while Base64 encoding file contents and/or writing encoded data to disk.", ex);
	                            throw new HibernateException(ex);
	                        }
	                    } else {
	                    	logger.debug("Could not retreive contents of file " + docFile.getId() + " directly from database because contents of this file are stored in signature container (separate file).");
	                    }
                    }

                    outputFilesList.add(f);
                	if (docFile.getDocumentFileTypeId() == DocumentService.FILETYPE_SIGNATURE_CONTAINER) {
                		resultContainsSignatureContainer = true;
                    }
                }
            }
        }

        if (includeFileContents && (doc.getSigned() != null) && doc.getSigned() && (signatureContainerFile != null)) {
        	try {
        		logger.debug("digidocConfigFile: " + digidocConfigFile);
                ConfigManager.init(digidocConfigFile);

        		SimplifiedDigiDocParser.extractFileContentsFromContainer(
    				new ByteArrayInputStream(signatureContainerFile.getFileData()),
	        		outputFilesList, temporaryFilesDir, Util.isBdocFile(signatureContainerFile.getFileName()));
        	} catch (IOException ex) {
                 throw new HibernateException(ex);
        	}catch (DigiDocException ex) {
                throw new HibernateException(ex);
        	}
        }

        // Add unsigned DDOC container containing all document files
        // (if DDOC container was requested and document is not signed)
        if (includeFileContents
        	&& !resultContainsSignatureContainer
        	&& (fileTypes != null)
        	&& (fileTypes.getFileType() != null)
        	&& (fileTypes.getFileType().size() > 0)
        	&& (fileTypes.getFileType().contains(DocumentService.FILETYPE_NAME_SIGNATURE_CONTAINER))) {

        	try {
	        	OutputDocumentFile dummyContainer =
	        		DocumentService.createSignatureContainerFromDocumentFiles(
	        			doc, digidocConfigFile, temporaryFilesDir);
	        	dummyContainer.setSysTempFile(Util.base64EncodeFile(dummyContainer.getSysTempFile(), temporaryFilesDir));
	        	totalBytes += (dummyContainer.getSizeBytes() == null) ? 0L : dummyContainer.getSizeBytes();
	        	outputFilesList.add(dummyContainer);
        	} catch (Exception ex) {
                throw new HibernateException(ex);
        	}
        }

        // Add ZIP archive containing all document files (if ZIP archive was requested)
        if (buildZipArchive) {
        	try {
	        	OutputDocumentFile zipArchive = DocumentService.createZipArchiveFromDocumentFiles(doc, outputFilesList, temporaryFilesDir);
	        	zipArchive.setSysTempFile(Util.base64EncodeFile(zipArchive.getSysTempFile(), temporaryFilesDir));

	        	// Remove from output files that were not requested but were
	        	// required for building ZIP archive
	        	totalBytes = 0L;
	        	for (int i = outputFilesList.size() - 1; i >= 0; i--) {
	        		if (!DocumentService.fileIsOfRequestedType(DocumentService.resolveFileTypeId(outputFilesList.get(i).getFileType()), fileTypes)) {
	        			outputFilesList.remove(i);
	        		} else {
	        			totalBytes += outputFilesList.get(i).getSizeBytes();
	        		}
	        	}

	        	totalBytes += (zipArchive.getSizeBytes() == null) ? 0L : zipArchive.getSizeBytes();
	        	outputFilesList.add(zipArchive);
        	} catch (Exception ex) {
                throw new HibernateException(ex);
        	}
        }

        OutputDocumentFilesList filesListWrapper = new OutputDocumentFilesList();
        filesListWrapper.setFiles(outputFilesList);
        filesListWrapper.setTotalFiles(outputFilesList.size());
        filesListWrapper.setTotalBytes(totalBytes);
        result.setFiles(filesListWrapper);

        // Signatures
        if (includeSignatures) {
            DocumentSignatureList docSignatures = new DocumentSignatureList();
            docSignatures.setSignatures(new ArrayList<ee.adit.pojo.Signature>());
            if ((doc.getSignatures() != null) && (!doc.getSignatures().isEmpty())) {
                Iterator it = doc.getSignatures().iterator();
                while (it.hasNext()) {
                    ee.adit.dao.pojo.Signature sig = (ee.adit.dao.pojo.Signature) it.next();
                    ee.adit.pojo.Signature outSig = new ee.adit.pojo.Signature();
                    outSig.setCity(sig.getCity());
                    outSig.setCountry(sig.getCountry());

                    String manifest = Util.isNullOrEmpty(sig.getSignerRole()) ? "" : sig.getSignerRole();
                    if (!Util.isNullOrEmpty(sig.getResolution())) {
                    	manifest += (Util.isNullOrEmpty(manifest) ? "" : " ") + sig.getResolution();
                    }
                    outSig.setManifest(manifest);

                    outSig.setSignerCode(sig.getSignerCode());
                    outSig.setSignerName(sig.getSignerName());
                    outSig.setState(sig.getCounty());
                    outSig.setZip(sig.getPostIndex());
                    outSig.setSigningDate(sig.getSigningDate());
                    outSig.setUserCode(sig.getUserCode());
                    outSig.setUserName(sig.getUserName());
                    docSignatures.getSignatures().add(outSig);
                }
            }
            result.setSignatures(docSignatures);
        }

        // Sharing/sending
        if (includeSharings) {
            DocumentSendingData sendingData = new DocumentSendingData();
            sendingData.setUserList(new ArrayList<DocumentSendingRecipient>());
            DocumentSharingData sharingData = new DocumentSharingData();
            sharingData.setUserList(new ArrayList<DocumentSharingRecipient>());
            Date sendingDateCheck = null;

            if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
                Iterator it = doc.getDocumentSharings().iterator();
                while (it.hasNext()) {
                    DocumentSharing sharing = (DocumentSharing) it.next();

                    if ((sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SHARE))
                        || (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SIGN))) {

                        DocumentSharingRecipient rec = new DocumentSharingRecipient();
                        rec.setCode(sharing.getUserCode());
                        rec.setHasBeenViewed((sharing.getFirstAccessDate() != null));
                        rec.setName(sharing.getUserName());
                        rec.setOpenedTime(sharing.getFirstAccessDate());
                        rec.setWorkflowStatusId(sharing.getDocumentWfStatus());
                        rec.setSharedTime(sharing.getCreationDate());
                        rec.setReasonForSharing(sharing.getTaskDescription());
                        rec.setSharedForSigning("sign".equalsIgnoreCase(sharing.getDocumentSharingType()));
                        sharingData.getUserList().add(rec);
                    } else {
                        DocumentSendingRecipient rec = new DocumentSendingRecipient();

                        if (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SHARINGTYPE_SEND_EMAIL)) {
                            rec.setEmail(sharing.getUserEmail());
                        } else {
	                        rec.setCode(sharing.getUserCode());
	                        rec.setHasBeenViewed((sharing.getFirstAccessDate() != null));
	                        rec.setName(sharing.getUserName());
	                        rec.setOpenedTime(sharing.getFirstAccessDate());
	                        rec.setWorkflowStatusId(sharing.getDocumentWfStatus());
	                        rec.setDvkStatusId(sharing.getDocumentDvkStatus());
                        }

                        sendingData.getUserList().add(rec);

                        // There should be possible to have only one sending per document.
                        // So it should be safe to take sending date from any sending record
                        // associated to current document.
                        sendingData.setSentTime(sharing.getCreationDate());

                        // Check if assumptions mentioned above are also valid in real world
                        if ((sendingDateCheck != null) && (sharing.getCreationDate() != null)) {
                        	long diffInMs = sharing.getCreationDate().getTime() - sendingDateCheck.getTime();
                        	if (Math.abs(diffInMs) > (60L * 1000L)) {
                        		logger.warn("Document " + doc.getId() + " has multiple sendings with sending times varying more than 1 minute.");
                        	}
                        }
                        if (sharing.getCreationDate() != null) {
                        	sendingDateCheck = sharing.getCreationDate();
                        }
                    }
                }
            }

            result.setSentTo(sendingData);
            result.setSharedTo(sharingData);
        }

        // Document data
        result.setCreated(doc.getCreationDate());
        result.setCreatorApplication(doc.getRemoteApplication());
        result.setCreatorCode(doc.getCreatorCode());
        result.setCreatorName(doc.getCreatorName());

        // If current request was executed by document creator
        // (the same user whom the document belongs to) then return
        // also data about the person, who created this document.
        // This is useful if document creator is an organization and one
        // wants to find out, who exactly in this organization created the
        // document.
        if ((currentRequestUserCode != null) && (currentRequestUserCode.equalsIgnoreCase(doc.getCreatorCode()))) {
            result.setCreatorUserCode(doc.getCreatorUserCode());
            result.setCreatorUserName(doc.getCreatorUserName());
        }

        result.setDeflated(doc.getDeflated());
        result.setDeflatingDate(doc.getDeflateDate());
        result.setDocumentType(doc.getDocumentType());
        result.setDvkId(doc.getDvkId());
        result.setDvkStatusId(doc.getDocumentDvkStatusId());
        result.setGuid(doc.getGuid());
        result.setId(doc.getId());
        result.setLastModified(doc.getLastModifiedDate());
        result.setLocked(doc.getLocked());
        result.setLockingDate(doc.getLockingDate());
        result.setSignable(doc.getSignable());
        result.setSigned(doc.getSigned());
        result.setTitle(doc.getTitle());
        result.setContent(doc.getContent());
        result.setWorkflowStatusId(doc.getDocumentWfStatusId());
        result.setFilesSizeBytes(doc.getFilesSizeBytes());
        result.setSenderReceiver(doc.getSenderReceiver());

        // Estimated document remove date
        Date removeDate = null;
        if ((documentRetentionDeadlineDays != null) && (documentRetentionDeadlineDays > 0)) {
        	Calendar cal = Calendar.getInstance();
        	if (doc.getLastModifiedDate() != null) {
        		cal.setTime(doc.getLastModifiedDate());
        	} else if (doc.getCreationDate() != null) {
        		cal.setTime(doc.getCreationDate());
        	}
        	cal.add(Calendar.DATE, documentRetentionDeadlineDays.intValue());
        	removeDate = cal.getTime();
        }
        if (removeDate != null) {
        	result.setRemoveDate(new org.exolab.castor.types.Date(removeDate));
        } else {
        	result.setRemoveDate(null);
        }

        // If data about document previous version is present
        // then add it to output
        if (doc.getDocument() != null) {
            result.setPreviousDocumentId(doc.getDocument().getId());
            result.setPreviousDocumentGuid(doc.getDocument().getGuid());
	        result.setPreviousDocumentTitle(doc.getDocument().getTitle());
	        result.setPreviousDocumentDeleted(Boolean.TRUE.equals(doc.getDocument().getDeleted()));
        }

        boolean hasSentReply = false;
        // Document folder
        if (currentRequestUserCode.equalsIgnoreCase(doc.getCreatorCode())
        	&& ((doc.getDocumentSharings() == null) || doc.getDocumentSharings().isEmpty())) {
        	result.setFolder("local");
        } else if (currentRequestUserCode.equalsIgnoreCase(doc.getCreatorCode())) {
        	result.setFolder("outgoing");
        } else {
        	result.setFolder("incoming");
        	// check if document has a sent reply to the sender
            if (doc.getDocuments() != null && !doc.getDocuments().isEmpty()) {
            	for (Document childDocument : doc.getDocuments()) {
    				if (childDocument.getDocumentSharings() != null && !childDocument.getDocumentSharings().isEmpty()) {
    					for (DocumentSharing sharing : childDocument.getDocumentSharings()) {
							if (!Boolean.TRUE.equals(sharing.getDeleted()) && sharing.getUserCode() != null && doc.getCreatorCode().equals(sharing.getUserCode())) {
								hasSentReply = true;
								break;
							}
    					}
    				}
    				if (hasSentReply) {
    					break;
    				}
            	}
            }
        }
        result.setHasSentReply(hasSentReply);

        if (doc.getEformUseId() != null) {
        	result.setEformUseId(doc.getEformUseId());
        }

        // Has the document been viewed?
        result.setHasBeenViewed(false);
        if (currentRequestUserCode.equalsIgnoreCase(doc.getCreatorCode())) {
        	result.setHasBeenViewed(true);
        } else {
            if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
                Iterator it = doc.getDocumentSharings().iterator();
                while (it.hasNext()) {
                    DocumentSharing sharing = (DocumentSharing) it.next();
                    if (sharing.getUserCode() != null && currentRequestUserCode.equalsIgnoreCase(sharing.getUserCode())) {
                    	result.setHasBeenViewed(sharing.getFirstAccessDate() != null);
                    	break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Save document with files.
     *
     * @param document document
     * @param files document files
     * @param remainingDiskQuota remaining disk quota for user
     * @param existingSession the existing database session
     *
     * @return save result
     * @throws Exception
     */
    public SaveItemInternalResult save(Document document, List<OutputDocumentFile> files, long remainingDiskQuota,
            Session existingSession) throws Exception {
        if ((existingSession != null) && (existingSession.isOpen())) {
            return saveImpl(document, files, remainingDiskQuota, existingSession);
        } else {
            return save(document, files, remainingDiskQuota);
        }
    }

    /**
     * Save document in new session.
     *
     * @param document document
     * @param files document files
     * @param remainingDiskQuota remaining disk quota for user
     *
     * @return save result
     * @throws Exception
     */
    public SaveItemInternalResult save(final Document document, final List<OutputDocumentFile> files,
            final long remainingDiskQuota) throws Exception {
        return (SaveItemInternalResult) this.getHibernateTemplate().execute(new HibernateCallback() {
            @Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
                try {
                    return saveImpl(document, files, remainingDiskQuota, session);
                } catch (Exception ex) {
                    throw new HibernateException(ex);
                }
            }
        });
    }

    /**
     * Save document.
     *
     * @param document document
     * @param files document files
     * @param remainingDiskQuota remaining disk quota for user
     * @param session session to be used for saving
     *
     * @return save result
     * @throws IOException
     */
    private SaveItemInternalResult saveImpl(Document document, List<OutputDocumentFile> files, long remainingDiskQuota,
            Session session) throws IOException {
        SaveItemInternalResult result = new SaveItemInternalResult();
        long filesTotalSize = 0;

        try {
            if (document.getDocumentFiles() == null) {
                document.setDocumentFiles(new HashSet<DocumentFile>());
            }

            boolean newFilesAddedToExistingDocument = false;
            if (files != null) {
                // Before actually saving files check data and disk quota
                long requiredDiskSpace = 0;
                for (int i = 0; i < files.size(); i++) {
                    OutputDocumentFile attachmentFile = files.get(i);

                    DocumentFile documentFile = new DocumentFile();
                    if ((attachmentFile.getId() != null) && (attachmentFile.getId() > 0)) {
                        documentFile = null;
                        Iterator it = document.getDocumentFiles().iterator();
                        while (it.hasNext()) {
                            DocumentFile f = (DocumentFile) it.next();
                            if (f.getId() == attachmentFile.getId()) {
                                documentFile = f;
                                break;
                            }
                        }
                    }

                    if (documentFile == null) {
                        result.setSuccess(false);
                        result.getMessages().addAll(
                                this.getMessageService().getMessages(
                                        "request.saveDocument.document.noFileToUpdate",
                                        new Object[] {attachmentFile.getId().toString(),
                                                new Long(document.getId()).toString() }));
                        return result;
                    }

                    long length = (new File(attachmentFile.getSysTempFile())).length();
                    if ((documentFile != null) && (documentFile.getId() > 0)) {
                        Long currentVersionLength = (documentFile.getFileSizeBytes() == null) ? 0 : documentFile
                                .getFileSizeBytes();
                        requiredDiskSpace += (length - currentVersionLength);
                    } else {
                        requiredDiskSpace += length;
                    }
                }

                // If disk quota is exceeded then return
                // a result indicating failure
                if (requiredDiskSpace > remainingDiskQuota) {
                    result.setSuccess(false);
                    // Message msg = new Message("en",
                    // this.getMessageSource().getMessage("request.saveDocument.document.files.quotaExceeded",
                    // new Object[] { remainingDiskQuota, requiredDiskSpace },
                    // Locale.ENGLISH));
                    // result.getMessages().add(msg);
                    result.getMessages().addAll(
                            (this.getMessageService().getMessages("request.saveDocument.document.files.quotaExceeded",
                                    new Object[] {new Long(remainingDiskQuota).toString(),
                                            new Long(requiredDiskSpace).toString() })));
                    return result;
                }

                // Save document and files
                for (int i = 0; i < files.size(); i++) {
                    OutputDocumentFile attachmentFile = files.get(i);
                    boolean updatingExisting = false;

                    DocumentFile documentFile = new DocumentFile();
                    if ((attachmentFile.getId() != null) && (attachmentFile.getId() > 0)) {
                        documentFile = null;
                        Iterator it = document.getDocumentFiles().iterator();
                        while (it.hasNext()) {
                            DocumentFile f = (DocumentFile) it.next();
                            if (f.getId() == attachmentFile.getId()) {
                                logger.debug("Found existing file with ID " + attachmentFile.getId()
                                        + ". Updating existing file.");
                                documentFile = f;
                                updatingExisting = true;
                                break;
                            }
                        }
                    } else {
                        logger.debug("Adding file as new file.");
                    }

                    if ((document.getId() > 0) && (documentFile != null) && (documentFile.getId() < 1)) {
                        newFilesAddedToExistingDocument = true;
                    }

                    String fileName = attachmentFile.getSysTempFile();
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(fileName);
                    } catch (FileNotFoundException e) {
                        logger.error("Error saving document file: ", e);
                    }
                    long length = (new File(fileName)).length();

                    byte[] fileData = new byte[fileInputStream.available()];
                    fileInputStream.read(fileData);
                    documentFile.setFileData(fileData);

                    if (updatingExisting) {
                        if ((documentFile != null) && (documentFile.getFileSizeBytes() != null)) {
                        	long diff = documentFile.getFileSizeBytes().longValue() - length;
                        	filesTotalSize = filesTotalSize + diff;
                        } else {
                        	filesTotalSize = filesTotalSize + length;
                        }
                    } else {
                        filesTotalSize = filesTotalSize + length;
                    }

                    documentFile.setFileDataInDdoc((attachmentFile.getDdocDataFileStartOffset() != null) && (attachmentFile.getDdocDataFileStartOffset() > 0));
                    documentFile.setDocumentFileTypeId(DocumentService.resolveFileTypeId(attachmentFile.getFileType()));

                    String guid = attachmentFile.getGuid() != null ? attachmentFile.getGuid() : UUID.randomUUID().toString();
                    documentFile.setGuid(guid);
                    documentFile.setContentType(attachmentFile.getContentType());
                    documentFile.setDeleted(false);
                    documentFile.setDescription(attachmentFile.getDescription());
                    documentFile.setFileName(attachmentFile.getName());
                    documentFile.setFileSizeBytes(length);
                    documentFile.setDocument(document);
                    documentFile.setLastModifiedDate(new Date());
                    document.getDocumentFiles().add(documentFile);
                }
            }

            session.saveOrUpdate(document);

            // If new files were added to an existing document then
            // we have to forcibly flush the session at this point.
            // Otherwise we will not be able to return IDs of added
            // files in query result.
            if (newFilesAddedToExistingDocument) {
                session.flush();
            }

            logger.debug("Saved document ID: " + document.getId());
            logger.info("filesTotalSize: " + filesTotalSize);

            result.setAddedFilesSize(filesTotalSize);
            result.setItemId(document.getId());
            result.setSuccess(document.getId() > 0);
        } catch (HibernateException e) {
            logger.error("HibernateException: ", e);
            throw e;
        }

        return result;
    }

    /**
     * Checks if document exists.
     *
     * @param documentDhlId
     *     DEC ID of document
     * @param recipientPersonalIdCode
     *     Personal ID code of recipient
     *
     * @return true if document exists
     */
    @SuppressWarnings("unchecked")
    public boolean checkIfDocumentExists(final Long documentDhlId,
    	final String recipientPersonalIdCode) {
        boolean result = true;

        String sql = "from Document where dvkId = " + documentDhlId + " and creatorCode = '"
                + recipientPersonalIdCode.trim() + "'";
        List<Document> existingDocuments = (List<Document>) this.getHibernateTemplate().find(sql);

        if (existingDocuments == null || existingDocuments.size() == 0) {
            result = false;
        }

        return result;
    }

    /**
     * Fetch documents without the specified DVK status.
     *
     * @param dvkStatusId DVK status ID
     * @return document list
     */
    @SuppressWarnings("unchecked")
    public List<Document> getDocumentsWithoutDVKStatus(Long dvkStatusId) {
        List<Document> result = null;
        logger.debug("Fetching documents for status update. StatusID: " + dvkStatusId);
        String sql = "from Document where dvkId is not null and (documentDvkStatusId is null or documentDvkStatusId != " + dvkStatusId + ")";
        logger.debug("SQL: " + sql);
        Session session = null;
        try {
            session = this.getSessionFactory().getCurrentSession();
            result = session.createQuery(sql).list();
        } catch (Exception e) {
            throw new AditInternalException("Error while updating Document: ", e);
        }

        return result;
    }

    
    /**
     * Fetch documents which have signatures.
     *
     * @return document list
     */
    @SuppressWarnings("unchecked")
    public List<Document> getSignedDocuments() {
        List<Document> result = null;
        logger.debug("Fetching signed documents");
        String sql = "select document from Document document join document.signatures signatures where signatures.id is not null ";
        logger.debug("SQL: " + sql);
        Session session = null;
        try {
            session = this.getSessionFactory().openSession();
            Query query = session.createQuery(sql);
            query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            result = query.list();
        } catch (Exception e) {
            throw new AditInternalException("Error while getting signed documents: ", e);
        }
        return result;
    }

    
    /**
     * Update existing document.
     *
     * @param document document
     */
    public void update(Document document) {
        Session session = null;
        Transaction transaction = null;
        try {

            session = this.getSessionFactory().getCurrentSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(document);
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new AditInternalException("Error while updating Document: ", e);
        }
    }

    /**
     * Fetch document by DVK ID.
     *
     * @param dvkMessageID document DVK ID
     * @return document
     */
    public Document getDocumentByDVKID(Long dvkMessageID) {
        Document result = null;
        String sql = "from Document where dvkId = " + dvkMessageID;

        Session session = null;
        try {
            session = this.getSessionFactory().getCurrentSession();
            result = (Document) session.createQuery(sql).uniqueResult();
        } catch (Exception e) {
            throw new AditInternalException("Error while updating Document: ", e);
        }

        return result;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

	/**
	 * Translates XML (WSDL) name of document field to database name of that
	 * field.
	 *
	 * @param xmlName
	 *	 Field name in XML (WSDL)
	 * @return
	 *	 Field name in database
	 */
	private String documentFieldXmlNameToDbName(String xmlName) {
		if ("id".equalsIgnoreCase(xmlName)) {
			return "documents.id";
		} else if ("guid".equalsIgnoreCase(xmlName)) {
			return "documents.guid";
		} else if ("title".equalsIgnoreCase(xmlName)) {
			return "LOWER(documents.title)";
		} else if ("document_type".equalsIgnoreCase(xmlName)) {
			return "documents.document_type";
		} else if ("created".equalsIgnoreCase(xmlName)) {
			return "documents.creation_date";
		} else if ("last_modified".equalsIgnoreCase(xmlName)) {
			return "documents.last_modified_date";
		} else if ("dvk_id".equalsIgnoreCase(xmlName)) {
			return "documents.dvk_id";
		} else if ("file_size".equalsIgnoreCase(xmlName)) {
			return "files_size_bytes";
		} else if ("sender_receiver".equalsIgnoreCase(xmlName)) {
			return "LOWER(sender_receiver)";
		} else if ("sender".equalsIgnoreCase(xmlName)) {
			return "LOWER(sender)";
		} else if ("receiver".equalsIgnoreCase(xmlName)) {
			return "LOWER(receiver)";
		} else {
			return null;
		}
	}

	/**
	 * Document search.
	 *
	 * @param param search parameters
	 * @param userCode user code (document owner)
	 * @param temporaryFilesDir temporary directory
	 * @param filesNotFoundMessageBase exception message base
	 * @param currentRequestUserCode user code (the user that activated the request)
	 * @param documentRetentionDeadlineDays
	 * 		Document retention deadline from application configuration. Will be
	 * 		used to calculate estimated remove date of document.
	 * @param digidocConfigFile
	 *	 Full path to DigiDoc library configuration file.
	 *
	 * @return document list
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public GetDocumentListResponseAttachment getDocumentSearchResult(final GetDocumentListRequest param,
			final String userCode, final String temporaryFilesDir, final String filesNotFoundMessageBase,
			final String currentRequestUserCode, final Long documentRetentionDeadlineDays,
			final String digidocConfigFile) throws SQLException {

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		if (param.isHasBeenViewed() == null) {
			parameterMap.put("hasBeenViewed", Arrays.asList(null, Hibernate.INTEGER).toArray());
		} else if (param.isHasBeenViewed() == false) {
			parameterMap.put("hasBeenViewed", Arrays.asList(0, Hibernate.INTEGER).toArray());
		} else if (param.isHasBeenViewed() == true) {
			parameterMap.put("hasBeenViewed", Arrays.asList(1, Hibernate.INTEGER).toArray());
		}
		parameterMap.put("userCode", Arrays.asList(userCode, Hibernate.STRING).toArray());
		parameterMap.put("searchPhrase", Arrays.asList(param.getSearchPhrase() == null || param.getSearchPhrase().length() == 0 ? null : "%" + param.getSearchPhrase().toLowerCase() + "%", Hibernate.STRING).toArray());
		if (param.isIsDeflated() == null) {
			parameterMap.put("deflated", Arrays.asList(null, Hibernate.INTEGER).toArray());
		} else if(param.isIsDeflated() == false) {
			parameterMap.put("deflated", Arrays.asList(0, Hibernate.INTEGER).toArray());
		} else if(param.isIsDeflated() == true) {
			parameterMap.put("deflated", Arrays.asList(1, Hibernate.INTEGER).toArray());
		}
		parameterMap.put("periodStart", Arrays.asList(param.getPeriodStart() == null ? null : param.getPeriodStart().toDate(), Hibernate.DATE).toArray());
		parameterMap.put("periodEnd", Arrays.asList(param.getPeriodEnd() == null ? null : param.getPeriodEnd().toDate(), Hibernate.DATE).toArray());
		parameterMap.put("folder", Arrays.asList(param.getFolder() == null || param.getFolder().length() == 0 ? null : param.getFolder().toLowerCase(), Hibernate.STRING).toArray());
		parameterMap.put("eformUseId", Arrays.asList(param.getEformUseId(), Hibernate.LONG).toArray());
		if (param.getSigned() == null) {
			parameterMap.put("signed", Arrays.asList(null, Hibernate.INTEGER).toArray());
		} else if(param.getSigned() == false) {
			parameterMap.put("signed", Arrays.asList(0, Hibernate.INTEGER).toArray());
		} else if(param.getSigned() == true) {
			parameterMap.put("signed", Arrays.asList(1, Hibernate.INTEGER).toArray());
		}

		StringBuilder selectSql = new StringBuilder("SELECT * FROM (\r\n");
		StringBuilder countSql = new StringBuilder("SELECT count(*) total FROM (\r\n");
		StringBuilder sql = new StringBuilder( 
				"	SELECT results.id, results.guid, results.title, results.type, results.creator_code, results.creator_name, " +
				"			results.creator_user_code, results.creator_user_name, results.creation_date, results.remote_application, " +
				"			results.last_modified_date, results.document_dvk_status_id, results.dvk_id, results.document_wf_status_id, " +
				"			results.parent_id, results.locked, results.locking_date, results.signable, results.deflated, " +
				"			results.deflate_date, results.deleted, results.invisible_to_owner, results.signed, results.migrated, " +
				"			results.eform_use_id, results.content, results.files_size_bytes, results.sender_receiver, " +
				"			row_number() over() AS rnum FROM (\r\n" +
//				"		SELECT documents.*, id_size_shared.file_size files_size_bytes, CASE WHEN documents.creator_code != :userCode THEN documents.creator_name ELSE id_size_shared.shared_to END sender_receiver FROM (\r\n" +
				"		SELECT documents.id, documents.guid, documents.title, documents.type, documents.creator_code, documents.creator_name, " +
				"				documents.creator_user_code, documents.creator_user_name, documents.creation_date, documents.remote_application, " +
				"				documents.last_modified_date, documents.document_dvk_status_id, documents.dvk_id, documents.document_wf_status_id, " +
				"				documents.parent_id, documents.locked, documents.locking_date, documents.signable, documents.deflated, " +
				"				documents.deflate_date, documents.deleted, documents.invisible_to_owner, documents.signed, documents.migrated, " +				
				"				documents.eform_use_id, documents.content, " +
				"				id_size_shared.file_size files_size_bytes, " +
				"				CASE WHEN documents.creator_code != :userCode THEN documents.creator_name ELSE id_size_shared.shared_to END sender_receiver, " +
				"				documents.creator_name sender, " +
				"				id_size_shared.shared_to receiver" +
				"		FROM (\r\n" +
//				"			SELECT id_size.id, id_size.file_size, LISTAGG(COALESCE(sharings.user_name, sharings.user_email), ', ') WITHIN GROUP (ORDER BY LOWER(sharings.user_name)) shared_to FROM (\r\n" +
				"			SELECT id_size.id, id_size.file_size, STRING_AGG(COALESCE(sharings.user_name, sharings.user_email), ', ' ORDER BY LOWER(sharings.user_name)) AS shared_to FROM (\r\n" +				
				"				SELECT ids.id, sum(files.file_size_bytes) as file_size FROM (\r\n" + 
				"					SELECT\r\n" + 
				"						DISTINCT d.id\r\n" + 
				"					FROM document d\r\n" + 
				"					LEFT JOIN document_sharing ds ON ds.document_id = d.id\r\n" + 
				"					LEFT JOIN document_file df ON df.document_id = d.id\r\n" + 
				"					LEFT JOIN document_history dh ON dh.document_id = d.id AND :hasBeenViewed IS NOT NULL AND dh.document_history_type = 'mark_viewed' AND dh.user_code = :userCode\r\n" + 
				"					LEFT JOIN signature s ON s.document_id = d.id AND :searchPhrase IS NOT NULL\r\n" + 
				"					WHERE\r\n" + 
				"						COALESCE(d.deleted, 0) = 0\r\n" + 
				"						AND (d.creator_code != :userCode OR COALESCE(d.invisible_to_owner, 0) = 0)\r\n" + 
				"						AND (\r\n" + 
				"							(d.creator_code = :userCode)\r\n" + 
				"							OR (\r\n" + 
				"								COALESCE(ds.deleted, 0) = 0\r\n" + 
				"								AND ds.user_code = :userCode\r\n" + 
				"							)\r\n" + 
				"						)\r\n" + 
				"						AND (\r\n" + 
				"							('local' != COALESCE(:folder, 'X'))\r\n" + 
				"							OR ('local' = :folder AND d.creator_code = :userCode AND ds.id IS NULL)\r\n" + 
				"						)\r\n" + 
				"						AND (\r\n" + 
				"							('incoming' != COALESCE(:folder, 'X'))\r\n" + 
				"							OR ('incoming' = :folder AND d.creator_code != :userCode AND ds.id IS NOT NULL)\r\n" + 
				"						)\r\n" + 
				"						AND (\r\n" + 
				"							('outgoing' != COALESCE(:folder, 'X'))\r\n" + 
				"							OR ('outgoing' = :folder AND d.creator_code = :userCode AND ds.id IS NOT NULL)\r\n" + 
				"						)\r\n" + 
				"						AND (\r\n" + 
				"							(:hasBeenViewed IS NULL)\r\n" + 
				"							OR (\r\n" + 
				"								1 = :hasBeenViewed\r\n" + 
				"								AND (\r\n" + 
				"									d.creator_code = :userCode\r\n" + 
				"									OR dh.id IS NOT NULL\r\n" + 
				"								)\r\n" + 
				"							)\r\n" + 
				"							OR (\r\n" + 
				"								0 = :hasBeenViewed\r\n" + 
				"								AND d.creator_code != :userCode\r\n" + 
				"								AND dh.id IS NULL\r\n" + 
				"							)\r\n" + 
				"						)\r\n" + 
				"						AND (:deflated IS NULL OR COALESCE(d.deflated, 0) = :deflated)\r\n" + 
				"						AND (\r\n" + 
				"							:searchPhrase IS NULL\r\n" + 
				"							OR (\r\n" + 
				"								LOWER(d.title) LIKE :searchPhrase\r\n" + 
				"								OR LOWER(d.creator_code) LIKE :searchPhrase\r\n" + 
				"								OR LOWER(d.creator_name) LIKE :searchPhrase\r\n" + 
				"								OR LOWER(s.signer_code) LIKE :searchPhrase\r\n" + 
				"								OR LOWER(s.signer_name) LIKE :searchPhrase\r\n" + 
				"								OR LOWER(ds.user_code) LIKE :searchPhrase\r\n" + 
				"								OR LOWER(ds.user_name) LIKE :searchPhrase\r\n" + 
				"								OR LOWER(df.file_name) LIKE :searchPhrase\r\n" + 
				"								OR LOWER(df.description) LIKE :searchPhrase\r\n" + 
				"							)\r\n" + 
				"						)\r\n" + 
				"						AND (\r\n" + 
				"							CAST(:periodStart AS TIMESTAMP) IS NULL\r\n" + 
				"							OR CAST(d.last_modified_date AS DATE) >= :periodStart\r\n" + 
				"						)\r\n" + 
				"						AND (\r\n" + 
				"							CAST(:periodEnd AS TIMESTAMP) IS NULL\r\n" + 
				"							OR CAST(d.last_modified_date AS DATE) <= :periodEnd\r\n" + 
				"						)\r\n" +
				"						AND (:eformUseId IS NULL OR d.eform_use_id = :eformUseId)\r\n" +
				"						AND (:signed IS NULL OR COALESCE(d.signed, 0) = :signed)");

		// no NULL support in IN
		
		// Document type
		List<String> documentTypes = new ArrayList<String>();
		if ((param.getDocumentTypes() != null) && (param.getDocumentTypes().getDocumentType() != null)
				&& !param.getDocumentTypes().getDocumentType().isEmpty()) {

			for (String docType : param.getDocumentTypes().getDocumentType()) {
				documentTypes.add(docType);
			}
			if (documentTypes.size() > 0) {
				sql.append("\r\n" + "						AND (d.type in (:documentTypes ))");
			}
		}

		// Document DVK status
		List<Long> documentDvkStatuses = new ArrayList<Long>();
		if ((param.getDocumentDvkStatuses() != null) && (param.getDocumentDvkStatuses().getStatusId() != null)
				&& !param.getDocumentDvkStatuses().getStatusId().isEmpty()) {

			for (Long statusId : param.getDocumentDvkStatuses().getStatusId()) {
				documentDvkStatuses.add(statusId);
			}
			if (documentDvkStatuses.size() > 0) {
				sql.append("\r\n" + "						AND (d.document_dvk_status_id in (:documentDvkStatuses ))");
			}
		}

		// Document workflow status
		List<Long> documentWfStatuses = new ArrayList<Long>();
		if ((param.getDocumentWorkflowStatuses() != null)
				&& (param.getDocumentWorkflowStatuses().getStatusId() != null)
				&& !param.getDocumentWorkflowStatuses().getStatusId().isEmpty()) {

			for (Long statusId : param.getDocumentWorkflowStatuses().getStatusId()) {
				documentWfStatuses.add(statusId);
			}
			if (documentWfStatuses.size() > 0) {
				sql.append("\r\n" + "						AND (d.document_wf_status_id in (:documentWfStatuses ))");
			}
		}

		// Creator application
		List<String> creatorApplications = new ArrayList<String>();
		if ((param.getCreatorApplications() != null)
				&& (param.getCreatorApplications().getCreatorApplication() != null)
				&& !param.getCreatorApplications().getCreatorApplication().isEmpty()) {

			for (String appName : param.getCreatorApplications().getCreatorApplication()) {
				creatorApplications.add(appName);
			}
			if (creatorApplications.size() > 0) {
				sql.append("\r\n" + "						AND (d.remote_application in (:creatorApplications ))");
			}
		}

		sql.append("\r\n" +
				"				) AS ids\r\n" +
				"				JOIN document documents ON documents.id = ids.id\r\n" +
				"				LEFT JOIN document_file files ON files.document_id = documents.id AND COALESCE(files.deleted, 0) = 0 AND COALESCE(documents.deflated, 0) = 0 AND COALESCE(documents.invisible_to_owner, 0) = 0 AND files.document_file_type_id NOT IN (:ddocContainers )\r\n" +
				"				GROUP BY ids.id\r\n" +
				"			) AS id_size\r\n" +
				"			LEFT JOIN document_sharing sharings ON sharings.document_id = id_size.id AND COALESCE(sharings.deleted, 0) = 0\r\n" +
				"			GROUP BY id_size.id, id_size.file_size\r\n" +
				"		) AS id_size_shared\r\n" +
				"		JOIN document documents ON documents.id = id_size_shared.id\r\n" +
				"		ORDER BY ");

		// Search result ordering
		String sortBy = documentFieldXmlNameToDbName("last_modified");
		String sortOrder = "DESC NULLS LAST";
		if (!Util.isNullOrEmpty(param.getSortBy())) {
			String sortByDbName = documentFieldXmlNameToDbName(param.getSortBy());
			if (sortByDbName != null) {
				sortBy = sortByDbName;
				sortOrder = "ASC NULLS FIRST";
			} else {
				AditCodedException aditCodedException = new AditCodedException(
						"request.getDocumentList.incorrectSortByParameter");
				aditCodedException.setParameters(new Object[] { param.getSortBy() });
				throw aditCodedException;
			}
		}
		if (!Util.isNullOrEmpty(param.getSortOrder())) {
			if ("asc".equalsIgnoreCase(param.getSortOrder())) {
				sortOrder = "ASC NULLS FIRST";
			} else if ("desc".equalsIgnoreCase(param.getSortOrder())) {
				sortOrder = "DESC NULLS LAST";
			} else {
				AditCodedException aditCodedException = new AditCodedException(
						"request.getDocumentList.incorrectSortOrderParameter");
				aditCodedException.setParameters(new Object[] { param.getSortOrder() });
				throw aditCodedException;
			}
		}
		sql.append(sortBy).append(" ").append(sortOrder);

		sql.append(",\r\n" +
				"		documents.last_modified_date DESC, documents.id ASC\r\n" +
				"	) AS results");

		selectSql.append(sql);
		countSql.append(sql);

		// Then apply paging and ordering and get the final list
		int startIndex = (param.getStartIndex() != null) ? param.getStartIndex().intValue() : 0;
		if (startIndex < 1) {
			startIndex = 1;
		}
		int maxResults = (param.getMaxResults() != null) ? param.getMaxResults().intValue() : 20;
		if (maxResults < 0) {
			// It is OK to ask 0 results because it is the only way to get total number
			// of documents without retrieving any of them.
			maxResults = 20;
		} else if (maxResults > 100) {
			maxResults = 100;
		}

		// limit for select
		selectSql.append("\r\n" +
				"	LIMIT :end");

		selectSql.append("\r\n" +
				") ");
		countSql.append("\r\n" +
				") ");

		// offset for select
		selectSql.append("AS total where rnum >= :start");

		selectSql.append("\r\n" +
				"order by rnum asc");
		countSql.append("\r\n" +
				"AS total");

		List<Document> documents = null;
		Integer count = 0;
		Session session = getSession();

		try {
			SQLQuery countQuery = session.createSQLQuery(countSql.toString());
			SQLQuery selectQuery = session.createSQLQuery(selectSql.toString());
			for (Iterator<String> iterator = parameterMap.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next();
				Object[] value = (Object[]) parameterMap.get(key);
				selectQuery.setParameter(key, value[0], (Type) value[1]);
				countQuery.setParameter(key, value[0], (Type) value[1]);
			}
			Long[] ddocContainers = new Long[] { DocumentService.FILETYPE_SIGNATURE_CONTAINER,
					DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT };
			selectQuery.setParameterList("ddocContainers", ddocContainers);
			countQuery.setParameterList("ddocContainers", ddocContainers);
			if (documentTypes.size() > 0) {
				selectQuery.setParameterList("documentTypes", documentTypes);
				countQuery.setParameterList("documentTypes", documentTypes);
			}
			if (documentDvkStatuses.size() > 0) {
				selectQuery.setParameterList("documentDvkStatuses", documentDvkStatuses);
				countQuery.setParameterList("documentDvkStatuses", documentDvkStatuses);
			}
			if (documentWfStatuses.size() > 0) {
				selectQuery.setParameterList("documentWfStatuses", documentWfStatuses);
				countQuery.setParameterList("documentWfStatuses", documentWfStatuses);
			}
			if (creatorApplications.size() > 0) {
				selectQuery.setParameterList("creatorApplications", creatorApplications);
				countQuery.setParameterList("creatorApplications", creatorApplications);
			}
			selectQuery.setParameter("start", startIndex);
			selectQuery.setParameter("end", startIndex + maxResults - 1);	//-1 is because Oracle 'LIMIT' was rownum < :end

			countQuery.addScalar("total", Hibernate.INTEGER);
			count = (Integer) countQuery.list().get(0);
			selectQuery.addEntity(Document.class);
			documents = selectQuery.list();

			GetDocumentListResponseAttachment innerResult = new GetDocumentListResponseAttachment();
			innerResult.setDocumentList(new ArrayList<OutputDocument>());

			for (Document doc : documents) {
				OutputDocument resultDoc = dbDocumentToOutputDocument(doc, null, true, true, false,
						param.getFileTypes(), temporaryFilesDir, filesNotFoundMessageBase,
						currentRequestUserCode, documentRetentionDeadlineDays, digidocConfigFile);
				innerResult.getDocumentList().add(resultDoc);
			}

			innerResult.setTotal(count);
			return innerResult;
		} catch (DataAccessException ex) {
			if (ex.getRootCause() instanceof AditException) {
				throw (AditException) ex.getRootCause();
			} else {
				throw ex;
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	
    /**
     * Fetches document from database by dhl id
     * @param dhlId
     * @return document
     */
    @SuppressWarnings("unchecked")
    public Document getDocumentByDhlId(final Long dhlId) {
        if (dhlId==null || dhlId <= 0) {
            throw new IllegalArgumentException("Document ID must be a positive integer. Currently supplied ID was "
                    + dhlId + ".");
        }
        Document result = null;
        Session session = null;
        try {
            logger.debug("Finding document for dhlids " + dhlId);
            String sql = "select document from Document document join document.documentSharings documentSharings where documentSharings.dvkId = :dhlId";
            session = this.getSessionFactory().openSession();
            Query query = session.createQuery(sql);
            query.setParameter("dhlId", dhlId);
            query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            result =  (Document) query.uniqueResult();
        } catch (Exception e) {
        	throw new AditInternalException("Error while fetching document: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return result;
    }


    /**
     * Fetches documents from database by list of dhl ids
     * @param dhlIds list of dhl ids
     * @return document
     */
    @SuppressWarnings("unchecked")
    public List<Document> getDocumentsByDhlIds(final List<Long> dhlIds) {
    	if(dhlIds==null || dhlIds.size()==0) {
   		 throw new IllegalArgumentException("Dhl ids list is empty");
	   	}
	   	for(Long dhlId : dhlIds) {
		        if (dhlId <= 0) {
		            throw new IllegalArgumentException("Document ID must be a positive integer. Currently supplied ID was "
		                    + dhlId + ".");
		        }
	   	}
        List<Document> result = null;
        Session session = null;
        try {
            logger.debug("Finding documents for dhlids: " + dhlIds);
            String sql = "select document from Document document join document.documentSharings documentSharings where documentSharings.dvkId in (:dhlIds)";
            session = this.getSessionFactory().openSession();
            Query query = session.createQuery(sql);
            query.setParameterList("dhlIds", dhlIds);
            query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            result =  query.list();
//            result = this.getHibernateTemplate().find(
//                    "select userContact from UserContact userContact join userContact.user user where userContact.user = ? and user.active = ? order by userContact.lastUsedDate desc", aditUser, true);
        } catch (Exception e) {
        	throw new AditInternalException("Error while fetching document: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        logger.debug("Found" + result.size() + " documents");

        return result;
    }

    /**
     * Get list of DocumentSendStatus objects by list of dhl ids.
     *
     * @param dhlIds document dhl IDs
     * @return documentSendStatusList
     * @throws Exception if any sort of exception occurred
     */
    public List<DocumentSendStatus> getDocumentsForSendStatus(final List<Long> dhlIds)
            throws Exception {
    	List<DocumentSendStatus> result = new ArrayList<DocumentSendStatus>();
		List<Document> documents = getDocumentsByDhlIds(dhlIds);
		for(Document document : documents) {
        	logger.debug("Attempting to convert document to outptudocument" + document.getId());
        	Collection<DocumentSendStatus> documentSendStatus = getDocumentSendStatusFromDocumentForSendStatus(document, dhlIds);
        	result.addAll(documentSendStatus);
        }
        return result;
    }

    /**
     *
     * @param document - document onbject
     * @return documentSendStatus
     */
    private Collection<DocumentSendStatus> getDocumentSendStatusFromDocumentForSendStatus (Document document, final List<Long> dhlIds) {
        Map<Long,DocumentSendStatus> docs = new TreeMap<Long,DocumentSendStatus>();

        if (document.getDocumentSharings()!=null && document.getDocumentSharings().size()>0) {

            for(DocumentSharing documentSharing : document.getDocumentSharings()) {
                DocumentSendStatus result = null;
                List<DocumentSharingRecipientStatus> recipients = null;
                Long dhlId = documentSharing.getDvkId();
                if (dhlId != null && dhlId > 0 && dhlIds.contains(dhlId)) {
                	result = docs.get(dhlId);
                    if (result == null) {
                        result = new DocumentSendStatus();
                        recipients = new ArrayList<DocumentSharingRecipientStatus>();
                        result.setRecipients(recipients);
                        result.setDhlId(dhlId);
                        docs.put(dhlId, result);
                    } else {
                    	recipients = result.getRecipients();
                    }
                    DocumentSharingRecipientStatus recipient = new DocumentSharingRecipientStatus();
                    recipient.setCode(documentSharing.getUserCode());
                    recipient.setName(documentSharing.getUserName());
                    boolean opened = false;
                    if(documentSharing.getFirstAccessDate()!=null) {
                        opened = true;
                    }
                    recipient.setHasBeenViewed(opened);
                    recipient.setOpenedTime(documentSharing.getFirstAccessDate());
                    recipients.add(recipient);
                }
            }
        }
        return docs.values();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Document> findAllWaitingToBeSentToDVK() {
        List<Document> results = new ArrayList<Document>();

        Session session = null;
        try {
            session = this.getSessionFactory().openSession();
            DetachedCriteria documentCriteria = DetachedCriteria.forClass(DocumentSharing.class)
                    .add(Property.forName("documentSharingType").eq(DocumentService.SHARINGTYPE_SEND_DVK))
                    .add(Restrictions.or(
                            Property.forName("documentDvkStatus").isNull(),
                            Property.forName("documentDvkStatus").eq("")))
                    .setProjection(Property.forName("id"));

            Criteria criteria = session.createCriteria(Document.class)
                    .add(Property.forName("documentId").in(documentCriteria));
            results = criteria.list();
        } catch (Exception e) {
            throw new AditInternalException("Error while fetching DVK DocumentSharings: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return results;
    }
}