package ee.adit.pojo;

import java.util.List;

public class GetUserInfoResponseAttachment {

    private List<GetUserInfoResponseAttachmentUser> userList;

    public List<GetUserInfoResponseAttachmentUser> getUserList() {
        return userList;
    }

    public void setUserList(List<GetUserInfoResponseAttachmentUser> userList) {
        this.userList = userList;
    }

}
