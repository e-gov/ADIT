package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.NotificationType;

/**
 * NotificationType data access class. Provides methods for retrieving and manipulating
 * notificationType data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class NotificationTypeDAO extends HibernateDaoSupport {

    private static Logger logger = Logger.getLogger(NotificationTypeDAO.class);

    /**
     * Fetch notification type list.
     * 
     * @return list of notification types
     */
    @SuppressWarnings("unchecked")
    public List<NotificationType> getNotificationTypeList() {
        List<NotificationType> result = null;

        try {
            logger.debug("Getting notification type list.");
            result = this.getHibernateTemplate().loadAll(NotificationType.class);
        } catch (Exception e) {
            logger.error("Exception while getting notification type list: ", e);
        }

        return result;
    }

}
