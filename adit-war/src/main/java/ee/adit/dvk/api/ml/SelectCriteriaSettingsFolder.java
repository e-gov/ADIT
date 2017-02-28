package ee.adit.dvk.api.ml;

import ee.adit.dvk.api.SelectCriteria;

public class SelectCriteriaSettingsFolder extends SelectCriteria {
    protected Long id;
    protected Long settingsId;
    protected String folderName;

    public long getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(long settingsId) {
        this.settingsId = settingsId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
