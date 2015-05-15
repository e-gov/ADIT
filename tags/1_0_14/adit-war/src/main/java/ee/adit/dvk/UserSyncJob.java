package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.UserService;
import ee.adit.util.DVKUserSyncResult;

/**
 * Synchronizes users from DVK to ADIT.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class UserSyncJob extends QuartzJobBean {

    private static Logger logger = Logger.getLogger(UserSyncJob.class);

    private UserService userService;

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {

        try {
            logger.info("Executing scheduled job: DVK user synchronization");

            // Send documents to DVK Client database
            DVKUserSyncResult result = this.getUserService().synchroinzeDVKUsers();

            logger.info("Users added: " + result.getAdded());
            logger.info("Users modified: " + result.getModified());
            logger.info("Users deactivated: " + result.getDeactivated());

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

}
