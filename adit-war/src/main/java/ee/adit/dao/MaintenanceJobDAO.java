package ee.adit.dao;

import ee.adit.dao.pojo.MaintenanceJob;
import ee.adit.exception.AditInternalException;
import java.sql.SQLException;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
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

    /**
     * Initialize running job statuses.
     */
    public void initializeJobRunningStatuses() {
        logger.debug("initializeJobRunningStatuses starting...");
        setAllJobsToNotRunning(findAllJobs());
    }

    /**
     * Find all jobs.
     * @return list of maintenance jobs.
     */
    public List<MaintenanceJob> findAllJobs() {
        Session session = null;
        try {
            session = this.getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(MaintenanceJob.class);
            return (List<MaintenanceJob>) criteria.list();
        } catch (Exception e) {
            throw new AditInternalException("Error while fetching DVK DocumentSharings: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void setAllJobsToNotRunning(final List<MaintenanceJob> jobs) {
        if (jobs != null) {
            logger.debug("found jobs: " + jobs.size());

            for (MaintenanceJob maintenanceJob: jobs) {
                Session session = null;
                try {
                    //clear the state
                    if (maintenanceJob.isRunning()) {
                        setJobRunningStatus(maintenanceJob.getId(), false);
                        logger.debug("job: " + maintenanceJob.getName() + " running status now changed to false");
                    }
                } catch (Exception e) {
                    throw new AditInternalException("Error while fetching DVK DocumentSharings: ", e);
                } finally {
                    if (session != null) {
                        session.close();
                    }
                }
            }
        }
    }
}
