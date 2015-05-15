package ee.adit.pojo;

public class OutputDocumentFileMonitor {
	
	private Long id;
	private String name;
	private String contentType;
	private String description;
	private Long sizeBytes;
	private String data;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getSizeBytes() {
		return sizeBytes;
	}

	public void setSizeBytes(Long sizeBytes) {
		this.sizeBytes = sizeBytes;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	/*
	public byte[] getData() {
		byte[] result = new byte[]{};
		
		if ((this.tmpFileName == null) || (this.tmpFileName.length() < 1)) {
			return result;
		}
		
		if (!(new File(this.tmpFileName)).exists()) {
			return result;
		}
		
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(this.tmpFileName);
			result = new byte[(int)(new File(this.tmpFileName)).length()];
			inStream.read(result);
		} catch (IOException ex) {
			
		} finally {
			if (inStream != null) {
				try { inStream.close(); }
				catch (Exception ex) {}
				inStream = null;
			}
		}
		
		return result;
	}
	*/
}
