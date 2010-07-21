package ee.adit.service;

import java.util.Date;

import ee.adit.dao.NotificationLogDAO;
import ee.adit.dao.RequestLogDAO;
import ee.adit.dao.pojo.NotificationLog;
import ee.adit.dao.pojo.RequestLog;

public class LogService {
	private RequestLogDAO requestLogDAO;
	private NotificationLogDAO notificationLogDAO;

	public RequestLogDAO getRequestLogDAO() {
		return requestLogDAO;
	}

	public void setRequestLogDAO(RequestLogDAO requestLogDAO) {
		this.requestLogDAO = requestLogDAO;
	}
	
	public NotificationLogDAO getNotificationLogDAO() {
		return notificationLogDAO;
	}

	public void setNotificationLogDAO(NotificationLogDAO notificationLogDAO) {
		this.notificationLogDAO = notificationLogDAO;
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
	
	public long addNotificationLogEntry(
			long documentId,
			String notificationType,
			String userCode,
			Date notificationDate,
			long notificationId) {
		
		NotificationLog logEntry = new NotificationLog();
		logEntry.setDocumentId(documentId);
		logEntry.setNotificationDate(notificationDate);
		logEntry.setNotificationType(notificationType);
		logEntry.setUserCode(userCode);
		logEntry.setNotificationId(notificationId);
		
		return this.notificationLogDAO.save(logEntry);
	}
}
