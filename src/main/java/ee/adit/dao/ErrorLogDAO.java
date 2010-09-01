package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.ErrorLog;

public class ErrorLogDAO extends HibernateDaoSupport {
	
	private static Logger LOG = Logger.getLogger(ErrorLogDAO.class);

	public Long save(final ErrorLog errorLogEntry) {
		LOG.debug("Attemptyng to save error log entry... ID: " + errorLogEntry.getId());
		Long result = null;

		result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {

			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.save(errorLogEntry);
				LOG.debug("Successfully saved error log entry with ID: " + errorLogEntry.getId());
				return errorLogEntry.getId();
			}
		});

		return result;
	}
}
