package ee.adit.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.TransformerException;

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
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

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
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dao.pojo.DocumentType;
import ee.adit.dvk.DvkDAO;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.util.Configuration;
import ee.adit.util.SaveDocumentAttachmentHandler;
import ee.adit.util.Util;

public class DocumentService {

	// Dokumendi jagamise tüüpide koodid
	public static final String SharingType_Sign = "sign";
	public static final String SharingType_Share = "share";
	public static final String SharingType_SendDvk = "send_dvk";
	public static final String SharingType_SendAdit = "send_adit";

	// Document DVK statuses
	public static final Long DVKStatus_Missing = new Long(100);
	public static final Long DVKStatus_Waiting = new Long(101);
	public static final Long DVKStatus_Sending = new Long(102);
	public static final Long DVKStatus_Sent = new Long(103);
	public static final Long DVKStatus_Aborted = new Long(104);
	public static final Long DVKStatus_Received = new Long(105);

	// Dokumendi ajaloosündmuste koodid
	public static final String HistoryType_Create = "create";
	public static final String HistoryType_Modify = "modify";
	public static final String HistoryType_AddFile = "add_file";
	public static final String HistoryType_ModifyFile = "modify_file";
	public static final String HistoryType_DeleteFile = "delete_file";
	public static final String HistoryType_ModifyStatus = "modify_status";
	public static final String HistoryType_Send = "send";
	public static final String HistoryType_Share = "share";
	public static final String HistoryType_UnShare = "unshare";
	public static final String HistoryType_Lock = "lock";
	public static final String HistoryType_UnLock = "unlock";
	public static final String HistoryType_Deflate = "deflate";
	public static final String HistoryType_Sign = "sign";
	public static final String HistoryType_Delete = "delete";
	public static final String HistoryType_MarkViewed = "markViewed";

	// Kasutatava DVK konteineri versioon
	public static final int DVK_CONTAINER_VERSION = 2;

	// DVK vastuskirja dokumendi pealkiri
	public static final String DvkErrorResponseMessage_Title = "ADIT vastuskiri";

	// DVK vastuskirja faili nimi
	public static final String DvkErrorResponseMessage_FileName = "ADIT_vastuskiri.pdf";

	// Document types
	public static final String DocType_Letter = "letter";
	public static final String DocType_Application = "application";

	public static final Integer DvkReceiveFailReason_UserDoesNotExist = 1;
	public static final Integer DvkReceiveFailReason_UserUsesDvk = 2;

	private static Logger LOG = Logger.getLogger(UserService.class);
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

