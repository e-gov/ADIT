package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;

import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;

public class AccessRestrictionDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(AccessRestrictionDAO.class);
	
	public List<AccessRestriction> getAccessRestrictionsForUser(AditUser aditUser) {
		List<AccessRestriction> result = null;
		
		try {
			result = this.getHibernateTemplate().find("from AccessRestriction accessRestriction where accessRestriction.aditUser = ?", aditUser);
		} catch (Exception e) {
			LOG.error("Exception while adding AditUser: ", e);
		}	
		
		return result;
	}
	
}
