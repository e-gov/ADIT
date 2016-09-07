package ee.adit.dvk.converter.documentcontainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dvk.api.container.v2_1.ContactInfo;
import dvk.api.container.v2_1.Recipient;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class RecipientBuilder {
    private Set<DocumentSharing> documentSharings;
    private Document document;
    private AditUserDAO aditUserDAO;

    /**
     * Constructor.
     * @param document {@link Document}
     * @param aditUserDAO {@link AditUserDAO}
     * @param documentSharings list of {@link AditUser}
     */
    public RecipientBuilder(final Document document, final AditUserDAO aditUserDAO, final Set<DocumentSharing> documentSharings) {
        this.document = document;
        this.aditUserDAO = aditUserDAO;
        this.documentSharings = documentSharings;
    }

    /**
     * List of {@link Recipient}.
     *
     * @return recipients
     */
    public List<Recipient> build() {
        List<Recipient> results = new ArrayList<Recipient>();

        if (documentSharings != null) {
            for (DocumentSharing documentSharing : documentSharings) {
            	if (documentSharing.isDocumentReadySendToDvk()) {
	                AditUser recipientUser = aditUserDAO.getUserByID(documentSharing.getUserCode());
	                ContactInfo contactInfo = new ContactInfoBuilder(document, recipientUser).build();
	                Recipient recipient = new Recipient();
	                recipient.setPerson(contactInfo.getPerson());
	                recipient.setContactData(contactInfo.getContactData());
	                recipient.setOrganisation(contactInfo.getOrganisation());
	                recipient.setMessageForRecipient(document.getContent());
	                results.add(recipient);
            	}
            }
        }

        return results;
    }

}
