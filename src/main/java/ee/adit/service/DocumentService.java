package ee.adit.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import oracle.sql.CLOB;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import dvk.api.DVKAPI;
import dvk.api.IMessage;
import dvk.api.ISessionCacheBox;
import dvk.api.container.v2.ContainerVer2;
import dvk.api.container.v2.Fail;
import dvk.api.container.v2.FailideKonteiner;
import dvk.api.container.v2.MetaManual;
import dvk.api.container.v2.Metainfo;
import dvk.api.ml.DvkSessionCacheBox;
import dvk.api.ml.PojoMessage;

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
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.util.Configuration;
import ee.adit.util.HibernateUtil;
import ee.adit.util.SaveDocumentAttachmentHandler;
import ee.adit.util.Util;

public class DocumentService {

	// Dokumendi jagamise tüüpide koodid
	public static final String SharingType_Sign = "sign";
	public static final String SharingType_Share = "share";
	public static final String SharingType_SendDvk = "send_dvk";
	public static final String SharingType_SendAdit = "send_adit";

	// Dokumendi ajaloosündmuste koodid
	public static final String HistoryType_Create = "create";
	public static final String HistoryType_Modify = "modify";
	public static final String HistoryType_AddFile = "add_file";
	public static final String HistoryType_ModifyFile = "modify_file";
	public static final String HistoryType_DeleteFile = "delete_file";
	public static final String HistoryType_ModifyStatus = "modify_status";
	public static final String HistoryType_Send = "send";
	public static final String HistoryType_Share = "share";
	public static final String HistoryType_Lock = "lock";
	public static final String HistoryType_Deflate = "deflate";
	public static final String HistoryType_Sign = "sign";
	public static final String HistoryType_Delete = "delete";
	public static final String HistoryType_MarkViewed = "markViewed";

	// Kasutatava DVK konteineri versioon
	public static final int DVK_CONTAINER_VERSION = 2;

	private static Logger LOG = Logger.getLogger(UserService.class);
	private MessageSource messageSource;
	private DocumentTypeDAO documentTypeDAO;
	private DocumentDAO documentDAO;
	private DocumentFileDAO documentFileDAO;
	private DocumentWfStatusDAO documentWfStatusDAO;
	private DocumentSharingDAO documentSharingDAO;
	private DocumentHistoryDAO documentHistoryDAO;
	private Configuration configuration;

	public List<String> checkAttachedDocumentMetadataForNewDocument(
			SaveDocumentRequestAttachment document, long remainingDiskQuota,
			String xmlFile, String tempDir) throws AditException {
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
					String errorMessage = this.getMessageSource().getMessage(
							"request.saveDocument.document.guid.wrongFormat",
							new Object[] {}, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}

			}

			LOG.debug("Checking title: " + document.getTitle());
			// Check title
			if (document.getTitle() == null
					|| "".equalsIgnoreCase(document.getTitle())) {
				String errorMessage = this.getMessageSource().getMessage(
						"request.saveDocument.document.title.undefined",
						new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
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
					String errorMessage = this
							.getMessageSource()
							.getMessage(
									"request.saveDocument.document.type.nonExistent",
									new Object[] { validDocumentTypes },
									Locale.ENGLISH);
					throw new AditException(errorMessage);
				}

			} else {
				String validDocumentTypes = getValidDocumentTypes();
				String errorMessage = this.getMessageSource().getMessage(
						"request.saveDocument.document.type.undefined",
						new Object[] { validDocumentTypes }, Locale.ENGLISH);
				throw new AditException(errorMessage);
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
					String errorMessage = this
							.getMessageSource()
							.getMessage(
									"request.saveDocument.document.previousDocument.nonExistent",
									new Object[] { document
											.getPreviousDocumentID() },
									Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
			}

			result = extractFilesFromXML(document.getFiles(), xmlFile,
					remainingDiskQuota, tempDir);

		} else {
			throw new AditInternalException("Document not initialized.");
		}

