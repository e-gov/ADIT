package ee.adit.schedule;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.dao.pojo.Notification;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;
import ee.adit.util.Constants;

/**
 * Quartz job for sending notifications to "teavituskalender" X-Road database.
 * Enables periodic sending of notifications that could not be sent in real time
 * (or previous periodic sending attempts). <br>
 * <br>
 * Quartz library: http://www.quartz-scheduler.org
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class SendNotificationsJob extends QuartzJobBean {
	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_NOTIFICATION_SEND;
    private static Logger logger = Logger.getLogger(SendNotificationsJob.class);

    private UserService userService;
    private Configuration configuration;
    private ScheduleClient scheduleClient;
    private MaintenanceJobDAO maintenanceJobDAO;

    /** {@inheritDoc} */
    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        try {
        	if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
        		try {
		        	logger.info("Executing scheduled job: Send unsent notifications.");
		            List<Notification> unsentNotifications = this.userService.getNotificationDAO().getUnsentNotifications();

		            for (Notification item : unsentNotifications) {
		                getScheduleClient().addEvent(item, this.configuration.getSchedulerEventTypeName(), userService);
		            }
        		} finally {
        			maintenanceJobDAO.setJobRunningStatus(jobId, false);
        		}
        	}
        } catch (Exception e) {
            logger.error("Error executing scheduled notification sending: ", e);
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public ScheduleClient getScheduleClient() {
        return scheduleClient;
    }

    public void setScheduleClient(ScheduleClient scheduleClient) {
        this.scheduleClient = scheduleClient;
    }

	public MaintenanceJobDAO getMaintenanceJobDAO() {
		return maintenanceJobDAO;
	}

	public void setMaintenanceJobDAO(MaintenanceJobDAO maintenanceJobDAO) {
		this.maintenanceJobDAO = maintenanceJobDAO;
	}
}
