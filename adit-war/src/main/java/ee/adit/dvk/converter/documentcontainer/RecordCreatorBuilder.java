package ee.adit.dvk.converter.documentcontainer;

import ee.adit.dvk.api.container.v2_1.ContactInfo;
import ee.adit.dvk.api.container.v2_1.RecordCreator;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class RecordCreatorBuilder {
    private ContactInfoBuilder contactInfoBuilder;

    /**
     * Constructor.
     * @param document - {@link Document}
     * @param aditUserDAO - {@link AditUserDAO}
     */
    public RecordCreatorBuilder(final Document document, final AditUserDAO aditUserDAO) {
        AditUser documentOwner = aditUserDAO.getUserByID(document.getCreatorCode());
        this.contactInfoBuilder = new ContactInfoBuilder(document, documentOwner);
    }

    /**
     * Build the {@link RecordCreator}.
     * @return recordCreator
     */
    public RecordCreator build() {
        RecordCreator recordCreator = new RecordCreator();
        ContactInfo contactInfo = contactInfoBuilder.build();
        recordCreator.setContactData(contactInfo.getContactData());
        recordCreator.setOrganisation(contactInfo.getOrganisation());
        recordCreator.setPerson(contactInfo.getPerson());
        return recordCreator;
    }
}
