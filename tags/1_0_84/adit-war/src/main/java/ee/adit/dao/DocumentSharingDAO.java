package ee.adit.dao;

import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ee.adit.dao.pojo.Document;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AditUser;
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
    public Long save(final DocumentSharing documentSharing) {
        return (Long) this.getHibernateTemplate().save(documentSharing);
    }

    /**
     * Update document sharing.
     *
     * @param documentSharing document sharing
     * @throws AditInternalException if document sharing update failed
     */
    public void update(final DocumentSharing documentSharing) throws AditInternalException {
    	update(documentSharing, false);
    }

    /**
     * Update document sharing.
     *
     * @param documentSharing document sharing
     * @param useExistingSession
     * 		Should existing session be used for DB interaction
     * @throws AditInternalException if document sharing update failed
     */
    public void update(final DocumentSharing documentSharing, final boolean useExistingSession) throws AditInternalException {
        if (useExistingSession) {
        	try {
        		this.getHibernateTemplate().saveOrUpdate(documentSharing);
        	} catch (Exception e) {
	            logger.error(e);
	            throw new AditInternalException("Error while updating DocumentSharing: ", e);
	        }
        } else {
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
    }

    /**
     * Get DVK sharings for document.
     *
     * @param documentID document ID
     * @return list of DVK sharings
     */
    @SuppressWarnings("unchecked")
    public List<DocumentSharing> getDVKSharings(final Long documentID) {
        String sql = "from DocumentSharing where documentId = " + documentID + " and documentSharingType = '"
                + DocumentService.SHARINGTYPE_SEND_DVK + "'";
        return this.getHibernateTemplate().find(sql);
    }

    /**
     * Get DVK sharings by creation date.
     *
     * @param creationDateComparison date to compare to
     * @return list of document sharings
     */
    @SuppressWarnings("unchecked")
    public List<DocumentSharing> getDVKSharings(final Date creationDateComparison) {
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

    /**
     * Get sharings for user.
     *
     * @param userCode user ID
     * @return list of sharings
     */
    @SuppressWarnings("unchecked")
    public List<DocumentSharing> getSharingsByUserCode(final String userCode) {
        List<DocumentSharing> result;
        DetachedCriteria dt = DetachedCriteria.forClass(DocumentSharing.class, "sharing");
        dt.add(Property.forName("sharing.userCode").eq(userCode));
        result = this.getHibernateTemplate().findByCriteria(dt);
        return result;
    }
}
