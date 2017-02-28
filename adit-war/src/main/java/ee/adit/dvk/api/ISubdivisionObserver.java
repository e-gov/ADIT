package ee.adit.dvk.api;

import java.math.BigDecimal;

public interface ISubdivisionObserver extends IElementObserver {
    /**
     * Returns subdivision's code.
     *
     * @return subdivision's code {@link BigDecimal}
     */
    BigDecimal getCode();

    /**
     * Returns subdivision's name.
     *
     * @return subdivision's name {@link String}
     */
    String getName();

    /**
     * Returns organization's name.
     *
     * @return organization's name {@link String}
     */
    String getOrgCode();
}
