package ee.adit.pojo;

import java.util.List;


import ee.adit.pojo.DocumentSharingRecipientStatus;

public class GetSendStatusResponseAttachment {
    private List<DocumentSendStatus> documents;

	public List<DocumentSendStatus> getDocuments() {
		return documents;
	}

	public void setDocuments(List<DocumentSendStatus> documents) {
		this.documents = documents;
	}
    


}