	public List<String> checkAttachedDocumentMetadataForNewDocument(SaveDocumentRequestAttachment document, long remainingDiskQuota, String xmlFile, String tempDir) throws AditException {
		List<String> result = null;
		LOG.debug("Checking attached document metadata for new document...");
		if (document != null) {

			LOG.debug("Checking GUID: " + document.getGuid());
			// Check GUID
			if (document.getGuid() != null) {
				// Check GUID format
				try {
					UUID.fromString(document.getGuid());
				} catch (Exception e) {
					String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.guid.wrongFormat", new Object[] {}, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}

			}

			LOG.debug("Checking title: " + document.getTitle());
			// Check title
			if (document.getTitle() == null || "".equalsIgnoreCase(document.getTitle())) {
				String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.title.undefined", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			LOG.debug("Checking document type: " + document.getDocumentType());
			// Check document_type

			if (document.getDocumentType() != null && !"".equalsIgnoreCase(document.getDocumentType().trim())) {

				// Is the document type valid?
				LOG.debug("Document type is defined. Checking if it is valid.");
				DocumentType documentType = this.getDocumentTypeDAO().getDocumentType(document.getDocumentType());

				if (documentType == null) {
					LOG.debug("Document type does not exist: " + document.getDocumentType());
					String validDocumentTypes = getValidDocumentTypes();
					String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.type.nonExistent", new Object[] { validDocumentTypes }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}

			} else {
				String validDocumentTypes = getValidDocumentTypes();
				String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.type.undefined", new Object[] { validDocumentTypes }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			LOG.debug("Checking previous document ID: " + document.getPreviousDocumentID());
			// Check previous_document_id
			if (document.getPreviousDocumentID() != null && document.getPreviousDocumentID() != 0) {
				// Check if the document exists

				Document previousDocument = this.getDocumentDAO().getDocument(document.getPreviousDocumentID());

				if (previousDocument == null) {
					String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.previousDocument.nonExistent", new Object[] { document.getPreviousDocumentID() },
							Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
			}

			result = extractFilesFromXML(document.getFiles(), xmlFile, remainingDiskQuota, tempDir);

		} else {
			throw new AditInternalException("Document not initialized.");
		}

		return result;
	}

	public List<String> extractFilesFromXML(List<OutputDocumentFile> files, String xmlFileName, long remainingDiskQuota, String tempDir) {

		List<String> result = new ArrayList<String>();

		// TODO: Check files - at least one file must be defined.
		// The <data> tags of the <file> elements did not get unmarshalled (to
		// save memory).
		// That is why we need to check those files on the disk. We need the
		// sizes of the <data> elements.
		// 1. Get the XML file
		// 2. find the <data> elements
		// 3. For each <data> element, create a temporary file and add a
		// reference to the document object
		long totalSize = 0;

		LOG.debug("Checking files");
		try {
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(xmlFileName);

				SaveDocumentAttachmentHandler handler = new SaveDocumentAttachmentHandler(tempDir);

				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				xmlReader.setContentHandler(handler);

				InputSource inputSource = new InputSource(fileInputStream);
				xmlReader.parse(inputSource);

				result = handler.getFiles();
			} finally {
				if (fileInputStream != null) {
					try {
						fileInputStream.close();
					} catch (Exception ex) {
					}
				}
			}

			// Add references to file objects
			for (int i = 0; i < result.size(); i++) {
				String fileName = result.get(i);
				String base64DecodedFile = Util.base64DecodeFile(fileName, tempDir);

				OutputDocumentFile file = files.get(i);
				LOG.debug("Adding reference to file object. File ID: " + file.getId() + " (" + file.getName() + "). Temporary file: " + base64DecodedFile);
				file.setTmpFileName(base64DecodedFile);

				totalSize += (new File(base64DecodedFile)).length();
			}

			LOG.debug("Total size of document files: " + totalSize);

			if (remainingDiskQuota < totalSize) {
				String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.files.quotaExceeded", new Object[] { remainingDiskQuota, totalSize }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

		} catch (Exception e) {
			throw new AditInternalException("Error parsing attachment: ", e);
		}

		return result;
	}

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

	public String deflateDocumentFile(long documentId, long fileId, boolean markDeleted) {
		return this.getDocumentFileDAO().deflateDocumentFile(documentId, fileId, markDeleted);
	}

	@Transactional
	public Long save(final SaveDocumentRequestAttachment attachmentDocument, final List<String> fileNames, final String creatorCode, final String remoteApplication) throws FileNotFoundException {
		final DocumentDAO docDao = this.getDocumentDAO();

		return (Long) this.getDocumentDAO().getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Date creationDate = new Date();
				Document document = new Document();
				if ((attachmentDocument.getId() != null) && (attachmentDocument.getId() > 0)) {
					document = (Document) session.get(Document.class, attachmentDocument.getId());
					LOG.debug("Document file count: " + document.getDocumentFiles().size());
				} else {
					document.setCreationDate(creationDate);
					document.setCreatorCode(creatorCode);
					document.setRemoteApplication(remoteApplication);
					document.setSignable(true);
				}

				document.setDocumentType(attachmentDocument.getDocumentType());
				if (attachmentDocument.getGuid() != null && !"".equalsIgnoreCase(attachmentDocument.getGuid().trim())) {
					document.setGuid(attachmentDocument.getGuid());
				} else if ((document.getGuid() == null) || "".equalsIgnoreCase(attachmentDocument.getGuid().trim())) {
					// Generate new GUID
					document.setGuid(Util.generateGUID());
				}

				document.setLastModifiedDate(creationDate);
				document.setTitle(attachmentDocument.getTitle());

				return docDao.save(document, attachmentDocument.getFiles(), session);
			}
		});
	}

	public Long saveDocumentFile(final long documentId, final OutputDocumentFile file, final String attachmentXmlFile, final long remainingDiskQuota, final String temporaryFilesDir) {

		final DocumentDAO docDao = this.getDocumentDAO();

		return (Long) this.getDocumentDAO().getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Document document = (Document) session.get(Document.class, documentId);
				List<OutputDocumentFile> filesList = new ArrayList<OutputDocumentFile>();
				filesList.add(file);

				// TODO: Document to database
				extractFilesFromXML(filesList, attachmentXmlFile, remainingDiskQuota, temporaryFilesDir);
				docDao.save(document, filesList, session);
				long fileId = filesList.get(0).getId();
				LOG.debug("File saved with ID: " + fileId);
				return fileId;
			}
		});
	}

	public void save(Document doc) {
		this.getDocumentDAO().save(doc, null, null);
	}

	/**
	 * Locks the document.
	 * 
	 * @param document
	 *            the document to be locked.
	 */
	public void lockDocument(Document document) {
		if (!document.getLocked()) {
			LOG.debug("Locking document: " + document.getId());
			document.setLocked(true);
			document.setLockingDate(new Date());
			save(document);
			LOG.info("Document locked: " + document.getId());
		}
	}

	public boolean sendDocument(Document document, AditUser recipient) {
		boolean result = false;

		DocumentSharing documentSharing = new DocumentSharing();
		documentSharing.setDocumentId(document.getId());
		documentSharing.setCreationDate(new Date());

		if (recipient.getDvkOrgCode() != null && !"".equalsIgnoreCase(recipient.getDvkOrgCode().trim())) {
			documentSharing.setDocumentSharingType(DocumentService.SharingType_SendDvk);
		} else {
			documentSharing.setDocumentSharingType(DocumentService.SharingType_SendAdit);
		}

		documentSharing.setUserCode(recipient.getUserCode());
		documentSharing.setUserName(recipient.getFullName());

		this.getDocumentSharingDAO().save(documentSharing);

		if (documentSharing.getId() == 0) {
			throw new AditInternalException("Could not add document sharing information to database.");
		}

		return result;
	}

	public void addHistoryEvent(String applicationName, Document doc, String userCode, String historyType, String xteeUserCode, String xteeUserName, String description) {
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
	 * are recognized by the following: 1. The document has at least one
	 * DocumentSharing associated with it 2. That DocumentSharing must have the
	 * "documentSharingType" equal to "send_dvk" 3. That DocumentSharing must
	 * have the "documentDvkStatus" not initialized or set to "100"
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public int sendDocumentsToDVK() {
		int result = 0;

		final String SQL_QUERY = "select doc from Document doc, DocumentSharing docSharing where docSharing.documentSharingType = 'send_dvk' and (docSharing.documentDvkStatus is null or docSharing.documentDvkStatus = 100) and docSharing.documentId = doc.id";

		final String tempDir = this.getConfiguration().getTempDir();

		LOG.debug("Fetching documents for sending to DVK...");
		Session session = this.getDocumentDAO().getSessionFactory().getCurrentSession();

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

				Iterator<DocumentSharing> documentSharings = document.getDocumentSharings().iterator();

				while (documentSharings.hasNext()) {
					DocumentSharing documentSharing = documentSharings.next();

					if (DocumentService.SharingType_SendDvk.equalsIgnoreCase(documentSharing.getDocumentSharingType())) {

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
					Fail dvkFile = new Fail();

					LOG.debug("FileName: " + f.getFileName());

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

				failideKonteiner.setKokku(count);
				failideKonteiner.setFailid(dvkFiles);
				dvkContainer.setFailideKonteiner(failideKonteiner);

				// Save document to DVK Client database

				SessionFactory sessionFactory = DVKAPI.createSessionFactory("hibernate_ora_dvk.cfg.xml");
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
					dvkMessage.setSendingStatusId(DocumentService.DVKStatus_Waiting);

					// Insert data as stream
					Clob clob = Hibernate.createClob(" ", dvkSession);
					dvkMessage.setData(clob);

					dvkMessageID = (Long) dvkSession.save(dvkMessage);

					if (dvkMessageID == null || dvkMessageID.longValue() == 0) {
						LOG.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
						throw new DataRetrievalFailureException("Error while saving outgoing message to DVK database - no ID returned by save method.");
					} else {
						LOG.info("Outgoing message saved to DVK database. ID: " + dvkMessageID);
					}

					LOG.debug("DVK Message saved to client database. GUID: " + dvkMessage.getDhlGuid());
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
					String temporaryFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();
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

					// Update document sharings status
					Iterator<DocumentSharing> documentSharingUpdateIterator = document.getDocumentSharings().iterator();
					while (documentSharingUpdateIterator.hasNext()) {
						DocumentSharing documentSharing = documentSharingUpdateIterator.next();
						if (DocumentService.SharingType_SendDvk.equalsIgnoreCase(documentSharing.getDocumentSharingType())) {
							documentSharing.setDocumentDvkStatus(DVKStatus_Sending);
							session.saveOrUpdate(documentSharing);
							LOG.debug("DocumentSharing status updated to: '" + DVKStatus_Sending + "'.");
						}
					}

					result++;

				} catch (Exception e) {
					dvkTransaction2.rollback();

					// Remove the document with empty clob from the database
					Session dvkSession3 = sessionFactory.openSession();
					Transaction dvkTransaction3 = dvkSession3.beginTransaction();
					try {
						LOG.debug("Starting to delete document from DVK Client database: " + dvkMessageID);
						PojoMessage dvkMessageToDelete = (PojoMessage) dvkSession3.load(PojoMessage.class, dvkMessageID);
						if (dvkMessageToDelete == null) {
							LOG.warn("DVK message to delete is not initialized.");
						}
						dvkSession3.delete(dvkMessageToDelete);
						dvkTransaction3.commit();
						LOG.info("Empty DVK document deleted from DVK Client database. ID: " + dvkMessageID);
					} catch (Exception dvkException) {
						dvkTransaction3.rollback();
						LOG.error("Error deleting document from DVK database: ", dvkException);
					} finally {
						if (dvkSession3 != null) {
							dvkSession3.close();
						}
					}

					throw new DataRetrievalFailureException("Error while adding message to DVK Client database (CLOB update): ", e);
				} finally {
					if (dvkSession2 != null) {
						dvkSession2.close();
					}
				}
			} catch (Exception e) {

				// TODO: if something fails during the operation - the
				// document.dvkId is not set and that reference is lost.
				// The GUID reference is still valid.

				throw new AditInternalException("Error while sending documents to DVK Client database: ", e);
			}
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
				Iterator<PojoMessage> dvkDocumentsIterator = dvkDocuments.iterator();

				while (dvkDocumentsIterator.hasNext()) {
					PojoMessage dvkDocument = dvkDocumentsIterator.next();

					// Get the DVK Container
					ContainerVer2 dvkContainer = this.getDVKContainer(dvkDocument);

					List<Saaja> recipients = dvkContainer.getTransport().getSaajad();

					if (recipients != null && recipients.size() > 0) {
						LOG.debug("Recipients for this message: " + recipients.size());

						// For every recipient - check if registered in ADIT
						Iterator<Saaja> recipientsIterator = recipients.iterator();

						while (recipientsIterator.hasNext()) {
							Saaja recipient = recipientsIterator.next();

							LOG.info("Recipient: " + recipient.getRegNr() + " (" + recipient.getAsutuseNimi() + "). Isikukood: '" + recipient.getIsikukood() + "'.");

							// The ADIT internal recipient is always marked by
							// the field <isikukood> in the DVK container,
							// regardless if it is actually a person or an
							// institution / company.
							if (recipient.getRegNr() != null && !recipient.getRegNr().equalsIgnoreCase("")) {
								if (recipient.getIsikukood() != null && !recipient.getIsikukood().equals("")) {
									// The recipient is specified - check if
									// it's a DVK user

									LOG.debug("Getting AditUser by personal code: " + recipient.getIsikukood().trim());
									AditUser user = this.getAditUserDAO().getUserByID(recipient.getIsikukood().trim());

									if (user != null && user.getActive()) {

										// Check if user uses DVK
										if (user.getDvkOrgCode() != null && !user.getDvkOrgCode().equalsIgnoreCase("")) {
											// The user uses DVK - this is not
											// allowed. Users that use DVK have
											// to exchange
											// documents with other users that
											// use DVK, over DVK.
											this.composeErrorResponse(DocumentService.DvkReceiveFailReason_UserUsesDvk, dvkContainer, recipient.getIsikukood().trim(), dvkDocument.getReceivedDate(),
													recipient.getNimi());
											throw new AditInternalException("User uses DVK - not allowed.");
										}

										LOG.debug("Constructing ADIT message");
										// Add document for this recipient to
										// ADIT database
										Document aditDocument = new Document();
										aditDocument.setCreationDate(new Date());
										aditDocument.setDocumentDvkStatusId(DVKStatus_Sent);
										aditDocument.setDvkId(dvkDocument.getDhlId());
										aditDocument.setGuid(dvkDocument.getDhlGuid());
										aditDocument.setLocked(true);
										aditDocument.setLockingDate(new Date());
										aditDocument.setSignable(true);
										aditDocument.setTitle(dvkDocument.getTitle());
										aditDocument.setDocumentType(DocType_Letter);

										// The creator is the recipient
										aditDocument.setCreatorCode(user.getUserCode());
										aditDocument.setCreatorName(user.getFullName());

										// Get document files from DVK container
										List<OutputDocumentFile> tempDocuments = this.getDocumentOutputFiles(dvkContainer);

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
												throw new AditInternalException("Document already sent to user. DVK ID: " + dvkDocument.getDhlId() + ", recipient: " + recipient.getIsikukood().trim());
											}

											// Save document
											Long aditDocumentID = this.getDocumentDAO().save(aditDocument, tempDocuments, aditSession);
											LOG.info("Document saved to ADIT database. ID: " + aditDocumentID);

											// Update document status to "sent"
											// (recipient_status_id = "102") in
											// DVK Client database
											dvkDocument.setRecipientStatusId(DVKStatus_Sent);
											this.getDvkDAO().updateDocument(dvkDocument);

											// Finally commit
											aditTransaction.commit();

										} catch (Exception e) {
											LOG.debug("Error saving document to ADIT database: ", e);
											if (aditTransaction != null) {
												aditTransaction.rollback();
											}
										} finally {
											if (aditSession != null) {
												aditSession.close();
											}
										}
									} else {
										LOG.error("User not found. Personal code: " + recipient.getIsikukood().trim());
										this.composeErrorResponse(DocumentService.DvkReceiveFailReason_UserDoesNotExist, dvkContainer, recipient.getIsikukood().trim(), dvkDocument.getReceivedDate(),
												recipient.getNimi());
									}
								}
							}
						}
					} else {
						LOG.warn("No recipients found for this message: " + dvkDocument.getDhlGuid());
					}
				}
			} else {
				LOG.info("No incoming messages found in DVK Client database.");
			}
		} catch (Exception e) {
			throw new AditInternalException("Error while receiving documents from DVK Client database: ", e);
		}

		return result;
	}

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
					throw new AditInternalException("DVK Container not properly initialized: <transport> section not initialized");
				}
			}

		} catch (Exception e) {
			throw new AditInternalException("Exception while reading DVK container from database: ", e);
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

	public List<OutputDocumentFile> getDocumentOutputFiles(ContainerVer2 dvkContainer) {
		List<OutputDocumentFile> result = new ArrayList<OutputDocumentFile>();

		try {
			List<Fail> dvkFiles = dvkContainer.getFailideKonteiner().getFailid();
			Iterator<Fail> dvkFilesIterator = dvkFiles.iterator();

			LOG.debug("Total number of files in DVK Container: " + dvkContainer.getFailideKonteiner().getKokku());

			while (dvkFilesIterator.hasNext()) {
				Fail dvkFile = dvkFilesIterator.next();
				LOG.debug("Processing file nr.: " + dvkFile.getJrkNr());

				// TODO: STREAM
				String fileContents = dvkFile.getZipBase64Sisu();
				InputStream inputStream = new StringBufferInputStream(fileContents);
				String tempFile = Util.createTemporaryFile(inputStream, this.getConfiguration().getTempDir());

				String decodedTempFile = Util.base64DecodeAndUnzip(tempFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());

				OutputDocumentFile tempDocument = new OutputDocumentFile();
				tempDocument.setTmpFileName(decodedTempFile);
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
						if (documentToDelete.getTmpFileName() != null && !documentToDelete.getTmpFileName().trim().equalsIgnoreCase("")) {
							File f = new File(documentToDelete.getTmpFileName());
							if (f.exists()) {
								f.delete();
							} else {
								throw new FileNotFoundException("Could not find temporary file (to delete): " + documentToDelete.getTmpFileName());
							}
						}
					} catch (Exception exc) {
						LOG.debug("Error while deleting temporary files: ", exc);
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
	public int updateDocumentsFromDVK() {
		int result = 0;

		// 1. Võtame kõik dokumendid ADIT andmebaasist, millel DVK staatus ei
		// ole "saadetud"
		List<Document> documents = this.getDocumentDAO().getDocumentsWithoutDVKStatus(DVKStatus_Sent);
		Iterator<Document> documentsIterator = documents.iterator();

		while (documentsIterator.hasNext()) {
			Document document = documentsIterator.next();

			try {
				LOG.info("Updating DVK status for document. DocumentID: " + document.getId());

				List<PojoMessageRecipient> messageRecipients = this.getDvkDAO().getMessageRecipients(document.getDvkId(), false);
				Iterator<PojoMessageRecipient> messageRecipientIterator = messageRecipients.iterator();
				List<DocumentSharing> documentSharings = this.getDocumentSharingDAO().getDVKSharings(document.getId());

				if (messageRecipients != null)
					LOG.debug("messageRecipients.size: " + messageRecipients.size());

				if (documentSharings != null)
					LOG.debug("documentSharings.size: " + documentSharings.size());

				while (messageRecipientIterator.hasNext()) {
					PojoMessageRecipient messageRecipient = messageRecipientIterator.next();

					LOG.debug("Updating for messageRecipient: " + messageRecipient.getRecipientOrgCode());

					boolean allDocumentSharingsSent = true;

					// Compare the status with the status of the sharing in ADIT
					for (int i = 0; i < documentSharings.size(); i++) {
						DocumentSharing documentSharing = documentSharings.get(i);
						LOG.debug("Updating for documentSharing: " + documentSharing.getId());

						if (documentSharing.getUserCode().equalsIgnoreCase(messageRecipient.getRecipientPersonCode())
								|| documentSharing.getUserCode().equalsIgnoreCase(messageRecipient.getRecipientOrgCode())) {

							// If the statuses differ, update the one in ADIT
							// database
							if (!documentSharing.getDocumentDvkStatus().equals(messageRecipient.getSendingStatusId())) {
								documentSharing.setDocumentDvkStatus(messageRecipient.getSendingStatusId());
								this.getDocumentSharingDAO().update(documentSharing);
								LOG.debug("DocumentSharing DVK status updated: documentSharingID: " + documentSharing.getId() + ", DVK status: " + documentSharing.getDocumentDvkStatus());
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
						document.setDocumentDvkStatusId(DocumentService.DVKStatus_Sent);
						this.getDocumentDAO().update(document);
						LOG.debug("All DVK sharings for this document updated to 'sent'. Updating document DVK status.");
					}
				}
			} catch (Exception e) {
				LOG.error("Error while updating status from DVK. DocumentID: " + document.getId(), e);
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
		List<PojoMessage> dvkDocuments = this.getDvkDAO().getIncomingDocumentsWithoutStatus(DocumentService.DVKStatus_Sent);
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

							// If the statuses do not match, update from ADIT to
							// DVK
							dvkDocument.setRecipientStatusId(document.getDocumentDvkStatusId());

							// Update DVK document
							this.getDvkDAO().updateDocument(dvkDocument);
						}
					} else {
						throw new AditInternalException("Could not update document with DVK_ID: " + dvkDocument.getDhlMessageId() + ". Document's DVK status is not defined in ADIT.");
					}
				} else {
					throw new AditInternalException("Could not find document with DVK_ID: " + dvkDocument.getDhlMessageId());
				}
			} catch (Exception e) {
				LOG.error("Error while updating DVK status for document. DVK_ID: " + dvkDocument.getDhlMessageId());
				LOG.error("Continue...");
			}
		}

		return result;
	}

	/**
	 * TODO: IMPLEMENT
	 * 
	 * Kui dokumendi sidumisel kasutajaga ilmnes, et kasutajat pole ADIT
	 * aktiivsete kasutajate hulgas, siis märgitakse dokument DVKs katkestatuks
	 * ning algsele saatjale koostatakse automaatselt vastuskiri, milles on
	 * toodud kaaskirja dokument (muudetav ADIT haldaja poolt) ning algne
	 * dokument. Kui dokumendi adressaadiks on DVK kasutaja, siis talitatakse
	 * sarnaselt eeltoodule, kuna DVK kasutajad peavad suhtlema otse omavahel,
	 * mitte ADIT kaudu.
	 */
	public void composeErrorResponse(Integer reasonCode, ContainerVer2 dvkContainer, String recipientCode, Date receivedDate, String recipientName) throws AditInternalException {

		// TODO:
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
			LOG.debug("DVK error response message composed. FileName: " + outputPDFFile);
			this.saveErrorResponseMessageToDVK(outputPDFFile, dvkContainer);

		} catch (Exception e) {
			LOG.error("Error while composing DVK error response message: ", e);
			throw new AditInternalException("Error while composing DVK error response message: ", e);
		}
	}

	public String createErrorResponseDataXML(ContainerVer2 dvkContainer, String recipientCode, Date receivedDate, String recipientName) {
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
				LOG.error("Error while parsing received date (system date): ", exc);
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
			title = dvkContainer.getMetainfo().getMetaManual().getDokumentPealkiri();
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
			metaManual.setDokumentLiik(DocType_Letter);
			metaManual.setDokumentPealkiri(DvkErrorResponseMessage_Title);

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
			responseFile.setFailNimi(DvkErrorResponseMessage_FileName);
			responseFile.setFailPealkiri(DvkErrorResponseMessage_Title);
			responseFile.setFailSuurus(dataFile.length());
			responseFile.setFailTyyp(MimeConstants.MIME_PDF);
			responseFile.setJrkNr((short) 1);
			responseFile.setKrypteering(false);
			responseFile.setPohiDokument(true);
			files.add(responseFile);
			failideKonteiner.setFailid(files);

			container.setFailideKonteiner(failideKonteiner);

			String temporaryFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();
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
					LOG.info("Outgoing message saved to DVK database. ID: " + dvkMessageID);
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

			LOG.debug("DVK Message saved to client database. GUID: " + message.getDhlGuid());
			dvkTransaction.commit();

			// Update CLOB
			Session dvkSession2 = this.getDocumentDAO().getSessionFactory().openSession();
			Transaction dvkTransaction2 = dvkSession2.beginTransaction();

			try {
				// Select the record for update
				PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2.load(PojoMessage.class, dvkMessageID, LockMode.UPGRADE);

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
					LOG.debug("Starting to delete document from DVK Client database: " + dvkMessageID);
					PojoMessage dvkMessageToDelete = (PojoMessage) dvkSession3.load(PojoMessage.class, dvkMessageID);
					if (dvkMessageToDelete == null) {
						LOG.warn("DVK message to delete is not initialized.");
					}
					dvkSession3.delete(dvkMessageToDelete);
					dvkTransaction3.commit();
					LOG.info("Empty DVK document deleted from DVK Client database. ID: " + dvkMessageID);
				} catch (Exception dvkException) {
					dvkTransaction3.rollback();
					LOG.error("Error deleting document from DVK database: ", dvkException);
				} finally {
					if (dvkSession3 != null) {
						dvkSession3.close();
					}
				}
				throw new DataRetrievalFailureException("Error while adding message to DVK Client database (CLOB update): ", e);
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
