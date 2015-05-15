package ee.adit.dvk.converter.documentcontainer;

/**
 * AccessConditionsCode:
 * AK - for offical usages (ametlikuks kasutamiseks)
 */
public enum AccessConditionsCode {
    AK("AK");

    AccessConditionsCode(String val) {
        this.val = val;
    }

    private String val;

    public String getVal() {
        return val;
    }
}