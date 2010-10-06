package ee.adit.dao;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.RemoteApplication;

public class RemoteApplicationDAO extends HibernateDaoSupport {

	private static Logger LOG = Logger.getLogger(RemoteApplicationDAO.class);
	
	public RemoteApplication getByShortName(String remoteApplicationShortName) {
		return (RemoteApplication) this.getHibernateTemplate().get(RemoteApplication.class, remoteApplicationShortName);
	}
	
}
