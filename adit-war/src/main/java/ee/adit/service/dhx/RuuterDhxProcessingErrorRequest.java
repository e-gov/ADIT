package ee.adit.service.dhx;

import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;

public class RuuterDhxProcessingErrorRequest {

    private String senderOrganisationCode;
    private String recipientCode;
    private DhxRecipientUserType recipientUserType;
    private String recipientUserName;
    private DhxProcessingErrorType errorCode;
    private ContainerVer2_1 containerVer2_1;

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

    public ContainerVer2_1 getContainerVer2_1() {
        return containerVer2_1;
    }

    /**
     * Sets container and parses out senderOrganisationCode
     */
    public void setContainerVer2_1(ContainerVer2_1 containerVer2_1) {
        this.containerVer2_1 = containerVer2_1;
        if (containerVer2_1 != null && containerVer2_1.getTransport() != null && containerVer2_1.getTransport().getDecSender() != null)
            setSenderOrganisationCode(containerVer2_1.getTransport().getDecSender().getOrganisationCode());
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

    public String getRecipientUserName() {
        return recipientUserName;
    }

    public void setRecipientUserName(String recipientUserName) {
        this.recipientUserName = recipientUserName;
    }
}
