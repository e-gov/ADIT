package ee.adit.pojo;

import java.util.Date;

public class DocumentSendingRecipient {
    private String code;
    private String name;
    private String email;
    private Boolean hasBeenViewed;
    private Date openedTime;
    private Long workflowStatusId;
    private Long dvkStatusId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getHasBeenViewed() {
        return hasBeenViewed;
    }

    public void setHasBeenViewed(Boolean hasBeenViewed) {
        this.hasBeenViewed = hasBeenViewed;
    }

    public Date getOpenedTime() {
        return openedTime;
    }

    public void setOpenedTime(Date openedTime) {
        this.openedTime = openedTime;
    }

    public Long getWorkflowStatusId() {
        return workflowStatusId;
    }

    public void setWorkflowStatusId(Long workflowStatusId) {
        this.workflowStatusId = workflowStatusId;
    }

    public Long getDvkStatusId() {
        return dvkStatusId;
    }

    public void setDvkStatusId(Long dvkStatusId) {
        this.dvkStatusId = dvkStatusId;
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
