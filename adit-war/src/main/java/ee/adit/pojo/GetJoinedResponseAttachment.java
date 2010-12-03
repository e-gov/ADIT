package ee.adit.pojo;

import java.util.List;

public class GetJoinedResponseAttachment {

    private Integer total;

    private List<GetJoinedResponseAttachmentUser> users;

    public List<GetJoinedResponseAttachmentUser> getUsers() {
        return users;
    }

    public void setUsers(List<GetJoinedResponseAttachmentUser> users) {
        this.users = users;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

}
