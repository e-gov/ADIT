package ee.adit.dvk;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;

import dvk.api.DVKAPI;
import dvk.api.ml.PojoMessage;

public class DvkDAO {

	private SessionFactory sessionFactory;

	public DvkDAO() {
		this.setSessionFactory(DVKAPI.createSessionFactory("hibernate_ora_dvk.cfg.xml"));
	}
	
	public List<PojoMessage> getIncomingDocuments() {
		List<PojoMessage> result = new ArrayList<PojoMessage>();
		
		final String SQL = "from PojoMessage where isIncoming = true and (recipientStatusId = null or recipientStatusId = 0 or recipientStatusId = 101 or recipientStatusId = 1)";
		result = this.getSessionFactory().openSession().createQuery(SQL).list();
		
		return result;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
}
