package ee.adit.service;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.pojo.Message;
import ee.adit.schedule.ScheduleClient;
import ee.adit.util.Configuration;
import ee.adit.util.Util;

public class NotificationService {

	private static Logger logger = Logger.getLogger(NotificationService.class);
	
	private Configuration configuration;
	
	private UserService userService;
	
	private MessageService messageService;
	
	private ScheduleClient scheduleClient;
	
	public void sendNotification(Document document, AditUser sender, AditUser recipient, String notificationType) {
		if (recipient != null && document != null) {
			logger.debug("Preparing notifications of type " + notificationType);
			
			Calendar requestDate = Calendar.getInstance();

			if ((userService.findNotification(recipient.getUserNotifications(), ScheduleClient.NOTIFICATION_TYPE_SEND) != null)) {
				String senderInfo = sender.getFullName() != null && !sender.getFullName().trim().isEmpty() ?
									sender.getFullName() : sender.getUserCode();
				
				List<Message> messageInAllKnownLanguages = this.getMessageService().getMessages(
						"scheduler.message.send", new Object[] {document.getTitle(), senderInfo});
				String eventText = Util.joinMessages(messageInAllKnownLanguages, "<br/>");
				
				this.scheduleClient.addEvent(recipient, eventText,
						this.getConfiguration().getSchedulerEventTypeName(), requestDate,
						notificationType, document.getId(), this.userService);
			}
			
			
		}
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public MessageService getMessageService() {
		return messageService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public ScheduleClient getScheduleClient() {
		return scheduleClient;
	}

	public void setScheduleClient(ScheduleClient scheduleClient) {
		this.scheduleClient = scheduleClient;
	}
	
}
