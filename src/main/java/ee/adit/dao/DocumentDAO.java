package ee.adit.dao;

import java.util.List;

import ee.adit.dao.pojo.Document;

public class DocumentDAO extends AbstractAditDAO {

	public int getUsedSpaceForUser(String userCode) {
		int result = -1;
		
		List<Document> userActiveDocuments = this.getHibernateTemplate().find("from DocumentFile as docFile where docFile.document in (select doc.id from Document doc where doc.userCode = ? and doc.deleted != ? and doc.deflated != ?)", new Object[] {userCode, new Boolean(true), new Boolean(true)});
		
		
		
		return result;
	}
	
}
