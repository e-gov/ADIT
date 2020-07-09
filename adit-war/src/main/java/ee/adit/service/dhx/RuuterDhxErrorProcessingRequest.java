package ee.adit.service.dhx;

import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;

import java.util.Objects;

public class RuuterDhxErrorProcessingRequest {

    private String senderOrganisationCode;
    private String recipientCode;
    private DhxRecipientUserType recipientUserType;
    private String recipientUserName;
    private DhxProcessingErrorType errorCode;
    // RecordMetadata.RecordType ADIT value (DocumentType.shortName)
    private String aditDocumentType;
    private ContainerVer2_1 document;

    public String getSenderOrganisationCode() {
        return senderOrganisationCode;
    }

    public void setSenderOrganisationCode(String senderOrganisationCode) {
        this.senderOrganisationCode = senderOrganisationCode;
    }

    public DhxProcessingErrorType getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(DhxProcessingErrorType errorCode) {
        this.errorCode = errorCode;
    }

    public ContainerVer2_1 getDocument() {
        return document;
    }

    /**
     * Sets container and parses out senderOrganisationCode
     */
    public void setDocument(ContainerVer2_1 document) {
        this.document = document;
        if (document != null && document.getTransport() != null && document.getTransport().getDecSender() != null)
            setSenderOrganisationCode(document.getTransport().getDecSender().getOrganisationCode());
    }

    public String getRecipientCode() {
        return recipientCode;
    }

    public void setRecipientCode(String recipientCode) {
        this.recipientCode = recipientCode;
    }

    public DhxRecipientUserType getRecipientUserType() {
        return recipientUserType;
    }

    public void setRecipientUserType(DhxRecipientUserType recipientUserType) {
        this.recipientUserType = recipientUserType;
    }

    public String getAditDocumentType() {
        return aditDocumentType;
    }

    public void setAditDocumentType(String aditDocumentType) {
        this.aditDocumentType = aditDocumentType;
    }

    public String getRecipientUserName() {
        return recipientUserName;
    }

    public void setRecipientUserName(String recipientUserName) {
        this.recipientUserName = recipientUserName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuuterDhxErrorProcessingRequest that = (RuuterDhxErrorProcessingRequest) o;
        return Objects.equals(senderOrganisationCode, that.senderOrganisationCode) &&
                Objects.equals(recipientCode, that.recipientCode) &&
                recipientUserType == that.recipientUserType &&
                Objects.equals(recipientUserName, that.recipientUserName) &&
                errorCode == that.errorCode &&
                document.equals(that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderOrganisationCode, recipientCode, recipientUserType, recipientUserName, errorCode, document);
    }
}
