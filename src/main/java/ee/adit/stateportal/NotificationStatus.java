package ee.adit.stateportal;

import java.util.List;

public class NotificationStatus {
	private String notificationTypeName;
	private boolean notificationEmailStatus;
	private List<String> emailList;
	
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
	
	public List<String> getEmailList() {
		return emailList;
	}
	public void setEmailList(List<String> emailList) {
		this.emailList = emailList;
	}
}
