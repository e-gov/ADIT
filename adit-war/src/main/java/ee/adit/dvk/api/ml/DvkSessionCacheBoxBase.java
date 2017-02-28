package ee.adit.dvk.api.ml;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ee.adit.dvk.api.DVKAPI.DvkType;
import ee.adit.dvk.api.ISessionCacheBox;
import ee.adit.dvk.api.NotImplementedException;

public abstract class DvkSessionCacheBoxBase implements ISessionCacheBox {
    protected SessionHelper sessHelper;

    public DvkSessionCacheBoxBase(Session sess) {
        if (sess == null) {
            throw new NullPointerException("Use existing session please");
        }

        sessHelper = new SessionHelper(sess);
    }

    void delete(PojoFacade<?> facade, Transaction tx) {
        sessHelper.delete(facade.getPojo(), tx);
    }

    int execDeleteQuery(String sql, Transaction tx) {
        return sessHelper.delete(sessHelper.createQuery(sql), tx);
    }

    void save(PojoFacade<?> facade, Transaction tx) {
        sessHelper.save(facade.getPojo(), tx);
    }

    void refresh(PojoFacade<?> facade) {
        sessHelper.refresh(facade.getPojo());
    }

    Transaction beginTransaction() {
        return sessHelper.beginTransaction();
    }

    void rollbackTransaction(Transaction tx) throws RuntimeException {
        sessHelper.rollback(tx);
    }

    protected abstract ICacheProxy<?> getCacheProxy(DvkType t);

    public void clearCache() {
        sessHelper.clearCache();
    }

    public void clearCache(DvkType t) {
        sessHelper.evictClass(getPojoClass(t));
    }

    void evict(PojoFacade<?> facade) {
        sessHelper.evict(facade.getPojo());
    }

    void evictObject(Object obj) {
        sessHelper.evict(obj);
    }

    public void destroy() {
        sessHelper.destroy();
        sessHelper = null;
    }

    Object getFromHibernateCache(Class<?> clazz, Serializable id) {
        return sessHelper.get(clazz, id);
    }

    Object getFromHibernateCache(String entityName, Serializable id) {
        return sessHelper.get(entityName, id);
    }

    public void delete(DvkType t, Object id, Transaction tx) {
        getCacheProxy(t).delete(id, tx);
    }

    public void delete(DvkType t, Object id) {
        getCacheProxy(t).delete(id, null);
    }

    protected List<?> getFullList(DvkType t) {
        return getCacheProxy(t).select((String) null);
    }

    public Query createQuery(String queryString) throws HibernateException {
        return sessHelper.createQuery(queryString);
    }

    Object createNewRecord(DvkType t, String extraArgs) throws HibernateException {
        switch (t) {
            case Message:
                Query q = sessHelper.getNamedQuery("CreateNewMessage");
                return q.uniqueResult();
            case SettingsFolder:
                q = sessHelper.getNamedQuery("CreateNewSettingsFolder");
                q.setParameter(0, extraArgs);// settingId
                return q.uniqueResult();
            case MessageRecipient:
                q = sessHelper.getNamedQuery("CreateNewMessageRecipient");
                q.setParameter(0, extraArgs);// dhlMessageId
                return q.uniqueResult();
        }

        return null;
    }

    void replicate(PojoFacade<?> facade) {
        sessHelper.replicate(facade.getPojo());
    }

    protected String getPojoName(DvkType t) {
        switch (t) {
            case Counter:
                return PojoCounter.PojoName;
            case Subdivision:
                return PojoSubdivision.PojoName;
            case Occupation:
                return PojoOccupation.PojoName;
            case Organization:
                return PojoOrganization.PojoName;
            case SettingsFolder:
                return PojoSettingsFolders.PojoName;
            case Settings:
                return PojoSettings.PojoName;
            case Message:
                return PojoMessage.PojoName;
            case MessageRecipient:
                return PojoMessageRecipient.PojoName;
            default:
                throw new NotImplementedException("Unexpected type in getPojoName(): " + t);
        }
    }

    protected Class<?> getPojoClass(DvkType t) {
        switch (t) {
            case Counter:
                return PojoCounter.class;
            case Subdivision:
                return PojoSubdivision.class;
            case Occupation:
                return PojoOccupation.class;
            case Organization:
                return PojoOrganization.class;
            case SettingsFolder:
                return PojoSettingsFolders.class;
            case Settings:
                return PojoSettings.class;
            case Message:
                return PojoMessage.class;
            case MessageRecipient:
                return PojoMessageRecipient.class;
            default:
                throw new NotImplementedException("Unexpected type in getPojoClass(): " + t);
        }
    }
}
