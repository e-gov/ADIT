package ee.adit.dvk.api.container.v2_1;

import java.util.Date;

/**
 * @author Hendrik Pärna
 * @since 28.01.14
 */
public class RecordMetadata {
    private String recordGuid;
    private String recordType;
    private String recordOriginalIdentifier;
    private Date recordDateRegistered;
    private String recordTitle;
    private String recordLanguage;
    private String recordAbstract;
    private Date replyDueDate;

    public String getRecordGuid() {
        return recordGuid;
    }

    public void setRecordGuid(String recordGuid) {
        this.recordGuid = recordGuid;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getRecordOriginalIdentifier() {
        return recordOriginalIdentifier;
    }

    public void setRecordOriginalIdentifier(String recordOriginalIdentifier) {
        this.recordOriginalIdentifier = recordOriginalIdentifier;
    }

    public String getRecordTitle() {
        return recordTitle;
    }

    public void setRecordTitle(String recordTitle) {
        this.recordTitle = recordTitle;
    }

    public String getRecordLanguage() {
        return recordLanguage;
    }

    public void setRecordLanguage(String recordLanguage) {
        this.recordLanguage = recordLanguage;
    }

    public String getRecordAbstract() {
        return recordAbstract;
    }

    public void setRecordAbstract(String recordAbstract) {
        this.recordAbstract = recordAbstract;
    }

    public Date getReplyDueDate() {
        return replyDueDate;
    }

    public void setReplyDueDate(Date replyDueDate) {
        this.replyDueDate = replyDueDate;
    }

    public Date getRecordDateRegistered() {
        return recordDateRegistered;
    }

    public void setRecordDateRegistered(Date recordDateRegistered) {
        this.recordDateRegistered = recordDateRegistered;
    }
}
