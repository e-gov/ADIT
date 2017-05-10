package ee.adit.dhx.api.container.v2_1;

import java.util.List;

/**
 * @author Hendrik Pärna
 * @since 29.01.14
 */
public class Access {
    private String accessConditionsCode;
    private List<AccessRestriction> accessRestriction;

    public String getAccessConditionsCode() {
        return accessConditionsCode;
    }

    public void setAccessConditionsCode(String accessConditionsCode) {
        this.accessConditionsCode = accessConditionsCode;
    }

    public List<AccessRestriction> getAccessRestriction() {
        return accessRestriction;
    }

    public void setAccessRestriction(List<AccessRestriction> accessRestriction) {
        this.accessRestriction = accessRestriction;
    }
}
