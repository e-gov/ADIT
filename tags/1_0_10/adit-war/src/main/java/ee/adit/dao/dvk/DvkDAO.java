package ee.adit.dao.dvk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import dvk.api.ml.PojoMessage;
import dvk.api.ml.PojoMessageRecipient;
import dvk.api.ml.PojoOrganization;
import dvk.api.ml.PojoSettings;
import ee.adit.service.DocumentService;

/**
 * DVK data access class. Provides methods for manipulating data in DVK client
 * database.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class DvkDAO {

    private static Logger logger = Logger.getLogger(DvkDAO.class);

    /**
     * Session factory
     */
    private SessionFactory sessionFactory;

    /**
     * Retrieves incoming documents list.
     * 
     * @return list of messages
     */
    @SuppressWarnings("unchecked")
    public List<PojoMessage> getIncomingDocuments() {
        List<PojoMessage> result = new ArrayList<PojoMessage>();
        Session session = this.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        try {
            final String sql = "from PojoMessage where isIncoming = true and (recipientStatusId = null or recipientStatusId = 0 or recipientStatusId = 101 or recipientStatusId = 1) and dhlMessageId != 9999999999";
            result = session.createQuery(sql).list();
        } catch (Exception e) {
            logger.error("Exception while fetching DVK incoming messages: ", e);
        }

        return result;
    }

    /**
     * Retrieves incoming documents list containing documents that do not have
     * any status assigned.
     * 
     * @param statusID
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<PojoMessage> getIncomingDocumentsWithoutStatus(Long statusID) throws Exception {
        List<PojoMessage> result = new ArrayList<PojoMessage>();
        Session session = null;
        final String sql = "from PojoMessage where isIncoming = true and (recipientStatusId != " + statusID
                + " or recipientStatusId is null) and dhlMessageId != 9999999999";

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            result = session.createQuery(sql).list();
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
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
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }

    }

    /**
     * Retrieves recipients for the specified DVK message.
     * 
     * @param dvkMessageID
     * @param incoming
     *            specifies, if the message should be incoming / outgoing
     * @return list of message recipients
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<PojoMessageRecipient> getMessageRecipients(Long dvkMessageID, boolean incoming) throws Exception {
        List<PojoMessageRecipient> result = new ArrayList<PojoMessageRecipient>();

        int incomingInt = 0;
        if (incoming) {
            incomingInt = 1;
        }

        Session session = null;
        String sql = "select mr from PojoMessageRecipient mr, PojoMessage m where mr.dhlMessageId = m.dhlMessageId and m.dhlMessageId = "
                + dvkMessageID + " and m.isIncoming = " + incomingInt + "  and m.dhlMessageId != 9999999999";

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            result = session.createQuery(sql).list();
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
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
        String sql = "from PojoSettings where id = (select max(id) from PojoSettings)";
        try {
            session = this.getSessionFactory().openSession();
            return (PojoSettings) session.createQuery(sql).uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Get only documents that have status 'sent' for all message recipients.
     * 
     * @return List of documents that have status 'sent' for all message
     *         recipients
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<PojoMessage> getSentDocuments() throws Exception {
        List<PojoMessage> result = new ArrayList<PojoMessage>();
        String sql = "from PojoMessage m where m.isIncoming = false and m.dhlId is not null and dhlMessageId != 9999999999 and (m.faultCode is null or m.faultCode != '"
                + DocumentService.DVK_FAULT_CODE_FOR_DELETED
                + "') and m.dhlMessageId not in (select mr.dhlMessageId from PojoMessageRecipient mr where mr.dhlMessageId = m.dhlMessageId and (mr.sendingStatusId is null or mr.sendingStatusId = "
                + DocumentService.DVK_STATUS_MISSING
                + " or mr.sendingStatusId = "
                + DocumentService.DVK_STATUS_RECEIVED
                + " or mr.sendingStatusId = "
                + DocumentService.DVK_STATUS_SENDING
                + " or mr.sendingStatusId = "
                + DocumentService.DVK_STATUS_WAITING
                + " or mr.sendingStatusId = "
                + DocumentService.DVK_STATUS_ABORTED
                + "))";
        Session session = null;

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            result = session.createQuery(sql).list();
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
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
        String sql = "from PojoMessage where isIncoming = true and dhlId is not null and dhlMessageId != 9999999999 and (faultCode is null or faultCode != '"
                + DocumentService.DVK_FAULT_CODE_FOR_DELETED
                + "') and (recipientStatusId = "
                + DocumentService.DVK_STATUS_ABORTED
                + " or recipientStatusId = "
                + DocumentService.DVK_STATUS_RECEIVED
                + ")";
        Session session = null;

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            session.createQuery(sql).list();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Retrieves DVK users list.
     * 
     * @return list of users.
     */
    @SuppressWarnings("unchecked")
    public List<PojoOrganization> getUsers() {
        List<PojoOrganization> result = new ArrayList<PojoOrganization>();
        String sql = "from PojoOrganization where dhlCapable = true";
        Session session = null;

        logger.debug("Fetching organizations...");

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            result = session.createQuery(sql).list();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Test document read.
     */
    public PojoMessage testRead(long dhlMessageId) {
        PojoMessage result = null;
        String sql = "from PojoMessage where dhlMessageId = " + dhlMessageId;
        Session session = null;

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            Query query = session.createQuery(sql);
            query.setMaxResults(1);
            result = (PojoMessage) query.uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Test document read.
     */
    public PojoMessage getMessage(long dhlMessageId) {
        PojoMessage result = null;
        String sql = "from PojoMessage where dhlMessageId = " + dhlMessageId;
        Session session = null;

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            result = (PojoMessage) session.createQuery(sql).uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Retrieves session factory
     * 
     * @return
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Sets session factory
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Get only documents that have status 'sent' for all message recipients.
     * 
     * @param beginDate time period start date
     * @param endDate time period end date
     * @return List of documents that have status 'sent' for all message
     *         recipients
     * @throws Exception
     */
    public long getSentDocuments(Date beginDate, Date endDate) throws Exception {
        long result = 0;

        String sql = "select count(*) from PojoMessage where isIncoming = false and sendingDate => :beginDate and sendingDate <= :endDate)";
        Session session = null;

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            Query query = session.createQuery(sql);
            query.setParameter("beginDate", beginDate);
            query.setParameter("endDate", endDate);
            result = (Long) query.uniqueResult();
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Fetch received documents from DVK client database.
     * 
     * @param comparisonDate comparison date
     * @return list of messages
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<PojoMessage> getReceivedDocuments(Date comparisonDate) throws Exception {
        List<PojoMessage> result = new ArrayList<PojoMessage>();

        String sql = "from PojoMessage where isIncoming = true and recipientStatusId = "
                + DocumentService.DVK_STATUS_RECEIVED + " and receivedDate <= :comparisonDate";
        Session session = null;

        try {
            session = this.getSessionFactory().openSession();
            session.beginTransaction();
            Query query = session.createQuery(sql);
            query.setParameter("comparisonDate", comparisonDate);
            result = query.list();
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

}
