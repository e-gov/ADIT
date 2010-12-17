package ee.adit.stateportal;

import java.util.List;

import ee.adit.pojo.EmailAddress;

/**
 * State portal notification ordering status data.
 * 
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class NotificationStatus {
    
    /**
     * Notification type name
     */
    private String notificationTypeName;
    
    /**
     * Notification email status
     */
    private boolean notificationEmailStatus;
    
    /**
     * Email address list
     */
    private List<EmailAddress> emailList;

    /**
     * Get NotificationTypeName
     * 
     * @return notification type name
     */
    public String getNotificationTypeName() {
        return notificationTypeName;
    }

    /**
     * Set NotificationTypeName
     * @param notificationTypeName notification type name
     */
    public void setNotificationTypeName(String notificationTypeName) {
        this.notificationTypeName = notificationTypeName;
    }

    /**
     * Get notification email status
     * 
     * @return notification email status
     */
    public boolean getNotificationEmailStatus() {
        return notificationEmailStatus;
    }

    /**
     * Set notification email status
     * 
     * @param notificationEmailStatus notification email status
     */
    public void setNotificationEmailStatus(boolean notificationEmailStatus) {
        this.notificationEmailStatus = notificationEmailStatus;
    }

    /**
     * Get email list
     * 
     * @return email list
     */
    public List<EmailAddress> getEmailList() {
        return emailList;
    }

    /**
     * Set email list
     * 
     * @param emailList email list
     */
    public void setEmailList(List<EmailAddress> emailList) {
        this.emailList = emailList;
    }
}
