package ee.adit.dhx.api.container.v2_1;

/**
 * @author Hendrik Pärna
 * @since 28.01.14
 */
public class ContactInfo {
    private OrganisationType organisation;
    private PersonType person;
    private ContactDataType contactData;

    public OrganisationType getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationType organisation) {
        this.organisation = organisation;
    }

    public PersonType getPerson() {
        return person;
    }

    public void setPerson(PersonType person) {
        this.person = person;
    }

    public ContactDataType getContactData() {
        return contactData;
    }

    public void setContactData(ContactDataType contactData) {
        this.contactData = contactData;
    }
}
