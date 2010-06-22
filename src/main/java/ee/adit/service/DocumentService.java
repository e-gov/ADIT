package ee.adit.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ee.adit.dao.DocumentDAO;
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

	private static Logger LOG = Logger.getLogger(UserService.class);
	
	private MessageSource messageSource;
	
	private DocumentTypeDAO documentTypeDAO;
	
	private DocumentDAO documentDAO;
	
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
	
}
