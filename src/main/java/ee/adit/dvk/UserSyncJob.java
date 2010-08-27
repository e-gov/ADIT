package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.UserService;
import ee.adit.util.DVKUserSyncResult;

public class UserSyncJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(UserSyncJob.class);
	
	private UserService userService;
	
	@Override
	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		
		try {
			LOG.info("Executing scheduled job: DVK user synchronization");
			
			// Send documents to DVK Client database
			DVKUserSyncResult result = this.getUserService().synchroinzeDVKUsers();

			LOG.info("Users added: " + result.getAdded());
			LOG.info("Users modified: " + result.getModified());
			LOG.info("Users deactivated: " + result.getDeactivated());
			
		} catch (Exception e) {
			LOG.error("Error executing scheduled DVK user synchronization: ", e);
		}

	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
}
