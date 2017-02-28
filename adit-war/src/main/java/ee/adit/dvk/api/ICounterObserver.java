package ee.adit.dvk.api;

import java.math.BigDecimal;

/**
 * @author User
 *         Read-only proxy class for a record in the table DHL_COUNTER.
 *         Every instance will be fetched directly from data
 *         storage without looking up it in the cache.
 */
public interface ICounterObserver extends IElementObserver {
    /**
     * Returns DHL id.
     *
     * @return java.math.BigDecimal
     */
    BigDecimal getDhlId();
}
