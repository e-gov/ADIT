package ee.adit.dao.pojo;

// Generated 21.06.2010 14:02:03 by Hibernate Tools 3.2.4.GA

import java.util.Date;

import ee.adit.service.DocumentService;
import ee.adit.util.Util;

/**
 * DocumentFile generated by hbm2java
 */
public class DocumentFile implements java.io.Serializable {

    private static final long serialVersionUID = 1428423268886079325L;
    private long id;
    private Document document;
    private String guid;
    private String fileName;
    private String contentType;
    private String description;
    private byte[] fileData;
    private Long fileSizeBytes;
    private Boolean deleted;
    private long documentFileTypeId = DocumentService.FILETYPE_DOCUMENT_FILE;
    private String ddocDataFileId;
    private Long ddocDataFileStartOffset;
    private Long ddocDataFileEndOffset;
    private Boolean fileDataInDdoc;
    private Date lastModifiedDate = new Date();

    public DocumentFile() {
    }

    public DocumentFile(long id, Document document, String fileName) {
        this.id = id;
        this.document = document;
        this.fileName = fileName;
    }

    public DocumentFile(long id, Document document, String fileName, String contentType, String description,
    		byte[] fileData, Long fileSizeBytes, Boolean deleted, long documentFileTypeId, String ddocDataFileId,
            Long ddocDataFileStartOffset, Long ddocDataFileEndOffset, Boolean fileDataInDdoc,
            Date lastModifiedDate) {
        this.id = id;
        this.document = document;
        this.fileName = fileName;
        this.contentType = contentType;
        this.description = description;
        this.fileData = fileData;
        this.fileSizeBytes = fileSizeBytes;
        this.deleted = deleted;
        this.documentFileTypeId = documentFileTypeId;
        this.ddocDataFileId = ddocDataFileId;
        this.ddocDataFileStartOffset = ddocDataFileStartOffset;
        this.ddocDataFileEndOffset = ddocDataFileEndOffset;
        this.fileDataInDdoc = fileDataInDdoc;
        this.lastModifiedDate = lastModifiedDate;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Document getDocument() {
        return this.document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        // Prevent content type from being empty
        if (Util.isNullOrEmpty(this.contentType)) {
        	this.contentType = DocumentService.UNKNOWN_MIME_TYPE;
        }

    	return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;

        // Prevent content type from being empty
        if (Util.isNullOrEmpty(this.contentType)) {
        	this.contentType = DocumentService.UNKNOWN_MIME_TYPE;
        }
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getFileData() {
        return this.fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public Long getFileSizeBytes() {
        return this.fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

	public long getDocumentFileTypeId() {
		return documentFileTypeId;
	}

	public void setDocumentFileTypeId(long documentFileTypeId) {
		this.documentFileTypeId = documentFileTypeId;
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

	public Boolean getFileDataInDdoc() {
		return fileDataInDdoc;
	}

	public void setFileDataInDdoc(Boolean fileDataInDdoc) {
		this.fileDataInDdoc = fileDataInDdoc;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

}
