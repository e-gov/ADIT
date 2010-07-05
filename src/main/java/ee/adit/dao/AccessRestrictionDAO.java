package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;

public class AccessRestrictionDAO extends HibernateDaoSupport {

	private static Logger LOG = Logger.getLogger(AccessRestrictionDAO.class);
	
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
