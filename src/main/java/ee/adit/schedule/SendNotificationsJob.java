package ee.adit.schedule;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.pojo.Notification;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;

public class SendNotificationsJob extends QuartzJobBean {
	private static Logger LOG = Logger.getLogger(SendNotificationsJob.class);
	private UserService userService;
	private Configuration configuration;
	
	@Override
	protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
		try {
			LOG.info("Executing scheduled job: Send unsent notifications.");
			List<Notification> unsentNotifications = this.userService.getNotificationDAO().getUnsentNotifications();
			
			for (Notification item : unsentNotifications) {
				ScheduleClient.addEvent(item, this.configuration.getSchedulerEventTypeName(), userService);
			}
		} catch (Exception e) {
			LOG.error("Error executing scheduled notification sending: ", e);
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

}
