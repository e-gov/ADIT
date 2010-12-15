package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.MetadataRequestLog;

/**
 * Metadata request log data access class. Provides methods for retrieving and manipulating
 * metadata request log data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class MetadataRequestLogDAO extends HibernateDaoSupport {
    
    private static Logger logger = Logger.getLogger(MetadataRequestLogDAO.class);

    /**
     * Save metadata request log.
     * 
     * @param logEntry log record
     * @return ID
     */
    public Long save(final MetadataRequestLog logEntry) {
        logger.debug("Attempting to save metadata request log entry...");
        Long result = null;

        result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.save(logEntry);
                logger.debug("Successfully saved metadata request log entry with ID: " + logEntry.getId());
                return logEntry.getId();
            }
        });

        return result;
    }
}
