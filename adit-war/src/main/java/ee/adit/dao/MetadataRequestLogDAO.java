package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.MetadataRequestLog;

public class MetadataRequestLogDAO extends HibernateDaoSupport {
    private static Logger LOG = Logger.getLogger(MetadataRequestLogDAO.class);

    public Long save(final MetadataRequestLog logEntry) {
        LOG.debug("Attempting to save metadata request log entry...");
        Long result = null;

        result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.save(logEntry);
                LOG.debug("Successfully saved metadata request log entry with ID: " + logEntry.getId());
                return logEntry.getId();
            }
        });

        return result;
    }
}
