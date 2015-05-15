package ee.adit.stateportal;

import java.util.List;

import ee.adit.pojo.EmailAddress;

/**
 * State portal notification ordering status data.
 * 
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class NotificationStatus {
	private String notificationTypeName;
	private boolean notificationEmailStatus;
	private List<EmailAddress> emailList;
	
	public String getNotificationTypeName() {
		return notificationTypeName;
	}
	public void setNotificationTypeName(String notificationTypeName) {
		this.notificationTypeName = notificationTypeName;
	}
	
	public boolean getNotificationEmailStatus() {
		return notificationEmailStatus;
	}
	public void setNotificationEmailStatus(boolean notificationEmailStatus) {
		this.notificationEmailStatus = notificationEmailStatus;
	}
	
	public List<EmailAddress> getEmailList() {
		return emailList;
	}
	public void setEmailList(List<EmailAddress> emailList) {
		this.emailList = emailList;
	}
}
