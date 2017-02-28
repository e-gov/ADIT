package ee.adit.dvk.api;

import java.util.Date;
import java.util.List;

import ee.adit.dvk.api.DVKAPI.DvkType;

public interface ISessionCacheBox {
    /**
     * If allowCreateNew boolean flag is true then looks for existing counter
     * entry and if it was found then will return it otherwise creates a new one.
     * If allowCreateNew boolean flag is false then will return an existing counter
     * if any was found otherwise null.
     *
     * @param dhlId          counter DHL id
     * @param allowCreateNew false if expected only existing entry or true if a new one can be created
     * @return {@link ICounter}
     */
    ICounter getCounter(Number dhlId, boolean allowCreateNew);

    /**
     * Returns only existing subdivision if any was found otherwise null.
     *
     * @param subdivisionCode subdivision code
     * @return {@link ISubdivision}
     */
    ISubdivision getSubdivision(Number subdivisionCode);

    /**
     * Returns only existing occupation if any was found otherwise null.
     *
     * @param occupationCode occupation code
     * @return {@link IOccupation}
     */
    IOccupation getOccupation(Number occupationCode);

    /**
     * If allowCreateNew boolean flag is true then looks for existing organization
     * entry and if it was found then will return it otherwise creates a new one.
     * If allowCreateNew boolean flag is false then will return an existing organization
     * if any was found otherwise null.
     *
     * @param orgCode        organization code
     * @param allowCreateNew false if expected only existing entry or true if a new one can be created
     * @return {@link IOrganization}
     */
    IOrganization getOrganization(String orgCode, boolean allowCreateNew);

    /**
     * Returns only existing organization if any was found otherwise null.
     *
     * @param orgName organization name
     * @return {@link IOrganization}
     */
    IOrganization getOrganizationByOrgName(String orgName);

    /**
     * Returns a list of existing organizations that are DHL capable.
     *
     * @param dhlCapable boolean flag
     * @return List of {@link IOrganization}
     */
    List<IOrganization> getOrganizationsByDHLCapable(boolean dhlCapable);

    /**
     * Returns a list of existing organizations that are DHL direct capable.
     *
     * @param dhlDirectCapable boolean flag
     * @return List of {@link IOrganization}
     */
    List<IOrganization> getOrganizationsByDHLDirectCapable(boolean dhlDirectCapable);

    /**
     * If allowCreateNew boolean flag is true then looks for existing setting
     * entry and if it was found then will return it otherwise creates a new one.
     * If allowCreateNew boolean flag is false then will return an existing setting
     * if any was found otherwise null.
     *
     * @param id             setting's ID
     * @param allowCreateNew false if expected only existing entry or true if a new one can be created
     * @return {@link ISetting}
     */
    ISetting getSetting(Number id, boolean allowCreateNew);

    /**
     * Returns a list of existing settings that have satisfying institution code.
     *
     * @param institutionCode institution code
     * @return List of {@link ISetting}
     */
    List<ISetting> getSettingsByInstitutionCode(String institutionCode);

    /**
     * Returns a list of existing settings that have satisfying personal ID code.
     *
     * @param personalIdCode institution code
     * @return List of {@link ISetting}
     */
    List<ISetting> getSettingsByPersonalIdCode(String personalIdCode);

    /**
     * Returns a list of existing settings that have satisfying unit ID.
     *
     * @param unitId unit ID
     * @return List of {@link ISetting}
     */
    List<ISetting> getSettingsByUnitId(Number unitId);

    /**
     * Returns only existing settings folder if any was found otherwise null.
     *
     * @param id settings folder ID
     * @return {@link ISettingsFolder}
     */
    ISettingsFolder getSettingsFolder(Number id);

    /**
     * Returns a list of existing settings folders that have satisfying folder's name.
     *
     * @param folderName folder's name
     * @return List of {@link ISettingsFolder}
     */
    List<ISettingsFolder> getSettingsFolderByFolderName(String folderName);

    /**
     * Returns only existing message if any was found otherwise null.
     *
     * @param dhlMessageId DHL message ID
     * @return {@link IMessage}
     */
    IMessage getMessage(Number dhlMessageId);

    /**
     * Creates a new DHL message.
     *
     * @return {@link IMessage}
     */
    IMessage createMessage();

    /**
     * Creates a new DHL message recipient with mandatory arguments which have to be supplied
     * with an instance of MessageRecipientCreateArgs class.
     *
     * @return {@link IMessageRecipient}
     */
    IMessageRecipient createMessageRecipient(MessageRecipientCreateArgs createArgs);

    /**
     * Returns only existing message recipient if any was found otherwise null.
     *
     * @param id message recipient's ID
     * @return {@link IMessageRecipient}
     */
    IMessageRecipient getMessageRecipient(Number id);

