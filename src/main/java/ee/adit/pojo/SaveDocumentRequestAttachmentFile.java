package ee.adit.pojo;

public class SaveDocumentRequestAttachmentFile {

	private Integer id;
	
	private String name;
	
	private String content_type;
	
	private String description;
	
	private Integer size_bytes;
	
	private String data;

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

	public String getContent_type() {
		return content_type;
	}

	public void setContent_type(String contentType) {
		content_type = contentType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSize_bytes() {
		return size_bytes;
	}

	public void setSize_bytes(Integer sizeBytes) {
		size_bytes = sizeBytes;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
}
