package ee.adit.dao;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentSharing;

public class DocumentSharingDAO extends HibernateDaoSupport{

	private static Logger LOG = Logger.getLogger(DocumentSharingDAO.class);
	
	public Long save(DocumentSharing documentSharing) {
		return (Long) this.getHibernateTemplate().save(documentSharing);
	}
	
}
