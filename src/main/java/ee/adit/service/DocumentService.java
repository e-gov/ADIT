package ee.adit.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.context.MessageSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentFileDAO;
import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentType;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveDocumentRequestAttachmentFile;
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
	
	private static Logger LOG = Logger.getLogger(UserService.class);
	private MessageSource messageSource;
	private DocumentTypeDAO documentTypeDAO;
	private DocumentDAO documentDAO;
	private DocumentFileDAO documentFileDAO;
	
	public List<String> checkAttachedDocumentMetadataForNewDocument(SaveDocumentRequestAttachment document, long remainingDiskQuota, String xmlFile, String tempDir) throws AditException {
		List<String> result = null;
		LOG.debug("Checking attached document metadata for new document...");
		if(document != null) {
			
			LOG.debug("Checking GUID: " + document.getGuid());
			// Check GUID
			if(document.getGuid() != null) {
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
			if(document.getTitle() == null || "".equalsIgnoreCase(document.getTitle())) {
				String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.title.undefined", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			LOG.debug("Checking document type: " + document.getDocumentType());
			// Check document_type
			
			if(document.getDocumentType() != null && !"".equalsIgnoreCase(document.getDocumentType().trim())) {
				
				// Is the document type valid?
				LOG.debug("Document type is defined. Checking if it is valid.");
				DocumentType documentType = this.getDocumentTypeDAO().getDocumentType(document.getDocumentType());
				
				if(documentType == null) {
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
			if(document.getPreviousDocumentID() != null && document.getPreviousDocumentID() != 0) {
				// Check if the document exists
				
				Document previousDocument = this.getDocumentDAO().getDocument(document.getPreviousDocumentID());
				
				if(previousDocument == null) {
					String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.previousDocument.nonExistent", new Object[] { document.getPreviousDocumentID() }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
			}
			
			// TODO: Check files - at least one file must be defined. 
			// The <data> tags of the <file> elements did not get unmarshalled (to save memory).
			// That is why we need to check those files on the disk. We need the sizes of the <data> elements.
			// 1. Get the XML file
			// 2. find the <data> elements
			// 3. For each <data> element, create a temporary file and add a reference to the document object
			long totalSize = 0;
			
			LOG.debug("Checking files");
			try {
				FileInputStream fileInputStream = new FileInputStream(xmlFile);
				
				SaveDocumentAttachmentHandler handler = new SaveDocumentAttachmentHandler(tempDir);
				
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				xmlReader.setContentHandler(handler);
				
				InputSource inputSource = new InputSource(fileInputStream);
				xmlReader.parse(inputSource);
				
				result = handler.getFiles();
				
				// Add references to file objects
				for(int i = 0; i < result.size(); i++) {
					String fileName = result.get(i);
					SaveDocumentRequestAttachmentFile file = document.getFiles().get(i);
					LOG.debug("Adding reference to file object. File ID: " + file.getId() + " (" + file.getName() + "). Temporary file: " + fileName);
					file.setTmpFileName(fileName);
					
					totalSize += (new File(fileName)).length();
					
				}				
				
				LOG.debug("Total size of document files: " + totalSize);
				
				if(remainingDiskQuota < totalSize) {
					String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.files.quotaExceeded", new Object[] { remainingDiskQuota, totalSize }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
				
			} catch (Exception e) {
				throw new AditInternalException("Error parsing attachment: ", e);
			}
			
			
		} else {
			throw new AditInternalException("Document not initialized.");
		}
		
		return result;
	}
	
	public String getValidDocumentTypes() {
		StringBuffer result = new StringBuffer();
		List<DocumentType> documentTypes = this.getDocumentTypeDAO().listDocumentTypes();
		
		for(int i = 0; i < documentTypes.size(); i++) {
			DocumentType documentType = documentTypes.get(i);
			
			if(i > 0) {
				result.append(", ");
			}
			result.append(documentType.getShortName());
			
		}
		
		return result.toString();
	}
	
	public String deflateDocumentFile(long documentId, long fileId, boolean markDeleted) {
		return this.getDocumentFileDAO().deflateDocumentFile(documentId, fileId, markDeleted);
	}
	
	public Long save(SaveDocumentRequestAttachment attachmentDocument, List<String> fileNames, String creatorCode, String remoteApplication) throws FileNotFoundException {
		
		// TODO
		// 1. Construct file objects
		
		Date creationDate = new Date();
		
		Document document = new Document();
		document.setCreationDate(creationDate);
		document.setCreatorCode(creatorCode);
		
		// Get document type
		DocumentType documentType = this.getDocumentTypeDAO().getDocumentType(attachmentDocument.getDocumentType());
		
		document.setDocumentType(documentType);
		
		if(attachmentDocument.getGuid() != null && !"".equalsIgnoreCase(attachmentDocument.getGuid().trim())) {
			document.setGuid(attachmentDocument.getGuid());
		} else {
			// Generate new GUID
			document.setGuid(Util.generateGUID());
		}
		
		
		document.setLastModifiedDate(creationDate);
		document.setRemoteApplication(remoteApplication);
		document.setSignable(true);
		document.setTitle(attachmentDocument.getTitle());
		
		
		
		
		
		return this.getDocumentDAO().save(document, attachmentDocument.getFiles());
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
	
}
