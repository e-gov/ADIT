package ee.adit.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditInternalException;
import ee.adit.service.DocumentService;

/**
 * Document sharing data access class. Provides methods for retrieving and manipulating
 * document sharing data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentSharingDAO extends HibernateDaoSupport {

    /**
     * Save document sharing.
     * 
     * @param documentSharing document sharing
     * @return ID
     */
    public Long save(DocumentSharing documentSharing) {
        return (Long) this.getHibernateTemplate().save(documentSharing);
    }

    /**
     * Update document sharing.
     * 
     * @param documentSharing document sharing
     * @throws AditInternalException if document sharing update failed
     */
    public void update(DocumentSharing documentSharing) throws AditInternalException {
        Session session = null;
        Transaction transaction = null;
        try {

            session = this.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(documentSharing);
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new AditInternalException("Error while updating DocumentSharing: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Get DVK sharings for document.
     * 
     * @param documentID document ID
     * @return list of DVK sharings
     */
    @SuppressWarnings("unchecked")
    public List<DocumentSharing> getDVKSharings(Long documentID) {
        String sql = "from DocumentSharing where documentId = " + documentID + " and documentSharingType = '"
                + DocumentService.SHARINGTYPE_SEND_DVK + "'";
        return this.getSessionFactory().openSession().createQuery(sql).list();
    }

    /**
     * Get DVK sharings by creation date.
     * 
     * @param creationDateComparison date to compare to
     * @return list of document sharings
     */
    @SuppressWarnings("unchecked")
    public List<DocumentSharing> getDVKSharings(Date creationDateComparison) {
        List<DocumentSharing> result = new ArrayList<DocumentSharing>();
        String sql = "from DocumentSharing where documentSharingType = '" + DocumentService.SHARINGTYPE_SEND_DVK
                + "' and documentDvkStatus = " + DocumentService.DVK_STATUS_MISSING
                + " and creationDate < :comparisonDate";

        Session session = null;
        try {
            session = this.getSessionFactory().openSession();
            Query query = session.createQuery(sql);
            query.setParameter("comparisonDate", creationDateComparison);
            result = query.list();

        } catch (Exception e) {
            throw new AditInternalException("Error while fetching DVK DocumentSharings: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

}
