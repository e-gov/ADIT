package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;

/**
 * AccessRestriction data access class. Provides methods for retrieving and
 * manipulating access restrictions.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class AccessRestrictionDAO extends HibernateDaoSupport {

    private static Logger logger = Logger.getLogger(AccessRestrictionDAO.class);

    /**
     * Retrieves access restrictions for the specified user.
     * 
     * @param aditUser user
     * @return list of access restrictions, null if none found
     */
    @SuppressWarnings("unchecked")
    public List<AccessRestriction> getAccessRestrictionsForUser(final AditUser aditUser) {
        List<AccessRestriction> result = null;

        try {
            logger.debug("Finding access restrictions for user: " + aditUser.getUserCode() + ", "
                            + aditUser.getFullName());
            result = (List<AccessRestriction>) this.getHibernateTemplate().find(
                    "from AccessRestriction accessRestriction where accessRestriction.aditUser = ?", aditUser);
        } catch (Exception e) {
            logger.error("Exception while finding access restrictions: ", e);
        }

        return result;
    }

}
