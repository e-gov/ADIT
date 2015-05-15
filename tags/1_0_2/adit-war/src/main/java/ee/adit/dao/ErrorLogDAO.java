package ee.adit.dao;

import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.ErrorLog;
import ee.adit.exception.AditInternalException;
import ee.adit.service.LogService;

public class ErrorLogDAO extends HibernateDaoSupport {
	
	private static Logger LOG = Logger.getLogger(ErrorLogDAO.class);

	public Long save(final ErrorLog errorLogEntry) {
		LOG.debug("Attempting to save error log entry...");
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
	
	public Long getErrors(Date comparisonDate) {
		Long result = 0L;
		String SQL = "select count(*) from ErrorLog where (errorLevel = '" + LogService.ErrorLogLevel_Fatal + "' or errorLevel = '" + LogService.ErrorLogLevel_Error + "') and errorDate <= :comparisonDate"; 
		
		Session session = null;
		try {
			session = this.getSessionFactory().openSession();
			Query query = session.createQuery(SQL);
			query.setParameter("comparisonDate", comparisonDate);
			result = (Long) query.uniqueResult();
		} catch (Exception e) {
			throw new AditInternalException("Error while fetching latest errors: ", e);
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
	
}
