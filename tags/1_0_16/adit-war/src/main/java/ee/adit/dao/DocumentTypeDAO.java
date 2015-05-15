package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentType;

/**
 * Document type data access class. Provides methods for retrieving and manipulating
 * document type data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentTypeDAO extends HibernateDaoSupport {

    private static Logger logger = Logger.getLogger(DocumentTypeDAO.class);

    /**
     * Fetch document type list.
     * 
     * @return document type list
     */
    @SuppressWarnings("unchecked")
    public List<DocumentType> listDocumentTypes() {
        return this.getHibernateTemplate().find("from DocumentType documentType");
    }

    /**
     * Fetch document type by short name.
     * 
     * @param documentTypeShortName document type short name
     * @return document type
     */
    public DocumentType getDocumentType(String documentTypeShortName) {
        logger.debug("Fetching document type by short name: " + documentTypeShortName);
        return (DocumentType) this.getHibernateTemplate().get(DocumentType.class, documentTypeShortName);
    }

}
