package ee.adit.pojo;

import java.util.List;

public class SaveDocumentRequestAttachment {

	private Integer id;
	
	private String guid;
	
	private String title;
	
	private String documentType;
	
	private Integer previousDocumentID;
	
	private List<SaveDocumentRequestAttachmentFile> files;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	public List<SaveDocumentRequestAttachmentFile> getFiles() {
		return files;
	}

	public void setFiles(List<SaveDocumentRequestAttachmentFile> files) {
		this.files = files;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public Integer getPreviousDocumentID() {
		return previousDocumentID;
	}

	public void setPreviousDocumentID(Integer previousDocumentID) {
		this.previousDocumentID = previousDocumentID;
	}
	
}
