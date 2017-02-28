package ee.adit.dvk.api.container.v2_1;

import java.util.Date;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class DecMetadata {
    private String decId;
    private String decFolder;
    private Date decReceiptDate;

    public String getDecId() {
        return decId;
    }

    public void setDecId(String decId) {
        this.decId = decId;
    }

    public String getDecFolder() {
        return decFolder;
    }

    public void setDecFolder(String decFolder) {
        this.decFolder = decFolder;
    }

    public Date getDecReceiptDate() {
        return decReceiptDate;
    }

    public void setDecReceiptDate(Date decReceiptDate) {
        this.decReceiptDate = decReceiptDate;
    }
}
