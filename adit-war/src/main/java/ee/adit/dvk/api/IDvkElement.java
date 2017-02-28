package ee.adit.dvk.api;

import ee.adit.dvk.api.DVKAPI.DvkType;

/**
 * @author User
 *         Base interface for all DVK entries in DHL data storage
 */
public interface IDvkElement extends ISaveSupportabe, IDeleteSupportabe {
    /**
     * @author User
     *         DVK entry state
     */
    public enum State {
        New, Persistent, Deleted
    }

    /**
     * Releases resources.
     */
    void destroy();

    /**
     * Indicates whether entry has changed data or not.
     *
     * @return true if data has changes
     */
    boolean isDirty();

    /**
     * Returns the current state.
     *
     * @return ee.adit.dvk.api.Sate
     */

    State getState();

    /**
     * Returns type of the DVK entry.
     *
     * @return one from the range of DvkType
     */
    DvkType getType();

    /**
     * Returns true if the DVK entry has persistent state and has changes in its data content
     * otherwise false.
     *
     * @return boolean
     */
    boolean isNew();

    /**
     * Returns true if and only if the entry represents a certain record in
     * the data storage.
     *
     * @return boolean
     */
    boolean isPersistent();

    /**
     * Resets all the changes if any and loads current values for the record
     * with which this entry was mapped.
     */
    void reload();

    /**
     * Returns immutable proxy-object containing values for this entry directly
     * from data storage (actual values' snapshot).
     *
     * @return ee.adit.dvk.api.IElementObserver
     */
    IElementObserver getOrigin();

    /**
     * Returns true if and only if the entry has persistent state and has changes.
     *
     * @return boolean
     */
    boolean isOriginActual();
}
