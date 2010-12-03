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

public class DocumentSharingDAO extends HibernateDaoSupport {

    public Long save(DocumentSharing documentSharing) {
        return (Long) this.getHibernateTemplate().save(documentSharing);
    }

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

    @SuppressWarnings("unchecked")
    public List<DocumentSharing> getDVKSharings(Long documentID) {
        String sql = "from DocumentSharing where documentId = " + documentID + " and documentSharingType = '"
                + DocumentService.SHARINGTYPE_SEND_DVK + "'";
        return this.getSessionFactory().openSession().createQuery(sql).list();
    }

    @SuppressWarnings("unchecked")
    public List<DocumentSharing> getDVKSharings(Date creationDateComparison) {
        List<DocumentSharing> result = new ArrayList<DocumentSharing>();
        String sql = "from DocumentSharing where documentSharingType = '" + DocumentService.SHARINGTYPE_SEND_DVK
                + "' and documentDvkStatus = " + DocumentService.DVK_STATUS_MISSING
                + " and creationDate < :comparisonDate";

        Session session = null;
        Transaction transaction = null;
        try {
            session = this.getSessionFactory().openSession();
            transaction = session.beginTransaction();
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

    /*
     * public long getDocumentsSentToDvk(Date beginDate, Date endDate) { long
     * result = 0; String SQL =
     * "select count(*) from DocumentSharing where documentSharingType = '" +
     * DocumentService.SharingType_SendDvk +
     * "' and dvkSendDate is not null and dvkSendDate >= :beginDate and dvkSendDate <= :endDate"
     * ;
     * 
     * Session session = null; Transaction transaction = null; try {
     * 
     * session = this.getSessionFactory().openSession(); transaction =
     * session.beginTransaction(); Query query = session.createQuery(SQL);
     * query.setParameter("beginDate", beginDate); query.setParameter("endDate",
     * endDate); result = (Long) query.uniqueResult();
     * 
     * } catch (Exception e) { throw new
     * AditInternalException("Error while fetching DVK DocumentSharings: ", e);
     * } finally { if(session != null) { session.close(); } }
     * 
     * return result; }
     */

}
