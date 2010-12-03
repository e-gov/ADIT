package ee.adit.dao.pojo;

import java.util.Date;

public class Notification implements java.io.Serializable {

    private static final long serialVersionUID = 1401042967511114468L;
    private long id;
    private String userCode;
    private long documentId;
    private Date eventDate;
    private String notificationType;
    private String notificationText;
    private Long notificationId;
    private Date notificationSendingDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Date getNotificationSendingDate() {
        return notificationSendingDate;
    }

    public void setNotificationSendingDate(Date notificationSendingDate) {
        this.notificationSendingDate = notificationSendingDate;
    }
}
