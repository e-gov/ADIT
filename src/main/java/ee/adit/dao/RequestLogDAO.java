package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.RequestLog;

public class RequestLogDAO extends HibernateDaoSupport {
	
	private static Logger LOG = Logger.getLogger(RequestLogDAO.class);
	
	public Long save(final RequestLog requestLogEntry) {
		LOG.debug("Attemptyng to save request log entry...");
		Long result = null;
		
		result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
			
			@Override
			public Object doInHibernate(Session session) throws HibernateException,	SQLException {
				session.save(requestLogEntry);
				LOG.debug("Successfully saved request log entry with ID: " + requestLogEntry.getId());
				return requestLogEntry.getId();
			}
		});
		
		return result;
	}
}
