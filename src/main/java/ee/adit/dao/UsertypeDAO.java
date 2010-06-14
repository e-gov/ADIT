package ee.adit.dao;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ee.adit.dao.pojo.Usertype;

public class UsertypeDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(RemoteApplicationDAO.class);
	
	public Usertype getByShortName(String userTypeShortName) {
		return (Usertype) this.getHibernateTemplate().get(Usertype.class, userTypeShortName);
	}
	
}
