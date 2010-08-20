package ee.adit.dvk;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import dvk.api.DVKAPI;
import dvk.api.ml.PojoMessage;
import dvk.api.ml.PojoMessageRecipient;
import dvk.api.ml.PojoOrganization;
import dvk.api.ml.PojoSettings;
import ee.adit.service.DocumentService;

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

	public List<PojoMessage> getIncomingDocumentsWithoutStatus(Long statusID) {
		List<PojoMessage> result = new ArrayList<PojoMessage>();

		final String SQL = "from PojoMessage where isIncoming = true and (recipientStatusId != " + statusID + " or recipientStatusId is null)";
		result = this.getSessionFactory().openSession().createQuery(SQL).list();

		return result;
	}
	
	public void updateDocument(PojoMessage document) throws Exception {
		
		Session session = this.getSessionFactory().openSession();
		Transaction transaction = null;
		
		try {
			transaction = session.beginTransaction();
		    session.saveOrUpdate(document);
		    transaction.commit();
		}
		catch (Exception e) {
		    if (transaction != null) transaction.rollback();
		    throw e;
		}
		finally {
			session.close();
		}
		
	}

	public List<PojoMessageRecipient> getMessageRecipients(Long dvkMessageID, boolean incoming) {
		// TODO: pojomesage.ID - invalid identifier
		int incomingInt = 0;
		if(incoming) {
			incomingInt = 1;
		}
		
		String SQL = "select mr from PojoMessageRecipient mr, PojoMessage m where mr.dhlMessageId = m.dhlMessageId and m.dhlMessageId = " + dvkMessageID + " and m.isIncoming = " + incomingInt;
		return this.getSessionFactory().openSession().createQuery(SQL).list();
	}
	
	public PojoSettings getDVKSettings() {
		Session session = this.getSessionFactory().openSession();
		String SQL = "from PojoSettings where id = (select max(id) from PojoSettings)";
		try {
		    return (PojoSettings) session.createQuery(SQL).uniqueResult();
		} finally {
			session.close();
		}
	}
	
	/**
	 * Get only documents that have status 'sent' for all message recipients.
	 * @return
	 */
	public List<PojoMessage> getSentDocuments() {
		List<PojoMessage> result = new ArrayList<PojoMessage>();
		String SQL = "from PojoMessage m where m.isIncoming = false and m.dhlId is not null and (m.faultCode is null or m.faultCode != '" + DocumentService.DVKFaultCodeFor_Deleted + "') and m.dhlMessageId not in (select mr.dhlMessageId from PojoMessageRecipient mr where mr.dhlMessageId = m.dhlMessageId and (mr.sendingStatusId is null or mr.sendingStatusId = " + DocumentService.DVKStatus_Missing + " or mr.sendingStatusId = " + DocumentService.DVKStatus_Received + " or mr.sendingStatusId = " + DocumentService.DVKStatus_Sending + " or mr.sendingStatusId = " + DocumentService.DVKStatus_Waiting + " or mr.sendingStatusId = " + DocumentService.DVKStatus_Aborted + "))";
		result = this.getSessionFactory().openSession().createQuery(SQL).list();
		return result;
	}
	
	public List<PojoMessage> getReceivedDocuments() {
		List<PojoMessage> result = new ArrayList<PojoMessage>();
		String SQL = "from PojoMessage where isIncoming = true and dhlId is not null and (faultCode is null or faultCode != '" + DocumentService.DVKFaultCodeFor_Deleted + "') and (recipientStatusId = " + DocumentService.DVKStatus_Aborted + " or recipientStatusId = " + DocumentService.DVKStatus_Received + ")";
		result = this.getSessionFactory().openSession().createQuery(SQL).list();
		return result;
	}
	
	public List<PojoOrganization> getUsers() {
		List<PojoOrganization> result = new ArrayList<PojoOrganization>();
		String SQL = "from PojoOrganization where dhlCapable = true";
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
