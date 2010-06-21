package ee.adit.service;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentType;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveDocumentRequestAttachmentFile;
import ee.adit.util.Util;

public class DocumentService {

	private static Logger LOG = Logger.getLogger(UserService.class);
	
	private MessageSource messageSource;
	
	private DocumentTypeDAO documentTypeDAO;
	
	private DocumentDAO documentDAO;
	
	public void checkAttachedDocumentMetadataForNewDocument(SaveDocumentRequestAttachment document, long remainingDiskQuota, String xmlFile) throws AditException {
		
		if(document != null) {
			
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
			
			// Check title
			if(document.getTitle() == null || "".equalsIgnoreCase(document.getTitle())) {
				String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.title.undefined", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check document_type
			if(document.getDocument_type() == null || "".equalsIgnoreCase(document.getDocument_type())) {
				
				// Is the document type valid?
				DocumentType documentType = this.getDocumentTypeDAO().getDocumentType(document.getDocument_type());
				
				if(documentType == null) {
					String validDocumentTypes = getValidDocumentTypes();
					String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.type.nonExistent", new Object[] { validDocumentTypes }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
				
			} else {
				String validDocumentTypes = getValidDocumentTypes();
				String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.type.undefined", new Object[] { validDocumentTypes }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check previous_document_id
			if(document.getPrevious_document_it() != null) {
				// Check if the document exists
				
				Document previousDocument = this.getDocumentDAO().getDocument(document.getPrevious_document_it());
				
				if(previousDocument == null) {
					String errorMessage = this.getMessageSource().getMessage("request.saveDocument.document.previousDocument.nonExistent", new Object[] { document.getPrevious_document_it() }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
			}
			
			// TODO: Check files - at least one file must be defined. 
			// The <data> tags of the <file> elements did not get unmarshalled (to save memory).
			// That is why we need to check those files on the disk. We need the sizes of the <data> elements.
			// 1. Get the XML file
			// 2. find the <data> elements
			// 3. For each <data> element, create a temporary file and add a reference to the document object
			
			try {
				FileInputStream fileInputStream = new FileInputStream(xmlFile);
				StringWriter sw = new StringWriter();
				
				int len;
				byte[] b = new byte[1024];
				boolean dataTagOpen = false;
				
				while((len = fileInputStream.read(b)) > 0) {
					String tmpString = new String(b, 0, len);
					
				}
				
			} catch (Exception e) {
				// TODO: throw exception
			}
			
			
		} else {
			throw new AditInternalException("Document not initialized.");
		}		
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
