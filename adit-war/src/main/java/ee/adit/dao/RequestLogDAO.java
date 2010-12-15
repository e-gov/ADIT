package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.RequestLog;

/**
 * Request log data access class. Provides methods for retrieving and manipulating
 * request log data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class RequestLogDAO extends HibernateDaoSupport {

    private static Logger logger = Logger.getLogger(RequestLogDAO.class);

    /**
     * Save request log.
     * 
     * @param requestLogEntry request log record
     * @return ID
     */
    public Long save(final RequestLog requestLogEntry) {
        logger.debug("Attempting to save request log entry...");
        Long result = null;

        result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.save(requestLogEntry);
                logger.debug("Successfully saved request log entry with ID: " + requestLogEntry.getId());
                return requestLogEntry.getId();
            }
        });

        return result;
    }
}
