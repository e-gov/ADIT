package ee.adit.dao;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ee.adit.dao.pojo.Usertype;

public class UsertypeDAO {

	private static Logger LOG = Logger.getLogger(RemoteApplicationDAO.class);
	
	private HibernateTemplate hibernateTemplate;

	private SessionFactory sessionFactory;
	
	public Usertype getByShortName(String userTypeShortName) {
		return (Usertype) this.getHibernateTemplate().get(Usertype.class, userTypeShortName);
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
