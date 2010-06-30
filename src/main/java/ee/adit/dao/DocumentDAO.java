package ee.adit.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	
	/**
	 * Asendab etteantud ID-le vastava faili sisu esialgse sisu MD5 räsikoodiga
	 * 
	 * @param fileId	Faili ID
	 * @throws NoSuchAlgorithmException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void deflateFile(int fileId) throws NoSuchAlgorithmException, SQLException, IOException {
		List<DocumentFile> files = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document=?", new Object[] {fileId});
		
		for(DocumentFile docFile : files) {
			MessageDigest md = MessageDigest.getInstance("MD5");
			InputStream dataStream = docFile.getFileData().getBinaryStream();
			
			// Loeme andmebaasist faili sisu ja arvutame selle MD5 räsi
			byte[] buf = new byte[10240];
			int len = 0;
			try {
				while ((len = dataStream.read(buf, 0, buf.length)) > 0) {
	                md.update(buf, 0, len);
	            }
			} finally {
				try {
					dataStream.close();
					dataStream = null;
				} catch (Exception e) {}
			}
			
			// Salvestame MD5 räsi faili sisuks
			byte[] digest = md.digest();
			OutputStream outStream = docFile.getFileData().setBinaryStream(0);
			try {
				outStream.write(digest, 0, digest.length);
			} finally {
				try {
					dataStream.close();
					dataStream = null;
				} catch (Exception e) {}
			}
		}
	}
	
	public Document getDocument(long id) {
		LOG.debug("Attempting to load document from database. Document id: " + String.valueOf(id));
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
						documentFile.setFileName(fileName);
						documentFile.setFileSizeBytes(new BigDecimal(length));
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
