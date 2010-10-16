package ee.adit.util;

public class MonitorConfiguration {

	private String aditServiceUrl;
	
	private String remoteApplicationShortName;
	
	private String userCode;
	
	private String institutionCode;
	
	private Long testDocumentId;
	
	private Long dvkTestDocumentID;
	
	private Long testDocumentFileId;
	
	private String testUserCode;
	
	private Long documentSaveInterval;
	
	private Long documentSendToDvkInterval;
	
	private Long documentSendToDvkServerInterval;
	
	private Long documentSendToDvkFromDvkServerInterval;
	
	private Long documentSendToAditInterval;
	
	private Long notificationSendInterval;
	
	private Long errorInterval;

	public String getAditServiceUrl() {
		return aditServiceUrl;
	}

	public void setAditServiceUrl(String aditServiceUrl) {
		this.aditServiceUrl = aditServiceUrl;
	}

	public String getRemoteApplicationShortName() {
		return remoteApplicationShortName;
	}

	public void setRemoteApplicationShortName(String remoteApplicationShortName) {
		this.remoteApplicationShortName = remoteApplicationShortName;
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public Long getTestDocumentId() {
		return testDocumentId;
	}

	public void setTestDocumentId(Long testDocumentId) {
		this.testDocumentId = testDocumentId;
	}

	public Long getTestDocumentFileId() {
		return testDocumentFileId;
	}

	public void setTestDocumentFileId(Long testDocumentFileId) {
		this.testDocumentFileId = testDocumentFileId;
	}

	public String getTestUserCode() {
		return testUserCode;
	}

	public void setTestUserCode(String testUserCode) {
		this.testUserCode = testUserCode;
	}

	public Long getDocumentSaveInterval() {
		return documentSaveInterval;
	}

	public void setDocumentSaveInterval(Long documentSaveInterval) {
		this.documentSaveInterval = documentSaveInterval;
	}

	public Long getDocumentSendToDvkInterval() {
		return documentSendToDvkInterval;
	}

	public void setDocumentSendToDvkInterval(Long documentSendToDvkInterval) {
		this.documentSendToDvkInterval = documentSendToDvkInterval;
	}

	public Long getDocumentSendToDvkServerInterval() {
		return documentSendToDvkServerInterval;
	}

	public void setDocumentSendToDvkServerInterval(
			Long documentSendToDvkServerInterval) {
		this.documentSendToDvkServerInterval = documentSendToDvkServerInterval;
	}

	public Long getDocumentSendToDvkFromDvkServerInterval() {
		return documentSendToDvkFromDvkServerInterval;
	}

	public void setDocumentSendToDvkFromDvkServerInterval(
			Long documentSendToDvkFromDvkServerInterval) {
		this.documentSendToDvkFromDvkServerInterval = documentSendToDvkFromDvkServerInterval;
	}

	public Long getDocumentSendToAditInterval() {
		return documentSendToAditInterval;
	}

	public void setDocumentSendToAditInterval(Long documentSendToAditInterval) {
		this.documentSendToAditInterval = documentSendToAditInterval;
	}

	public Long getNotificationSendInterval() {
		return notificationSendInterval;
	}

	public void setNotificationSendInterval(Long notificationSendInterval) {
		this.notificationSendInterval = notificationSendInterval;
	}

	public Long getErrorInterval() {
		return errorInterval;
	}

	public void setErrorInterval(Long errorInterval) {
		this.errorInterval = errorInterval;
	}

	public Long getDvkTestDocumentID() {
		return dvkTestDocumentID;
	}

	public void setDvkTestDocumentID(Long dvkTestDocumentID) {
		this.dvkTestDocumentID = dvkTestDocumentID;
	}

	public String getInstitutionCode() {
		return institutionCode;
	}

	public void setInstitutionCode(String institutionCode) {
		this.institutionCode = institutionCode;
	}
	
}
