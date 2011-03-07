package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditInternalException;

/**
 * AditUser data access class. Provides methods for retrieving and manipulating
 * user data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class AditUserDAO extends HibernateDaoSupport {

    private static Logger logger = Logger.getLogger(AditUserDAO.class);

    /**
     * Retrieves user by ID.
     * 
     * @param userCode user code (ID)
     * @return user
     */
    public AditUser getUserByID(String userCode) {
        AditUser result = null;

        try {
            result = (AditUser) this.getHibernateTemplate().get(AditUser.class, userCode);
        } catch (Exception e) {
            logger.error("Exception while finding AditUser by registration code: ", e);
        }

        return result;
    }

    /**
     * Saves changes for this user or inserts a new record.
     * 
     * @param aditUser user
     */
    public void saveOrUpdate(AditUser aditUser) {
        Session session = null;
        Transaction transaction = null;
        try {

            session = this.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(aditUser);
            transaction.commit();

        } catch (Exception e) {
            logger.error(e);
        	if (transaction != null) {
                transaction.rollback();
            }
            throw new AditInternalException("Error while updating AditUser: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        /*
         * try { this.getHibernateTemplate().saveOrUpdate(aditUser); } catch
         * (Exception e) { LOG.error("Exception while adding AditUser: ", e); }
         */
    }

    /**
     * Fetches a list of all the active users ordered by name.
     * 
     * @param startIndex start index (offset) of the result list
     * @param maxResults maximum number of results
     * @return list of users
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<AditUser> listUsers(int startIndex, int maxResults) throws Exception {
        List<AditUser> result = null;

        DetachedCriteria dt = DetachedCriteria.forClass(AditUser.class, "aditUser");
        dt.add(Property.forName("aditUser.active").eq(new Boolean(true)));
        dt.addOrder(Order.asc("aditUser.fullName"));
        result = this.getHibernateTemplate().findByCriteria(dt, startIndex, maxResults);

        return result;
    }

    /**
     * Fetches all DVK users. No additional filtering is applied.
     * 
     * @return list of DVK users
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<AditUser> listDVKUsers() throws Exception {
        List<AditUser> result = null;
        String sql = "from AditUser where dvkOrgCode is not null";
        Session session = null;
        try {
            session = this.getSessionFactory().openSession();
            result = session.createQuery(sql).list();
        } catch (Exception e) {
            throw new AditInternalException("Error while fetching DVK users from database: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Calculates used disk quota for user.
     * 
     * @param userCode user code
     * @return used space in bytes
     */
    public Long getUsedSpaceForUser(String userCode) {
        Long result = new Long(0);
        AditUser user = this.getUserByID(userCode);

        if (user != null) {
            result = (user.getDiskQuotaUsed() == null) ? 0L : user.getDiskQuotaUsed();
        } else {
            throw new AditInternalException("Did not find user: " + userCode);
        }

        return result;
    }

}
