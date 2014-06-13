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
    public List<Pair<AditUser, Recipient>> build() {
        List<Pair<AditUser, Recipient>> allRecipients = new ArrayList<Pair<AditUser, Recipient>>();

        // For every recipient - check if registered in ADIT
        for (final Recipient recipient: container.getRecipient()) {
            // First of all make sure that this recipient is supposed to be found in ADIT
            if (recipient.getOrganisation() != null && !Util.isNullOrEmpty(recipient.getOrganisation().getOrganisationCode())
                    && getConfiguration().getDvkOrgCode().equalsIgnoreCase(recipient.getOrganisation().getOrganisationCode())) {

                logger.info("Recipient: " + recipient.getOrganisation().getOrganisationCode());
                if (recipient.getPerson() != null) {
                    logger.info("Isikukood: " + recipient.getPerson().getPersonalIdCode());
                }

                // The ADIT internal recipient is always marked by
                // the field <isikukood> in the DVK container,
                // regardless if it is actually a person or an
                // institution / company.
                if (!Util.isNullOrEmpty(recipient.getOrganisation().getOrganisationCode())
                        && recipient.getPerson() != null && !Util.isNullOrEmpty(recipient.getPerson().getPersonalIdCode())) {
                    // The recipient is specified - check if it's a DVK user
                    String personalIdCodeWithCountryPrefix = recipient.getPerson().getPersonalIdCode().trim();
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
                            allRecipients.add(new Pair<AditUser, Recipient>() {
                                @Override
                                public AditUser getLeft() {
                                    return user;
                                }

                                @Override
                                public Recipient getRight() {
                                    return recipient;
                                }

                                @Override
                                public Recipient setValue(final Recipient value) {
                                    throw new UnsupportedOperationException("No supported");
                                }
                            });
                        }
                    } else {
                        throw new IllegalStateException("User not found. Personal code: " + personalIdCodeWithCountryPrefix);
                    }
                }
            }
        }

        return allRecipients;
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
