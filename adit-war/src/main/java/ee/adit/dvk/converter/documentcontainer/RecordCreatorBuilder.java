package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.ContactDataType;
import dvk.api.container.v2_1.OrganisationType;
import dvk.api.container.v2_1.PersonType;
import dvk.api.container.v2_1.RecordCreator;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.pojo.PersonName;
import ee.adit.service.UserService;
import ee.adit.util.Util;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class RecordCreatorBuilder {
    private Document document;
    private AditUserDAO aditUserDAO;

    /**
     * Constructor.
     * @param document - {@link Document}
     * @param aditUserDAO - {@link AditUserDAO}
     */
    public RecordCreatorBuilder(final Document document, final AditUserDAO aditUserDAO) {
        this.document = document;
        this.aditUserDAO = aditUserDAO;
    }

    /**
     * Build the {@link RecordCreator}.
     * @return recordCreator
     */
    public RecordCreator build() {
        RecordCreator recordCreator = new RecordCreator();

        AditUser documentOwner = aditUserDAO.getUserByID(document.getCreatorCode());

        //organization
        OrganisationType organisation = new OrganisationType();
        if (documentOwner != null) {
            organisation.setName(documentOwner.getFullName());
            organisation.setOrganisationCode(Util.getPersonalIdCodeWithoutCountryPrefix(documentOwner.getUserCode()));
        }
        recordCreator.setOrganisation(organisation);

        recordCreator.setPerson(getPersonType(documentOwner));

        ContactDataType contactDataType = new ContactDataType();

        return recordCreator;
    }

    private PersonType getPersonType(final AditUser documentOwner) {
        PersonType personType = new PersonType();

        if (documentOwner != null) {
            personType.setName(documentOwner.getFullName());

            PersonName ownersName = null;
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
        }
        return personType;
    }
}
