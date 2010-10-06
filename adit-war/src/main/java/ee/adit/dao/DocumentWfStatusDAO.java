package ee.adit.dao;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import ee.adit.dao.pojo.DocumentWfStatus;

public class DocumentWfStatusDAO extends HibernateDaoSupport {
	private static Logger LOG = Logger.getLogger(DocumentWfStatusDAO.class);
	
	public DocumentWfStatus getDocumentWfStatus(Long statusId) {
		LOG.debug("Fetching document workflow status by ID: " + statusId);		
		return (DocumentWfStatus) this.getHibernateTemplate().get(DocumentWfStatus.class, statusId);
	}
}
