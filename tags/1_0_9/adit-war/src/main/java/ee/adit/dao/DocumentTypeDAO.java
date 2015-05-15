package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentType;

public class DocumentTypeDAO extends HibernateDaoSupport {

    private static Logger logger = Logger.getLogger(DocumentTypeDAO.class);

    @SuppressWarnings("unchecked")
    public List<DocumentType> listDocumentTypes() {
        return this.getHibernateTemplate().find("from DocumentType documentType");
    }

    public DocumentType getDocumentType(String documentTypeShortName) {
        logger.debug("Fetching document type by short name: " + documentTypeShortName);
        return (DocumentType) this.getHibernateTemplate().get(DocumentType.class, documentTypeShortName);
    }

}
