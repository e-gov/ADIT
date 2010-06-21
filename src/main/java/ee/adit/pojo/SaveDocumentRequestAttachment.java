package ee.adit.pojo;

import java.util.List;

public class SaveDocumentRequestAttachment {

	private Integer id;
	
	private String guid;
	
	private String title;
	
	private String document_type;
	
	private Integer previous_document_it;
	
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

	public String getDocument_type() {
		return document_type;
	}

	public void setDocument_type(String documentType) {
		document_type = documentType;
	}

	public Integer getPrevious_document_it() {
		return previous_document_it;
	}

	public void setPrevious_document_it(Integer previousDocumentIt) {
		previous_document_it = previousDocumentIt;
	}

	public List<SaveDocumentRequestAttachmentFile> getFiles() {
		return files;
	}

	public void setFiles(List<SaveDocumentRequestAttachmentFile> files) {
		this.files = files;
	}
	
}
