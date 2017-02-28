package ee.adit.dvk.api.ml;

import java.math.BigDecimal;

import ee.adit.dvk.api.SelectCriteria;

public class SelectCriteriaSetting extends SelectCriteria {

    @SuppressWarnings("unused")
    private Long id;
    @SuppressWarnings("unused")
    private String institutionCode;
    @SuppressWarnings("unused")
    private String institutionName;
    @SuppressWarnings("unused")
    private String personalIdCode;
    @SuppressWarnings("unused")
    private Long unitId;
    @SuppressWarnings("unused")
    private BigDecimal subdivisionCode;
    @SuppressWarnings("unused")
    private BigDecimal occupationCode;

    public void setId(Long id) {
        this.id = id;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public void setPersonalIdCode(String personalIdCode) {
        this.personalIdCode = personalIdCode;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public void setSubdivisionCode(BigDecimal subdivisionCode) {
        this.subdivisionCode = subdivisionCode;
    }

    public void setOccupationCode(BigDecimal occupationCode) {
        this.occupationCode = occupationCode;
    }

}
