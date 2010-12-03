package ee.adit.pojo;

import java.util.Date;
import java.util.List;

public class DocumentSendingData {
    private Date sentTime;
    private List<DocumentSendingRecipient> userList;

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public List<DocumentSendingRecipient> getUserList() {
        return userList;
    }

    public void setUserList(List<DocumentSendingRecipient> userList) {
        this.userList = userList;
    }
}
