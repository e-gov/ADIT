package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.NotificationType;

public class NotificationTypeDAO extends HibernateDaoSupport {

	private static Logger LOG = Logger.getLogger(NotificationTypeDAO.class);
	
	public List<NotificationType> getNotificationTypeList() {
		List<NotificationType> result = null;
		
		try {
			LOG.debug("Getting notification type list.");
			result = this.getHibernateTemplate().loadAll(NotificationType.class);
		} catch (Exception e) {
			LOG.error("Exception while getting notification type list: ", e);
		}	
		
		return result;
	}
	
}
