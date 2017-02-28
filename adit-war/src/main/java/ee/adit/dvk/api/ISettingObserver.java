package ee.adit.dvk.api;

import java.math.BigDecimal;

public interface ISettingObserver extends IElementObserver {
    /**
     * Returns setting's ID.
     *
     * @return setting's ID {@link long}
     */
    public long getId();

    /**
     * Returns institution's code.
     *
     * @return institution's code {@link String}
     */
    public String getInstitutionCode();

    /**
     * Returns institution's name.
     *
     * @return institution's name {@link String}
     */
    public String getInstitutionName();

    /**
     * Returns personal ID code.
     *
     * @return personal ID code {@link String}
     */
    public String getPersonalIdCode();

    /**
     * Returns unit's ID.
     *
     * @return unit's ID {@link long}
     */
    public long getUnitId();

    /**
     * Returns subdivision's code.
     *
     * @return subdivision's code {@link BigDecimal}
     */
    public BigDecimal getSubdivisionCode();

    /**
     * Returns occupation's code.
     *
     * @return occupation's code {@link BigDecimal}
     */
    public BigDecimal getOccupationCode();
}
