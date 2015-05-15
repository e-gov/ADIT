package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.*;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.pojo.PersonName;
import ee.adit.service.UserService;
import ee.adit.util.Util;

/**
 * @author Hendrik Pärna
 * @since 7.05.14
 */
public class ContactInfoBuilder {

    private Document document;
    private AditUser aditUser;

    /**
     * Constructor.
     *
     * @param document    {@link Document}
     * @param aditUser {@link AditUser}
     */
    public ContactInfoBuilder(final Document document, final AditUser aditUser) {
        this.document = document;
        this.aditUser = aditUser;
    }

    /**
     * Builds a {@link ContactInfo}.
     *
     * @return ContactInfo
     */
    public ContactInfo build() {
        ContactInfo contactInfo = new ContactInfo();

        if (aditUser.isPerson()) {
            contactInfo.setPerson(createPersonType(aditUser));
        } else {
            contactInfo.setOrganisation(createOrganizationType(aditUser));
        }

        ContactDataType contactDataType = new ContactDataType();
        contactInfo.setContactData(contactDataType);

        return contactInfo;
    }

    private OrganisationType createOrganizationType(final AditUser documentOwner) {
        OrganisationType organisation = new OrganisationType();
        organisation.setName(documentOwner.getFullName());
        organisation.setOrganisationCode(documentOwner.getDvkOrgCode());
        organisation.setResidency(documentOwner.getUserCode().substring(0, 2));
        return organisation;
    }

    private PersonType createPersonType(final AditUser documentOwner) {
        PersonType personType = new PersonType();

        personType.setName(documentOwner.getFullName());

        PersonName ownersName;

        if (UserService.USERTYPE_PERSON.equalsIgnoreCase(documentOwner.getUsertype().getShortName())) {
            ownersName = Util.splitPersonName(documentOwner.getFullName());
        } else {
            ownersName = Util.splitPersonName(document.getCreatorUserName());
        }

        if (ownersName != null) {
            personType.setGivenName(ownersName.getFirstName());
            personType.setSurname(ownersName.getSurname());
        }

        personType.setPersonalIdCode(Util.getPersonalIdCodeWithoutCountryPrefix(documentOwner.getUserCode()));
        personType.setResidency(documentOwner.getUserCode().substring(0, 2));
        return personType;
    }

}
