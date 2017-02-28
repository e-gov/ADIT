package ee.adit.dvk.api.ml;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;

import ee.adit.dvk.api.IElementObserver;
import ee.adit.dvk.api.SelectCriteria;

public abstract class CacheProxyBase<ID, FACADE, POJO extends IElementObserver> implements ICacheProxy<FACADE> {
    protected HashMap<ID, FACADE> cache = new HashMap<ID, FACADE>();
    protected DvkSessionCacheBox cacheBox;
    protected SelectCriteria selectCriteria;
    private static final String IdRequestTemplate = "select %s from %s where %s = %s";
    private static final String IdRequestTemplateOfTypeText = "select %s from %s where %s = '%s'";

    public CacheProxyBase(DvkSessionCacheBox cacheBox) {
        this.cacheBox = cacheBox;
    }

    protected abstract String getPojoName();

    protected abstract ID getPojoId(POJO pojo);

    public abstract String getIdFieldName();

    public void destroy() {
        cache.clear();
        cache = null;

        cacheBox = null;

        if (selectCriteria != null) {
            selectCriteria.reset();
            selectCriteria = null;
        }
    }

    @SuppressWarnings("unchecked")
    public void clearCache() {
        if (cache.size() == 0) {
            return;
        }

        for (FACADE facade : cache.values()) {
            cacheBox.evict((PojoFacade<POJO>) facade);
        }

        cache.clear();
    }

    public List<FACADE> select(SelectCriteria criteria) {
        String query = getDefaultQuery() + " where " + criteria.getWhereClause();
        return select(query);
    }

    @SuppressWarnings("unchecked")
    public void stateChanged(PojoFacade<?> facade) {
        switch (facade.getState()) {
            case Deleted:
                if (cache.containsKey(facade.getPojoId())) {
                    cacheBox.evict(facade);
                    cache.remove(facade);
                }
                break;
            case Persistent:
                Object id = facade.getPojoId();
                if (!cache.containsKey(id)) {
                    cache.put((ID) id, (FACADE) facade);
                }
                break;
        }
    }

    public Iterator<FACADE> elements() {
        return cache.values().iterator();
    }

    public FACADE lookupLocal(Object id) {
        return cache.get(id);
    }

    @SuppressWarnings("unchecked")
    public void delete(Object id, Transaction tx, Object... extraArgs) throws HibernateException {
        FACADE facade = lookup(id, false);

        if (facade != null) {
            ((PojoFacade<POJO>) facade).delete(tx);
        } else {
            String query = "delete from " + PojoCounter.PojoName + " where " + getIdFieldName() + " = " + id;
            cacheBox.execDeleteQuery(query, null);
        }
    }

    public String getDefaultQuery() {
        return "from " + getPojoName();
    }

    public SelectCriteria getSelectCriteria(boolean reset) {
        if (selectCriteria != null && reset) {
            selectCriteria.reset();
        }

        return selectCriteria;
    }

    @SuppressWarnings("unchecked")
    public List<FACADE> select(String query) {
        List<POJO> selectList = null;
        List<FACADE> retList = new ArrayList<FACADE>();

        if (query == null) {
            // select all
            selectList = cacheBox.createQuery(getDefaultQuery()).list();
        } else {
            selectList = cacheBox.createQuery(query).list();
        }

        if (Util.isEmpty(selectList)) {
            return retList;
        }

        for (POJO pojo : selectList) {
            retList.add(lookup(getPojoId(pojo), false));
        }

        selectList.clear();

        return retList;
    }

    public boolean isExistingId(ID id) {
        String template = (id instanceof String) ? IdRequestTemplateOfTypeText : IdRequestTemplate;
        String queryString = String.format(template, getIdFieldName(), getPojoName(), getIdFieldName(), id);

        Query q = cacheBox.createQuery(queryString);

        Object res = q.uniqueResult();

        return res != null;
    }

    @SuppressWarnings("unchecked")
    public List<ID> getExistingIdList() {
        String q = String.format("select %s from %s", getIdFieldName(), getPojoName());

        return cacheBox.createQuery(q).list();
    }

    @SuppressWarnings("unchecked")
    public List<ID> getExistingIdList(SelectCriteria criteria) {
        String q = String.format("select %s from %s where %s", getIdFieldName(), getPojoName(), criteria.getWhereClause());

        return cacheBox.createQuery(q).list();
    }

    @SuppressWarnings("unchecked")
    protected Object getArgumet(Object hash, String key, boolean isMandatory) {
        Hashtable<String, Object> hashtable = (Hashtable<String, Object>) hash;

        Object value = hashtable.get(key);

        if (value == null && isMandatory) {
            throw new NullPointerException("Expected argument '" + key + "' is absent");
        }

        return value;
    }

    protected void asserExtraArgs(Object[] args) {
        if (args == null) {
            throw new NullPointerException("Expected argument 'extraArgs' cannot be null");
        }

        if (args.length == 0) {
            throw new NullPointerException("Expected mandatory arguments");
        }
    }

    @SuppressWarnings("unchecked")
    public POJO getOriginVersion(Object id) {
        Long idSettingsFolder = Util.getLong(id);

        return (POJO) cacheBox.getFromHibernateCache(getPojoName(), idSettingsFolder);
    }
}
