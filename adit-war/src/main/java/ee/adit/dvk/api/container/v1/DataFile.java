package ee.adit.dvk.api.container.v1;

/**
 * Contains data of DigiDoc containers DataFile element.
 *
 * @author Jaak Lember (jaak@millibitt.ee)
 */
public class DataFile {
    /**
     * Default XML namespace of DigiDoc container.
     */
    private static final String ddocNamesapce = "http://www.sk.ee/DigiDoc/v1.3.0#";

    /**
     * Name of data file (e.g. "Document1.doc").
     */
    private String fileName;

    /**
     * Indicates how the file is stored in DigiDoc container
     * (e.g. EMBEDDED_BASE64).
     */
    private String fileContentType;

	/**
	 * ID of file (e.g. "D1"). Unique within one container).
	 */
    private String fileId;

	/**
	 * MIME type of file (e.g "text/plain").
	 */
    private String fileMimeType;

	/**
	 * Size of file in bytes.
	 */
    private String fileSize;

	/**
	 * File contents as base64-encoded String.
	 */
    private String fileBase64Content;

	public boolean isContentNull() {
		return fileBase64Content == null;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileContentType() {
		return fileContentType;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileMimeType() {
		return fileMimeType;
	}

	public void setFileMimeType(String fileMimeType) {
		this.fileMimeType = fileMimeType;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileBase64Content() {
		return fileBase64Content;
	}

	public void setFileBase64Content(String fileBase64Content) {
		this.fileBase64Content = fileBase64Content;
	}

	public String getDdocNamespace() {
		return ddocNamesapce;
	}

	/**
	 * Dummy method for XML marshalling/unmarshalling.
	 * @param xmlns
	 *     XML namespace URI
	 */
	public void setDdocNamespace(String xmlns) {
	}
}
