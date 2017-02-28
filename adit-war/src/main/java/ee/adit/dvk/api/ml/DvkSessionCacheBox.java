package ee.adit.dvk.api.ml;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import ee.adit.dvk.api.DVKAPI.DvkType;
import ee.adit.dvk.api.ICounter;
import ee.adit.dvk.api.IMessage;
import ee.adit.dvk.api.IMessageRecipient;
import ee.adit.dvk.api.IOccupation;
import ee.adit.dvk.api.IOrganization;
import ee.adit.dvk.api.ISetting;
import ee.adit.dvk.api.ISettingsFolder;
import ee.adit.dvk.api.ISubdivision;
import ee.adit.dvk.api.MessageRecipientCreateArgs;
import ee.adit.dvk.api.NotImplementedException;
import ee.adit.dvk.api.SelectCriteria;

public class DvkSessionCacheBox extends DvkSessionCacheBoxBase {
    private ICacheProxy<DvkCounter> countersCache;
    private ICacheProxy<DvkSubdivision> subdivisionsCache;
    private ICacheProxy<DvkOccupation> occupationsCache;
    private ICacheProxy<DvkOrganization> organizationsCache;
    private ICacheProxy<DvkSetting> settingsCache;
    private ICacheProxy<DvkSettingsFolder> settingsFoldersCache;
    private ICacheProxy<DvkMessage> messagesCache;
    private ICacheProxy<DvkMessageRecipient> messageRecipientCache;
    private static Hashtable<String, Object> extraArgs = new Hashtable<String, Object>();

    public DvkSessionCacheBox(Session sess) {
        super(sess);
    }

    @Override
    protected ICacheProxy<?> getCacheProxy(DvkType t) {
        switch (t) {
            case Counter:
                if (countersCache == null) {
                    countersCache = DvkCounter.createCacheProxy(this);
                }

                return countersCache;
            case Subdivision:
                if (subdivisionsCache == null) {
                    subdivisionsCache = DvkSubdivision.createCacheProxy(this);
                }

                return subdivisionsCache;
            case Occupation:
                if (occupationsCache == null) {
                    occupationsCache = DvkOccupation.createCacheProxy(this);
                }

                return occupationsCache;
            case Organization:
                if (organizationsCache == null) {
                    organizationsCache = DvkOrganization.createCacheProxy(this);
                }

                return organizationsCache;
            case SettingsFolder:
                if (settingsFoldersCache == null) {
                    settingsFoldersCache = DvkSettingsFolder.createCacheProxy(this);
                }

                return settingsFoldersCache;
            case Settings:
                if (settingsCache == null) {
                    settingsCache = DvkSetting.createCacheProxy(this);
                }

                return settingsCache;
            case Message:
                if (messagesCache == null) {
                    messagesCache = DvkMessage.createCacheProxy(this);
                }

                return messagesCache;
            case MessageRecipient:
                if (messageRecipientCache == null) {
                    messageRecipientCache = DvkMessageRecipient.createCacheProxy(this);
                }

                return messageRecipientCache;
            default:
                throw new NotImplementedException("Unexpected type in getCacheProxy(): " + t);
        }
    }

    @Override
    public void destroy() {
        destroySafely(countersCache);
        countersCache = null;
        //
        destroySafely(subdivisionsCache);
        subdivisionsCache = null;
        //
        destroySafely(occupationsCache);
        occupationsCache = null;
        //
        destroySafely(organizationsCache);
        organizationsCache = null;
        //
        destroySafely(settingsCache);
        settingsCache = null;
        //
        destroySafely(settingsFoldersCache);
        settingsFoldersCache = null;
        //
        destroySafely(messagesCache);
        messagesCache = null;

        super.destroy();
    }

    private void destroySafely(ICacheProxy<?> proxy) {
        if (proxy != null) {
            proxy.destroy();
        }
    }

    void notifyStateChanged(PojoFacade<?> facade) {
        getCacheProxy(facade.getType()).stateChanged(facade);
    }

    public DvkCounter getCounter(Number dhlId, boolean allowCreateNew) {
        return (DvkCounter) getCacheProxy(DvkType.Counter).lookup(dhlId, allowCreateNew);
    }

