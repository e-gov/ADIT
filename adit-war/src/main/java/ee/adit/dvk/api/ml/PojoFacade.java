package ee.adit.dvk.api.ml;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import ee.adit.dvk.api.DVKAPI.DvkType;
import ee.adit.dvk.api.IDvkElement;
import ee.adit.dvk.api.IElementObserver;

public abstract class PojoFacade<T extends IElementObserver> implements IDvkElement {
    public enum PendingState {
        Add, Undefined
    }

    private boolean isDirty = true;// new object is dirty
    private State state = State.New;
    private DvkSessionCacheBox cacheBox;

    public PojoFacade(DvkSessionCacheBox cacheBox, boolean isNew) {
        isDirty = isNew;
        state = isNew ? State.New : State.Persistent;
        this.cacheBox = cacheBox;
    }

    public void delete() {
        if (!allowDelete()) {
            return;
        }

        Transaction tx = cacheBox.beginTransaction();

        try {
            delete(tx);
            //
            tx.commit();
            //
            commitChanges(State.Deleted);
        } catch (RuntimeException ex) {
            cacheBox.rollbackTransaction(tx);
            throw ex;
        }
    }

    void delete(Transaction tx) {
        if (allowDelete()) {
            cacheBox.delete(this, tx);

            if (tx == null) {
                commitChanges(State.Deleted);
            }
        }
    }

    public void save() {
        Transaction tx = cacheBox.beginTransaction();

        try {
            save(tx);
            //
            tx.commit();
            //
            commitChanges(State.Persistent);
        } catch (RuntimeException ex) {
            cacheBox.rollbackTransaction(tx);
            throw ex;
        }
    }

    void save(Transaction tx) throws HibernateException {
        if (allowSave()) {
            cacheBox.save(this, tx);

            saveDescendants(tx);

            if (tx == null) {
                commitChanges(State.Persistent);
            }
        }
    }

    public void destroy() {
        cacheBox = null;
    }

    public boolean isDirty() {
        return isDirty;
    }

    protected void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    public State getState() {
        return state;
    }

    abstract T getPojo();

    public boolean isNew() {
        return state == State.New;
    }

    protected abstract T clonePojo();

    public boolean isPersistent() {
        return state == State.Persistent;
    }

    public boolean isDeleted() {
        return state == State.Deleted;
    }

    abstract Object getPojoId();

    protected static boolean hasSameValue(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == b) {
            return true;
        }

        return ((a != null) && a.equals(b));
    }

    public void reload() {
        if (isPersistent()) {
            cacheBox.refresh(this);
        }
    }

    void commitChanges(State state) {
        isDirty = false;

        if (!hasSameValue(state, this.state)) {
            this.state = state;
        }
    }

    protected boolean allowSave() {
        return isDirty();
    }

    protected boolean allowDelete() {
        return !isDeleted();
    }

    void saveDescendants(Transaction tx) {
    }

    protected Object createNewRecord(DvkType t) {
        return cacheBox.createNewRecord(t, null);
    }

    protected Object createNewRecord(DvkType t, String args) {
        return cacheBox.createNewRecord(t, args);
    }

    protected void substituteInCache() {
        cacheBox.replicate(this);
    }

    protected DvkSessionCacheBox getCacheBox() {
        return cacheBox;
    }

    @SuppressWarnings("unchecked")
    public T getOrigin() {
        if (!isPersistent()) {
            return null;
        }

        cacheBox.evict(this);

        T pojo = (T) cacheBox.getCacheProxy(getType()).getOriginVersion(getPojoId());

        cacheBox.evictObject(pojo);

        return pojo;
    }

    public boolean isOriginActual() {
        return isPersistent() && isDirty;
    }
}

