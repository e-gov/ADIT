package ee.adit.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
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
import ee.adit.pojo.DocumentSendingData;
import ee.adit.pojo.DocumentSendingRecipient;
import ee.adit.pojo.DocumentSharingData;
import ee.adit.pojo.DocumentSharingRecipient;
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

/**
 * Document data access class. Provides methods for retrieving and manipulating
 * document data.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentDAO extends HibernateDaoSupport {

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
     *     Full path to DigiDoc library configuration file.
     *
     * @return document list
     */
    @SuppressWarnings("unchecked")
    public GetDocumentListResponseAttachment getDocumentSearchResult(final GetDocumentListRequest param,
            final String userCode, final String temporaryFilesDir, final String filesNotFoundMessageBase,
            final String currentRequestUserCode, final Long documentRetentionDeadlineDays, final String digidocConfigFile) {

        GetDocumentListResponseAttachment result = null;

        try {
            result = (GetDocumentListResponseAttachment) getHibernateTemplate().execute(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    GetDocumentListResponseAttachment innerResult = new GetDocumentListResponseAttachment();
                    innerResult.setDocumentList(new ArrayList<OutputDocument>());
                    Criteria criteria = session.createCriteria(Document.class, "doc");

                    // General parameters
                    criteria.add(Restrictions.or(Restrictions.isNull("deleted"), Restrictions.eq("deleted", false)));

                    // Document "folder" (local, incoming, outgoing)
                    if (!Util.isNullOrEmpty(param.getFolder())) {
                        if (param.getFolder().equalsIgnoreCase("local")) {
                            criteria.add(Restrictions.eq("creatorCode", userCode));
                            criteria.add(Restrictions.or(Restrictions.isNull("invisibleToOwner"), Restrictions.eq("invisibleToOwner", false)));
                            criteria.add(Restrictions.or(Restrictions.isNull("documentSharings"), Restrictions.isEmpty("documentSharings")));
                        } else if (param.getFolder().equalsIgnoreCase("incoming")) {
                            // "Incoming" means that:
                            // - someone else is document creator
                            // - document has been shared to user
                            criteria.add(Restrictions.ne("creatorCode", userCode));
                            DetachedCriteria sharedToMeSubquery = DetachedCriteria.forClass(Document.class, "doc1")
	                            .createCriteria("documentSharings", "sh1")
	                            .add(Restrictions.eq("userCode", userCode))
	                            .add(Restrictions.or(Restrictions.isNull("deleted"), Restrictions.eq("deleted", false)))
                                .add(Property.forName("doc.id").eqProperty("doc1.id"))
                                .setProjection(Projections.id());
                            criteria.add(Subqueries.exists(sharedToMeSubquery));
                        } else if (param.getFolder().equalsIgnoreCase("outgoing")) {
                            // "Outgoing" means that:
                            // - user is document owner
                            // - and document has been sent or shared to
                            // someone else
                            criteria.add(Restrictions.eq("creatorCode", userCode));
                            criteria.add(Restrictions.or(Restrictions.isNull("invisibleToOwner"), Restrictions.eq("invisibleToOwner", false)));
                            criteria.add(Restrictions.isNotNull("documentSharings"));
                            criteria.add(Restrictions.isNotEmpty("documentSharings"));
                        } else {
                        	// If incorrect folder is specified then return all documents
                        	// accessible to given user.
                            DetachedCriteria sharedToMeSubquery = DetachedCriteria.forClass(Document.class, "doc1")
                                    .createCriteria("documentSharings", "sh1")
                                    .add(Restrictions.eq("userCode", userCode))
                                    .add(Restrictions.or(Restrictions.isNull("deleted"), Restrictions.eq("deleted", false)))
                                    .add(Property.forName("doc.id").eqProperty("doc1.id"))
                                    .setProjection(Projections.id());
                            criteria.add(Restrictions.or(
                            	Restrictions.and(
                            		Restrictions.eq("creatorCode", userCode),
                            		Restrictions.or(Restrictions.isNull("invisibleToOwner"), Restrictions.eq("invisibleToOwner", false))),
                            	Subqueries.exists(sharedToMeSubquery)));
                        }
                    } else {
                    	// If no folder is specified then return all documents accessible to given user.
                    	DetachedCriteria sharedToMeSubquery = DetachedCriteria.forClass(Document.class, "doc1")
	                        .createCriteria("documentSharings", "sh1")
	                        .add(Restrictions.eq("userCode", userCode))
	                        .add(Restrictions.or(Restrictions.isNull("deleted"), Restrictions.eq("deleted", false)))
	                        .add(Property.forName("doc.id").eqProperty("doc1.id"))
	                        .setProjection(Projections.id());
                        criteria.add(Restrictions.or(
                        	Restrictions.and(
                        		Restrictions.eq("creatorCode", userCode),
                        		Restrictions.or(Restrictions.isNull("invisibleToOwner"), Restrictions.eq("invisibleToOwner", false))),
	                		Subqueries.exists(sharedToMeSubquery)));
                    }

                    // Document type
                    if ((param.getDocumentTypes() != null) && (param.getDocumentTypes().getDocumentType() != null)
                            && !param.getDocumentTypes().getDocumentType().isEmpty()) {

                        Disjunction disjunction = Restrictions.disjunction();
                        for (String docType : param.getDocumentTypes().getDocumentType()) {
                            disjunction.add(Restrictions.eq("documentType", docType));
                        }
                        criteria.add(disjunction);
                    }

                    // Document DVK status
                    if ((param.getDocumentDvkStatuses() != null)
                            && (param.getDocumentDvkStatuses().getStatusId() != null)
                            && !param.getDocumentDvkStatuses().getStatusId().isEmpty()) {

                        Disjunction disjunction = Restrictions.disjunction();
                        for (Long statusId : param.getDocumentDvkStatuses().getStatusId()) {
                            disjunction.add(Restrictions.eq("documentDvkStatusId", statusId));
                        }
                        criteria.add(disjunction);
                    }

                    // Document workflow status
                    if ((param.getDocumentWorkflowStatuses() != null)
                            && (param.getDocumentWorkflowStatuses().getStatusId() != null)
                            && !param.getDocumentWorkflowStatuses().getStatusId().isEmpty()) {

                        Disjunction disjunction = Restrictions.disjunction();
                        for (Long statusId : param.getDocumentWorkflowStatuses().getStatusId()) {
                            disjunction.add(Restrictions.eq("documentWfStatusId", statusId));
                        }
                        criteria.add(disjunction);
                    }

                    // Has the document been viewed?
                    // - if user is document creator, then it is viewed
                    // - if document was sent to user then check viewing status
                    // - if document was shared to user then check viewing status
                    if (param.isHasBeenViewed() != null) {
                        DetachedCriteria historySubquery = DetachedCriteria.forClass(Document.class, "doc5")
                                .createCriteria("documentHistories", "history").add(
                                        Restrictions.eq("userCode", userCode)).add(
                                        Restrictions.eq("documentHistoryType", DocumentService.HISTORY_TYPE_MARK_VIEWED))
                                .add(Property.forName("doc.id").eqProperty("doc5.id")).setProjection(Projections.id());

                        if (param.isHasBeenViewed()) {
                            Disjunction disjunction = Restrictions.disjunction();
                            disjunction.add(Restrictions.eq("creatorCode", userCode));
                            disjunction.add(Subqueries.exists(historySubquery));
                            criteria.add(disjunction);
                        } else {
                        	criteria.add(Restrictions.ne("creatorCode", userCode));
                        	criteria.add(Subqueries.notExists(historySubquery));
                        }
                    }

                    // Include deflated documents
                    if (param.isIsDeflated() != null) {
	                    if (param.isIsDeflated()) {
	                    	criteria.add(Restrictions.eq("deflated", true));
	                    } else {
	                        criteria.add(Restrictions.or(Restrictions.isNull("deflated"), Restrictions.eq("deflated", false)));
	                    }
                    }

                    // Creator application
                    if ((param.getCreatorApplications() != null)
                            && (param.getCreatorApplications().getCreatorApplication() != null)
                            && !param.getCreatorApplications().getCreatorApplication().isEmpty()) {

                        Disjunction disjunction = Restrictions.disjunction();
                        for (String appName : param.getCreatorApplications().getCreatorApplication()) {
                            disjunction.add(Restrictions.eq("remoteApplication", appName));
                        }
                        criteria.add(disjunction);
                    }

                    // Phrase search
                    if ((param.getSearchPhrase() != null) && (param.getSearchPhrase().length() > 0)) {
                        Disjunction disjunction = Restrictions.disjunction();
                        disjunction.add(Restrictions.ilike("title", param.getSearchPhrase(), MatchMode.ANYWHERE));
                        disjunction.add(Restrictions.ilike("creatorCode", param.getSearchPhrase(), MatchMode.ANYWHERE));
                        disjunction.add(Restrictions.ilike("creatorName", param.getSearchPhrase(), MatchMode.ANYWHERE));

                        DetachedCriteria sigSubquery = DetachedCriteria.forClass(Document.class, "doc2")
                                .createCriteria("signatures", "sig").add(
                                        Restrictions.or(Restrictions.ilike("signerCode", param.getSearchPhrase(),
                                                MatchMode.ANYWHERE), Restrictions.ilike("signerName", param
                                                .getSearchPhrase(), MatchMode.ANYWHERE))).add(
                                        Property.forName("doc.id").eqProperty("doc2.id")).setProjection(
                                        Projections.id());
                        disjunction.add(Subqueries.exists(sigSubquery));

                        DetachedCriteria shareSubquery = DetachedCriteria.forClass(Document.class, "doc3")
                                .createCriteria("documentSharings", "sh").add(
                                        Restrictions.or(Restrictions.ilike("userCode", param.getSearchPhrase(),
                                                MatchMode.ANYWHERE), Restrictions.ilike("userName", param
                                                .getSearchPhrase(), MatchMode.ANYWHERE))).add(
                                        Property.forName("doc.id").eqProperty("doc3.id")).setProjection(
                                        Projections.id());
                        disjunction.add(Subqueries.exists(shareSubquery));

                        DetachedCriteria filesSubquery = DetachedCriteria.forClass(Document.class, "doc4")
                                .createCriteria("documentFiles", "files").add(
                                        Restrictions.or(Restrictions.ilike("fileName", param.getSearchPhrase(),
                                                MatchMode.ANYWHERE), Restrictions.ilike("description", param
                                                .getSearchPhrase(), MatchMode.ANYWHERE))).add(
                                        Property.forName("doc.id").eqProperty("doc4.id")).setProjection(
                                        Projections.id());
                        disjunction.add(Subqueries.exists(filesSubquery));

                        criteria.add(disjunction);
                    }

                    // Last modified date range
                    if (param.getPeriodStart() != null) {
                    	criteria.add(Restrictions.isNotNull("lastModifiedDate"));
                    	criteria.add(Restrictions.ge("lastModifiedDate", param.getPeriodStart().toDate()));
                    }
                    if (param.getPeriodEnd() != null) {
                    	// Increase end date by 1 day to get documents modified
                    	// before 00:00 of next day.
                    	Calendar cal = Calendar.getInstance();
                    	cal.setTime(param.getPeriodEnd().toDate());
                    	cal.add(Calendar.DATE, 1);
                    	criteria.add(Restrictions.isNotNull("lastModifiedDate"));
                    	criteria.add(Restrictions.lt("lastModifiedDate", cal.getTime()));
                    } else if (param.getPeriodStart() != null) {
                    	// If period start date is set and end date is not then return only
                    	// documents modified within 7 days from period start date.
                    	Calendar cal = Calendar.getInstance();
                    	cal.setTime(param.getPeriodStart().toDate());
                    	cal.add(Calendar.DATE, 8);
                    	criteria.add(Restrictions.isNotNull("lastModifiedDate"));
                    	criteria.add(Restrictions.lt("lastModifiedDate", cal.getTime()));
                    }


                    // First get total number of matching documents
                    criteria.setProjection(Projections.rowCount());
                    innerResult.setTotal(((Long) criteria.uniqueResult()).intValue());
                    criteria.setProjection(null);
                    criteria.setResultTransformer(Criteria.ROOT_ENTITY);

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

                    criteria.setFirstResult(startIndex - 1);
                    criteria.setMaxResults(maxResults);

                    // Search result ordering
                    String sortBy = "lastModifiedDate";
                    String sortOrder = "desc";
                    if (!Util.isNullOrEmpty(param.getSortBy())) {
                    	String sortByDbName = documentFieldXmlNameToDbName(param.getSortBy());
	                    if (Arrays.asList("id", "guid", "title", "document_type", "created", "last_modified", "dvk_id").contains(param.getSortBy())
	                    	&& Util.classContainsField(Document.class, sortByDbName)) {
	                    	sortBy = sortByDbName;
	                    	sortOrder = "asc";
                    	} else {
                            AditCodedException aditCodedException = new AditCodedException("request.getDocumentList.incorrectSortByParameter");
                            aditCodedException.setParameters(new Object[] {param.getSortBy()});
                            throw aditCodedException;
                    	}
                    }
                    if (!Util.isNullOrEmpty(param.getSortOrder())) {
                    	if ("asc".equalsIgnoreCase(param.getSortOrder()) || "desc".equalsIgnoreCase(param.getSortOrder())) {
                    		sortOrder = param.getSortOrder().toLowerCase();
                    	} else {
                            AditCodedException aditCodedException = new AditCodedException("request.getDocumentList.incorrectSortOrderParameter");
                            aditCodedException.setParameters(new Object[] {param.getSortOrder()});
                            throw aditCodedException;
                    	}
                    }

                    if ("asc".equalsIgnoreCase(sortOrder)) {
                    	criteria.addOrder(Order.asc(sortBy).ignoreCase());
                    } else {
                    	criteria.addOrder(Order.desc(sortBy).ignoreCase());
                    }

                    // If primary sorting is done by field that can be NULL or have
                    // same value for multiple documents then add secondary sorting by
                    // last modified date.
                    if (sortBy.equalsIgnoreCase("title") || sortBy.equalsIgnoreCase("documentType") || sortBy.equalsIgnoreCase("dvkId")) {
                    	criteria.addOrder(Order.desc("lastModifiedDate").ignoreCase());
                    }

                    List<Document> docList = criteria.list();

                    for (Document doc : docList) {
                        OutputDocument resultDoc = dbDocumentToOutputDocument(doc, null, true, true, false, param.getFileTypes(),
                                temporaryFilesDir, filesNotFoundMessageBase, currentRequestUserCode, documentRetentionDeadlineDays,
                                digidocConfigFile);
                        innerResult.getDocumentList().add(resultDoc);
                    }

                    return innerResult;
                }
            });
        } catch (DataAccessException ex) {
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
                	&& internalIdList.contains((Long) docFile.getId())) {
                    internalIdList.remove((Long) docFile.getId());
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
	                            blobDataStream = docFile.getFileData().getBinaryStream();
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
        		SimplifiedDigiDocParser.extractFileContentsFromDdoc(
	        		signatureContainerFile.getFileData().getBinaryStream(),
	        		outputFilesList, temporaryFilesDir);
        	} catch (IOException ex) {
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
                        rec.setHasBeenViewed((sharing.getLastAccessDate() != null));
                        rec.setName(sharing.getUserName());
                        rec.setOpenedTime(sharing.getLastAccessDate());
                        rec.setWorkflowStatusId(sharing.getDocumentWfStatus());
                        rec.setSharedTime(sharing.getCreationDate());
                        rec.setReasonForSharing(sharing.getTaskDescription());
                        rec.setSharedForSigning("sign".equalsIgnoreCase(sharing.getDocumentSharingType()));
                        sharingData.getUserList().add(rec);
                    } else {
                        DocumentSendingRecipient rec = new DocumentSendingRecipient();
                        rec.setCode(sharing.getUserCode());
                        rec.setHasBeenViewed((sharing.getLastAccessDate() != null));
                        rec.setName(sharing.getUserName());
                        rec.setOpenedTime(sharing.getLastAccessDate());
                        rec.setWorkflowStatusId(sharing.getDocumentWfStatus());
                        rec.setDvkStatusId(sharing.getDocumentDvkStatus());
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
        result.setWorkflowStatusId(doc.getDocumentWfStatusId());

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
        }

        // Document folder
        if (currentRequestUserCode.equalsIgnoreCase(doc.getCreatorCode())
        	&& ((doc.getDocumentSharings() == null) || doc.getDocumentSharings().isEmpty())) {
        	result.setFolder("local");
        } else if (currentRequestUserCode.equalsIgnoreCase(doc.getCreatorCode())) {
        	result.setFolder("outgoing");
        } else {
        	result.setFolder("incoming");
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
                    if (currentRequestUserCode.equalsIgnoreCase(sharing.getUserCode())) {
                    	result.setHasBeenViewed(sharing.getLastAccessDate() != null);
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

                    // Blob fileData = Hibernate.createBlob(fileInputStream,
                    // length, session);
                    Blob fileData = Hibernate.createBlob(fileInputStream, length);
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
        List<Document> existingDocuments = this.getHibernateTemplate().find(sql);

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
     *     Field name in XML (WSDL)
     * @return
     *     Field name in database
     */
    private String documentFieldXmlNameToDbName(String xmlName) {
    	if ("document_type".equalsIgnoreCase(xmlName)) {
    		return "documentType";
    	} else if ("last_modified".equalsIgnoreCase(xmlName)) {
    		return "lastModifiedDate";
    	}  else if ("dvk_id".equalsIgnoreCase(xmlName)) {
    		return "dvkId";
    	}  else if ("created".equalsIgnoreCase(xmlName)) {
    		return "creationDate";
    	} else {
    		return xmlName.toLowerCase();
    	}
    }
}
