package ee.adit.dao;

import java.util.List;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.UserContact;
import ee.adit.exception.AditInternalException;

/**
 * User contact data access class. Provides methods for retrieving and manipulating
 * User contact data.
 * 
 * @author Dmitri Timofejev, Finestmedia, dmitri.timofejev@finestmedia.ee
 */
public class UserContactDAO extends HibernateDaoSupport {

    private static Logger logger = LogManager.getLogger(UserContactDAO.class);

    /**
     * Save user contact.
     *
     * @param userContact user contact
     * @return ID
     */
    public Long save(UserContact userContact) {
        return (Long) this.getHibernateTemplate().save(userContact);
    }
    
    /**
     * Update user contact.
     *
     * @param userContact user contact
     * @throws AditInternalException if user contact update failed
     */
    public void saveOrUpdate(UserContact userContact) throws AditInternalException {
    	saveOrUpdate(userContact, false);
    }
    
    /**
     * Update user contact.
     *
     * @param userContact user contact
     * @param useExistingSession
     * 		Should existing session be used for DB interaction
     * @throws AditInternalException if user contact update failed
     */
    public void saveOrUpdate(UserContact userContact, boolean useExistingSession) throws AditInternalException {
        if (useExistingSession) {
        	try {
        		this.getHibernateTemplate().saveOrUpdate(userContact);
        	} catch (Exception e) {
	            logger.error(e);
	            throw new AditInternalException("Error while updating UserContact: ", e);
	        }
        } else {
	    	Session session = null;
	        Transaction transaction = null;
	        try {

	            session = this.getSessionFactory().openSession();
	            transaction = session.beginTransaction();
	            session.saveOrUpdate(userContact);
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
     * Fetch user contact by user and contact.
     * 
     * @param user 
     * @param contact user
     * @return document type
     */
    public UserContact getUserContact(AditUser user, AditUser contact) {
        logger.debug("Fetching user contact by user : " + user.getFullName() + " and contact : " + contact.getFullName());
        
        UserContact result;
        String sql = "select uc from UserContact uc where uc.user = :user and uc.contact = :contact";

        Session session = null;
        try {
            session = this.getSessionFactory().openSession();
            Query query = session.createQuery(sql);
            query.setParameter("user", user);
            query.setParameter("contact", contact);
            result = (UserContact) query.uniqueResult();
        } catch (Exception e) {
            throw new AditInternalException("Error while fetching user contact: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        return (UserContact) result;
    }
    
    /**
     * Get contacts of a user.
     *
     * @param userCode user code
     * @return list of user contacts
     */
    @SuppressWarnings("unchecked")
    public List<UserContact> getUserContacts(final AditUser aditUser) {
        List<UserContact> result = null;
        Session session = null;
        try {
            logger.debug("Finding contacts for user: " + aditUser.getUserCode() + ", "
                            + aditUser.getFullName());
            String sql = "select userContact from UserContact userContact join userContact.contact contact where userContact.user = :user and contact.active = :active order by userContact.lastUsedDate desc";
            session = this.getSessionFactory().openSession();
            Query query = session.createQuery(sql);
            query.setParameter("user", aditUser);
            query.setParameter("active", true);
            result =  query.list();
//            result = this.getHibernateTemplate().find(
//                    "select userContact from UserContact userContact join userContact.user user where userContact.user = ? and user.active = ? order by userContact.lastUsedDate desc", aditUser, true);
        } catch (Exception e) {
        	throw new AditInternalException("Error while fetching user contact: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

}
