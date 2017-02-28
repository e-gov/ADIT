package ee.adit.dvk.api.ml;

import ee.adit.dvk.api.SelectCriteria;

public class SelectCriteriaOrganization extends SelectCriteria {
    @SuppressWarnings("unused")
    private String orgCode;
    @SuppressWarnings("unused")
    private String orgName;
    @SuppressWarnings("unused")
    private boolean dhlCapable;
    @SuppressWarnings("unused")
    private boolean dhlDirectCapable;
    @SuppressWarnings("unused")
    private String dhlDirectProducerName;
    @SuppressWarnings("unused")
    private String dhlDirectServiceUrl;

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public void setDhlCapable(boolean dhlCapable) {
        this.dhlCapable = dhlCapable;
    }

    public void setDhlDirectCapable(boolean dhlDirectCapable) {
        this.dhlDirectCapable = dhlDirectCapable;
    }

    public void setDhlDirectProducerName(String dhlDirectProducerName) {
        this.dhlDirectProducerName = dhlDirectProducerName;
    }

    public void setDhlDirectServiceUrl(String dhlDirectServiceUrl) {
        this.dhlDirectServiceUrl = dhlDirectServiceUrl;
    }

}
