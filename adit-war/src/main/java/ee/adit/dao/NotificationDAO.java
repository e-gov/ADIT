package ee.adit.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.Notification;
import ee.adit.exception.AditInternalException;

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
	
	public Long getUnsentNotifications(Date comparisonDate) {
		Long result = 0L;
		String SQL = "select count(*) from Notification where notificationId is null and eventDate <= :comparisonDate"; 
		
		Session session = null;
		try {
			session = this.getSessionFactory().openSession();
			result = (Long) session.createQuery(SQL).uniqueResult();
		} catch (Exception e) {
			throw new AditInternalException("Error while fetching unsent notifications: ", e);
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
}
