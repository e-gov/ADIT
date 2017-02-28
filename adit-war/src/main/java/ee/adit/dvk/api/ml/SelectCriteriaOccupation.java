package ee.adit.dvk.api.ml;

import java.math.BigDecimal;

import ee.adit.dvk.api.SelectCriteria;

public class SelectCriteriaOccupation extends SelectCriteria {
    @SuppressWarnings("unused")
    private BigDecimal occupationCode;
    @SuppressWarnings("unused")
    private String occupationName;
    @SuppressWarnings("unused")
    private String orgCode;

    public void setOccupationCode(BigDecimal occupationCode) {
        this.occupationCode = occupationCode;
    }

    public void setOccupationName(String occupationName) {
        this.occupationName = occupationName;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

}
