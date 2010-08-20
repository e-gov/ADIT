package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.DocumentService;
import ee.adit.service.UserService;

public class UserSyncJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(UserSyncJob.class);
	
	private UserService userService;
	
	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		
		try {
			LOG.info("Executing scheduled job: DVK user synchronization");
			
			// Send documents to DVK Client database
			int sentDocumentsCount = this.getUserService();

			LOG.debug("Users synchronized (" + sentDocumentsCount + ")");
			
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
