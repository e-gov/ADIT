package ee.adit.dvk.api;

public interface ISettingsFolderObserver extends IElementObserver {
    /**
     * Returns settings' folder ID of this settings folder.
     *
     * @return settings' folder ID {@link long}
     */
    long getId();

    /**
     * Returns settings' ID of this settings folder.
     *
     * @return settings' ID {@link long}
     */
    long getSettingsId();

    /**
     * Returns folder's name of this settings folder.
     *
     * @return folder's name {@link String}
     */
    String getFolderName();
}
