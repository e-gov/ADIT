package ee.adit.util;

/**
 * Monitor component configuration class.
 *  
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class MonitorConfiguration {

    /**
     * ADIT web-service URL.
     */
    private String aditServiceUrl;

    /**
     * Remote application name used for testing.
     */
    private String remoteApplicationShortName;

    /**
     * XTee user code used for testing.
     */
    private String userCode;

    /**
     * XTee Institution code.
     */
    private String institutionCode;

    /**
     * Test document ID.
     */
    private Long testDocumentId;


    /**
     * DVK test documendi kaust.
     */
    private String dvkFolder;

	/**
     * Test document file ID.
     */
    private Long testDocumentFileId;

    /**
     * Test user code.
     */
    private String testUserCode;

    /**
     * Document save check interval.
     */
    private Long documentSaveInterval;

    /**
     * Document send to DVK check interval.
     */
    private Long documentSendToDhxInterval;

    /**
     * Document send to ADIT check interval.
     */
    private Long documentSendToAditInterval;

    /**
     * Notification send interval.
     */
    private Long notificationSendInterval;

    /**
     * Check error interval.
     */
    private Long errorInterval;

    /**
     * Check error level.
     */
    private String errorLevel;

    /**
     * Get ADIT web-service URL.
     * @return ADIT web-service URL
     */
    public String getAditServiceUrl() {
        return aditServiceUrl;
    }

    /**
     * Set ADIT web-service URL.
     * @param aditServiceUrl ADIT web-service URL
     */
    public void setAditServiceUrl(String aditServiceUrl) {
        this.aditServiceUrl = aditServiceUrl;
    }

    /**
     * Remote application name.
     * @return remote application name
     */
    public String getRemoteApplicationShortName() {
        return remoteApplicationShortName;
    }

    /**
     * Remote application name.
     * @param remoteApplicationShortName remote application name
     */
    public void setRemoteApplicationShortName(String remoteApplicationShortName) {
        this.remoteApplicationShortName = remoteApplicationShortName;
    }

    /**
     * Get user code.
     * @return user code
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * Set user code.
     * @param userCode user code
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    /**
     * Get test document ID.
     * @return test document ID
     */
    public Long getTestDocumentId() {
        return testDocumentId;
    }

    /**
     * Set test document ID.
     * @param testDocumentId test document ID
     */
    public void setTestDocumentId(Long testDocumentId) {
        this.testDocumentId = testDocumentId;
    }

    /**
     * Get test document file ID.
     * @return test document file ID
     */
    public Long getTestDocumentFileId() {
        return testDocumentFileId;
    }

    /**
     * Set test document file ID.
     * @param testDocumentFileId test document file ID
     */
    public void setTestDocumentFileId(Long testDocumentFileId) {
        this.testDocumentFileId = testDocumentFileId;
    }

    /**
     * Get test user code.
     * @return test user code
     */
    public String getTestUserCode() {
        return testUserCode;
    }

    /**
     * Set test user code.
     * @param testUserCode test user code
     */
    public void setTestUserCode(String testUserCode) {
        this.testUserCode = testUserCode;
    }

    /**
     * Get document save interval.
     * @return document save interval
     */
    public Long getDocumentSaveInterval() {
        return documentSaveInterval;
    }

    /**
     * Set document save interval.
     * @param documentSaveInterval document save interval
     */
    public void setDocumentSaveInterval(Long documentSaveInterval) {
        this.documentSaveInterval = documentSaveInterval;
    }

    /**
     * Get document send to DVK interval.
     * @return document send to DVK interval
     */
    public Long getDocumentSendToDhxInterval() {
        return documentSendToDhxInterval;
    }

    /**
     * Set document send to DVK interval.
     * @param documentSendToDvkInterval document send to DVK interval
     */
    public void setDocumentSendToDhxInterval(Long documentSendToDhxInterval) {
        this.documentSendToDhxInterval = documentSendToDhxInterval;
    }
    
    /**
     * Get document send to ADIT interval.
     * @return document send to ADIT interval
     */
    public Long getDocumentSendToAditInterval() {
        return documentSendToAditInterval;
    }

    /**
     * Set document send to ADIT interval.
     * @param documentSendToAditInterval document send to ADIT interval
     */
    public void setDocumentSendToAditInterval(Long documentSendToAditInterval) {
        this.documentSendToAditInterval = documentSendToAditInterval;
    }

    /**
     * Get notification send interval.
     * @return
     *     Notification send interval in milliseconds
     */
    public Long getNotificationSendInterval() {
        return notificationSendInterval;
    }

    /**
     * Set notification send interval.
     * @param notificationSendInterval notification send interval
     */
    public void setNotificationSendInterval(Long notificationSendInterval) {
        this.notificationSendInterval = notificationSendInterval;
    }

    /**
     * Get error inverval.
     * @return error inverval
     */
    public Long getErrorInterval() {
        return errorInterval;
    }

    /**
     * Set error inverval.
     * @param errorInterval error inverval
     */
    public void setErrorInterval(Long errorInterval) {
        this.errorInterval = errorInterval;
    }

    /**
     * Get DVK test document kausta nimi.
     * @return DVK document folder
     */
    public String getDvkFolder() {
		return dvkFolder;
	}

    /**
     * Set DVK test document kausta nimi.
     * @param dvkFolder DVK test document folder name
     */
	public void setDvkFolder(String dvkFolder) {
		this.dvkFolder = dvkFolder;
	}
    /**
     * Get institution code.
     * @return institution code
     */
    public String getInstitutionCode() {
        return institutionCode;
    }

    /**
     * Set institution code.
     * @param institutionCode institution code
     */
    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    /**
     * Get error level.
     * @return error level
     */
    public String getErrorLevel() {
        return errorLevel;
    }

    /**
     * Set error level.
     * @param errorLevel error level
     */
    public void setErrorLevel(String errorLevel) {
        this.errorLevel = errorLevel;
    }

}
