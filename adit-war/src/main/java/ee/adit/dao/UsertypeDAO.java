package ee.adit.dao;

import java.util.List;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditInternalException;

/**
 * Usertype data access class. Provides methods for retrieving and manipulating
 * usertype log data.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class UsertypeDAO extends HibernateDaoSupport {

    private static Logger logger = LogManager.getLogger(RemoteApplicationDAO.class);

    /**
     * Fetch usertype by short name.
     *
     * @param userTypeShortName usertype short name
     * @return usertype
     */
    public Usertype getByShortName(String userTypeShortName) {
        return (Usertype) this.getHibernateTemplate().get(Usertype.class, userTypeShortName.toLowerCase());
    }

    /**
     * Fetch usertype for user.
     *
     * @param user user
     * @return usertype
     */
    @SuppressWarnings("unchecked")
    public Usertype getUsertype(AditUser user) {
        Usertype result = null;

        try {
            List<Usertype> usertypeList = (List<Usertype>) this.getHibernateTemplate().find(
                    "select user.usertype from AditUser user where user.userCode = ?", user.getUserCode());

            if (usertypeList != null && usertypeList.size() > 0) {
                result = usertypeList.get(0);
            } else {
                throw new AditInternalException("Usertype not defined for user: " + user.getUserCode());
            }

        } catch (Exception e) {
            logger.error("Error while fetching Usertype by AditUser", e);
        }

        return result;
    }

    /**
     * Fetch usertype list.
     *
     * @return list of all usertypes
     */
    @SuppressWarnings("unchecked")
    public List<Usertype> listUsertypes() {
        Session session = null;
        String sql = "from Usertype";
        try {
            session = this.getSessionFactory().openSession();
            return session.createQuery(sql).list();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
