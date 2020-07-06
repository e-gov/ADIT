package ee.adit.service.dhx;

import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;

public class RuuterDhxProcessingErrorRequest {

    private String senderOrganisationCode;
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

    public void setContainerVer2_1(ContainerVer2_1 containerVer2_1) {
        this.containerVer2_1 = containerVer2_1;
    }
}
