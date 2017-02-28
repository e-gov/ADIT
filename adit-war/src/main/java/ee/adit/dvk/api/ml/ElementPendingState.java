package ee.adit.dvk.api.ml;

import ee.adit.dvk.api.ml.PojoFacade.PendingState;

public class ElementPendingState {
    private DescendantsContainerFacade<?> container;
    private PendingState state = PendingState.Undefined;

    public ElementPendingState(DescendantsContainerFacade<?> dc) {
        this.container = dc;
    }

    public ElementPendingState(DescendantsContainerFacade<?> dc, PendingState state) {
        this.container = dc;
        this.state = state;
    }

    public DescendantsContainerFacade<?> getContainer() {
        return container;
    }

    public PendingState getState() {
        return state;
    }

    public void setState(PendingState state) {
        this.state = state;
    }

    @SuppressWarnings("unchecked")
    public void removePendingFromContainer(PojoFacade<?> facade) {
        if (state == PendingState.Undefined || container == null) {
            return;
        }

        DescendantsContainerFacade cont = container;
        cont.removePending(facade);
    }

    public void destroy() {
        container = null;
        state = PendingState.Undefined;
    }
}
