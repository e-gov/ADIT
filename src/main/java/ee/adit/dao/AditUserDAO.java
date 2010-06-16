package ee.adit.dao;

import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.HibernateCallback;

import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;

public class AditUserDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(AditUserDAO.class);

	public AditUser getUserByID(String userRegCode) {
		AditUser result = null;

		try {
			result = (AditUser) this.getHibernateTemplate().get(AditUser.class,
					userRegCode);
		} catch (Exception e) {
			LOG.error(
					"Exception while finding AditUser by registration code: ",
					e);
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

		DetachedCriteria criteria = DetachedCriteria.forClass(AditUser.class);
		criteria.addOrder(Order.asc("fullName"));
		result = this.getHibernateTemplate().findByCriteria(criteria, startIndex, maxResults);
		
		return result;
	}

}
