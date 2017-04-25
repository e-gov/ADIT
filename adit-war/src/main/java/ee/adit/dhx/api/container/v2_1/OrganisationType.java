package ee.adit.dhx.api.container.v2_1;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class OrganisationType {
    private String name;
    private String organisationCode;
    private String structuralUnit;
    private String positionTitle;
    private String residency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public String getResidency() {
        return residency;
    }

    public void setResidency(String residency) {
        this.residency = residency;
    }
}
