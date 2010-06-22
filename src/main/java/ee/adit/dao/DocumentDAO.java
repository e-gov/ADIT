package ee.adit.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.pojo.SaveDocumentRequestAttachmentFile;

public class DocumentDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(DocumentDAO.class);
	
	public long getUsedSpaceForUser(String userCode) {
		
		// TODO: add conditions for files - do not count in deflated/deleted
		
		Boolean deflated = new Boolean(true);
		Boolean deleted = new Boolean(true);
		
		//List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and doc.deleted != ? and doc.deflated != ?)", new Object[] {userCode, new Boolean(true), new Boolean(true)});
		List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and (doc.deflated is null or doc.deflated != ?) and (doc.deleted is null or doc.deleted != ?))", new Object[] {userCode, deflated, deleted});
		
		long result = 0;
		for(DocumentFile docFile : userFiles) {
			BigDecimal fileSize = docFile.getFileSizeBytes();
			result += fileSize.longValue();
		}
		
		return result;
	}
	
	public Document getDocument(Integer id) {
		return (Document) this.getHibernateTemplate().get(Document.class, id);
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
					documentFile.setFileName(fileName);
					documentFile.setFileSizeBytes(new BigDecimal(length));
					documentFile.setDocument(document);
					documentFiles.add(documentFile);
				}
				
				document.setDocumentFiles(documentFiles);
				Long tmp = (Long) session.save(document);
				LOG.debug("ID TMP serializable: " + tmp);
				LOG.debug("ID TMP: " + document.getId());
				return tmp;
				
			}
		});
		
		return result;
	}
	
}
