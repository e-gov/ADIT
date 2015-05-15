package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;

/**
 * AccessRestriction data access class. Provides methods for retrieving and manipulating access restrictions.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class AccessRestrictionDAO extends HibernateDaoSupport {

	private static Logger LOG = Logger.getLogger(AccessRestrictionDAO.class);
	
	/**
	 * Retrieves access restrictions for the specified user.
	 * 
	 * @param aditUser
	 * @return list of access restrictions, null if none found
	 */
	public List<AccessRestriction> getAccessRestrictionsForUser(AditUser aditUser) {
		List<AccessRestriction> result = null;
		
		try {
			LOG.debug("Finding access restrictions for user: " + aditUser.getUserCode() + ", " + aditUser.getFullName());
			result = this.getHibernateTemplate().find("from AccessRestriction accessRestriction where accessRestriction.aditUser = ?", aditUser);
		} catch (Exception e) {
			LOG.error("Exception while finding access restrictions: ", e);
		}	
		
		return result;
	}
	
}
