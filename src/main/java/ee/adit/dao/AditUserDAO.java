package ee.adit.dao;

import org.apache.log4j.Logger;

import ee.adit.dao.pojo.AditUser;

public class AditUserDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(AditUserDAO.class);
	
	public AditUser getUserByID(String userRegCode) {
		AditUser result = null;
		
		try {
			result = (AditUser) this.getHibernateTemplate().get(AditUser.class, userRegCode);
		} catch(Exception e) {
			LOG.error("Exception while finding AditUser by registration code: ", e);
		}
		
		return result;
	}
	
	public void saveOrUpdate(AditUser aditUser) {
		try {
			this.getHibernateTemplate().saveOrUpdate(aditUser);
		} catch(Exception e) {
			LOG.error("Exception while adding AditUser: ", e);
		}
	}
	
}
