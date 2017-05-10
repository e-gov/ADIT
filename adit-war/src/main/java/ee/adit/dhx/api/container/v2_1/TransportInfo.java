package ee.adit.dhx.api.container.v2_1;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class TransportInfo {
    private String organisationCode;
    private String structuralUnit;
    private String personalIdCode;

    public String getOrganisationCode() {
        return organisationCode;
    }

    public void setOrganisationCode(String organisationCode) {
        this.organisationCode = organisationCode;
    }

    public String getStructuralUnit() {
        return structuralUnit;
    }

    public void setStructuralUnit(String structuralUnit) {
        this.structuralUnit = structuralUnit;
    }

    public String getPersonalIdCode() {
        return personalIdCode;
    }

    public void setPersonalIdCode(String personalIdCode) {
        this.personalIdCode = personalIdCode;
    }
}
