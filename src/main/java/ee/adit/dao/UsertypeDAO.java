package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditInternalException;

public class UsertypeDAO extends HibernateDaoSupport {

	private static Logger LOG = Logger.getLogger(RemoteApplicationDAO.class);
	
	public Usertype getByShortName(String userTypeShortName) {
		return (Usertype) this.getHibernateTemplate().get(Usertype.class, userTypeShortName.toLowerCase());
	}
	
	public Usertype getUsertype(AditUser user) {
		Usertype result = null;
		
		try {
			List<Usertype> usertypeList = this.getHibernateTemplate().find("select user.usertype from AditUser user where user.userCode = ?", user.getUserCode());
			
			if(usertypeList != null && usertypeList.size() > 0) {
				result = usertypeList.get(0);
			} else {
				throw new AditInternalException("Usertype not defined for user: " + user.getUserCode());
			}
			
		} catch (Exception e) {
			LOG.error("Error while fetching Usertype by AditUser", e);
		}
		
		return result;
	}
	
	public List<Usertype> listUsertypes() {
		Session session = null;
		String SQL = "from Usertype";
		try {
			session = this.getSessionFactory().openSession();
			return (List<Usertype>) session.createQuery(SQL).list();
		} finally {
			if(session != null) {
				session.clear();
			}
		}
	}
	
}
