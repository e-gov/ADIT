package ee.adit.dao;

import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.ErrorLog;
import ee.adit.exception.AditInternalException;
import ee.adit.service.LogService;

/**
 * Error log data access class. Provides methods for retrieving and manipulating
 * error log data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class ErrorLogDAO extends HibernateDaoSupport {

    private static Logger logger = LogManager.getLogger(ErrorLogDAO.class);

    /**
     * Save error log.
     * 
     * @param errorLogEntry error log record
     * @return ID
     */
    public Long save(final ErrorLog errorLogEntry) {
        logger.debug("Attempting to save error log entry...");
        Long result = null;

        result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.save(errorLogEntry);
                logger.debug("Successfully saved error log entry with ID: " + errorLogEntry.getId());
                return errorLogEntry.getId();
            }
        });

        return result;
    }

    /**
     * Fetch error log by date and level.
     * 
     * @param comparisonDate
     * 		Oldest allowed date since when errors will be requested
     * @param level log level
     * @return list of log entries
     */
    public Long getErrors(Date comparisonDate, String level) {

        String sql = null;
        // FATAL
        if (LogService.ERROR_LOG_LEVEL_FATAL.equalsIgnoreCase(level)) {
            sql = "select count(*) from ErrorLog where (errorLevel = '" + LogService.ERROR_LOG_LEVEL_FATAL
                    + "') and errorDate >= :comparisonDate";
        } else if (LogService.ERROR_LOG_LEVEL_ERROR.equalsIgnoreCase(level)) {
            sql = "select count(*) from ErrorLog where (errorLevel = '" + LogService.ERROR_LOG_LEVEL_FATAL
                    + "' or errorLevel = '" + LogService.ERROR_LOG_LEVEL_ERROR + "') and errorDate >= :comparisonDate";
        } else if (LogService.ERROR_LOG_LEVEL_WARN.equalsIgnoreCase(level)) {
            sql = "select count(*) from ErrorLog where (errorLevel = '" + LogService.ERROR_LOG_LEVEL_FATAL
                    + "' or errorLevel = '" + LogService.ERROR_LOG_LEVEL_ERROR + "' or errorLevel = '"
                    + LogService.ERROR_LOG_LEVEL_WARN + "') and errorDate >= :comparisonDate";
        } else {
            sql = "select count(*) from ErrorLog where (errorLevel = '" + LogService.ERROR_LOG_LEVEL_FATAL
                    + "') and errorDate >= :comparisonDate";
        }

        Long result = 0L;

        Session session = null;
        try {
            session = this.getSessionFactory().openSession();
            Query query = session.createQuery(sql);
            query.setParameter("comparisonDate", comparisonDate);
            result = (Long) query.uniqueResult();
        } catch (Exception e) {
            throw new AditInternalException("Error while fetching latest errors: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

}
