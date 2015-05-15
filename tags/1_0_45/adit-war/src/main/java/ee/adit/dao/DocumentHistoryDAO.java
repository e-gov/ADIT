package ee.adit.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.exception.AditInternalException;

/**
 * Document history data access class. Provides methods for retrieving and manipulating
 * document history data.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentHistoryDAO extends HibernateDaoSupport {

    /**
     * Save document history.
     *
     * @param documentHistory document histroy record
     * @return document history ID
     */
    public Long save(DocumentHistory documentHistory) {
        Long result = null;
        Session session = null;
        Transaction transaction = null;
        try {

            session = this.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            result = (Long) session.save(documentHistory);
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new AditInternalException("Error while saving DocumentHistory: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
        // return (Long) this.getHibernateTemplate().save(documentHistory);
    }

    /**
     * Fetch document history list ordered by event date.
     *
     * @param documentID document ID
     * @return history list
     */
    @SuppressWarnings("unchecked")
    public List<DocumentHistory> getSortedList(Long documentID) {
        String sql = "from DocumentHistory where documentId = " + documentID + " order by eventDate";

        List<DocumentHistory> result = null;
        Session session = null;
        Transaction transaction = null;
        try {

            session = this.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            result = session.createQuery(sql).list();
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new AditInternalException("Error while fetching DocumentHistory list: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Checks if a history event with given type exists for a given document.
     *
     * @param historyTypeCode
     * 		Type code of history event
     * @param documentId
     * 		Related document ID
     * @param userCode
     * 		Code of user whose events will be checked
     * @return
     * 		{@code true} if a history event with given parameters exists.
     * 		{@code false} otherwise.
     */
    public boolean checkIfHistoryEventExists(final String historyTypeCode,
    	final long documentId, final String userCode) {

    	boolean result = true;

        String sql = "from DocumentHistory where documentId=" + documentId
        	+ " and userCode='" + userCode + "' and documentHistoryType='"
        	+ historyTypeCode + "'";

        List<DocumentHistory> existingHistoryEvents =
        	this.getHibernateTemplate().find(sql);

        if (existingHistoryEvents == null || existingHistoryEvents.size() < 1) {
            result = false;
        }

        return result;
    }
}
