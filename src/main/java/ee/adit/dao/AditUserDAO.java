package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.exception.AditInternalException;

public class AditUserDAO extends HibernateDaoSupport {

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
	
}
