package ee.adit.dvk.converter.containerdocument;

import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.DecRecipient;
import dvk.api.container.v2_1.Recipient;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.util.Configuration;
import ee.adit.util.Util;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hendrik Pärna
 * @since 10.06.14
 */
public class RecipientsBuilder {
    private static Logger logger = Logger.getLogger(RecipientsBuilder.class);
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
    public List<Pair<AditUser, String>> build() {
        List<Pair<AditUser, String>> allRecipients = new ArrayList<Pair<AditUser, String>>();

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
                        // Check if user uses DVK
                        if (!Util.isNullOrEmpty(user.getDvkOrgCode())) {
                            // The user uses DVK - this is not allowed.
                            // Users that use DVK have to exchange documents with
                            // other users that use DVK, over DVK.
                            throw new IllegalStateException("User uses DVK - not allowed.");
                        } else {
                            allRecipients.add(new Pair<AditUser, String>() {
                                @Override
                                public AditUser getLeft() {
                                    return user;
                                }

                                @Override
                                public String getRight() {
                                    return findMessageForRecipientFromContainer(recipient, container);
                                }

                                @Override
                                public String setValue(final String value) {
                                    return null;
                                }
                            });
                        }
                    } else {
                        throw new IllegalStateException("User not found. Personal code: " + recipient.getPersonalIdCode());
                    }
                }
            }
        }

        return allRecipients;
    }

    private String findMessageForRecipientFromContainer(final DecRecipient decRecipient, final ContainerVer2_1 containerVer2_1) {
        String result = null;

        if (containerVer2_1.getRecipient() != null) {
            for (Recipient recipient: containerVer2_1.getRecipient()) {
                if (recipient.getPerson() != null
                        && recipient.getPerson().getPersonalIdCode().equalsIgnoreCase(decRecipient.getPersonalIdCode())) {
                    result = recipient.getMessageForRecipient();
                }
            }
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