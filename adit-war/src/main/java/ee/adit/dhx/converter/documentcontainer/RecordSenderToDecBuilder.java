package ee.adit.dhx.converter.documentcontainer;


import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dhx.api.container.v2_1.ContactInfo;
import ee.adit.dhx.api.container.v2_1.RecordSenderToDec;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class RecordSenderToDecBuilder {
    private ContactInfoBuilder contactInfoBuilder;

    /**
     * Constructor.
     *
     * @param document    {@link ee.adit.dao.pojo.Document}
     * @param aditUserDAO {@link ee.adit.dao.AditUserDAO}
     */
    public RecordSenderToDecBuilder(final Document document, final AditUserDAO aditUserDAO) {
        AditUser documentOwner = aditUserDAO.getUserByID(document.getCreatorCode());
        this.contactInfoBuilder = new ContactInfoBuilder(document, documentOwner);
    }

    /**
     * Builds a {@link RecordSenderToDec}.
     *
     * @return recordSenderToDec
     */
    public RecordSenderToDec build() {
        ContactInfo contactInfo = contactInfoBuilder.build();
        RecordSenderToDec recordSenderToDec = new RecordSenderToDec();
        recordSenderToDec.setOrganisation(contactInfo.getOrganisation());
        recordSenderToDec.setContactData(contactInfo.getContactData());
        recordSenderToDec.setPerson(contactInfo.getPerson());
        return recordSenderToDec;
    }
}
