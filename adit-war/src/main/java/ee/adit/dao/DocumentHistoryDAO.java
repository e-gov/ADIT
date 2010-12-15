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
     * @return
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

}
