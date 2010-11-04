package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditInternalException;

/**
 * AditUser data access class. Provides methods for retrieving and manipulating user data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class AditUserDAO extends HibernateDaoSupport {

	/**
	 * Log4J logger
	 */
	private static Logger LOG = Logger.getLogger(AditUserDAO.class);

	/**
	 * Retrieves user by ID.
	 * 
	 * @param userCode
	 * @return
	 */
	public AditUser getUserByID(String userCode) {
		AditUser result = null;
		
		try {
			result = (AditUser) this.getHibernateTemplate().get(AditUser.class, userCode);
		} catch (Exception e) {
			LOG.error(
					"Exception while finding AditUser by registration code: ", e);
		}

		return result;
	}
	
	/**
	 * 
	 * @param aditUser
	 */
	public void saveOrUpdate(AditUser aditUser) {
		try {
			this.getHibernateTemplate().saveOrUpdate(aditUser);
		} catch (Exception e) {
			LOG.error("Exception while adding AditUser: ", e);
		}
	}

	

	public List<AditUser> listUsers(int startIndex, int maxResults) throws Exception {
		List<AditUser> result = null;
		
		DetachedCriteria dt = DetachedCriteria.forClass(AditUser.class, "aditUser");
		dt.add(Property.forName("aditUser.active").eq(new Boolean(true)));
		dt.addOrder(Order.asc("aditUser.fullName"));
		result = this.getHibernateTemplate().findByCriteria(dt, startIndex, maxResults);
		
		return result;
	}
	
	public List<AditUser> listDVKUsers() throws Exception {
		List<AditUser> result = null;
		String SQL = "from AditUser where dvkOrgCode is not null";
		Session session = null;
		try {
			session = this.getSessionFactory().openSession();
			result = session.createQuery(SQL).list();
		} catch (Exception e) {
			throw new AditInternalException("Error while fetching DVK users from database: ", e);
		} finally {
			if(session != null) {
				session.close();
			}
		}
			
		return result;
	}
	
	public Long getUsedSpaceForUser(String userCode) {
		Long result = new Long(0);
		AditUser user = this.getUserByID(userCode);
		
		if(user != null) {
			result = user.getDiskQuotaUsed();
		} else {
			throw new AditInternalException("Did not find user: " + userCode);
		}
		
		return result;
	}
	
}
