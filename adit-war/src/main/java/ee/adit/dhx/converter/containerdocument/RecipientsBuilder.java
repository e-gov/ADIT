package ee.adit.dhx.converter.containerdocument;

import java.util.ArrayList;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.DecRecipient;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.util.Configuration;
import ee.adit.util.Util;

/**
 * @author Hendrik Pärna
 * @since 10.06.14
 */
public class RecipientsBuilder {
    private static Logger logger = LogManager.getLogger(RecipientsBuilder.class);
    private ContainerVer2_1 container;
    private Configuration configuration;
    private AditUserDAO aditUserDAO;

    /**
     * Constructor.
     * @param container 2.1 container version
     */
    public RecipientsBuilder(final ContainerVer2_1 container) {
       this.container = container;
    }

    /**
     * Build a list of recipients.
     * @return recipients
     */
    public List<Pair<AditUser, Recipient>> build() {
        List<Pair<AditUser, Recipient>> allRecipients = new ArrayList<Pair<AditUser, Recipient>>();

        // For every recipient - check if registered in ADIT
        for (final DecRecipient recipient: container.getTransport().getDecRecipient()) {
            // First of all make sure that this recipient is supposed to be found in ADIT
            if (!Util.isNullOrEmpty(recipient.getOrganisationCode())
                    && getConfiguration().getDvkOrgCode().equalsIgnoreCase(recipient.getOrganisationCode())) {

                logger.info("Recipient: " + recipient.getOrganisationCode()
                        + " Isikukood: '" + recipient.getPersonalIdCode() + "'.");

                // The ADIT internal recipient is always marked by
                // the field <isikukood> in the DVK container,
                // regardless if it is actually a person or an
                // institution / company.
                if (!Util.isNullOrEmpty(recipient.getOrganisationCode())
                        && !Util.isNullOrEmpty(recipient.getPersonalIdCode())) {
                    // The recipient is specified - check if it's a DVK user
                    String personalIdCodeWithCountryPrefix = recipient.getPersonalIdCode().trim();
                    if (!personalIdCodeWithCountryPrefix.startsWith("EE")) {
                        personalIdCodeWithCountryPrefix = "EE" + personalIdCodeWithCountryPrefix;
                    }

                    logger.debug("Getting AditUser by personal code: " + personalIdCodeWithCountryPrefix);
                    final AditUser user = this.getAditUserDAO().getUserByID(personalIdCodeWithCountryPrefix);

                    if (user != null && user.getActive()) {
                        // Check if user uses DHX
                        if (!Util.isNullOrEmpty(user.getDvkOrgCode())) {
                            // The user uses DHX - this is not allowed.
                            // Users that use DVK have to exchange documents with
                            // other users that use DVK, over DVK.
                            throw new IllegalStateException("User uses DHX - not allowed.");
                        } else {
                            final Recipient recipientFromContainer = findAppropriateRecipientFromContainer(recipient, container);

                            if (recipientFromContainer != null) {
                                allRecipients.add(new Pair<AditUser, Recipient>() {
                                    @Override
                                    public AditUser getLeft() {
                                        return user;
                                    }

                                    @Override
                                    public Recipient getRight() {
                                        return recipientFromContainer;
                                    }

                                    @Override
                                    public Recipient setValue(final Recipient value) {
                                        return null;
                                    }
                                });
                            }
                        }
                    } else {
                        throw new IllegalStateException("User not found. Personal code: " + recipient.getPersonalIdCode());
                    }
                }
            }
        }

        return allRecipients;
    }

    private Recipient findAppropriateRecipientFromContainer(final DecRecipient decRecipient, final ContainerVer2_1 containerVer2_1) {
        Recipient result = null;

        if (containerVer2_1.getRecipient() != null) {
            result = containerVer2_1.getRecipient(decRecipient);
        }

        return result;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    public AditUserDAO getAditUserDAO() {
        return aditUserDAO;
    }

    public void setAditUserDAO(final AditUserDAO aditUserDAO) {
        this.aditUserDAO = aditUserDAO;
    }
}
