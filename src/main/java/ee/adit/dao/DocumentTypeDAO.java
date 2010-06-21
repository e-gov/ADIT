package ee.adit.dao;

import java.util.Collection;
import java.util.List;

import ee.adit.dao.pojo.DocumentType;

public class DocumentTypeDAO extends AbstractAditDAO {
	
	public List<DocumentType> listDocumentTypes() {
		return this.getHibernateTemplate().find("from DocumentType documentType");
	}

	public DocumentType getDocumentType(String documentTypeShortName) {
		return (DocumentType) this.getHibernateTemplate().get(DocumentType.class, documentTypeShortName);
	}
	
}
