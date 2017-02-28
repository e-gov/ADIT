package ee.adit.dvk.api;

import java.math.BigDecimal;

/**
 * @author User
 *         Delegate for work with a record from the table DHL_SETTINGS.
 */
public interface ISetting extends ISettingObserver, IDvkElement {
    /**
     * Sets institution's code of this setting.
     *
     * @param institutionCode {@link String}
     */
    public void setInstitutionCode(String institutionCode);

    /**
     * Sets institution's name of this setting.
     *
     * @param institutionName {@link String}
     */
    public void setInstitutionName(String institutionName);

    /**
     * Sets personal ID code of this setting.
     *
     * @param personalIdCode {@link String}
     */
    public void setPersonalIdCode(String personalIdCode);

    /**
     * Sets unit's ID code of this setting.
     *
     * @param unitId {@link long}
     */
    public void setUnitId(long unitId);

    /**
     * Sets subdivision's code of this setting.
     *
     * @param subdivisionCode {@link BigDecimal}
     */
    public void setSubdivisionCode(BigDecimal subdivisionCode);

    /**
     * Sets occupation's code of this setting.
     *
     * @param occupationCode {@link BigDecimal}
     */
    public void setOccupationCode(BigDecimal occupationCode);

    /**
     * Creates a new settings folder and puts it to the pending list of settings folder and will save
     * it after setting's or settings folder's save method will be called.
     *
     * @param folderName new settings' folder name {@link String}
     * @return a new created but not saved in the data storage settings folder
     */
    ISettingsFolder createSettingsFolder(String folderName);

    /**
     * Adds an existing settings folder to a pending list of settings folders if it doesn't belong to
     * this setting yet and will change its setting ID after settings folder's or setting's
     * save method will be called.
     *
     * @param settFolder {@link ISettingsFolder}
     * @return true if adding setting folder doesn't belong to this setting yet
     */
    public boolean add(ISettingsFolder settFolder);

    /**
     * Returns immutable proxy-object of this setting containing actual
     * values directly from the data storage.
     *
     * @return {@link ISettingObserver}
     */
    ISettingObserver getOrigin();
}