    /**
     * Returns a list of existing message recipients that have satisfying organization code.
     *
     * @param orgCode organization code
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsByOrgCode(String orgCode);

    /**
     * Returns a list of existing message recipients that have satisfying DHL message ID.
     *
     * @param dhlMessageId DHL message ID
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsByMessageId(Number dhlMessageId);

    /**
     * Returns a list of existing message recipients that have satisfying recipient's person code.
     *
     * @param recipientPersonCode recipient's person code
     * @return List of {@link IMessageRecipient}
     */
    IMessageRecipient getMessageRecipientByPersonCode(String recipientPersonCode);

    /**
     * Returns a list of existing message recipients that have satisfying organization's name.
     *
     * @param orgName organization's name
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsByOrgName(String orgName);

    /**
     * Returns only existing message recipient if any was found otherwise null.
     *
     * @param recipientName recipient's name
     * @return {@link IMessageRecipient}
     */
    IMessageRecipient getMessageRecipientByName(String recipientName);

    /**
     * Returns a list of existing message recipients that have satisfying date of sending.
     *
     * @param sendingDate the date of sending
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsBySendingDate(Date sendingDate);

    /**
     * Returns a list of existing message recipients that has been sent in period between
     * begin and end dates.
     *
     * @param begin period start
     * @param end   period end
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsByReceivedDate(Date begin, Date end);

    /**
     * Returns a list of existing message recipients that that have satisfying sending status ID.
     *
     * @param sendingStatusId sending status ID
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsBySendingStatusId(Number sendingStatusId);

    /**
     * Returns a list of existing message recipients that that have satisfying recipient's status ID.
     *
     * @param recipientStatusId recipient's status ID
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsByStatusId(Number recipientStatusId);

    /**
     * Returns a list of existing message recipients that that have satisfying fault's actor.
     *
     * @param faultActor fault's actor
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsByFaultActor(String faultActor);

    /**
     * Returns a list of existing message recipients that that have satisfying fault's code.
     *
     * @param faultCode fault's code
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsByFaultCode(String faultCode);

    /**
     * Returns a list of existing message recipients that that have satisfying fault's description.
     *
     * @param faultString fault's description
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getMessageRecipientsByFaultString(String faultString);

    /**
     * Returns a list containing all counters from the data storage.
     *
     * @return List of {@link ICounter}
     */
    List<ICounter> getAllCounters();

    /**
     * Returns a list containing all subdivisions from the data storage.
     *
     * @return List of {@link ISubdivision}
     */
    List<ISubdivision> getAllSubdivisions();

    /**
     * Returns a list containing all occupations from the data storage.
     *
     * @return List of {@link IOccupation}
     */
    List<IOccupation> getAllOccupations();

    /**
     * Returns a list containing all organizations from the data storage.
     *
     * @return List of {@link IOrganization}
     */
    List<IOrganization> getAllOrganizations();

    /**
     * Returns a list containing all settings from the data storage.
     *
     * @return List of {@link ISetting}
     */
    List<ISetting> getAllSettings();

    /**
     * Returns a list containing all settings folders from the data storage.
     *
     * @return List of {@link ISettingsFolder}
     */
    List<ISettingsFolder> getAllSettingsFolders();

    /**
     * Returns a list containing all messages from the data storage.
     *
     * @return List of {@link IMessage}
     */
    List<IMessage> getAllMessages();

    /**
     * Returns a list containing all message recipients from the data storage.
     *
     * @return List of {@link IMessageRecipient}
     */
    List<IMessageRecipient> getAllMessageRecipients();

    /**
     * Returns a list of existing occupations that that have satisfying organization code.
     *
     * @param orgCode organization code
     * @return List of {@link IOccupation}
     */
    List<IOccupation> getOccupationsByOrgCode(String orgCode);

    /**
     * Returns a list of existing occupations that that have satisfying occupation's name.
     *
     * @param occupationName occupation's name
     * @return List of {@link IOccupation}
     */
    List<IOccupation> getOccupationsByName(String occupationName);

    /**
     * Returns a list of existing subdivisions that that have satisfying subdivision's name.
     *
     * @param subdivisionName subdivision's name
     * @return List of {@link ISubdivision}
     */
    List<ISubdivision> getSubdivisionsByName(String subdivisionName);

    /**
     * Returns a list of existing subdivisions that that have satisfying organization's code.
     *
     * @param orgCode organization's code
     * @return List of {@link ISubdivision}
     */
    List<ISubdivision> getSubdivisionsByOrgCode(String orgCode);

    /**
     * Returns a list of existing settings folders that that have satisfying setting's ID.
     *
     * @param settingId setting's ID
     * @return List of {@link ISettingsFolder}
     */
    List<ISettingsFolder> getSettingsFoldersBySettingId(Number settingId);

    /**
     * Releases resources that were used by this session cache box.
     */
    void destroy();

    /**
     * Static way to delete an entry without having a DVK entry instance by ID.
     *
     * @param type DVK entry type {@link DvkType}
     * @param id   entry's ID
     */
    void delete(DvkType type, Object id);

    /**
     * Returns the rows' count in the table corresponding to the requested DVK type.
     *
     * @param type DVK entry type {@link DvkType}
     * @return rows' count
     */
    long countRows(DvkType type);
}
