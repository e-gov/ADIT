package ee.adit.dvk.api.ml;

import java.math.BigDecimal;

import ee.adit.dvk.api.SelectCriteria;

public class SelectCriteriaMessageRecipient extends SelectCriteria {
    private Long dhlMessageId;
    private String recipientOrgCode;
    private String recipientPersonCode;
    private BigDecimal recipientDivisionId;
    private BigDecimal recipientPositionId;
    private Long sendingStatusId;

    public SelectCriteriaMessageRecipient() {
    }

    public Long getDhlMessageId() {
        return dhlMessageId;
    }

    public void setDhlMessageId(Long dhlMessageId) {
        this.dhlMessageId = dhlMessageId;
    }

    public String getRecipientOrgCode() {
        return recipientOrgCode;
    }

    public void setRecipientOrgCode(String recipientOrgCode) {
        this.recipientOrgCode = recipientOrgCode;
    }

    public String getRecipientPersonCode() {
        return recipientPersonCode;
    }

    public void setRecipientPersonCode(String recipientPersonCode) {
        this.recipientPersonCode = recipientPersonCode;
    }

    public BigDecimal getRecipientDivisionId() {
        return recipientDivisionId;
    }

    public void setRecipientDivisionId(BigDecimal recipientDivisionId) {
        this.recipientDivisionId = recipientDivisionId;
    }

    public BigDecimal getRecipientPositionId() {
        return recipientPositionId;
    }

    public void setRecipientPositionId(BigDecimal recipientPositionId) {
        this.recipientPositionId = recipientPositionId;
    }

    public Long getSendingStatusId() {
        return sendingStatusId;
    }

    public void setSendingStatusId(Long sendingStatusId) {
        this.sendingStatusId = sendingStatusId;
    }
}
