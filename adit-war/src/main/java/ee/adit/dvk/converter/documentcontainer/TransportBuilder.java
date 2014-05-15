package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.DecRecipient;
import dvk.api.container.v2_1.DecSender;
import dvk.api.container.v2_1.Transport;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;
import ee.adit.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class TransportBuilder {

    private Document document;
    private AditUserDAO aditUserDAO;
    private Configuration configuration;

    /**
     * Constructor.
     * @param document {@link Document}
     * @param aditUserDAO {@link AditUserDAO}
     * @param configuration {@link Configuration}
     */
    public TransportBuilder(final Document document, final AditUserDAO aditUserDAO, final Configuration configuration) {
        this.document = document;
        this.aditUserDAO = aditUserDAO;
        this.configuration = configuration;
    }


    public Transport build() {
        Transport transport = new Transport();
        List<DecRecipient> recipients = new ArrayList<DecRecipient>();

        DecSender decSender = new DecSender();
        decSender.setOrganisationCode(Util.removeCountryPrefix(configuration.getDvkOrgCode()));
        decSender.setPersonalIdCode(Util.removeCountryPrefix(document.getCreatorCode()));

        AditUser sender = aditUserDAO.getUserByID(document.getCreatorCode());

        if (isAditUserNotAPerson(sender)) {
            decSender.setStructuralUnit(sender.getFullName());
        }
        transport.setDecSender(decSender);

        Set<DocumentSharing> documentSharings = document.getDocumentSharings();
        for (DocumentSharing documentSharing : documentSharings) {
            if (isDocumentReadyForSharing(documentSharing)) {
                AditUser recipient = aditUserDAO.getUserByID(documentSharing.getUserCode());
                DecRecipient decRecipient = new DecRecipient();
                decRecipient.setOrganisationCode(recipient.getDvkOrgCode());
                if (isAditUserNotAPerson(recipient)) {
                    decRecipient.setStructuralUnit(recipient.getFullName());
                } else {
                    decRecipient.setPersonalIdCode(Util.removeCountryPrefix(recipient.getUserCode()));
                }
                recipients.add(decRecipient);
            }
        }
        transport.setDecRecipient(recipients);

        return transport;
    }

    private boolean isAditUserNotAPerson(final AditUser aditUser) {
        boolean result = false;
        if (aditUser.getUsertype() != null
                && !UserService.USERTYPE_PERSON.equalsIgnoreCase(aditUser.getUsertype().getShortName())) {
            result = true;
        }
        return result;
    }

    private boolean isDocumentReadyForSharing(final DocumentSharing documentSharing) {
        boolean result = false;
        if (DocumentService.SHARINGTYPE_SEND_DVK.equalsIgnoreCase(documentSharing.getDocumentSharingType())
                && (DocumentService.DVK_STATUS_WAITING.equals(documentSharing.getDocumentDvkStatus())
                || DocumentService.DVK_STATUS_MISSING.equals(documentSharing.getDocumentDvkStatus())
                || documentSharing.getDocumentDvkStatus() == null)) {
            result = true;
        }
        return result;
    }
}
