package ee.adit.pojo;

import java.util.List;

public class SaveDocumentRequestAttachmentMonitor {

    private Long id;

    private String guid;

    private String title;

    private String documentType;

    private Long previousDocumentID;

    private List<OutputDocumentFileMonitor> files;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public List<OutputDocumentFileMonitor> getFiles() {
        return files;
    }

    public void setFiles(List<OutputDocumentFileMonitor> files) {
        this.files = files;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Long getPreviousDocumentID() {
        return previousDocumentID;
    }

    public void setPreviousDocumentID(Long previousDocumentID) {
        this.previousDocumentID = previousDocumentID;
    }

}
