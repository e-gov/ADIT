package ee.adit.dao;

import org.apache.log4j.Logger;

import ee.adit.dao.pojo.AditUser;

public class AditUserDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(AditUserDAO.class);
	
	public AditUser getUserByID(String userRegCode) {
		AditUser result = null;
		
		try {
			this.getHibernateTemplate().get(AditUser.class, userRegCode);
		} catch(Exception e) {
			LOG.error("Exception while finding AditUser by registration code: ", e);
		}
		
		return result;
	}
	
}
