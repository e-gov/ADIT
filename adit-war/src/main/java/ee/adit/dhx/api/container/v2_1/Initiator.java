package ee.adit.dhx.api.container.v2_1;

import java.util.Date;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class Initiator {
    private String initiatorRecordOriginalIdentifier;
    private Date initiatorRecordDate;
    private OrganisationType organisation;
    private PersonType person;
    private ContactDataType contactData;

    public String getInitiatorRecordOriginalIdentifier() {
        return initiatorRecordOriginalIdentifier;
    }

    public void setInitiatorRecordOriginalIdentifier(String initiatorRecordOriginalIdentifier) {
        this.initiatorRecordOriginalIdentifier = initiatorRecordOriginalIdentifier;
    }

    public Date getInitiatorRecordDate() {
        return initiatorRecordDate;
    }

    public void setInitiatorRecordDate(Date initiatorRecordDate) {
        this.initiatorRecordDate = initiatorRecordDate;
    }

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
