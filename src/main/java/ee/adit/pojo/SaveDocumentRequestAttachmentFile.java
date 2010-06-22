package ee.adit.pojo;

public class SaveDocumentRequestAttachmentFile {

	private Integer id;
	
	private String name;
	
	private String contentType;
	
	private String description;
	
	private Integer sizeBytes;
	
	private String tmpFileName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTmpFileName() {
		return tmpFileName;
	}

	public void setTmpFileName(String tmpFileName) {
		this.tmpFileName = tmpFileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Integer getSizeBytes() {
		return sizeBytes;
	}

	public void setSizeBytes(Integer sizeBytes) {
		this.sizeBytes = sizeBytes;
	}

}
