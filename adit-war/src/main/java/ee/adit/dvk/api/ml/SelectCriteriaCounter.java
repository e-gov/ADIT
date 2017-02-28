package ee.adit.dvk.api.ml;

import java.math.BigDecimal;

import ee.adit.dvk.api.SelectCriteria;

public class SelectCriteriaCounter extends SelectCriteria {
    @SuppressWarnings("unused")
    private BigDecimal dhlId;

    public void setDhlId(BigDecimal dhlId) {
        this.dhlId = dhlId;
    }

}
