package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DownloadRequestLog;

public class DownloadRequestLogDAO extends HibernateDaoSupport {
	private static Logger LOG = Logger.getLogger(DownloadRequestLogDAO.class);

	public Long save(final DownloadRequestLog logEntry) {
		LOG.debug("Attemptyng to save download log entry...");
		Long result = null;

		result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.save(logEntry);
				LOG.debug("Successfully saved download log entry with ID: " + logEntry.getId());
				return logEntry.getId();
			}
		});

		return result;
	}
}
