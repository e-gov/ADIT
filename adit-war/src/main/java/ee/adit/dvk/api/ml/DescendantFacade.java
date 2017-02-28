package ee.adit.dvk.api.ml;

import org.hibernate.HibernateException;

import org.hibernate.Transaction;

import ee.adit.dvk.api.IElementObserver;


public abstract class DescendantFacade<POJO extends IElementObserver> extends PojoFacade<POJO> {
    protected ElementPendingState pendingState;

    public DescendantFacade(DvkSessionCacheBox cacheBox, boolean isNew) {
        super(cacheBox, isNew);
    }

    protected ElementPendingState getPendingState() {
        return pendingState;
    }

    void desrtoyPendingState() {
        if (pendingState != null) {
            pendingState.destroy();
            pendingState = null;
        }
    }

    public void setPendingState(DescendantsContainerFacade<?> dc, PendingState state) {
        if (pendingState != null) {
            pendingState.removePendingFromContainer(this);
            pendingState.destroy();
        }

        pendingState = new ElementPendingState(dc, state);
    }

    @Override
    void save(Transaction tx) throws HibernateException {
        if (pendingState != null && pendingState.getState() == PendingState.Add) {
            DescendantsContainerFacade<?> dc = pendingState.getContainer();

            if (dc.isNew()) {
                getCacheBox().save(dc, tx);
            }
        }

        super.save(tx);
    }

    @Override
    public void commitChanges(State state) {
        super.commitChanges(state);

        if (pendingState != null && pendingState.getState() != PendingState.Undefined) {
            pendingState.removePendingFromContainer(this);
            pendingState.destroy();
            pendingState = null;
        }
    }
}
