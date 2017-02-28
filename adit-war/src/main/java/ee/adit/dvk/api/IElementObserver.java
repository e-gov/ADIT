package ee.adit.dvk.api;

import ee.adit.dvk.api.DVKAPI.DvkType;

/**
 * @author User
 *         Base interface for entries of some type which contain actual values taken
 *         directly from the data storage but not from the cache.
 */
public interface IElementObserver {
    DvkType getType();
}
