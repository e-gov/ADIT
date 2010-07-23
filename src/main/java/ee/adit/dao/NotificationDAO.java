package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Notification;

public class NotificationDAO extends HibernateDaoSupport {
	private static Logger LOG = Logger.getLogger(NotificationDAO.class);
	
	public Long save(final Notification notification) {
		LOG.debug("Attemptyng to save notification...");
		Long result = null;
		
		result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
			
			@Override
			public Object doInHibernate(Session session) throws HibernateException,	SQLException {
				session.save(notification);
				LOG.debug("Successfully saved notification with ID: " + notification.getId());
				return notification.getId();
			}
		});
		
		return result;
	}
}
