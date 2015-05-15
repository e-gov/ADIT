package ee.adit.pojo;

import java.util.Date;
import java.util.List;

public class OutputDocument {
    private Long id;
    private String guid;
    private String title;
    private String folder;
    private Boolean hasBeenViewed;
    private String documentType;
    private String creatorCode;
    private String creatorName;
    private String creatorUserCode;
    private String creatorUserName;
    private Date created;
    private String creatorApplication;
    private Date lastModified;
    private Long dvkStatusId;
    private Long dvkId;
    private Long workflowStatusId;
    private Date lastAccessed;
    private Long previousDocumentId;
    private String previousDocumentGuid;
    private Boolean locked;
    private Date lockingDate;
    private Boolean signable;
    private Boolean deflated;
    private Date deflatingDate;
    private List<Signature> signatures;
    private DocumentSendingData sentTo;
    private DocumentSharingData sharedTo;
    private OutputDocumentFilesList files;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public Boolean getHasBeenViewed() {
		return hasBeenViewed;
	}

	public void setHasBeenViewed(Boolean hasBeenViewed) {
		this.hasBeenViewed = hasBeenViewed;
	}

	public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getCreatorCode() {
        return creatorCode;
    }

    public void setCreatorCode(String creatorCode) {
        this.creatorCode = creatorCode;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorUserCode() {
        return creatorUserCode;
    }

    public void setCreatorUserCode(String creatorUserCode) {
        this.creatorUserCode = creatorUserCode;
    }

    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreatorApplication() {
        return creatorApplication;
    }

    public void setCreatorApplication(String creatorApplication) {
        this.creatorApplication = creatorApplication;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Long getDvkStatusId() {
        return dvkStatusId;
    }

    public void setDvkStatusId(Long dvkStatusId) {
        this.dvkStatusId = dvkStatusId;
    }

    public Long getDvkId() {
        return dvkId;
    }

    public void setDvkId(Long dvkId) {
        this.dvkId = dvkId;
    }

    public Long getWorkflowStatusId() {
        return workflowStatusId;
    }

    public void setWorkflowStatusId(Long workflowStatusId) {
        this.workflowStatusId = workflowStatusId;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Long getPreviousDocumentId() {
        return previousDocumentId;
    }

    public void setPreviousDocumentId(Long previousDocumentId) {
        this.previousDocumentId = previousDocumentId;
    }

    public String getPreviousDocumentGuid() {
        return previousDocumentGuid;
    }

    public void setPreviousDocumentGuid(String previousDocumentGuid) {
        this.previousDocumentGuid = previousDocumentGuid;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Date getLockingDate() {
        return lockingDate;
    }

    public void setLockingDate(Date lockingDate) {
        this.lockingDate = lockingDate;
    }

    public Boolean getSignable() {
        return signable;
    }

    public void setSignable(Boolean signable) {
        this.signable = signable;
    }

    public Boolean getDeflated() {
        return deflated;
    }

    public void setDeflated(Boolean deflated) {
        this.deflated = deflated;
    }

    public Date getDeflatingDate() {
        return deflatingDate;
    }

    public void setDeflatingDate(Date deflatingDate) {
        this.deflatingDate = deflatingDate;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<Signature> signatures) {
        this.signatures = signatures;
    }

    public DocumentSendingData getSentTo() {
        return sentTo;
    }

    public void setSentTo(DocumentSendingData sentTo) {
        this.sentTo = sentTo;
    }

    public DocumentSharingData getSharedTo() {
        return sharedTo;
    }

    public void setSharedTo(DocumentSharingData sharedTo) {
        this.sharedTo = sharedTo;
    }

    public OutputDocumentFilesList getFiles() {
        return files;
    }

    public void setFiles(OutputDocumentFilesList files) {
        this.files = files;
    }
}
