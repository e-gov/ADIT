package ee.adit.pojo;

public class OutputDocumentFile implements Comparable<OutputDocumentFile> {
    private Long id;
    private String name;
    private String contentType;
    private String description;
    private Long sizeBytes;
    private String sysTempFile;
    private String fileType;
    private String ddocDataFileId;
    private Long ddocDataFileStartOffset;
    private Long ddocDataFileEndOffset;

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

    public String getSysTempFile() {
        return sysTempFile;
    }

    public void setSysTempFile(String sysTempFile) {
        this.sysTempFile = sysTempFile;
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

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	public String getDdocDataFileId() {
		return ddocDataFileId;
	}

	public void setDdocDataFileId(String ddocDataFileId) {
		this.ddocDataFileId = ddocDataFileId;
	}

	public Long getDdocDataFileStartOffset() {
		return ddocDataFileStartOffset;
	}

	public void setDdocDataFileStartOffset(Long ddocDataFileStartOffset) {
		this.ddocDataFileStartOffset = ddocDataFileStartOffset;
	}

	public Long getDdocDataFileEndOffset() {
		return ddocDataFileEndOffset;
	}

	public void setDdocDataFileEndOffset(Long ddocDataFileEndOffset) {
		this.ddocDataFileEndOffset = ddocDataFileEndOffset;
	}

	/**
	 * Compares {@link OutputDocumentFile} instances by DigiDoc DataFile start offset.
	 */
    public int compareTo(OutputDocumentFile compareObject) {
        if (getDdocDataFileStartOffset() < compareObject.getDdocDataFileStartOffset())
            return -1;
        else if (getDdocDataFileStartOffset() == compareObject.getDdocDataFileStartOffset())
            return 0;
        else
            return 1;
    }
}
