package ee.adit.dao;

import java.util.Collection;

import ee.adit.dao.pojo.DocumentType;

public class DocumentTypeDAO extends AbstractAditDAO {
	
	public Collection<DocumentType> listDocumentTypes() {
		return this.getHibernateTemplate().find("from DocumentType documentType");
	}
	
}
