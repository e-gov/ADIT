package ee.adit.pojo;

import java.util.List;

public class GetUserContactsResponseAttachment {

    private Integer total;

    private List<GetUserContactsResponseAttachmentUserContact> userContacts;

    public List<GetUserContactsResponseAttachmentUserContact> getUserContacts() {
        return userContacts;
    }

    public void setUsers(List<GetUserContactsResponseAttachmentUserContact> userContacts) {
        this.userContacts = userContacts;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

}
