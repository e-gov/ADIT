package ee.adit.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Notification;

public class NotificationDAO extends HibernateDaoSupport {
	private static Logger LOG = Logger.getLogger(NotificationDAO.class);
	
	public List<Notification> getUnsentNotifications() {
		List<Notification> result = null;
		
		try {
			LOG.debug("Finding unsent notifications... ");
			result = this.getHibernateTemplate().find("from Notification notification where notification.notificationId is null");
		} catch (Exception e) {
			LOG.error("Exception while finding notifications: ", e);
		}	
		
		return result;
	}
	
	public Long save(final Notification notification) {
		LOG.debug("Attemptyng to save notification...");
		Long result = null;
		
		result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
			
			@Override
			public Object doInHibernate(Session session) throws HibernateException,	SQLException {
				session.saveOrUpdate(notification);
				LOG.debug("Successfully saved notification with ID: " + notification.getId());
				return notification.getId();
			}
		});
		
		return result;
	}
}
