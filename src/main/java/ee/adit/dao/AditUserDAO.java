package ee.adit.dao;

import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.springframework.orm.hibernate3.HibernateCallback;

import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;

public class AditUserDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(AditUserDAO.class);

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

	public void saveOrUpdate(AditUser aditUser) {
		try {
			this.getHibernateTemplate().saveOrUpdate(aditUser);
		} catch (Exception e) {
			LOG.error("Exception while adding AditUser: ", e);
		}
	}

	public List<AccessRestriction> getAccessRestrictionsForUser(AditUser aditUser) {
		List<AccessRestriction> result = null;
		
		try {
			result = this.getHibernateTemplate().find("from AccessRestriction accessRestriction where accessRestriction.aditUser = ?", aditUser);
		} catch (Exception e) {
			LOG.error("Exception while adding AditUser: ", e);
		}	
		
		return result;
	}

	public List<AditUser> listUsers(int startIndex, int maxResults) throws Exception {
		List<AditUser> result = null;
		
		DetachedCriteria dt = DetachedCriteria.forClass(AditUser.class, "aditUser");
		dt.add(Property.forName("aditUser.active").eq(new Boolean(true)));
		dt.addOrder(Order.asc("aditUser.fullName"));
		result = this.getHibernateTemplate().findByCriteria(dt, 0, maxResults);
		
		return result;
	}
	
}
