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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.exception.AditException;
import ee.adit.pojo.GetDocumentFileResponseAttachmentFile;
import ee.adit.pojo.SaveDocumentRequestAttachmentFile;
import ee.adit.util.Util;

public class DocumentDAO extends HibernateDaoSupport {

	private static Logger LOG = Logger.getLogger(DocumentDAO.class);
	
	public long getUsedSpaceForUser(String userCode) {
		
		// TODO: add conditions for files - do not count in deflated/deleted
		
		Boolean deflated = new Boolean(true);
		Boolean deleted = new Boolean(true);
		
		//List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and doc.deleted != ? and doc.deflated != ?)", new Object[] {userCode, new Boolean(true), new Boolean(true)});
		List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and (doc.deflated is null or doc.deflated != ?) and (doc.deleted is null or doc.deleted != ?))", new Object[] {userCode, deflated, deleted});
		
		long result = 0;
		for(DocumentFile docFile : userFiles) {
			long fileSize = docFile.getFileSizeBytes();
			result += fileSize;
		}
		
		return result;
	}
	
	public Document getDocument(long id) {
		LOG.debug("Attempting to load document from database. Document id: " + String.valueOf(id));
		return (Document) this.getHibernateTemplate().get(Document.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<GetDocumentFileResponseAttachmentFile> getDocumentFiles(final long documentId, final List<Long> fileIdList, final String temporaryFilesDir, final String filesNotFoundMessageBase) throws Exception {
		if (documentId <= 0) {
			throw new IllegalArgumentException("Document ID must be a positive integer. Currently supplied ID was " + documentId + ".");
		}
		
		List<GetDocumentFileResponseAttachmentFile> result = null;
		
		try {
			LOG.debug("Attempting to load document files for document " + documentId);
			result = (ArrayList<GetDocumentFileResponseAttachmentFile>) getHibernateTemplate().execute(new HibernateCallback() {
				
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException
	            {
	            	List<GetDocumentFileResponseAttachmentFile> innerResult = new ArrayList<GetDocumentFileResponseAttachmentFile>();
	            	Document doc = (Document)session.get(Document.class, documentId);
	            	List<DocumentFile> filesList = new ArrayList<DocumentFile>(doc.getDocumentFiles());            	
	            	
	            	// Check if all requested files exist
	            	if ((fileIdList != null) && !fileIdList.isEmpty()) {
	            		List<Long> internalIdList = new ArrayList<Long>();
	            		internalIdList.addAll(fileIdList);
	            		
	            		for (DocumentFile docFile : filesList) {
	        				if (!docFile.getDeleted() && internalIdList.contains((Long)docFile.getId())) {
	        					internalIdList.remove((Long)docFile.getId());
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
	            			throw new SQLException(new AditException(filesNotFoundMessageBase + " " + idListString));
	            		}
	            	}
	            	
	            	int itemIndex = 0;
	            	for (DocumentFile docFile : filesList) {
	            		if ((fileIdList == null) || fileIdList.isEmpty() || fileIdList.contains(docFile.getId())) {
	            			GetDocumentFileResponseAttachmentFile f = new GetDocumentFileResponseAttachmentFile();
	            			f.setContentType(docFile.getContentType());
	            			f.setDescription(docFile.getDescription());
	            			f.setId(docFile.getId());
	            			f.setName(docFile.getFileName());
	            			f.setSizeBytes(docFile.getFileSizeBytes());
	            			
	            			// Read file data from BLOB and write it to temporary file.
	            			// This is necessary to avoid storing potentially large
	            			// amounts of binary data in server memory.
	            			itemIndex++;
	        				String outputFileName = Util.generateRandomFileNameWithoutExtension();
	        				outputFileName = temporaryFilesDir + File.separator + outputFileName + "_" + itemIndex + "_GDFv1.adit";
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
	        					f.setTmpFileName(outputFileName);
	        				} catch (IOException ex) {
	        					throw new HibernateException(ex);
	        				} finally {
	        					try {
	        						if (blobDataStream != null) {
	        							blobDataStream.close();
	        						}
	        						blobDataStream = null;
	        					} catch (Exception ex) {}
	        					
	        					try {
	        						if (fileOutputStream != null) {
	        							fileOutputStream.close();
	        						}
	        						fileOutputStream = null;
	        					} catch (Exception ex) {}
	        				}
	        				
	        				innerResult.add(f);
	            		}
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
	 * FIXME - ei tööta praegu salvestamine
	 * @param document
	 * @param files
	 * @return
	 */
	public Long save(final Document document, final List<SaveDocumentRequestAttachmentFile> files) {
		Long result = null;
		Set<DocumentFile> documentFiles = new HashSet<DocumentFile>();
				
		result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
			
			@Override
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				
				Set<DocumentFile> documentFiles = new HashSet<DocumentFile>();
				
				if (files != null) {
					for(int i = 0; i < files.size(); i++) {
						DocumentFile documentFile = new DocumentFile();
						SaveDocumentRequestAttachmentFile attachmentFile = files.get(i);
						String fileName = attachmentFile.getTmpFileName();
						
						FileInputStream fileInputStream = null;
						try {
							fileInputStream = new FileInputStream(fileName);
						} catch (FileNotFoundException e) {
							LOG.error("Error saving document file: ", e);
						}
						
						long length = (new File(fileName)).length();
						
						Blob fileData = Hibernate.createBlob(fileInputStream, length);
						documentFile.setFileData(fileData);
						documentFile.setContentType(attachmentFile.getContentType());
						documentFile.setDescription(attachmentFile.getDescription());
						documentFile.setFileName(attachmentFile.getName());
						documentFile.setFileSizeBytes(length);
						documentFile.setDocument(document);
						documentFiles.add(documentFile);
					}
				}
				
				document.setDocumentFiles(documentFiles);
				session.saveOrUpdate(document);
				LOG.debug("Saved document ID: " + document.getId());
				return document.getId();
			}
		});
		
		return result;
	}
	
}
