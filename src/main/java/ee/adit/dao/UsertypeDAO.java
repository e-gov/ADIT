package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditInternalException;

public class UsertypeDAO extends AbstractAditDAO {

	private static Logger LOG = Logger.getLogger(RemoteApplicationDAO.class);
	
	public Usertype getByShortName(String userTypeShortName) {
		return (Usertype) this.getHibernateTemplate().get(Usertype.class, userTypeShortName);
	}
	
	public Usertype getUsertype(AditUser user) {
		Usertype result = null;
		
		try {
			List<Usertype> usertypeList = this.getHibernateTemplate().find("user.Usertype from AditUser user where user.userCode = ?", user.getUserCode());
			
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
	
}
