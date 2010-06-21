package ee.adit.dao;

import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;

public class DocumentDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(DocumentDAO.class);
	
	public int getUsedSpaceForUser(String userCode) {
		
		// TODO: add conditions for files - do not count in deflated/deleted
		
		Boolean deflated = new Boolean(true);
		Boolean deleted = new Boolean(true);
		
		//List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and doc.deleted != ? and doc.deflated != ?)", new Object[] {userCode, new Boolean(true), new Boolean(true)});
		List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and (doc.deflated is null or doc.deflated != ?) and (doc.deleted is null or doc.deleted != ?))", new Object[] {userCode, deflated, deleted});
		
		int result = 0;
		for(DocumentFile docFile : userFiles) {
			BigDecimal fileSize = docFile.getFileSizeBytes();
			result += fileSize.intValue();
		}
		
		return result;
	}
	
}
