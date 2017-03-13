package ee.adit.dhx.api.container.v2_1;

/**
 * @author Hendrik Pärna
 * @since 28.01.14
 */
public class Recipient extends ContactInfo {
    private String messageForRecipient;
    private String recipientRecordOriginalIdentifier;
    private String recipientRecordGuid;

    public String getMessageForRecipient() {
        return messageForRecipient;
    }

    public void setMessageForRecipient(String messageForRecipient) {
        this.messageForRecipient = messageForRecipient;
    }

    public String getRecipientRecordOriginalIdentifier() {
        return recipientRecordOriginalIdentifier;
    }

    public void setRecipientRecordOriginalIdentifier(String recipientRecordOriginalIdentifier) {
        this.recipientRecordOriginalIdentifier = recipientRecordOriginalIdentifier;
    }

    public String getRecipientRecordGuid() {
        return recipientRecordGuid;
    }

    public void setRecipientRecordGuid(String recipientRecordGuid) {
        this.recipientRecordGuid = recipientRecordGuid;
    }
}