    public DvkSubdivision getSubdivision(Number subdivisionCode) {
        return (DvkSubdivision) getCacheProxy(DvkType.Subdivision).lookup(subdivisionCode, false);
    }

    @SuppressWarnings("unchecked")
    public List<ISubdivision> getSubdivisionsByName(String subdivisionName) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Subdivision);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoSubdivision.FieldNames.subdivisionName, subdivisionName);
        //
        return (List<ISubdivision>) proxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<ISubdivision> getSubdivisionsByOrgCode(String orgCode) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Subdivision);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoSubdivision.FieldNames.orgCode, orgCode);
        //
        return (List<ISubdivision>) proxy.select(criteria);
    }

    public DvkOccupation getOccupation(Number occupationCode) {
        return (DvkOccupation) getCacheProxy(DvkType.Occupation).lookup(occupationCode, false);
    }

    DvkOccupation createOccupation(Number occupationCode, String orgCode) {
        extraArgs.clear();
        extraArgs.put(PojoOrganization.FieldNames.orgCode, orgCode);
        //
        DvkOccupation occup = (DvkOccupation) getCacheProxy(DvkType.Occupation).lookup(occupationCode, true, extraArgs);
        //
        return occup;
    }

    DvkSubdivision createSubdivision(Number subdivisionCode, String orgCode) {
        extraArgs.clear();
        extraArgs.put(PojoOrganization.FieldNames.orgCode, orgCode);
        //
        DvkSubdivision subdiv = (DvkSubdivision) getCacheProxy(DvkType.Subdivision).lookup(subdivisionCode, true, extraArgs);
        //
        return subdiv;
    }

    DvkSettingsFolder createSettingsFolder(Number settingsId) {
        extraArgs.clear();
        extraArgs.put(PojoSettingsFolders.FieldNames.settingsId, settingsId);
        //
        DvkSettingsFolder settFolder = (DvkSettingsFolder) getCacheProxy(DvkType.SettingsFolder).lookup(null, true, extraArgs);
        //
        return settFolder;
    }

    @SuppressWarnings("unchecked")
    public List<IOccupation> getOccupationsByOrgCode(String orgCode) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Occupation);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoOccupation.FieldNames.orgCode, orgCode);
        //
        return (List<IOccupation>) proxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<IOccupation> getOccupationsByName(String occupationName) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Occupation);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoOccupation.FieldNames.occupationCode, occupationName);
        //
        return (List<IOccupation>) proxy.select(criteria);
    }

    public DvkOrganization getOrganization(String orgCode, boolean allowCreateNew) {
        return (DvkOrganization) getCacheProxy(DvkType.Organization).lookup(orgCode, allowCreateNew);
    }

    @SuppressWarnings("unchecked")
    public List<IOrganization> getOrganizationsByDHLCapable(boolean dhlCapable) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Organization);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoOrganization.FieldNames.dhlCapable, dhlCapable);
        //
        return (List<IOrganization>) proxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<IOrganization> getOrganizationsByDHLDirectCapable(boolean dhlDirectCapable) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Organization);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoOrganization.FieldNames.dhlDirectCapable, dhlDirectCapable);
        //
        return (List<IOrganization>) proxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public IOrganization getOrganizationByOrgName(String orgName) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Organization);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoOrganization.FieldNames.orgName, orgName);
        //
        List<IOrganization> resList = (List<IOrganization>) proxy.select(criteria);

        try {
            return resList.size() > 0 ? resList.get(0) : null;
        } finally {
            resList.clear();
        }
    }

    public DvkSetting getSetting(Number id, boolean allowCreateNew) {
        return (DvkSetting) getCacheProxy(DvkType.Settings).lookup(id, allowCreateNew);
    }

    @SuppressWarnings("unchecked")
    public List<ISetting> getSettingsByInstitutionCode(String institutionCode) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Settings);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoSettings.FieldNames.institutionCode, institutionCode);
        //
        return (List<ISetting>) proxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<ISetting> getSettingsByPersonalIdCode(String personalIdCode) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Settings);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoSettings.FieldNames.personalIdCode, personalIdCode);
        //
        return (List<ISetting>) proxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<ISetting> getSettingsByUnitId(Number unitId) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Settings);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);
        criteria.setValue(PojoSettings.FieldNames.unitId, unitId);
        //
        return (List<ISetting>) proxy.select(criteria);
    }

    public DvkSettingsFolder getSettingsFolder(Number id) {
        return (DvkSettingsFolder) getCacheProxy(DvkType.SettingsFolder).lookup(id, false);
    }

    public DvkMessage getMessage(Number dhlMessageId) {
        return (DvkMessage) getCacheProxy(DvkType.Message).lookup(dhlMessageId, false);
    }

    public List<IMessage> getMessagesByIsIncoming(boolean isIncoming) {
        return getMessages(PojoMessage.FieldNames.isIncoming, isIncoming);
    }

    public List<IMessage> getMessagesBySenderOrgCode(String senderOrgCode) {
        return getMessages(PojoMessage.FieldNames.senderOrgCode, senderOrgCode);
    }

    public List<IMessage> getMessagesByRecipientOrgCode(String recipientOrgCode) {
        return getMessages(PojoMessage.FieldNames.recipientOrgCode, recipientOrgCode);
    }

    @SuppressWarnings("unchecked")
    private List<IMessage> getMessages(String field, Object value) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.Message);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);

        if (field == null) {
            criteria.setCustomCondition((String) value, false);
        } else {
            criteria.setValue(field, value);
        }
        //
        return (List<IMessage>) proxy.select(criteria);
    }

    public DvkMessage createMessage() {
        return (DvkMessage) getCacheProxy(DvkType.Message).lookup(null, true);
    }

    public IMessageRecipient createMessageRecipient(MessageRecipientCreateArgs createArgs) {
        extraArgs.clear();
        extraArgs.put(MessageRecipientCreateArgs.class.getName(), createArgs);
        //
        DvkMessageRecipient recip = (DvkMessageRecipient) getCacheProxy(DvkType.MessageRecipient).lookup(null, true, extraArgs);
        //
        return recip;
    }

    public IMessageRecipient getMessageRecipient(Number id) {
        return (DvkMessageRecipient) getCacheProxy(DvkType.MessageRecipient).lookup(id, false);
    }

    @SuppressWarnings("unchecked")
    public List<ICounter> getAllCounters() {
        return (List<ICounter>) getCacheProxy(DvkType.Counter).select((String) null);
    }

    @SuppressWarnings("unchecked")
    public List<ISubdivision> getAllSubdivisions() {
        return (List<ISubdivision>) getCacheProxy(DvkType.Subdivision).select((String) null);
    }

    @SuppressWarnings("unchecked")
    public List<IOccupation> getAllOccupations() {
        return (List<IOccupation>) getCacheProxy(DvkType.Occupation).select((String) null);
    }

    @SuppressWarnings("unchecked")
    public List<IOrganization> getAllOrganizations() {
        return (List<IOrganization>) getCacheProxy(DvkType.Organization).select((String) null);
    }

    @SuppressWarnings("unchecked")
    public List<ISetting> getAllSettings() {
        return (List<ISetting>) getCacheProxy(DvkType.Settings).select((String) null);
    }

    @SuppressWarnings("unchecked")
    public List<ISettingsFolder> getAllSettingsFolders() {
        return (List<ISettingsFolder>) getCacheProxy(DvkType.SettingsFolder).select((String) null);
    }

    @SuppressWarnings("unchecked")
    public List<ISettingsFolder> getSettingsFoldersBySettingId(Number settingId) {
        ICacheProxy<?> cacheProxy = getCacheProxy(DvkType.SettingsFolder);

        SelectCriteriaSettingsFolder criteria = (SelectCriteriaSettingsFolder) cacheProxy.getSelectCriteria(true);
        criteria.setSettingsId(Util.getLong(settingId));

        return (List<ISettingsFolder>) cacheProxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<ISettingsFolder> getSettingsFolderByFolderName(String folderName) {
        ICacheProxy<?> cacheProxy = getCacheProxy(DvkType.SettingsFolder);

        SelectCriteriaSettingsFolder criteria = (SelectCriteriaSettingsFolder) cacheProxy.getSelectCriteria(true);
        criteria.setFolderName(folderName);

        return (List<ISettingsFolder>) cacheProxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<IMessage> getAllMessages() {
        return (List<IMessage>) getCacheProxy(DvkType.Message).select((String) null);
    }

    public List<IMessageRecipient> getMessageRecipientsByMessageId(Number dhlMessageId) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.dhlMessageId, dhlMessageId);
    }

    public List<IMessageRecipient> getMessageRecipientsByOrgCode(String orgCode) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.recipientOrgCode, orgCode);
    }

    public IMessageRecipient getMessageRecipientByPersonCode(String recipientPersonCode) {
        List<IMessageRecipient> resList = getMessageRecipients(PojoMessageRecipient.FieldNames.recipientPersonCode,
                recipientPersonCode);
        acceptMaxOneRecord(resList);

        return resList.size() == 0 ? null : resList.get(0);
    }

    public List<IMessageRecipient> getMessageRecipientsByOrgName(String orgName) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.recipientOrgName, orgName);
    }

    public IMessageRecipient getMessageRecipientByName(String recipientName) {
        List<IMessageRecipient> resList = getMessageRecipients(PojoMessageRecipient.FieldNames.recipientName, recipientName);
        acceptMaxOneRecord(resList);

        return resList.size() == 0 ? null : resList.get(0);
    }

    public List<IMessageRecipient> getMessageRecipientsBySendingDate(Date sendingDate) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.sendingDate, sendingDate);
    }

    public List<IMessageRecipient> getMessageRecipientsByReceivedDate(Date receivedDate) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.receivedDate, receivedDate);
    }

    public List<IMessageRecipient> getMessageRecipientsByReceivedDate(Date begin, Date end) {
        String where = String.format("%s >= %s and %s <= %s", PojoMessageRecipient.FieldNames.receivedDate, SelectCriteria
                .formatDate(begin), PojoMessageRecipient.FieldNames.receivedDate, SelectCriteria.formatDate(end));

        return getMessageRecipients(null, where);
    }

    public List<IMessageRecipient> getMessageRecipientsBySendingStatusId(Number sendingStatusId) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.sendingStatusId, sendingStatusId);
    }

    public List<IMessageRecipient> getMessageRecipientsByStatusId(Number recipientStatusId) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.recipientStatusId, recipientStatusId);
    }

    public List<IMessageRecipient> getMessageRecipientsByFaultActor(String faultActor) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.faultActor, faultActor);
    }

    public List<IMessageRecipient> getMessageRecipientsByFaultCode(String faultCode) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.faultCode, faultCode);
    }

    public List<IMessageRecipient> getMessageRecipientsByFaultString(String faultString) {
        return getMessageRecipients(PojoMessageRecipient.FieldNames.faultString, faultString);
    }

    @SuppressWarnings("unchecked")
    private List<IMessageRecipient> getMessageRecipients(String field, Object value) {
        ICacheProxy<?> proxy = getCacheProxy(DvkType.MessageRecipient);
        //
        SelectCriteria criteria = proxy.getSelectCriteria(true);

        if (field == null) {
            criteria.setCustomCondition((String) value, false);
        } else {
            criteria.setValue(field, value);
        }
        //
        return (List<IMessageRecipient>) proxy.select(criteria);
    }

    @SuppressWarnings("unchecked")
    public List<IMessageRecipient> getAllMessageRecipients() {
        return (List<IMessageRecipient>) getCacheProxy(DvkType.MessageRecipient).select((String) null);
    }

    DvkOrganization findLocalOrganization(String orgCode) {
        return (DvkOrganization) getCacheProxy(DvkType.Organization).lookupLocal(orgCode);
    }

    public long countRows(DvkType t) {
        Query q = sessHelper.createQuery("select count(*) from " + getPojoName(t));

        return (Long) q.uniqueResult();
    }

    private static void acceptMaxOneRecord(List<?> list) {
        if (list.size() > 1) {
            throw new RuntimeException(
                    "Unexpected result of sql statement's execution. Selected records' count must not be more than one.");
        }
    }
}
