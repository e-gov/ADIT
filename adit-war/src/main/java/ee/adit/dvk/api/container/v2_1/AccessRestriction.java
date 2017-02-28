package ee.adit.dvk.api.container.v2_1;

import java.util.Date;

/**
 * @author Hendrik Pärna
 * @since 29.01.14
 */
public class AccessRestriction {
    private String restrictionIdentifier;
    private Date restrictionBeginDate;
    private Date restrictionEndDate;
    private String restrictionEndEvent;
    private Date restrictionInvalidSince;
    private String restrictionBasis;
    private String informationOwner;

    public String getRestrictionIdentifier() {
        return restrictionIdentifier;
    }

    public void setRestrictionIdentifier(String restrictionIdentifier) {
        this.restrictionIdentifier = restrictionIdentifier;
    }

    public Date getRestrictionBeginDate() {
        return restrictionBeginDate;
    }

    public void setRestrictionBeginDate(Date restrictionBeginDate) {
        this.restrictionBeginDate = restrictionBeginDate;
    }

    public Date getRestrictionEndDate() {
        return restrictionEndDate;
    }

    public void setRestrictionEndDate(Date restrictionEndDate) {
        this.restrictionEndDate = restrictionEndDate;
    }

    public String getRestrictionEndEvent() {
        return restrictionEndEvent;
    }

    public void setRestrictionEndEvent(String restrictionEndEvent) {
        this.restrictionEndEvent = restrictionEndEvent;
    }

    public Date getRestrictionInvalidSince() {
        return restrictionInvalidSince;
    }

    public void setRestrictionInvalidSince(Date restrictionInvalidSince) {
        this.restrictionInvalidSince = restrictionInvalidSince;
    }

    public String getRestrictionBasis() {
        return restrictionBasis;
    }

    public void setRestrictionBasis(String restrictionBasis) {
        this.restrictionBasis = restrictionBasis;
    }

    public String getInformationOwner() {
        return informationOwner;
    }

    public void setInformationOwner(String informationOwner) {
        this.informationOwner = informationOwner;
    }
}
