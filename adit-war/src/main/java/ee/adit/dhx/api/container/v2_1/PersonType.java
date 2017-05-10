package ee.adit.dhx.api.container.v2_1;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class PersonType {
    private String name;
    private String givenName;
    private String surname;
    private String personalIdCode;
    private String residency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPersonalIdCode() {
        return personalIdCode;
    }

    public void setPersonalIdCode(String personalIdCode) {
        this.personalIdCode = personalIdCode;
    }

    public String getResidency() {
        return residency;
    }

    public void setResidency(String residency) {
        this.residency = residency;
    }
}