		return result;
	}

	public List<String> extractFilesFromXML(List<OutputDocumentFile> files,
			String xmlFileName, long remainingDiskQuota, String tempDir) {

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

				SaveDocumentAttachmentHandler handler = new SaveDocumentAttachmentHandler(
						tempDir);

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
				String base64DecodedFile = Util.base64DecodeFile(fileName,
						tempDir);

				OutputDocumentFile file = files.get(i);
				LOG.debug("Adding reference to file object. File ID: "
						+ file.getId() + " (" + file.getName()
						+ "). Temporary file: " + base64DecodedFile);
				file.setTmpFileName(base64DecodedFile);

				totalSize += (new File(base64DecodedFile)).length();
			}

			LOG.debug("Total size of document files: " + totalSize);

			if (remainingDiskQuota < totalSize) {
				String errorMessage = this.getMessageSource().getMessage(
						"request.saveDocument.document.files.quotaExceeded",
						new Object[] { remainingDiskQuota, totalSize },
						Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

		} catch (Exception e) {
			throw new AditInternalException("Error parsing attachment: ", e);
		}

		return result;
	}

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

	public String deflateDocumentFile(long documentId, long fileId,
			boolean markDeleted) {
		return this.getDocumentFileDAO().deflateDocumentFile(documentId,
				fileId, markDeleted);
	}

	@Transactional
	public Long save(final SaveDocumentRequestAttachment attachmentDocument,
			final List<String> fileNames, final String creatorCode,
			final String remoteApplication) throws FileNotFoundException {
		final DocumentDAO docDao = this.getDocumentDAO();

		return (Long) this.getDocumentDAO().getHibernateTemplate().execute(
				new HibernateCallback() {
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

						document.setLastModifiedDate(creationDate);
						document.setTitle(attachmentDocument.getTitle());

						return docDao.save(document, attachmentDocument
								.getFiles(), session);
					}
				});
	}

	public Long saveDocumentFile(final long documentId,
			final OutputDocumentFile file, final String attachmentXmlFile,
			final long remainingDiskQuota, final String temporaryFilesDir) {

		final DocumentDAO docDao = this.getDocumentDAO();

		return (Long) this.getDocumentDAO().getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						Document document = (Document) session.get(
								Document.class, documentId);
						List<OutputDocumentFile> filesList = new ArrayList<OutputDocumentFile>();
						filesList.add(file);

						// TODO: Document to database
						extractFilesFromXML(filesList, attachmentXmlFile,
								remainingDiskQuota, temporaryFilesDir);
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

	public void addHistoryEvent(String applicationName, Document doc,
			String userCode, String historyType, String xteeUserCode,
			String xteeUserName, String description) {
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
	public void getDocumentsForDVKSending() {

		final String SQL_QUERY = "select doc from Document doc, DocumentSharing docSharing where docSharing.documentSharingType = 'send_dvk' and (docSharing.documentDvkStatus is null or docSharing.documentDvkStatus = 100) and docSharing.documentId = doc.id";

		final String tempDir = this.getConfiguration().getTempDir();

		LOG.debug("Fetching documents for sending to DVK...");
		Session session = this.getDocumentDAO().getSessionFactory()
				.getCurrentSession();

		Query query = session.createQuery(SQL_QUERY);
		List<Document> documents = query.list();

		LOG.debug("Documents fetched successfully (" + documents.size() + ")");

		Iterator<Document> i = documents.iterator();

		while (i.hasNext()) {

			Document document = i.next();

			Iterator<DocumentSharing> documentSharings = document
					.getDocumentSharings().iterator();

			while (documentSharings.hasNext()) {
				DocumentSharing documentSharing = documentSharings.next();

				if (DocumentService.SharingType_SendDvk
						.equalsIgnoreCase(documentSharing
								.getDocumentSharingType())) {

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

					// Recipient information
					metaManual.setSaajaIsikukood(null);
					metaManual.setSaajaAsutuseNr(documentSharing.getUserCode());
					metaManual.setSaajaNimi(null);
					metaManual.setSaajaOsakond(null);

					metainfo.setMetaManual(metaManual);

					dvkContainer.setMetainfo(metainfo);

					FailideKonteiner failideKonteiner = new FailideKonteiner();

					Set aditFiles = document.getDocumentFiles();
					List dvkFiles = new ArrayList();

					Iterator aditFilesIterator = aditFiles.iterator();
					short count = 0;

					// TODO: convert the adit file to dvk file
					while (aditFilesIterator.hasNext()) {
						DocumentFile f = (DocumentFile) aditFilesIterator
								.next();
						Fail dvkFile = new Fail();

						LOG.debug("FileName: " + f.getFileName());

						dvkFile.setFailNimi(f.getFileName());
						dvkFile.setFailPealkiri(null);
						dvkFile.setFailSuurus(f.getFileSizeBytes());
						dvkFile.setFailTyyp(f.getContentType());
						dvkFile.setJrkNr(count++);

						// TODO: create a temporary file from the ADIT file and
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
					}

					failideKonteiner.setKokku(count);
					failideKonteiner.setFailid(dvkFiles);
					dvkContainer.setFailideKonteiner(failideKonteiner);

					// Save document to DVK Client database
					try {
						SessionFactory sessionFactory = DVKAPI
								.createSessionFactory("hibernate_ora_dvk.cfg.xml");
						Session dvkSession = sessionFactory.openSession();
						Transaction dvkTransaction = dvkSession
								.beginTransaction();

						PojoMessage dvkMessage = new PojoMessage();
						dvkMessage.setIsIncoming(false);
						dvkMessage.setTitle(document.getTitle());

						// Get sender org code
						String documentOwnerCode = document.getCreatorCode();

						AditUser documentOwner = (AditUser) session.get(
								AditUser.class, documentOwnerCode);

						dvkMessage.setSenderOrgCode(documentOwner
								.getDvkOrgCode());
						dvkMessage.setSenderPersonCode(documentOwner
								.getUserCode());
						dvkMessage.setSenderName(documentOwner.getFullName());
						dvkMessage.setDhlGuid(document.getGuid());

						// Insert data as stream
						Clob clob = Hibernate.createClob(" ", dvkSession);
						dvkMessage.setData(clob);

						Long dvkMessageID = (Long) dvkSession.save(dvkMessage);
						
						if(dvkMessageID == null || dvkMessageID.longValue() == 0) {
							LOG.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
							throw new DataRetrievalFailureException("Error while saving outgoing message to DVK database - no ID returned by save method.");
						} else {
							LOG.info("Outgoing message saved to DVK database. ID: " + dvkMessageID);
						}

						dvkTransaction.commit();
						dvkSession.close();

						LOG.debug("DVK Message saved to client database. GUID: " + dvkMessage.getDhlGuid());

						// Update CLOB
						Session dvkSession2 = sessionFactory.openSession();
						Transaction dvkTransaction2 = dvkSession2
								.beginTransaction();

						PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2
								.load(PojoMessage.class, dvkMessageID,
										LockMode.UPGRADE);

						String temporaryFile = this.getConfiguration().getTempDir() + File.separator + Util.generateRandomFileName();
						dvkContainer.save2File(temporaryFile);

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

						dvkTransaction2.commit();
						dvkSession2.close();

					} catch (Exception e) {
						throw new HibernateException(
								"Error while saving DVK Container to temporary file: ",
								e);
					}
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
}
