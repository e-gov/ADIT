package ee.adit.pojo;

import java.util.List;

public class DocumentSharingData {
	private List<DocumentSharingRecipient> userList;

	public List<DocumentSharingRecipient> getUserList() {
		return userList;
	}

	public void setUserList(List<DocumentSharingRecipient> userList) {
		this.userList = userList;
	}
}
