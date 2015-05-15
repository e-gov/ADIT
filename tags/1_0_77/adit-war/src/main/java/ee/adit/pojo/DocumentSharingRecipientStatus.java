package ee.adit.pojo;

import java.util.Date;

public class DocumentSharingRecipientStatus {
    private String code;
    private String name;
    private Boolean hasBeenViewed;
    private Date openedTime;

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
}
