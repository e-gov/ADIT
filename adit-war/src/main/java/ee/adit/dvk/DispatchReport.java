package ee.adit.dvk;

import java.io.Serializable;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;

public class DispatchReport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean success;
	
	private Document document;
	
	private AditUser recipient;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public AditUser getRecipient() {
		return recipient;
	}

	public void setRecipient(AditUser recipient) {
		this.recipient = recipient;
	}

}
