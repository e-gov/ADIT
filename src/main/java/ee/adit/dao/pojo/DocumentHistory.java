package ee.adit.dao.pojo;

// Generated 16.06.2010 14:33:21 by Hibernate Tools 3.2.4.GA

import java.util.Date;

/**
 * DocumentHistory generated by hbm2java
 */
public class DocumentHistory implements java.io.Serializable {

	private long id;
	private RemoteApplication remoteApplication;
	private Document document;
	private String documentHistoryType;
	private String description;
	private Date eventDate;
	private String userCode;
	private String notificationStatus;
	private String xteeNotificationId;
	private String xteeUserCode;

	public DocumentHistory() {
	}

	public DocumentHistory(long id, Document document) {
		this.id = id;
		this.document = document;
	}

	public DocumentHistory(long id, RemoteApplication remoteApplication,
			Document document, String documentHistoryType, String description,
			Date eventDate, String userCode, String notificationStatus,
			String xteeNotificationId, String xteeUserCode) {
		this.id = id;
		this.remoteApplication = remoteApplication;
		this.document = document;
		this.documentHistoryType = documentHistoryType;
		this.description = description;
		this.eventDate = eventDate;
		this.userCode = userCode;
		this.notificationStatus = notificationStatus;
		this.xteeNotificationId = xteeNotificationId;
		this.xteeUserCode = xteeUserCode;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public RemoteApplication getRemoteApplication() {
		return this.remoteApplication;
	}

	public void setRemoteApplication(RemoteApplication remoteApplication) {
		this.remoteApplication = remoteApplication;
	}

	public Document getDocument() {
		return this.document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getDocumentHistoryType() {
		return this.documentHistoryType;
	}

	public void setDocumentHistoryType(String documentHistoryType) {
		this.documentHistoryType = documentHistoryType;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getEventDate() {
		return this.eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public String getUserCode() {
		return this.userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getNotificationStatus() {
		return this.notificationStatus;
	}

	public void setNotificationStatus(String notificationStatus) {
		this.notificationStatus = notificationStatus;
	}

	public String getXteeNotificationId() {
		return this.xteeNotificationId;
	}

	public void setXteeNotificationId(String xteeNotificationId) {
		this.xteeNotificationId = xteeNotificationId;
	}

	public String getXteeUserCode() {
		return this.xteeUserCode;
	}

	public void setXteeUserCode(String xteeUserCode) {
		this.xteeUserCode = xteeUserCode;
	}

}
