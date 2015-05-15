package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.SetJobRunningStatusResult;

/**
 * Maintenance job data access class. Provides methods for retrieving and manipulating
 * maintenance job data.
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class MaintenanceJobDAO extends HibernateDaoSupport {
	private static Logger logger = Logger.getLogger(MaintenanceJobDAO.class);

    /**
     * Attempts to set "running" flag for given maintenance job (either
     * "running" or "not running"). This is necessary to make sure that no more
     * than one application cluster node is running the same job at the same
     * time.
     * If this method returns false then given maintenance job should not be
     * started.
     *
     * @param jobId
     * 		Unique ID of maintenance job
     * @param isRunning
     * 		Job running state
     * @return
     * 		{@code true} if setting job state was successful.
     * 		If method returns {@code false} then the job is already running
     *      and therefore should not be started.
     */
	public boolean setJobRunningStatus(
    		final long jobId,
    		final boolean isRunning) {

        logger.debug("setJobRunningStatus starting...");
        SetJobRunningStatusResult result = (SetJobRunningStatusResult) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        Query q = session.getNamedQuery("SET_JOB_RUNNING_STATUS");
                        q.setLong("jobId", jobId);
                        q.setBoolean("isRunning", isRunning);

                        logger.debug("Executing stored procedure SET_JOB_RUNNING_STATUS");
                        return q.uniqueResult();
                    }
                });

        logger.debug("setJobRunningStatus result for job " + jobId + " is: " + result.getResultCode());
        return ("ok".equalsIgnoreCase(result.getResultCode()));
    }
}
