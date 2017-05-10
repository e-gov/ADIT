package ee.adit.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Notification;
import ee.adit.exception.AditInternalException;

/**
 * Notification data access class. Provides methods for retrieving and manipulating
 * notification data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class NotificationDAO extends HibernateDaoSupport {
    
    private static Logger logger = LogManager.getLogger(NotificationDAO.class);

    /**
     * Fetch unsent notifications.
     * 
     * @return list of unsent notifications
     */
    @SuppressWarnings("unchecked")
    public List<Notification> getUnsentNotifications() {
        List<Notification> result = null;

        try {
            logger.debug("Finding unsent notifications... ");
            result = (List<Notification>) this.getHibernateTemplate().find(
                    "from Notification notification where notification.notificationId is null");
        } catch (Exception e) {
            logger.error("Exception while finding notifications: ", e);
        }

        return result;
    }

    /**
     * Save notification.
     * 
     * @param notification notification
     * @return ID
     */
    public Long save(final Notification notification) {
        logger.debug("Attemptyng to save notification...");
        Long result = null;

        result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.saveOrUpdate(notification);
                logger.debug("Successfully saved notification with ID: " + notification.getId());
                return notification.getId();
            }
        });

        return result;
    }

    /**
     * Fetch unsent notifications by date.
     * 
     * @param comparisonDate comparison date
     * @return list of unsent notifications
     */
    public Long getUnsentNotifications(Date comparisonDate) {
        Long result = 0L;
        String sql = "select count(*) from Notification where notificationId is null and eventDate <= :comparisonDate";

        Session session = null;
        try {
            session = this.getSessionFactory().openSession();
            Query query = session.createQuery(sql);
            query.setParameter("comparisonDate", comparisonDate);
            result = (Long) query.uniqueResult();
        } catch (Exception e) {
            throw new AditInternalException("Error while fetching unsent notifications: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }
}
