package ee.adit.dvk.api.container.v2_1;

/**
 * @author Hendrik Pärna
 * @since 29.01.14
 */
public class File {
    private String fileGuid;
    private Boolean recordMainComponent;
    private String fileName;
    private Integer fileSize;
    private String zipBase64Content;
    private String mimeType;

    public String getFileGuid() {
        return fileGuid;
    }

    public void setFileGuid(String fileGuid) {
        this.fileGuid = fileGuid;
    }

    public Boolean getRecordMainComponent() {
        return recordMainComponent;
    }

    public void setRecordMainComponent(Boolean recordMainComponent) {
        this.recordMainComponent = recordMainComponent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getZipBase64Content() {
        return zipBase64Content;
    }

    public void setZipBase64Content(String zipBase64Content) {
        this.zipBase64Content = zipBase64Content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
