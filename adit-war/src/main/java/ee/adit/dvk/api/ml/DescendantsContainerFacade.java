package ee.adit.dvk.api.ml;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import ee.adit.dvk.api.IDvkElement;
import ee.adit.dvk.api.IElementObserver;

public abstract class DescendantsContainerFacade<POJO extends IElementObserver> extends PojoFacade<POJO> {
    public DescendantsContainerFacade(DvkSessionCacheBox cacheBox, boolean isNew) {
        super(cacheBox, isNew);
    }

    @Override
    abstract void saveDescendants(Transaction tx);

    abstract void removePending(PojoFacade<POJO> pojo);

    protected abstract boolean hasDirtyDescendants();

    abstract void commitPendingChanges(State state);

    @Override
    protected boolean allowSave() {
        return isDirty() || hasDirtyDescendants();
    }

    @Override
    void save(Transaction tx) throws HibernateException {
        if (allowSave()) {
            if (isDirty()) {
                getCacheBox().save(this, tx);
            }

            if (hasDirtyDescendants()) {
                saveDescendants(tx);
            }

            if (tx == null) {
                commitChanges(State.Persistent);

                commitPendingChanges(State.Persistent);
            }
        }
    }

    @Override
    public void delete(Transaction tx) throws HibernateException {
        if (!allowDelete()) {
            return;
        }

        deleteDescendants(tx);

        getCacheBox().delete(this, tx);

        if (tx == null) {
            commitChanges(State.Deleted);

            commitPendingChanges(State.Deleted);
        }
    }

    abstract void deleteDescendants(Transaction tx);

    protected static void commitPendingDescendants(List<? extends IDvkElement> descendantList, State state) {
        if (Util.isEmpty(descendantList)) {
            return;
        }

        for (IDvkElement e : descendantList) {
            DescendantFacade<?> desc = (DescendantFacade<?>) e;
            desc.desrtoyPendingState();
            desc.commitChanges(state);
        }

        descendantList.clear();
    }
}
