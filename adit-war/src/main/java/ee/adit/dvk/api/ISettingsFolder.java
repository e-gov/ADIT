package ee.adit.dvk.api;

public interface ISettingsFolder extends ISettingsFolderObserver, IDvkElement {
    /**
     * Sets folder's name of this settings folder.
     *
     * @param folderName {@link String}
     */
    void setFolderName(String folderName);

    /**
     * Returns immutable proxy-object of this settings folder containing actual values directly
     * from the data storage.
     *
     * @return {@link ISettingsFolderObserver}
     */
    ISettingsFolderObserver getOrigin();
}
