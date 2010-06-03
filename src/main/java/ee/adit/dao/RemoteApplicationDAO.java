package ee.adit.dao;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ee.adit.dao.pojo.RemoteApplication;

public class RemoteApplicationDAO {

	private static Logger LOG = Logger.getLogger(RemoteApplicationDAO.class);
	
	private HibernateTemplate hibernateTemplate;

	private SessionFactory sessionFactory;
	
	public RemoteApplication getByShortName(String remoteApplicationShortName) {
		
		return (RemoteApplication) this.getHibernateTemplate().get(RemoteApplication.class, remoteApplicationShortName);
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.hibernateTemplate = new HibernateTemplate(this.getSessionFactory());
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}
	
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
}
