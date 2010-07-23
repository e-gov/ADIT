package ee.adit.service;

import java.util.Date;

import ee.adit.dao.NotificationDAO;
import ee.adit.dao.RequestLogDAO;
import ee.adit.dao.pojo.Notification;
import ee.adit.dao.pojo.RequestLog;

public class LogService {
	private RequestLogDAO requestLogDAO;
	private NotificationDAO notificationDAO;

	public RequestLogDAO getRequestLogDAO() {
		return requestLogDAO;
	}

	public void setRequestLogDAO(RequestLogDAO requestLogDAO) {
		this.requestLogDAO = requestLogDAO;
	}
	
	public NotificationDAO getNotificationDAO() {
		return notificationDAO;
	}

	public void setNotificationLogDAO(NotificationDAO notificationDAO) {
		this.notificationDAO = notificationDAO;
	}

	public Long addRequestLogEntry(RequestLog requestLogEntry) {
		return this.requestLogDAO.save(requestLogEntry);
	}
	
	public Long addRequestLogEntry(
			String requestName,
			Long documentId,
			Date requestDate,
			String remoteApplicationShortName,
			String userCode,
			String organizationCode,
			String additionalInformation) {
		
		RequestLog logEntry = new RequestLog();
		logEntry.setAdditionalInformation(additionalInformation);
		logEntry.setDocumentId(documentId);
		logEntry.setOrganizationCode(organizationCode);
		logEntry.setRemoteApplicationShortName(remoteApplicationShortName);
		logEntry.setRequest(requestName);
		logEntry.setRequestDate(requestDate);
		logEntry.setUserCode(userCode);
		
		return this.requestLogDAO.save(logEntry);
	}
	
	public long addNotification(
			long documentId,
			String notificationType,
			String userCode,
			Date eventDate,
			String notificationText,
			Long notificationId,
			Date notificationSendingDate) {
		
		Notification notification = new Notification();
		notification.setUserCode(userCode);
		notification.setDocumentId(documentId);
		notification.setEventDate(eventDate);
		notification.setNotificationType(notificationType);
		notification.setNotificationText(notificationText);
		notification.setNotificationId(notificationId);
		notification.setNotificationSendingDate(notificationSendingDate);
		
		return this.notificationDAO.save(notification);
	}
}
