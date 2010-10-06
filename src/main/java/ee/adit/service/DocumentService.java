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
import java.util.Date;
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

import dvk.api.DVKAPI;
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
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.PrepareSignatureInternalResult;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveItemInternalResult;
import ee.adit.util.Configuration;
import ee.adit.util.Util;
import ee.sk.digidoc.DataFile;
import ee.sk.digidoc.Signature;
import ee.sk.digidoc.SignatureProductionPlace;
import ee.sk.digidoc.SignedDoc;
import ee.sk.digidoc.factory.SAXDigiDocFactory;
import ee.sk.utils.ConfigManager;

/**
 * Implements business logic for document processing. Provides methods for processing 
 * documents (saving, retrieving, performing checks, etc.). Where possible, the actual 
 * data queries are forwarded to DAO classes.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentService {

	/**
	 * Document sharing type code - sign
	 */
	public static final String SharingType_Sign = "sign";
	
	/**
	 * Document sharing type code - share
	 */
	public static final String SharingType_Share = "share";
	
	/**
	 * Document sharing type code - send using DVK
	 */
	public static final String SharingType_SendDvk = "send_dvk";
	
	/**
	 * Document sharing type code - send using ADIT
	 */
	public static final String SharingType_SendAdit = "send_adit";

	/**
	 * Document DVK status - missing
	 */
	public static final Long DVKStatus_Missing = new Long(100);
	
	/**
	 * Document DVK status - waiting
	 */
	public static final Long DVKStatus_Waiting = new Long(101);
	
	/**
	 * Document DVK status - sending
	 */
	public static final Long DVKStatus_Sending = new Long(102);
	
	/**
	 * Document DVK status - sent
	 */
	public static final Long DVKStatus_Sent = new Long(103);
	
	/**
	 * Document DVK status - aborted
	 */
	public static final Long DVKStatus_Aborted = new Long(104);
	
	/**
	 * Document DVK status - received
	 */
	public static final Long DVKStatus_Received = new Long(105);

	/**
	 * DVK fault code used for deleted documents. Inserted to DVK when document deleted.
	 */
	public static final String DVKFaultCodeFor_Deleted = "NO_FAULT: DELETED BY ADIT";
	
	/**
	 * DVK message string for deleted documents. Inserted to DVK when document deleted.
	 */
	public static final String DVKBlobMessage_Deleted = "DELETED BY ADIT";
	
	/**
	 * Document history type code - create
	 */
	public static final String HistoryType_Create = "create";
	
	/**
	 * Document history type code - modify
	 */
	public static final String HistoryType_Modify = "modify";
	
	/**
	 * Document history type code - add file
	 */
	public static final String HistoryType_AddFile = "add_file";
	
	/**
	 * Document history type code - modify file
	 */
	public static final String HistoryType_ModifyFile = "modify_file";
	
	/**
	 * Document history type code - delete file
	 */
	public static final String HistoryType_DeleteFile = "delete_file";
	
	/**
	 * Document history type code - modify status
	 */
	public static final String HistoryType_ModifyStatus = "modify_status";
	
	/**
	 * Document history type code - send
	 */
	public static final String HistoryType_Send = "send";
	
	/**
	 * Document history type code - share
	 */
	public static final String HistoryType_Share = "share";
	
	/**
	 * Document history type code - unshare
	 */
	public static final String HistoryType_UnShare = "unshare";
	
	/**
	 * Document history type code - lock
	 */
	public static final String HistoryType_Lock = "lock";
	
	/**
	 * Document history type code - unlock
	 */
	public static final String HistoryType_UnLock = "unlock";
	
	/**
	 * Document history type code - deflate
	 */
	public static final String HistoryType_Deflate = "deflate";
	
	/**
	 * Document history type code - sign
	 */
	public static final String HistoryType_Sign = "sign";
	
	/**
	 * Document history type code - delete
	 */
	public static final String HistoryType_Delete = "delete";
	
	/**
	 * Document history type code - mark viewed
	 */
	public static final String HistoryType_MarkViewed = "mark_viewed";

	/**
	 * DVK container version used when sending documents using DVK
	 */
	public static final int DVK_CONTAINER_VERSION = 2;

	/**
	 * DVK response message title
	 */
	public static final String DvkErrorResponseMessage_Title = "ADIT vastuskiri";

	/**
	 * DVK response message file name
	 */
	public static final String DvkErrorResponseMessage_FileName = "ADIT_vastuskiri.pdf";
	
	/**
	 * Document type - letter
	 */
	public static final String DocType_Letter = "letter";
	
	/**
	 * Document type - application
	 */
	public static final String DocType_Application = "application";

	/**
	 * DVK receive fail reason - user does not exist
	 */
	public static final Integer DvkReceiveFailReason_UserDoesNotExist = 1;
	
	/**
	 * DVK receive fail reason - user uses DVK to exchange documents
	 */
	public static final Integer DvkReceiveFailReason_UserUsesDvk = 2;

	// Document history description literals
	public static final String DocumentHistoryDescription_Create = "Document created";	
	public static final String DocumentHistoryDescription_Lock = "Document locked";
	public static final String DocumentHistoryDescription_Delete = "Document deleted";
	public static final String DocumentHistoryDescription_DeleteFile = "Document file deleted. ID: ";
	public static final String DocumentHistoryDescription_ModifyStatus = "Document status modified to: ";
	public static final String DocumentHistoryDescription_Modify = "Document modified";
	public static final String DocumentHistoryDescription_ModifyFile = "Document file modified. ID: ";
	
	
	/**
	 * Log4J logger
	 */
	private static Logger LOG = Logger.getLogger(UserService.class);
	
	/**
	 * Message source
	 */
	private MessageSource messageSource;
	
	/**
	 * Document type DAO
	 */
	private DocumentTypeDAO documentTypeDAO;
	
	/**
	 * Document DAO
	 */
	private DocumentDAO documentDAO;
	
	/**
	 * Document file DAO
	 */
	private DocumentFileDAO documentFileDAO;
	
	/**
	 * Document workflow status DAO
	 */
	private DocumentWfStatusDAO documentWfStatusDAO;
	
	/**
	 * Document sharing DAO
	 */
	private DocumentSharingDAO documentSharingDAO;
	
	/**
	 * Document history DAO
	 */
	private DocumentHistoryDAO documentHistoryDAO;
	
	/**
	 * ADIT user DAO
	 */
	private AditUserDAO aditUserDAO;
	
	/**
	 * Configuration
	 */
	private Configuration configuration;
	
	/**
	 * DVK DAO
	 */
	private DvkDAO dvkDAO;

	/**
	 * Checks if document metadata is sufficient and correct for creating a new document.
	 * 
	 * @param document document metadata
	 * @throws AditCodedException if metadata is insuffidient or incorrect
	 */
	public void checkAttachedDocumentMetadataForNewDocument(
			SaveDocumentRequestAttachment document) throws AditCodedException {
		LOG.debug("Checking attached document metadata for new document...");
		if (document != null) {

			LOG.debug("Checking GUID: " + document.getGuid());
			// Check GUID
			if (document.getGuid() != null) {
				// Check GUID format
				try {
					UUID.fromString(document.getGuid());
				} catch (Exception e) {
					throw new AditCodedException(
							"request.saveDocument.document.guid.wrongFormat");
				}
			}

			LOG.debug("Checking title: " + document.getTitle());
			// Check title
			if (document.getTitle() == null
					|| "".equalsIgnoreCase(document.getTitle())) {
				throw new AditCodedException(
						"request.saveDocument.document.title.undefined");
			}

			LOG.debug("Checking document type: " + document.getDocumentType());
			// Check document_type

			if (document.getDocumentType() != null
					&& !"".equalsIgnoreCase(document.getDocumentType().trim())) {

				// Is the document type valid?
				LOG.debug("Document type is defined. Checking if it is valid.");
				DocumentType documentType = this.getDocumentTypeDAO()
						.getDocumentType(document.getDocumentType());

				if (documentType == null) {
					LOG.debug("Document type does not exist: "
							+ document.getDocumentType());
					String validDocumentTypes = getValidDocumentTypes();

					AditCodedException aditCodedException = new AditCodedException(
							"request.saveDocument.document.type.nonExistent");
					aditCodedException
							.setParameters(new Object[] { validDocumentTypes });
					throw aditCodedException;
				}

			} else {
				String validDocumentTypes = getValidDocumentTypes();
				AditCodedException aditCodedException = new AditCodedException(
						"request.saveDocument.document.type.undefined");
				aditCodedException
						.setParameters(new Object[] { validDocumentTypes });
				throw aditCodedException;
			}

			LOG.debug("Checking previous document ID: "
					+ document.getPreviousDocumentID());
			// Check previous_document_id
			if (document.getPreviousDocumentID() != null
					&& document.getPreviousDocumentID() != 0) {
				// Check if the document exists

				Document previousDocument = this.getDocumentDAO().getDocument(
						document.getPreviousDocumentID());

				if (previousDocument == null) {
					AditCodedException aditCodedException = new AditCodedException(
							"request.saveDocument.document.previousDocument.nonExistent");
					aditCodedException.setParameters(new Object[] { document
							.getPreviousDocumentID().toString() });
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
	 * @return	List of valid document types as a comma separated list
	 */
	public String getValidDocumentTypes() {
		StringBuffer result = new StringBuffer();
		List<DocumentType> documentTypes = this.getDocumentTypeDAO()
				.listDocumentTypes();

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
	 * Defaltes document file. Replaces the data with MD5 hash.
	 * <br><br>
	 * Returns one of the following result codes:<br>
	 * "ok" - deflation succeeded<br>
	 * "already_deleted" - specified file is already deleted<br>
	 * "file_does_not_belong_to_document" - specified file does not belong to specified document<br>
	 * "file_does_not_exist" - specified file does not exist
	 * 
	 * @param documentId document ID
	 * @param fileId file ID
	 * @param markDeleted
	 * @return	Deflation result code.
	 */
	public String deflateDocumentFile(long documentId, long fileId,
			boolean markDeleted) {
		return this.getDocumentFileDAO().deflateDocumentFile(documentId,
				fileId, markDeleted);
	}

	/**
	 * Saves a document using the request attachment.
	 * 
	 * @param attachmentDocument document as an attachment
	 * @param creatorCode document creator code
	 * @param remoteApplication remote application name
	 * @param remainingDiskQuota disk quota remaining for this user
	 * @return save result
	 * @throws FileNotFoundException
	 */
	@Transactional
	public SaveItemInternalResult save(
			final SaveDocumentRequestAttachment attachmentDocument,
			final String creatorCode, final String remoteApplication,
			final long remainingDiskQuota, final String creatorUserCode, final String creatorUserName, final String creatorName) throws FileNotFoundException {
		final DocumentDAO docDao = this.getDocumentDAO();

		return (SaveItemInternalResult) this.getDocumentDAO()
				.getHibernateTemplate().execute(new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Date creationDate = new Date();
						Document document = new Document();
						if ((attachmentDocument.getId() != null)
								&& (attachmentDocument.getId() > 0)) {
							document = (Document) session.get(Document.class,
									attachmentDocument.getId());
							LOG.debug("Document file count: "
									+ document.getDocumentFiles().size());
						} else {
							document.setCreationDate(creationDate);
							document.setCreatorCode(creatorCode);
							document.setRemoteApplication(remoteApplication);
							document.setSignable(true);
						}

						document.setDocumentType(attachmentDocument
								.getDocumentType());
						if (attachmentDocument.getGuid() != null
								&& !"".equalsIgnoreCase(attachmentDocument
										.getGuid().trim())) {
							document.setGuid(attachmentDocument.getGuid());
						} else if ((document.getGuid() == null)
								|| "".equalsIgnoreCase(attachmentDocument
										.getGuid().trim())) {
							// Generate new GUID
							document.setGuid(Util.generateGUID());
						}

						document.setCreatorName(creatorName);
						document.setLastModifiedDate(creationDate);
						document.setTitle(attachmentDocument.getTitle());
						document.setCreatorUserCode(creatorUserCode);
						document.setCreatorUserName(creatorUserName);
						
						try {
							return docDao.save(document, attachmentDocument
									.getFiles(), remainingDiskQuota, session);
						} catch (Exception e) {
							throw new HibernateException(e);
						}
					}
				});
	}

	/**
	 * Saves document file to database.
	 * 
	 * @param documentId			Document ID
	 * @param file					File as {@link OutputDocumentFile} object	
	 * @param remainingDiskQuota	Remaining disk quota of current user (in bytes)
	 * @param temporaryFilesDir		Absolute path to temporary files directory
	 * @return						Result of save as {@link SaveItemInternalResult} object.
	 */
	public SaveItemInternalResult saveDocumentFile(final long documentId,
			final OutputDocumentFile file,
			final long remainingDiskQuota, final String temporaryFilesDir) {
		final DocumentDAO docDao = this.getDocumentDAO();

		return (SaveItemInternalResult) this.getDocumentDAO()
				.getHibernateTemplate().execute(new HibernateCallback() {
					@SuppressWarnings("unchecked")
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						SaveItemInternalResult result = new SaveItemInternalResult();
						List<OutputDocumentFile> filesList = new ArrayList<OutputDocumentFile>();
						filesList.add(file);

						Document document = (Document) session.get(
								Document.class, documentId);

						// Remember highest ID of existing files.
						// This is useful later to find out which file was
						// added.
						long maxId = -1;
						if ((document != null)
								&& (document.getDocumentFiles() != null)) {
							Iterator it = document.getDocumentFiles()
									.iterator();
							if (it != null) {
								while (it.hasNext()) {
									DocumentFile f = (DocumentFile) it.next();
									if (f.getId() > maxId) {
										maxId = f.getId();
									}
								}
							}
						}
						LOG.debug("Highest existing file ID: " + maxId);

						// Document to database
						try {
							result = docDao.save(document, filesList,
									remainingDiskQuota, session);
						} catch (Exception e) {
							throw new HibernateException(e);
						}

						long fileId = 0;
						if ((file.getId() != null) && (file.getId() > 0)) {
							fileId = file.getId();
							LOG.debug("Existing file saved with ID: " + fileId);
						} else if ((document != null)
								&& (document.getDocumentFiles() != null)) {
							Iterator it = document.getDocumentFiles()
									.iterator();
							if (it != null) {
								while (it.hasNext()) {
									DocumentFile f = (DocumentFile) it.next();
									if (f.getId() > maxId) {
										fileId = f.getId();
										LOG.debug("New file saved with ID: "
												+ fileId);
										break;
									}
								}
							}
						}
						result.setItemId(fileId);

						return result;
					}
				});
	}

	/**
	 * Saves document considering the disk quota for the user.
	 * 
	 * @param doc document
	 * @param remainingDiskQuota remaining disk quota for user
	 * @throws Exception
	 */
	public void save(Document doc, long remainingDiskQuota) throws Exception {
		this.getDocumentDAO().save(doc, null, remainingDiskQuota, null);
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
			LOG.debug("Locking document: " + document.getId());
			document.setLocked(true);
			document.setLockingDate(new Date());
			save(document, Long.MAX_VALUE);
			LOG.info("Document locked: " + document.getId());
		}
	}

	/**
	 * Sends document to the specified user.
	 * 
	 * @param document document
	 * @param recipient user
	 * @return true, if sending succeeded
	 */
	public boolean sendDocument(Document document, AditUser recipient) {
		boolean result = false;

		DocumentSharing documentSharing = new DocumentSharing();
		documentSharing.setDocumentId(document.getId());
		documentSharing.setCreationDate(new Date());

		if (recipient.getDvkOrgCode() != null
				&& !"".equalsIgnoreCase(recipient.getDvkOrgCode().trim())) {
			documentSharing
					.setDocumentSharingType(DocumentService.SharingType_SendDvk);
		} else {
			documentSharing
					.setDocumentSharingType(DocumentService.SharingType_SendAdit);
		}

		documentSharing.setUserCode(recipient.getUserCode());
		documentSharing.setUserName(recipient.getFullName());

		this.getDocumentSharingDAO().save(documentSharing);

		if (documentSharing.getId() == 0) {
			throw new AditInternalException(
					"Could not add document sharing information to database.");
		}

		return result;
	}

	/**
	 * Adds a document history event.
	 * 
	 * @param applicationName remote application short name
	 * @param doc document
	 * @param userCode user code - the user that caused this event 
	 * @param historyType history event type name
	 * @param xteeUserCode X-Tee user code
	 * @param xteeUserName X-Tee user name
	 * @param description event description
	 */
	public void addHistoryEvent(String applicationName, Document doc,
			String userCode, String historyType, String xteeUserCode,
			String xteeUserName, String description, String userName) {
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
	 * 2. That DocumentSharing must have the "documentSharingType" equal to "send_dvk" <br />
	 * 3. That DocumentSharing must have the "documentDvkStatus" not initialized or set to "100"
	 */
	@SuppressWarnings("unchecked")
	public int sendDocumentsToDVK() {
		int result = 0;

		final String SQL_QUERY = "select doc from Document doc, DocumentSharing docSharing where docSharing.documentSharingType = 'send_dvk' and (docSharing.documentDvkStatus is null or docSharing.documentDvkStatus = 100) and docSharing.documentId = doc.id";

		final String tempDir = this.getConfiguration().getTempDir();

		LOG.debug("Fetching documents for sending to DVK...");
		Session session = this.getDocumentDAO().getSessionFactory()
				.openSession();

		Query query = session.createQuery(SQL_QUERY);
		List<Document> documents = query.list();

		LOG.debug("Documents fetched successfully (" + documents.size() + ")");

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

				Iterator<DocumentSharing> documentSharings = document
						.getDocumentSharings().iterator();

				while (documentSharings.hasNext()) {
					DocumentSharing documentSharing = documentSharings.next();

					if (DocumentService.SharingType_SendDvk
							.equalsIgnoreCase(documentSharing
									.getDocumentSharingType())) {

						AditUser recipient = this.getAditUserDAO().getUserByID(
								documentSharing.getUserCode());

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
					Fail dvkFile = new Fail();

					LOG.debug("FileName: " + f.getFileName());

					// Create a temporary file from the ADIT file and
					// add a reference to the DVK file
					try {
						InputStream inputStream = f.getFileData()
								.getBinaryStream();
						String temporaryFile = Util.createTemporaryFile(
								inputStream, tempDir);

						dvkFile.setFile(new File(temporaryFile));
						dvkFiles.add(dvkFile);

					} catch (Exception e) {
						throw new HibernateException(
								"Unable to create temporary file: ", e);
					}

					dvkFile.setFailNimi(f.getFileName());
					dvkFile.setFailPealkiri(null);
					dvkFile.setFailSuurus(f.getFileSizeBytes());
					dvkFile.setFailTyyp(f.getContentType());
					dvkFile.setJrkNr(count++);
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

					AditUser documentOwner = (AditUser) session.get(
							AditUser.class, documentOwnerCode);

					dvkMessage.setSenderOrgCode(documentOwner.getDvkOrgCode());
					dvkMessage.setSenderPersonCode(documentOwner.getUserCode());
					dvkMessage.setSenderName(documentOwner.getFullName());
					dvkMessage.setDhlGuid(document.getGuid());
					dvkMessage
							.setSendingStatusId(DocumentService.DVKStatus_Waiting);

					// Insert data as stream
					Clob clob = Hibernate.createClob(" ", dvkSession);
					dvkMessage.setData(clob);

					LOG.debug("Saving document to DVK database");
					dvkMessageID = (Long) dvkSession.save(dvkMessage);

					if (dvkMessageID == null || dvkMessageID.longValue() == 0) {
						LOG
								.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
						throw new DataRetrievalFailureException(
								"Error while saving outgoing message to DVK database - no ID returned by save method.");
					} else {
						LOG.info("Outgoing message saved to DVK database. ID: "
								+ dvkMessageID);
					}

					LOG.debug("DVK Message saved to client database. GUID: "
							+ dvkMessage.getDhlGuid());
					dvkTransaction.commit();

				} catch (Exception e) {
					dvkTransaction.rollback();
					throw new DataRetrievalFailureException(
							"Error while adding message to DVK Client database: ",
							e);
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
					PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2
							.load(PojoMessage.class, dvkMessageID,
									LockMode.UPGRADE);

					// Write the DVK Container to temporary file
					String temporaryFile = this.getConfiguration().getTempDir()
							+ File.separator + Util.generateRandomFileName();
					dvkContainer.save2File(temporaryFile);

					// Write the temporary file to the database
					InputStream is = new FileInputStream(temporaryFile);
					Writer clobWriter = dvkMessageToUpdate.getData()
							.setCharacterStream(1);

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

					LOG.debug("Starting to update document sharings");
					Transaction aditTransaction = session.beginTransaction();
					// Update document sharings status
					Iterator<DocumentSharing> documentSharingUpdateIterator = document
							.getDocumentSharings().iterator();
					while (documentSharingUpdateIterator.hasNext()) {
						DocumentSharing documentSharing = documentSharingUpdateIterator
								.next();
						if (DocumentService.SharingType_SendDvk
								.equalsIgnoreCase(documentSharing
										.getDocumentSharingType())) {
							documentSharing
									.setDocumentDvkStatus(DVKStatus_Sending);
							session.saveOrUpdate(documentSharing);
							LOG.debug("DocumentSharing status updated to: '"
									+ DVKStatus_Sending + "'.");
						}
					}
					
					aditTransaction.commit();
					
					result++;

				} catch (Exception e) {
					dvkTransaction2.rollback();

					// Remove the document with empty clob from the database
					Session dvkSession3 = sessionFactory.openSession();
					Transaction dvkTransaction3 = dvkSession3
							.beginTransaction();
					try {
						LOG
								.debug("Starting to delete document from DVK Client database: "
										+ dvkMessageID);
						PojoMessage dvkMessageToDelete = (PojoMessage) dvkSession3
								.load(PojoMessage.class, dvkMessageID);
						if (dvkMessageToDelete == null) {
							LOG
									.warn("DVK message to delete is not initialized.");
						}
						dvkSession3.delete(dvkMessageToDelete);
						dvkTransaction3.commit();
						LOG
								.info("Empty DVK document deleted from DVK Client database. ID: "
										+ dvkMessageID);
					} catch (Exception dvkException) {
						dvkTransaction3.rollback();
						LOG.error(
								"Error deleting document from DVK database: ",
								dvkException);
					} finally {
						if (dvkSession3 != null) {
							dvkSession3.close();
						}
					}

					throw new DataRetrievalFailureException(
							"Error while adding message to DVK Client database (CLOB update): ",
							e);
				} finally {
					if (dvkSession2 != null) {
						dvkSession2.close();
					}
				}
			} catch (Exception e) {

				// TODO: if something fails during the operation - the
				// document.dvkId is not set and that reference is lost.
				// The GUID reference is still valid.

				throw new AditInternalException(
						"Error while sending documents to DVK Client database: ",
						e);
			}
		}
		
		if(session != null)
			session.close();
		
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
	public int receiveDocumentsFromDVK() {
		int result = 0;

		try {

			// Fetch all incoming documents from DVK Client database which have
			// the required status - "sending" (recipient_status_id = "101" or
			// "1");
			LOG.info("Fetching documents from DVK Client database.");
			List<PojoMessage> dvkDocuments = this.getDvkDAO().getIncomingDocuments();

			if (dvkDocuments != null && dvkDocuments.size() > 0) {

				LOG.debug("Found " + dvkDocuments.size());
				Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments
						.iterator();

				while (dvkDocumentsIterator.hasNext()) {
					PojoMessage dvkDocument = dvkDocumentsIterator.next();

					// Get the DVK Container
					ContainerVer2 dvkContainer = this
							.getDVKContainer(dvkDocument);

					List<Saaja> recipients = dvkContainer.getTransport()
							.getSaajad();

					if (recipients != null && recipients.size() > 0) {
						LOG.debug("Recipients for this message: "
								+ recipients.size());

						// For every recipient - check if registered in ADIT
						Iterator<Saaja> recipientsIterator = recipients
								.iterator();

						while (recipientsIterator.hasNext()) {
							Saaja recipient = recipientsIterator.next();

							LOG.info("Recipient: " + recipient.getRegNr()
									+ " (" + recipient.getAsutuseNimi()
									+ "). Isikukood: '"
									+ recipient.getIsikukood() + "'.");

							// The ADIT internal recipient is always marked by
							// the field <isikukood> in the DVK container,
							// regardless if it is actually a person or an
							// institution / company.
							if (recipient.getRegNr() != null
									&& !recipient.getRegNr().equalsIgnoreCase(
											"")) {
								if (recipient.getIsikukood() != null
										&& !recipient.getIsikukood().equals("")) {
									// The recipient is specified - check if
									// it's a DVK user

									LOG
											.debug("Getting AditUser by personal code: "
													+ recipient.getIsikukood()
															.trim());
									AditUser user = this.getAditUserDAO()
											.getUserByID(
													recipient.getIsikukood()
															.trim());

									if (user != null && user.getActive()) {

										// Check if user uses DVK
										if (user.getDvkOrgCode() != null
												&& !user.getDvkOrgCode()
														.equalsIgnoreCase("")) {
											// The user uses DVK - this is not
											// allowed. Users that use DVK have
											// to exchange
											// documents with other users that
											// use DVK, over DVK.
											this
													.composeErrorResponse(
															DocumentService.DvkReceiveFailReason_UserUsesDvk,
															dvkContainer,
															recipient
																	.getIsikukood()
																	.trim(),
															dvkDocument
																	.getReceivedDate(),
															recipient.getNimi());
											throw new AditInternalException(
													"User uses DVK - not allowed.");
										}

										LOG.debug("Constructing ADIT message");
										// Add document for this recipient to
										// ADIT database
										Document aditDocument = new Document();
										aditDocument
												.setCreationDate(new Date());
										aditDocument
												.setDocumentDvkStatusId(DVKStatus_Sent);
										aditDocument.setDvkId(dvkDocument
												.getDhlId());
										aditDocument.setGuid(dvkDocument
												.getDhlGuid());
										aditDocument.setLocked(true);
										aditDocument.setLockingDate(new Date());
										aditDocument.setSignable(true);
										aditDocument.setTitle(dvkDocument
												.getTitle());
										aditDocument
												.setDocumentType(DocType_Letter);

										// The creator is the recipient
										aditDocument.setCreatorCode(user
												.getUserCode());
										aditDocument.setCreatorName(user
												.getFullName());

										// Get document files from DVK container
										List<OutputDocumentFile> tempDocuments = this
												.getDocumentOutputFiles(dvkContainer);

										Session aditSession = null;
										Transaction aditTransaction = null;
										try {

											// Save the document
											aditSession = this.getDocumentDAO()
													.getSessionFactory()
													.openSession();
											aditTransaction = aditSession
													.beginTransaction();

											// Before we save the document to
											// ADIT, check if the recipient has
											// already received a document
											// with the same GUID or DVK ID.
											if (this.getDocumentDAO()
													.checkIfDocumentExists(
															dvkDocument,
															recipient)) {
												throw new AditInternalException(
														"Document already sent to user. DVK ID: "
																+ dvkDocument
																		.getDhlId()
																+ ", recipient: "
																+ recipient
																		.getIsikukood()
																		.trim());
											}

											// Save document
											SaveItemInternalResult saveResult = this
													.getDocumentDAO().save(
															aditDocument,
															tempDocuments,
															Long.MAX_VALUE,
															aditSession);
											if (saveResult == null) {
												throw new AditInternalException(
														"Document saving failed!");
											}
											if (saveResult.isSuccess()) {
												LOG
														.info("Document saved to ADIT database. ID: "
																+ saveResult
																		.getItemId());
											} else {
												if ((saveResult.getMessages() != null)
														&& (saveResult
																.getMessages()
																.size() > 0)) {
													throw new AditInternalException(
															saveResult
																	.getMessages()
																	.get(0)
																	.getValue());
												} else {
													throw new AditInternalException(
															"Document saving failed!");
												}
											}

											// Update document status to "sent"
											// (recipient_status_id = "102") in
											// DVK Client database
											dvkDocument
													.setRecipientStatusId(DVKStatus_Sent);
											this.getDvkDAO().updateDocument(
													dvkDocument);

											// Finally commit
											aditTransaction.commit();

										} catch (Exception e) {
											LOG
													.debug(
															"Error saving document to ADIT database: ",
															e);
											if (aditTransaction != null) {
												aditTransaction.rollback();
											}
										} finally {
											if (aditSession != null) {
												aditSession.close();
											}
										}
									} else {
										LOG
												.error("User not found. Personal code: "
														+ recipient
																.getIsikukood()
																.trim());
										this
												.composeErrorResponse(
														DocumentService.DvkReceiveFailReason_UserDoesNotExist,
														dvkContainer,
														recipient
																.getIsikukood()
																.trim(),
														dvkDocument
																.getReceivedDate(),
														recipient.getNimi());
									}
								}
							}
						}
					} else {
						LOG.warn("No recipients found for this message: "
								+ dvkDocument.getDhlGuid());
					}
				}
			} else {
				LOG.info("No incoming messages found in DVK Client database.");
			}
		} catch (Exception e) {
			throw new AditInternalException(
					"Error while receiving documents from DVK Client database: ",
					e);
		}

		return result;
	}

	/**
	 * Converts the document object to {@code ContainerVer2} object.
	 * 
	 * @param document
	 * @return DVK container object
	 * @throws AditInternalException
	 */
	public ContainerVer2 getDVKContainer(PojoMessage document)
			throws AditInternalException {
		ContainerVer2 result = null;

		// Write the clob data to a temporary file
		Reader clobReader = null;
		FileWriter fileWriter = null;
		try {
			clobReader = document.getData().getCharacterStream();
			String tmpFile = this.getConfiguration().getTempDir()
					+ File.separator + Util.generateRandomFileName();
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
				throw new AditInternalException(
						"DVK Container not initialized.");
			} else {
				if (result.getTransport() == null) {
					throw new AditInternalException(
							"DVK Container not properly initialized: <transport> section not initialized");
				}
			}

		} catch (Exception e) {
			throw new AditInternalException(
					"Exception while reading DVK container from database: ", e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception e) {
					LOG.warn("Error while closing file writer: ", e);
				}
			}

			if (clobReader != null) {
				try {
					clobReader.close();
				} catch (Exception e) {
					LOG.warn("Error while closing Clob reader: ", e);
				}
			}
		}

		return result;
	}

	/**
	 * Extracts files from DVK container.
	 * 
	 * @param dvkContainer DVK container
	 * @return list of files extracted
	 */
	public List<OutputDocumentFile> getDocumentOutputFiles(
			ContainerVer2 dvkContainer) {
		List<OutputDocumentFile> result = new ArrayList<OutputDocumentFile>();

		try {
			List<Fail> dvkFiles = dvkContainer.getFailideKonteiner()
					.getFailid();
			Iterator<Fail> dvkFilesIterator = dvkFiles.iterator();

			LOG.debug("Total number of files in DVK Container: "
					+ dvkContainer.getFailideKonteiner().getKokku());

			while (dvkFilesIterator.hasNext()) {
				Fail dvkFile = dvkFilesIterator.next();
				LOG.debug("Processing file nr.: " + dvkFile.getJrkNr());

				// TODO: STREAM
				String fileContents = dvkFile.getZipBase64Sisu();
				InputStream inputStream = new StringBufferInputStream(
						fileContents);
				String tempFile = Util.createTemporaryFile(inputStream, this
						.getConfiguration().getTempDir());

				String decodedTempFile = Util.base64DecodeAndUnzip(tempFile,
						this.getConfiguration().getTempDir(), this
								.getConfiguration()
								.getDeleteTemporaryFilesAsBoolean());

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
				Iterator<OutputDocumentFile> documentsToDelete = result
						.iterator();
				while (documentsToDelete.hasNext()) {
					OutputDocumentFile documentToDelete = documentsToDelete
							.next();
					try {
						if (documentToDelete.getSysTempFile() != null
								&& !documentToDelete.getSysTempFile().trim()
										.equalsIgnoreCase("")) {
							File f = new File(documentToDelete.getSysTempFile());
							if (f.exists()) {
								f.delete();
							} else {
								throw new FileNotFoundException(
										"Could not find temporary file (to delete): "
												+ documentToDelete
														.getSysTempFile());
							}
						}
					} catch (Exception exc) {
						LOG
								.debug(
										"Error while deleting temporary files: ",
										exc);
					}
				}
				LOG.info("Temporary files deleted.");
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

		// 1. Võtame kõik dokumendid ADIT andmebaasist, millel DVK staatus ei
		// ole "saadetud"
		List<Document> documents = this.getDocumentDAO()
				.getDocumentsWithoutDVKStatus(DVKStatus_Sent);
		Iterator<Document> documentsIterator = documents.iterator();

		while (documentsIterator.hasNext()) {
			Document document = documentsIterator.next();

			try {
				LOG.info("Updating DVK status for document. DocumentID: "
						+ document.getId());

				List<PojoMessageRecipient> messageRecipients = this.getDvkDAO()
						.getMessageRecipients(document.getDvkId(), false);
				Iterator<PojoMessageRecipient> messageRecipientIterator = messageRecipients
						.iterator();
				List<DocumentSharing> documentSharings = this
						.getDocumentSharingDAO().getDVKSharings(
								document.getId());

				if (messageRecipients != null)
					LOG.debug("messageRecipients.size: "
							+ messageRecipients.size());

				if (documentSharings != null)
					LOG.debug("documentSharings.size: "
							+ documentSharings.size());

				while (messageRecipientIterator.hasNext()) {
					PojoMessageRecipient messageRecipient = messageRecipientIterator
							.next();

					LOG.debug("Updating for messageRecipient: "
							+ messageRecipient.getRecipientOrgCode());

					boolean allDocumentSharingsSent = true;

					// Compare the status with the status of the sharing in ADIT
					for (int i = 0; i < documentSharings.size(); i++) {
						DocumentSharing documentSharing = documentSharings
								.get(i);
						LOG.debug("Updating for documentSharing: "
								+ documentSharing.getId());

						if (documentSharing.getUserCode().equalsIgnoreCase(
								messageRecipient.getRecipientPersonCode())
								|| documentSharing.getUserCode()
										.equalsIgnoreCase(
												messageRecipient
														.getRecipientOrgCode())) {

							// If the statuses differ, update the one in ADIT
							// database
							if (!documentSharing.getDocumentDvkStatus().equals(
									messageRecipient.getSendingStatusId())) {
								documentSharing
										.setDocumentDvkStatus(messageRecipient
												.getSendingStatusId());
								this.getDocumentSharingDAO().update(
										documentSharing);
								LOG
										.debug("DocumentSharing DVK status updated: documentSharingID: "
												+ documentSharing.getId()
												+ ", DVK status: "
												+ documentSharing
														.getDocumentDvkStatus());
								result++;
							}

							if (messageRecipient.getSendingStatusId() != DocumentService.DVKStatus_Sent) {
								allDocumentSharingsSent = false;
							}
						}
					}

					// If all documentSharings statuses are "sent" then update
					// the document's dvk status
					if (allDocumentSharingsSent) {
						// Update document DVK status ID
						document
								.setDocumentDvkStatusId(DocumentService.DVKStatus_Sent);
						this.getDocumentDAO().update(document);
						LOG
								.debug("All DVK sharings for this document updated to 'sent'. Updating document DVK status.");
					}
				}
			} catch (Exception e) {
				LOG.error("Error while updating status from DVK. DocumentID: "
						+ document.getId(), e);
				LOG.info("Continue...");
			}
		}

		return result;
	}

	/**
	 * Updates document statuses for incoming messages.
	 * 
	 * @return number of messages updated.
	 */
	public int updateDocumentsToDVK() {

		int result = 0;
		List<PojoMessage> dvkDocuments = this.getDvkDAO()
				.getIncomingDocumentsWithoutStatus(
						DocumentService.DVKStatus_Sent);
		Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments.iterator();

		while (dvkDocumentsIterator.hasNext()) {
			PojoMessage dvkDocument = dvkDocumentsIterator.next();

			try {
				// Find the message from ADIT database
				Document document = this.getDocumentDAO().getDocumentByDVKID(
						dvkDocument.getDhlMessageId());

				if (document != null) {

					// Compare the statuses - ADIT status prevails
					if (document.getDocumentDvkStatusId() != null) {
						if (!document.getDocumentDvkStatusId().equals(
								dvkDocument.getRecipientStatusId())) {

							// If the statuses do not match, update from ADIT to
							// DVK
							dvkDocument.setRecipientStatusId(document
									.getDocumentDvkStatusId());

							// Update DVK document
							this.getDvkDAO().updateDocument(dvkDocument);
						}
					} else {
						throw new AditInternalException(
								"Could not update document with DVK_ID: "
										+ dvkDocument.getDhlMessageId()
										+ ". Document's DVK status is not defined in ADIT.");
					}
				} else {
					throw new AditInternalException(
							"Could not find document with DVK_ID: "
									+ dvkDocument.getDhlMessageId());
				}
			} catch (Exception e) {
				LOG
						.error("Error while updating DVK status for document. DVK_ID: "
								+ dvkDocument.getDhlMessageId());
				LOG.error("Continue...");
			}
		}

		return result;
	}

	/**
	 * Deletes documents from DVK, that have the status 'sent'
	 * 
	 * @return				Number of documents deleted
	 * @throws Exception
	 */
	public int deleteSentDocumentsFromDVK() throws Exception {
		int result = 0;
		try {
			List<PojoMessage> dvkDocuments = this.getDvkDAO()
					.getSentDocuments();

			LOG.info("Documents (sent) fetched: " + dvkDocuments.size());

			Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments
					.iterator();

			Session dvkSession = null;
			Transaction transaction = null;
			
			try {
				dvkSession = this.getDvkDAO().getSessionFactory().openSession();
				transaction = dvkSession.beginTransaction();
				while (dvkDocumentsIterator.hasNext()) {
					PojoMessage dvkDocument = dvkDocumentsIterator.next();
					deleteDVKDocument(dvkDocument, dvkSession);
					LOG.info("Document deleted from DVK. DHL_ID: " + dvkDocument.getDhlId());
					result++;
				}
				transaction.commit();
				
			} catch (Exception e) {
				LOG.error("Error while deleting documents from DVK database: ", e);
				if(transaction != null)
					transaction.rollback();
				result = 0;
				throw e;
			}
			finally {
				if (dvkSession != null) {
					dvkSession.close();
				}
			}

		} catch (Exception e) {
			LOG.error("Error while deleting documents from DVK: ", e);
			throw e;
		}

		return result;
	}

	/**
	 * Deletes received documents from DVK client database. 
	 * Deleting means the DVK container XML is deleted (not the document metadata).
	 * 
	 * @return
	 * @throws Exception
	 */
	public int deleteReceivedDocumentsFromDVK() throws Exception {
		int result = 0;
		try {
			List<PojoMessage> dvkDocuments = this.getDvkDAO()
					.getReceivedDocuments();

			LOG.info("Documents (received / aborted) fetched: " + dvkDocuments.size());

			Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments
					.iterator();

			Session dvkSession = null;
			Transaction transaction = null;
			
			try {
				dvkSession = this.getDvkDAO().getSessionFactory().openSession();
				transaction = dvkSession.beginTransaction();
				while (dvkDocumentsIterator.hasNext()) {
					PojoMessage dvkDocument = dvkDocumentsIterator.next();
					deleteDVKDocument(dvkDocument, dvkSession);
					LOG.info("Document deleted from DVK. DHL_ID: " + dvkDocument.getDhlId());
				}
				transaction.commit();
				
			} catch (Exception e) {
				transaction.rollback();
				throw e;
			}
			finally {
				if (dvkSession != null) {
					dvkSession.close();
				}
			}

		} catch (Exception e) {
			LOG.error("Error while deleting documents from DVK: ", e);
			throw e;
		}

		return result;
	}
	
	/**
	 * Deletes the messages content (data) and updates 'faultCode' to 'NO_FAULT:
	 * DELETED BY ADIT'
	 * 
	 * @param message	DVK document to be deleted.
	 * @param session	Current hibernate session.
	 */
	public void deleteDVKDocument(PojoMessage message, Session session) {
		LOG.debug("Deleting document. DHL_ID: " + message.getDhlId());
		message.setData(Hibernate.createClob(DocumentService.DVKBlobMessage_Deleted, session));
		message.setFaultCode(DocumentService.DVKFaultCodeFor_Deleted);
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
	 */
	public void composeErrorResponse(Integer reasonCode,
			ContainerVer2 dvkContainer, String recipientCode,
			Date receivedDate, String recipientName)
			throws AditInternalException {

		try {
			// 1. Gather data required for response message
			String xml = this.createErrorResponseDataXML(dvkContainer,
					recipientCode, receivedDate, recipientName);

			// 2. Transform to XSL-FO
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					xml.getBytes("UTF-8"));
			String xmlTempFile = Util.createTemporaryFile(byteArrayInputStream,
					this.getConfiguration().getTempDir());
			String outputXslFoFile = Util.generateRandomFileName();
			Util.applyXSLT(xmlTempFile, this.getConfiguration()
					.getDvkResponseMessageStylesheet(), outputXslFoFile);

			// 3. Transform to PDF
			String outputPDFFile = Util.generateRandomFileName();
			Util.generatePDF(outputPDFFile, outputXslFoFile);

			// 4. Save the response message PDF to DVK
			LOG.debug("DVK error response message composed. FileName: "
					+ outputPDFFile);
			this.saveErrorResponseMessageToDVK(outputPDFFile, dvkContainer);

		} catch (Exception e) {
			LOG.error("Error while composing DVK error response message: ", e);
			throw new AditInternalException(
					"Error while composing DVK error response message: ", e);
		}
	}

	/**
	 * Creates DVK response message data XML string.
	 * 
	 * @param dvkContainer DVK container
	 * @param recipientCode recipient code
	 * @param receivedDate receiving date
	 * @param recipientName recipient name
	 * @return XML string
	 */
	public String createErrorResponseDataXML(ContainerVer2 dvkContainer,
			String recipientCode, Date receivedDate, String recipientName) {
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
			LOG.error("Error while getting Sender information: ", e);
		}

		try {
			receiveDateTmp = Util.dateToXMLDate(receivedDate);
		} catch (Exception e) {
			try {
				receiveDateTmp = Util.dateToXMLDate(new Date());
			} catch (Exception exc) {
				LOG.error("Error while parsing received date (system date): ",
						exc);
			}
			LOG.error("Error while parsing received date: ", e);
		}

		try {
			dhlId = dvkContainer.getMetainfo().getMetaAutomatic().getDhlId();
		} catch (Exception e) {
			LOG.error("Error while getting document DHL ID: ", e);
		}

		try {
			guid = dvkContainer.getMetainfo().getMetaManual().getDokumentGuid();
		} catch (Exception e) {
			LOG.error("Error while getting document GUID: ", e);
		}

		try {
			title = dvkContainer.getMetainfo().getMetaManual()
					.getDokumentPealkiri();
		} catch (Exception e) {
			LOG.error("Error while getting document title: ", e);
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
	 * @param fileName absolute path to the data file.
	 * @param originalContainer original (request) DVK container
	 * @throws Exception
	 */
	public void saveErrorResponseMessageToDVK(String fileName,
			ContainerVer2 originalContainer) throws Exception {

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
				recipient.setAllyksuseLyhinimetus(originalSender
						.getAllyksuseLyhinimetus());
				recipient.setAllyksuseNimetus(originalSender
						.getAllyksuseNimetus());
				recipient.setAmetikohaLyhinimetus(originalSender
						.getAmetikohaLyhinimetus());
				recipient.setAmetikohaNimetus(originalSender
						.getAmetikohaNimetus());
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
			metaManual.setDokumentLiik(DocType_Letter);
			metaManual.setDokumentPealkiri(DvkErrorResponseMessage_Title);

			SaatjaKontekst saatjaKontekst = new SaatjaKontekst();
			saatjaKontekst.setDokumentSaatjaGuid(originalContainer
					.getMetainfo().getMetaManual().getDokumentGuid());
			saatjaKontekst.setSeosviit(originalContainer.getMetainfo()
					.getMetaManual().getDokumentViit());

			metaManual.setSaatjaKontekst(saatjaKontekst);
			metaManual.setSeotudDhlId(originalContainer.getMetainfo()
					.getMetaAutomatic().getDhlId());
			metaManual.setSeotudDokumendinrSaajal(originalContainer
					.getMetainfo().getMetaManual().getDokumentViit());

			metainfo.setMetaManual(metaManual);
			container.setMetainfo(metainfo);
			FailideKonteiner failideKonteiner = new FailideKonteiner();
			failideKonteiner.setKokku((short) 1);

			List<Fail> files = new ArrayList<Fail>();

			Fail responseFile = new Fail();
			File dataFile = new File(fileName);
			responseFile.setFile(dataFile);
			responseFile.setFailNimi(DvkErrorResponseMessage_FileName);
			responseFile.setFailPealkiri(DvkErrorResponseMessage_Title);
			responseFile.setFailSuurus(dataFile.length());
			responseFile.setFailTyyp(MimeConstants.MIME_PDF);
			responseFile.setJrkNr((short) 1);
			responseFile.setKrypteering(false);
			responseFile.setPohiDokument(true);

			// Add the response document
			files.add(responseFile);

			// Add the original files
			FailideKonteiner originalFileContainer = originalContainer
					.getFailideKonteiner();
			List<Fail> originalFiles = originalFileContainer.getFailid();

			for (int i = 0; i < originalFiles.size(); i++) {
				Fail originalFile = originalFiles.get(i);
				files.add(originalFile);
			}

			failideKonteiner.setFailid(files);

			container.setFailideKonteiner(failideKonteiner);

			String temporaryFile = this.getConfiguration().getTempDir()
					+ File.separator + Util.generateRandomFileName();
			container.save2File(temporaryFile);

			LOG.info("DVK error response message DVK container saved to temporary file: " + temporaryFile);

			// 2. Construct a DVK PojoMessage
			PojoMessage message = new PojoMessage();
			message.setDhlGuid(guid);
			message.setDhlFolderName(null);
			message.setIsIncoming(false);
			message.setSendingStatusId(DVKStatus_Waiting);
			message.setTitle(DvkErrorResponseMessage_Title);

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
					LOG.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
					throw new DataRetrievalFailureException("Error while saving outgoing message to DVK database - no ID returned by save method.");
				} else {
					LOG.info("Outgoing message saved to DVK database. ID: "	+ dvkMessageID);
				}
			} catch (Exception e) {
				if (dvkTransaction != null) {
					dvkTransaction.rollback();
				}
				throw new DataRetrievalFailureException(
						"Error while adding message to DVK Client database: ",
						e);
			} finally {
				if (dvkSession != null) {
					dvkSession.close();
				}
			}

			LOG.debug("DVK Message saved to client database. GUID: "
					+ message.getDhlGuid());
			dvkTransaction.commit();

			// Update CLOB
			Session dvkSession2 = this.getDocumentDAO().getSessionFactory()
					.openSession();
			Transaction dvkTransaction2 = dvkSession2.beginTransaction();

			try {
				// Select the record for update
				PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2
						.load(PojoMessage.class, dvkMessageID, LockMode.UPGRADE);

				// Write the temporary file to the database
				InputStream is = new FileInputStream(temporaryFile);
				Writer clobWriter = dvkMessageToUpdate.getData()
						.setCharacterStream(1);

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
				Session dvkSession3 = this.getDocumentDAO().getSessionFactory()
						.openSession();
				Transaction dvkTransaction3 = dvkSession3.beginTransaction();
				try {
					LOG
							.debug("Starting to delete document from DVK Client database: "
									+ dvkMessageID);
					PojoMessage dvkMessageToDelete = (PojoMessage) dvkSession3
							.load(PojoMessage.class, dvkMessageID);
					if (dvkMessageToDelete == null) {
						LOG.warn("DVK message to delete is not initialized.");
					}
					dvkSession3.delete(dvkMessageToDelete);
					dvkTransaction3.commit();
					LOG
							.info("Empty DVK document deleted from DVK Client database. ID: "
									+ dvkMessageID);
				} catch (Exception dvkException) {
					dvkTransaction3.rollback();
					LOG.error("Error deleting document from DVK database: ",
							dvkException);
				} finally {
					if (dvkSession3 != null) {
						dvkSession3.close();
					}
				}
				throw new DataRetrievalFailureException(
						"Error while adding message to DVK Client database (CLOB update): ",
						e);
			} finally {
				if (dvkSession2 != null) {
					dvkSession2.close();
				}
			}
		} catch (Exception e) {
			LOG.error("Error while constructing DVK response message: ", e);
			throw e;
		}
	}

	/**
	 * Marks document matching the given ID as deleted. Document
	 * file contents will be replaced with their MD5 hash codes. Document and
	 * individual files will be marked as "deleted".
	 * 
	 * @param documentId
	 *            ID of document to be deleted
	 * @param userCode
	 *            Code of the user who executed current request
	 * @param applicationName
	 *            Short name of application that executed current request
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public void deleteDocument(long documentId, String userCode, String applicationName) throws Exception {
		Document doc = this.getDocumentDAO().getDocument(documentId);

		// Check whether or not the document exists
		if (doc == null) {
			AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
			aditCodedException.setParameters(new Object[] { new Long(documentId).toString() });
			throw aditCodedException;
		}

		// Make sure that the document is not already deleted
		// NB! doc.getDeleted() can be NULL
		if ((doc.getDeleted() != null) && doc.getDeleted()) {
			AditCodedException aditCodedException = new AditCodedException("request.deleteDocument.document.deleted");
			aditCodedException.setParameters(new Object[] { new Long(documentId).toString() });
			throw aditCodedException;
		}

		boolean saveDocument = false;

		// Check whether or not given document belongs to current user
		if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {

			// Make sure the document is not locked.
			if ((doc.getLocked() == null) || !doc.getLocked()) {

				// Replace file contents with MD5 hash of original contents
				if ((doc.getDocumentSharings() == null) || (doc.getDocumentSharings().size() < 1)) {
					Iterator it = doc.getDocumentFiles().iterator();
					while (it.hasNext()) {
						DocumentFile docFile = (DocumentFile) it.next();
						String resultCode = this.deflateDocumentFile(doc.getId(), docFile.getId(), true);

						// Make sure no known error code was returned
						if (resultCode.equalsIgnoreCase("already_deleted")) {
							AditCodedException aditCodedException = new AditCodedException("file.isDeleted");
							aditCodedException.setParameters(new Object[] { new Long(docFile.getId()).toString() });
							throw aditCodedException;
						} else if (resultCode.equalsIgnoreCase("file_does_not_exist")) {
							AditCodedException aditCodedException = new AditCodedException("file.nonExistent");
							aditCodedException.setParameters(new Object[] { new Long(docFile.getId()).toString() });
							throw aditCodedException;
						} else if (resultCode.equalsIgnoreCase("file_does_not_belong_to_document")) {
							AditCodedException aditCodedException = new AditCodedException("file.doesNotBelongToDocument");
							aditCodedException.setParameters(new Object[] { new Long(docFile.getId()).toString(), new Long(doc.getId()).toString() });
							throw aditCodedException;
						}
					}
				}

				// Mark document as deleted
				doc.setDeleted(true);
				saveDocument = true;
			} else {
				AditCodedException aditCodedException = new AditCodedException("request.deleteDocument.document.locked");
				aditCodedException.setParameters(new Object[] { new Long(documentId).toString() });
				throw aditCodedException;
			}
		} else if (doc.getDocumentSharings() != null) {
			// Check whether or not the document has been shared to current user
			boolean changesMade = false;
			Iterator it = doc.getDocumentSharings().iterator();
			while (it.hasNext()) {
				DocumentSharing sharing = (DocumentSharing) it.next();
				if (sharing.getUserCode().equalsIgnoreCase(userCode)) {
					//doc.getDocumentSharings().remove(sharing); // NB! DO NOT DO THAT - can throw ConcurrentModificationException
					it.remove();
					sharing.setDocumentId(0);
					changesMade = true;
				}
			}
			if (changesMade) {
				saveDocument = true;
			} else {
				AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
				aditCodedException.setParameters(new Object[] { new Long(documentId).toString(), userCode });
				throw aditCodedException;
			}
		} else {
			AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
			aditCodedException.setParameters(new Object[] { new Long(documentId).toString(), userCode });
			throw aditCodedException;
		}

		// Save changes to database
		if (saveDocument) {
			// Using Long.MAX_VALUE for disk quota because it is not possible to
			// exceed disk quota by deleting files. Therefore it does not make much
			// sense to calculate the actual disk quota here. 
			this.getDocumentDAO().save(doc, null, Long.MAX_VALUE, null);
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
	public void DeflateDocument(long documentId, String userCode, String applicationName) throws Exception {
		Document doc = this.getDocumentDAO().getDocument(documentId);

		// Check whether or not the document exists
		if (doc == null) {
			AditCodedException aditCodedException = new AditCodedException("document.nonExistent");
			aditCodedException.setParameters(new Object[] { new Long(documentId).toString() });
			throw aditCodedException;
		}

		// Make sure that the document is not deleted
		// NB! doc.getDeleted() can be NULL
		if ((doc.getDeleted() != null) && doc.getDeleted()) {
			AditCodedException aditCodedException = new AditCodedException("document.deleted");
			aditCodedException.setParameters(new Object[] { new Long(documentId).toString() });
			throw aditCodedException;
		}

		// Make sure that the document is not already deflated
		// NB! doc.getDeflated() can be NULL
		if ((doc.getDeflated() != null) && doc.getDeflated()) {
			AditCodedException aditCodedException = new AditCodedException("document.deflated");
			aditCodedException.setParameters(new Object[] { Util.dateToEstonianDateString(doc.getDeflateDate()) });
			throw aditCodedException;
		}

		// Check whether or not the document belongs to user
		if (!doc.getCreatorCode().equalsIgnoreCase(userCode)) {
			AditCodedException aditCodedException = new AditCodedException("document.doesNotBelongToUser");
			aditCodedException.setParameters(new Object[] { new Long(documentId).toString(), userCode });
			throw aditCodedException;

		}

		// Replace file contents with their MD5 hash codes
		Iterator it = doc.getDocumentFiles().iterator();
		while (it.hasNext()) {
			DocumentFile docFile = (DocumentFile) it.next();
			String resultCode = deflateDocumentFile(doc.getId(), docFile.getId(), false);

			// Handle possible exceptions
			if (!resultCode.equalsIgnoreCase("already_deleted")) {
				// Ignore files that are already deleted

				if (resultCode.equalsIgnoreCase("file_does_not_exist")) {
					AditCodedException aditCodedException = new AditCodedException("file.nonExistent");
					aditCodedException.setParameters(new Object[] { new Long(docFile.getId()).toString() });
					throw aditCodedException;
				} else if (resultCode.equalsIgnoreCase("file_does_not_belong_to_document")) {
					AditCodedException aditCodedException = new AditCodedException("file.doesNotBelongToDocument");
					aditCodedException.setParameters(new Object[] {	new Long(docFile.getId()).toString(), new Long(doc.getId()).toString() });
					throw aditCodedException;
				}
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
	}

	/**
	 * Initializes signing of specified document.<br>
	 * Adds a pending signature to documents signature container and returns
	 * hash code of added signature. Returned hash code can be signed using
	 * ID-card in any user interface.
	 * 
	 * @param documentId
	 * 		Document ID specifying which document should be signed
	 * @param manifest
	 * 		Role or resolution of signer
	 * @param country
	 * 		Country part of signers address
	 * @param state
	 * 		County/state part of signers address
	 * @param city
	 * 		City/town/village part of signers address 
	 * @param zip
	 * 		Postal code of signers address
	 * @param certFile
	 * 		Absolute path to signers signing certificate file
	 * @param digidocConfigFile
	 * 		Absolute path tod digidoc configuration file
	 * @param temporaryFilesDir
	 * 		Absolute path to applications temporary files directory 
	 * @param xroadUser
	 * 		{@link AditUser} who executed current request 
	 * @return
	 * 		{@link PrepareSignatureInternalResult} that contains hash code
	 * 		of added signature and indication whether or not adding new
	 * 		signature succeeded. 
	 * @throws Exception
	 */
	public PrepareSignatureInternalResult prepareSignature(
			final long documentId, final String manifest, final String country,
			final String state, final String city, final String zip,
			final String certFile, final String digidocConfigFile,
			final String temporaryFilesDir, final AditUser xroadUser)
			throws Exception {

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
				// compared to cert personal id code more reliably
				String fixedUserCode = Util.getPersonalIdCodeWithoutCountryPrefix(xroadUser.getUserCode());

				// Determine if certificate belongs to same person
				// who executed current query
				String certPersonalIdCode = SignedDoc
						.getSubjectPersonalCode(cert);
				if (!fixedUserCode.equalsIgnoreCase(certPersonalIdCode)) {
					LOG.info("Attempted to sign document " + documentId
							+ " by person \"" + certPersonalIdCode
							+ "\" while logged in as person \"" + fixedUserCode
							+ "\"");
					result.setSuccess(false);
					result
							.setErrorCode("request.prepareSignature.signer.notCurrentUser");
					return result;
				}

				// Load document
				Document doc = (Document) session.get(Document.class,
						documentId);

				SignedDoc sdoc = null;
				if (doc.getSignatureContainer() == null) {
					LOG.debug("Creating new signature container.");
					sdoc = new SignedDoc(SignedDoc.FORMAT_DIGIDOC_XML,
							SignedDoc.VERSION_1_3);
				} else {
					LOG.debug("Loading existing signature container");
					SAXDigiDocFactory factory = new SAXDigiDocFactory();
					sdoc = factory.readSignedDoc(doc.getSignatureContainer()
							.getBinaryStream());

					// Make sure that document is not already signed
					// by the same person.
					int sigCount = sdoc.countSignatures();
					for (int i = 0; i < sigCount; i++) {
						Signature existingSig = sdoc.getSignature(i);
						if (existingSig != null) {
							int certCount = existingSig.countCertValues();
							for (int j = 0; j < certCount; j++) {
								if ((existingSig.getCertValue(j) != null)
										&& (existingSig.getCertValue(j)
												.getCert() != null)) {
									if (fixedUserCode
											.equalsIgnoreCase(SignedDoc
													.getSubjectPersonalCode(existingSig
															.getCertValue(j)
															.getCert()))) {
										throw new AditCodedException(
												"request.prepareSignature.signer.hasAlreadySigned");
									}
								}
							}
						}
					}
				}

				String[] claimedRoles = null;
				if ((manifest != null) && (manifest.length() > 0)) {
					claimedRoles = new String[] { manifest };
				}
				SignatureProductionPlace address = null;
				if (((country != null) && (country.length() > 0))
						|| ((state != null) && (state.length() > 0))
						|| ((city != null) && (city.length() > 0))
						|| ((zip != null) && (zip.length() > 0))) {

					address = new SignatureProductionPlace();
					address.setCountryName(country);
					address.setStateOrProvince(state);
					address.setCity(city);
					address.setPostalCode(zip);
				}

				// Create unique subdirectory for files
				uniqueDir = new File(temporaryFilesDir + File.separator
						+ documentId);
				int uniqueCounter = 0;
				while (uniqueDir.exists()) {
					uniqueDir = new File(temporaryFilesDir + File.separator
							+ documentId + "_" + (++uniqueCounter));
				}
				uniqueDir.mkdir();

				List<DocumentFile> filesList = new ArrayList<DocumentFile>(doc.getDocumentFiles());
				for (DocumentFile docFile : filesList) {
					if (!docFile.getDeleted()) {
						String outputFileName = uniqueDir.getAbsolutePath()
								+ File.separator + docFile.getFileName();

						InputStream blobDataStream = null;
						FileOutputStream fileOutputStream = null;
						try {
							byte[] buffer = new byte[10240];
							int len = 0;
							blobDataStream = docFile.getFileData()
									.getBinaryStream();
							fileOutputStream = new FileOutputStream(
									outputFileName);
							while ((len = blobDataStream.read(buffer)) > 0) {
								fileOutputStream.write(buffer, 0, len);
							}

							// Add file to signature container
							sdoc.addDataFile(new File(outputFileName), docFile.getContentType(), DataFile.CONTENT_EMBEDDED_BASE64);
						} catch (IOException ex) {
							throw new HibernateException(ex);
						} finally {
							try {
								if (blobDataStream != null) {
									blobDataStream.close();
								}
								blobDataStream = null;
							} catch (Exception ex) {
							}

							try {
								if (fileOutputStream != null) {
									fileOutputStream.close();
								}
								fileOutputStream = null;
							} catch (Exception ex) {
							}
						}

					}
				}

				// Add signature and calculate digest
				Signature sig = sdoc.prepareSignature(cert, claimedRoles,
						address);
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
				String containerFileName = Util
						.generateRandomFileNameWithoutExtension();
				containerFileName = temporaryFilesDir + File.separator
						+ containerFileName + "_PSv1.adit";
				sdoc.writeToFile(new File(containerFileName));

				// Add signature container to document table
				FileInputStream fileInputStream = null;
				try {
					fileInputStream = new FileInputStream(containerFileName);
				} catch (FileNotFoundException e) {
					LOG.error("Error reading digidoc container file: ", e);
				}
				long length = (new File(containerFileName)).length();
				// Blob containerData = Hibernate.createBlob(fileInputStream,
				// length, session);
				Blob containerData = Hibernate.createBlob(fileInputStream, length);
				doc.setSignatureContainer(containerData);

				// Update document
				session.update(doc);
			} finally {
				// Delete temporary directory that was created
				// only for this method.
				try {
					Util.deleteDir(uniqueDir);
				} catch (Exception ex) {
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
	 * 		Document ID specifying which document the users signature belongs to
	 * @param signatureFileName
	 * 		Absolute path to file containing users signature
	 * @param requestPersonalCode
	 * 		Personal ID code of the person who executed current request
	 * @param digidocConfigFile
	 * 		Absolute path tod digidoc configuration file
	 * @param temporaryFilesDir
	 * 		Absolute path to applications temporary files directory 
	 * @throws Exception
	 */
	public void confirmSignature(final long documentId,
			final String signatureFileName, final String requestPersonalCode,
			final String digidocConfigFile, final String temporaryFilesDir)
			throws Exception {

		Session session = null;
		Transaction tx = null;
		try {
			session = this.getDocumentDAO().getSessionFactory().openSession();
			tx = session.beginTransaction();

			Document doc = (Document) session.get(Document.class, documentId);

			ConfigManager.init(digidocConfigFile);
			SAXDigiDocFactory factory = new SAXDigiDocFactory();
			SignedDoc sdoc = factory.readSignedDoc(doc.getSignatureContainer()
					.getBinaryStream());

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
					}
				}
			}

			Signature sig = null;
			for (int i = 0; i < sdoc.countSignatures(); i++) {
				String signerPersonalCode = SignedDoc
						.getSubjectPersonalCode(sdoc.getSignature(i)
								.getLastCertValue().getCert());
				if (requestPersonalCode.endsWith(signerPersonalCode)) {
					sig = sdoc.getSignature(i);
					break;
				}
			}

			if (sig != null) {
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
					LOG.error("Error reading digidoc container file: ", e);
				}
				long length = (new File(containerFileName)).length();
				// Blob containerData = Hibernate.createBlob(fileInputStream,
				// length, session);
				Blob containerData = Hibernate.createBlob(fileInputStream, length);
				doc.setSignatureContainer(containerData);

				// Update document
				session.update(doc);

				// Add signature metadata to signature table
				ee.adit.dao.pojo.Signature aditSig = new ee.adit.dao.pojo.Signature();
				if (sig.getSignedProperties() != null) {
					if (sig.getSignedProperties().getSignatureProductionPlace() != null) {
						ee.sk.digidoc.SignatureProductionPlace location = sig.getSignedProperties().getSignatureProductionPlace();
						aditSig.setCity(location.getCity());
						aditSig.setCountry(location.getCountryName());
						aditSig.setCounty(location.getStateOrProvince());
						aditSig.setPostIndex(location.getPostalCode());
					}
					if (sig.getSignedProperties().getClaimedRole(0) != null) {
						aditSig.setSignerRole(sig.getSignedProperties().getClaimedRole(0));
					}
				}
				aditSig.setDocument(doc);
				if ((sig.getLastCertValue() != null)
					&& (sig.getLastCertValue().getCert() != null)) {
					X509Certificate cert = sig.getLastCertValue().getCert();
					aditSig.setSignerCode(SignedDoc.getSubjectPersonalCode(cert));
					aditSig.setSignerName(SignedDoc.getSubjectLastName(cert) + ", " + SignedDoc.getSubjectFirstName(cert));
				}
				aditSig.setUserCode(requestPersonalCode);
				session.save(aditSig);
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
