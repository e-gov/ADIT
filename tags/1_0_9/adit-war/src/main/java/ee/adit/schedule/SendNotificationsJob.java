package ee.adit.schedule;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.pojo.Notification;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;

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
     * Log4J logger
     */
    private static Logger logger = Logger.getLogger(SendNotificationsJob.class);

    /**
     * Instance of {@link UserService} class that implements business logic of
     * user-specific operations.
     */
    private UserService userService;

    /**
     * Application configuration as {@link Configuration} object.
     */
    private Configuration configuration;

    private ScheduleClient scheduleClient;

    /** {@inheritDoc} */
    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        try {
            logger.info("Executing scheduled job: Send unsent notifications.");
            List<Notification> unsentNotifications = this.userService.getNotificationDAO().getUnsentNotifications();

            for (Notification item : unsentNotifications) {
                getScheduleClient().addEvent(item, this.configuration.getSchedulerEventTypeName(), userService);
            }
        } catch (Exception e) {
            logger.error("Error executing scheduled notification sending: ", e);
        }
    }

    /**
     * Gets current user service instance.
     * 
     * @return User service instance
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * Sets current user service instance.
     * 
     * @param userService
     *            User service instance
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets current application configuration
     * 
     * @return Application configuration object
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets current application configuration as {@link Configuration} object
     * 
     * @param configuration
     *            Application configuration object
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public ScheduleClient getScheduleClient() {
        return scheduleClient;
    }

    public void setScheduleClient(ScheduleClient scheduleClient) {
        this.scheduleClient = scheduleClient;
    }

}
