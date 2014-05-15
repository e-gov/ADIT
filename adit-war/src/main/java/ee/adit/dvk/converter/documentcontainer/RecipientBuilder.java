package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.ContactDataType;
import dvk.api.container.v2_1.OrganisationType;
import dvk.api.container.v2_1.PersonType;
import dvk.api.container.v2_1.Recipient;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.pojo.PersonName;
import ee.adit.service.UserService;
import ee.adit.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class RecipientBuilder {
    private Document document;
    private List<AditUser> recipients;

    /**
     * Constructor.
     * @param document {@link Document}
     * @param recipients list of {@link AditUser}
     */
    public RecipientBuilder(final Document document, final List<AditUser> recipients) {
        this.document = document;
        this.recipients = recipients;
    }

    /**
     * List of {@link Recipient}.
     *
     * @return recipients
     */
    public List<Recipient> build() {
        List<Recipient> results = new ArrayList<Recipient>();

        if (recipients != null) {
            for (AditUser aditUser : recipients) {
                Recipient recipient = new Recipient();
                OrganisationType organization = new OrganisationType();
                PersonType personType = new PersonType();

                if (UserService.USERTYPE_PERSON.equalsIgnoreCase(aditUser.getUsertype().getShortName())) {
                    PersonName personName = Util.splitPersonName(aditUser.getFullName());
                    personType.setGivenName(personName.getFirstName());
                    personType.setSurname(personName.getSurname());
                    personType.setName(aditUser.getFullName());
                    personType.setPersonalIdCode(aditUser.getUserCode());
                } else {
                    organization.setName(aditUser.getFullName());
                }

                recipient.setOrganisation(organization);
                recipient.setPerson(personType);

                ContactDataType contactDataType = new ContactDataType();
                recipient.setContactData(contactDataType);
            }
        }

        return results;
    }

}
