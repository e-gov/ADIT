package ee.adit.dao;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentWfStatus;

/**
 * Document workflow status data access class. Provides methods for retrieving and manipulating
 * document workflow status data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentWfStatusDAO extends HibernateDaoSupport {
    
    private static Logger logger = LogManager.getLogger(DocumentWfStatusDAO.class);

    /**
     * Fetch document workflow status by ID.
     * 
     * @param statusId workflow status ID
     * @return workflow status
     */
    public DocumentWfStatus getDocumentWfStatus(Long statusId) {
        logger.debug("Fetching document workflow status by ID: " + statusId);
        return (DocumentWfStatus) this.getHibernateTemplate().get(DocumentWfStatus.class, statusId);
    }
}
