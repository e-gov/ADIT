package ee.adit.dao.dvk;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import dvk.api.DVKAPI;
import dvk.api.ml.PojoMessage;
import dvk.api.ml.PojoMessageRecipient;
import dvk.api.ml.PojoOrganization;
import dvk.api.ml.PojoSettings;
import ee.adit.service.DocumentService;
import org.springframework.transaction.annotation.Transactional;

/**
 * DVK data access class. Provides methods for manipulating data in DVK client database.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class DvkDAO {

	private static Logger LOG = Logger.getLogger(DvkDAO.class);
	
	/**
	 * Session factory
	 */
	private SessionFactory sessionFactory;	
	
	/**
	 * Retrieves incoming documents list.
	 * 
	 * @return
	 */
	
	// TODO: open a new session, don't use getCurrentSession()
	public List<PojoMessage> getIncomingDocuments() {
		List<PojoMessage> result = new ArrayList<PojoMessage>();
		Session session = this.getSessionFactory().getCurrentSession();
		Transaction transaction = session.beginTransaction();
		PojoMessage t = null;
		try {
			final String SQL = "from PojoMessage where isIncoming = true and (recipientStatusId = null or recipientStatusId = 0 or recipientStatusId = 101 or recipientStatusId = 1) and dhlMessageId != 9999999999";
			result = session.createQuery(SQL).list();
		} catch (Exception e) {
			LOG.error("Exception while fetching DVK incoming messages: ", e);
		}
		
		return result;
	}
	
	
	/**
	 * Retrieves incoming documents list containing documents that do not have any status assigned.
	 * 
	 * @param statusID
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<PojoMessage> getIncomingDocumentsWithoutStatus(Long statusID) throws Exception {
		List<PojoMessage> result = new ArrayList<PojoMessage>();
		Session session = null;
		final String SQL = "from PojoMessage where isIncoming = true and (recipientStatusId != " + statusID + " or recipientStatusId is null) and dhlMessageId != 9999999999";
		
		try {
			session = this.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			result = session.createQuery(SQL).list();
		} catch(Exception e) {
			throw e;
		} finally {
			if(session != null)
				session.close();
		}	

		return result;
	}
	
	/**
	 * Updates document.
	 * 
	 * @param document
	 * @throws Exception
	 */
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

	/**
	 * Retrieves recipients for the specified DVK message. 
	 * 
	 * @param dvkMessageID
	 * @param incoming specifies, if the message should be incoming / outgoing
	 * @return list of message recipients
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<PojoMessageRecipient> getMessageRecipients(Long dvkMessageID, boolean incoming) throws Exception {
		List<PojoMessageRecipient> result = new ArrayList<PojoMessageRecipient>();
		
		int incomingInt = 0;
		if(incoming) {
			incomingInt = 1;
		}
		
		Session session = null;
		String SQL = "select mr from PojoMessageRecipient mr, PojoMessage m where mr.dhlMessageId = m.dhlMessageId and m.dhlMessageId = " + dvkMessageID + " and m.isIncoming = " + incomingInt + "  and m.dhlMessageId != 9999999999";
		
		try {		
			session = this.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
		    result = session.createQuery(SQL).list();
		} catch (Exception e) {
		    throw e;
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
	
	/**
	 * Retrieves DVK client settings.
	 * 
	 * @return settings
	 */
	public PojoSettings getDVKSettings() {
		Session session = null;
		String SQL = "from PojoSettings where id = (select max(id) from PojoSettings)";
		try {
			session = this.getSessionFactory().openSession();
		    return (PojoSettings) session.createQuery(SQL).uniqueResult();
		} finally {
			if(session != null)
				session.close();
		}
	}
	
	/**
	 * Get only documents that have status 'sent' for all message recipients.
	 * @return	List of documents that have status 'sent' for all message recipients
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<PojoMessage> getSentDocuments() throws Exception {
		List<PojoMessage> result = new ArrayList<PojoMessage>();
		String SQL = "from PojoMessage m where m.isIncoming = false and m.dhlId is not null and dhlMessageId != 9999999999 and (m.faultCode is null or m.faultCode != '" + DocumentService.DVKFaultCodeFor_Deleted + "') and m.dhlMessageId not in (select mr.dhlMessageId from PojoMessageRecipient mr where mr.dhlMessageId = m.dhlMessageId and (mr.sendingStatusId is null or mr.sendingStatusId = " + DocumentService.DVKStatus_Missing + " or mr.sendingStatusId = " + DocumentService.DVKStatus_Received + " or mr.sendingStatusId = " + DocumentService.DVKStatus_Sending + " or mr.sendingStatusId = " + DocumentService.DVKStatus_Waiting + " or mr.sendingStatusId = " + DocumentService.DVKStatus_Aborted + "))";
		Session session = null;
		
		try {
			session = this.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			result = session.createQuery(SQL).list();
		} catch(Exception e) {
			throw e;
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
	
	/**
	 * Retrieve all received documents.
	 * 
	 * @return list of documents.
	 */
	public List<PojoMessage> getReceivedDocuments() {
		List<PojoMessage> result = new ArrayList<PojoMessage>();
		String SQL = "from PojoMessage where isIncoming = true and dhlId is not null and dhlMessageId != 9999999999 and (faultCode is null or faultCode != '" + DocumentService.DVKFaultCodeFor_Deleted + "') and (recipientStatusId = " + DocumentService.DVKStatus_Aborted + " or recipientStatusId = " + DocumentService.DVKStatus_Received + ")";
		Session session = null;
		
		try {
			session = this.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			session.createQuery(SQL).list();
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
	
	/**
	 * Retrieves DVK users list.
	 * 
	 * @return list of users.
	 */
	public List<PojoOrganization> getUsers() {
		List<PojoOrganization> result = new ArrayList<PojoOrganization>();
		String SQL = "from PojoOrganization where dhlCapable = true";
		Session session = null;
		
		try {
			session = this.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			session.createQuery(SQL).list();
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
	
	/**
	 * Test document read.
	 */
	public PojoMessage testRead(long dhlMesageId) {
		PojoMessage result = null;
		String SQL = "from PojoMessage where dhlMesageId = " + dhlMesageId;
		Session session = null;
		
		try {
			session = this.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			Query query = session.createQuery(SQL);
			query.setMaxResults(1);
			result = (PojoMessage) query.uniqueResult();
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
	
	/**
	 * Test document read.
	 */
	public PojoMessage getMessage(long dhlMesageId) {
		PojoMessage result = null;
		String SQL = "from PojoMessage where dhlMesageId = " + dhlMesageId;
		Session session = null;
		
		try {
			session = this.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			result = (PojoMessage) session.createQuery(SQL).uniqueResult();
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
	
	/**
	 * Retrieves session factory
	 * @return
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Sets session factory
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
