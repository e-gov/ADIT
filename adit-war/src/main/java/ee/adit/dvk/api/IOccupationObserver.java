package ee.adit.dvk.api;

import java.math.BigDecimal;

/**
 * @author User
 *         Delegate for a certain occupation entry which contains actual values taken
 *         directly from the data storage but not from the cache using occupation's
 *         identification attribute(s).
 */
public interface IOccupationObserver extends IElementObserver {
    /**
     * Returns the code of this occupation.
     *
     * @return occupation's code {@link BigDecimal}
     */
    BigDecimal getCode();

    /**
     * Returns the name of this occupation.
     *
     * @return occupation's code {@link String}
     */
    String getName();

    /**
     * Returns the organization's code of this occupation.
     *
     * @return organization's code {@link String}
     */
    String getOrgCode();
}
