package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.service.UserService;
import ee.adit.util.Constants;
import ee.adit.util.DVKUserSyncResult;

/**
 * Synchronizes users from DVK to ADIT.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class UserSyncJob extends QuartzJobBean {
	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_DVK_USER_SYNC;
    private static Logger logger = Logger.getLogger(UserSyncJob.class);

    private UserService userService;
    private MaintenanceJobDAO maintenanceJobDAO;

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        try {
        	if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
        		try {
		            logger.info("Executing scheduled job: DVK user synchronization");

		            DVKUserSyncResult result = this.getUserService().synchroinzeDVKUsers();

		            logger.info("Users added: " + result.getAdded());
		            logger.info("Users modified: " + result.getModified());
		            logger.info("Users deactivated: " + result.getDeactivated());
        		} finally {
        			maintenanceJobDAO.setJobRunningStatus(jobId, false);
        		}
        	}
        } catch (Exception e) {
            logger.error("Error executing scheduled DVK user synchronization: ", e);
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

	public MaintenanceJobDAO getMaintenanceJobDAO() {
		return maintenanceJobDAO;
	}

	public void setMaintenanceJobDAO(MaintenanceJobDAO maintenanceJobDAO) {
		this.maintenanceJobDAO = maintenanceJobDAO;
	}
}
