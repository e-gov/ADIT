package ee.adit.dao;

import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;

public class DocumentDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(DocumentDAO.class);
	
	public int getUsedSpaceForUser(String userCode) {
		
		//List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and doc.deleted != ? and doc.deflated != ?)", new Object[] {userCode, new Boolean(true), new Boolean(true)});
		List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ?)", new Object[] {userCode});
		
		LOG.debug("userFiles.size: " + userFiles.size());
		
		int result = 0;
		for(DocumentFile docFile : userFiles) {
			BigDecimal fileSize = docFile.getFileSizeBytes();
			LOG.debug("UserFile.size: " + fileSize.toString());
			result += fileSize.intValue();
		}
		
		return result;
	}
	
}
