package ee.adit.dao;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentHistory;

public class DocumentHistoryDAO extends HibernateDaoSupport {

	private static Logger LOG = Logger.getLogger(DocumentHistoryDAO.class);
	
	public Long save(DocumentHistory documentHistory) {
		return (Long) this.getHibernateTemplate().save(documentHistory);
	}
	
}
