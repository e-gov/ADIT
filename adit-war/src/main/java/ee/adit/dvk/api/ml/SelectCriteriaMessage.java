package ee.adit.dvk.api.ml;

import java.math.BigDecimal;
import java.util.Date;

import ee.adit.dvk.api.SelectCriteria;

public class SelectCriteriaMessage extends SelectCriteria {
    @SuppressWarnings("unused")
    private Long dhlMessageId;
    @SuppressWarnings("unused")
    private Boolean isIncoming;
    @SuppressWarnings("unused")
    private String title;
    @SuppressWarnings("unused")
    private String senderOrgCode;
    @SuppressWarnings("unused")
    private String senderOrgName;
    @SuppressWarnings("unused")
    private String senderPersonCode;
    @SuppressWarnings("unused")
    private String senderName;
    @SuppressWarnings("unused")
    private String recipientOrgCode;
    @SuppressWarnings("unused")
    private String recipientOrgName;
    @SuppressWarnings("unused")
    private String recipientPersonCode;
    @SuppressWarnings("unused")
    private String recipientName;
    @SuppressWarnings("unused")
    private String caseName;
    @SuppressWarnings("unused")
    private String dhlFolderName;
    @SuppressWarnings("unused")
    private Long sendingStatusId;
    @SuppressWarnings("unused")
    private Long unitId;
    @SuppressWarnings("unused")
    private Long dhlId;
    @SuppressWarnings("unused")
    private Date sendingDate;
    @SuppressWarnings("unused")
    private Date receivedDate;
    @SuppressWarnings("unused")
    private Long localItemId;
    @SuppressWarnings("unused")
    private Long recipientStatusId;
    @SuppressWarnings("unused")
    private String faultCode;
    @SuppressWarnings("unused")
    private String faultActor;
    @SuppressWarnings("unused")
    private String faultString;
    @SuppressWarnings("unused")
    private String faultDetail;
    @SuppressWarnings("unused")
    private Long statusUpdateNeeded;
    @SuppressWarnings("unused")
    private String metaxml;
    @SuppressWarnings("unused")
    private String queryId;
    @SuppressWarnings("unused")
    private String proxyOrgCode;
    @SuppressWarnings("unused")
    private String proxyOrgName;
    @SuppressWarnings("unused")
    private String proxyPersonCode;
    @SuppressWarnings("unused")
    private String proxyName;
    @SuppressWarnings("unused")
    private String recipientDepartmentNr;
    @SuppressWarnings("unused")
    private String recipientDepartmentName;
    @SuppressWarnings("unused")
    private String recipientEmail;
    @SuppressWarnings("unused")
    private BigDecimal recipientDivisionId;
    @SuppressWarnings("unused")
    private String recipientDivisionName;
    @SuppressWarnings("unused")
    private BigDecimal recipientPositionId;
    @SuppressWarnings("unused")
    private String recipientPositionName;

    public SelectCriteriaMessage() {
    }

    public void setDhlMessageId(Long dhlMessageId) {
        this.dhlMessageId = dhlMessageId;
    }

    public void setIsIncoming(Boolean isIncoming) {
        this.isIncoming = isIncoming;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSenderOrgCode(String senderOrgCode) {
        this.senderOrgCode = senderOrgCode;
    }

    public void setSenderOrgName(String senderOrgName) {
        this.senderOrgName = senderOrgName;
    }

    public void setSenderPersonCode(String senderPersonCode) {
        this.senderPersonCode = senderPersonCode;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setRecipientOrgCode(String recipientOrgCode) {
        this.recipientOrgCode = recipientOrgCode;
    }

    public void setRecipientOrgName(String recipientOrgName) {
        this.recipientOrgName = recipientOrgName;
    }

    public void setRecipientPersonCode(String recipientPersonCode) {
        this.recipientPersonCode = recipientPersonCode;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public void setDhlFolderName(String dhlFolderName) {
        this.dhlFolderName = dhlFolderName;
    }

    public void setSendingStatusId(Long sendingStatusId) {
        this.sendingStatusId = sendingStatusId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public void setDhlId(Long dhlId) {
        this.dhlId = dhlId;
    }

    public void setSendingDate(Date sendingDate) {
        this.sendingDate = sendingDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public void setLocalItemId(Long localItemId) {
        this.localItemId = localItemId;
    }

    public void setRecipientStatusId(Long recipientStatusId) {
        this.recipientStatusId = recipientStatusId;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public void setFaultActor(String faultActor) {
        this.faultActor = faultActor;
    }

    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    public void setFaultDetail(String faultDetail) {
        this.faultDetail = faultDetail;
    }

    public void setStatusUpdateNeeded(Long statusUpdateNeeded) {
        this.statusUpdateNeeded = statusUpdateNeeded;
    }

    public void setMetaxml(String metaxml) {
        this.metaxml = metaxml;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public void setProxyOrgCode(String proxyOrgCode) {
        this.proxyOrgCode = proxyOrgCode;
    }

    public void setProxyOrgName(String proxyOrgName) {
        this.proxyOrgName = proxyOrgName;
    }

    public void setProxyPersonCode(String proxyPersonCode) {
        this.proxyPersonCode = proxyPersonCode;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public void setRecipientDepartmentNr(String recipientDepartmentNr) {
        this.recipientDepartmentNr = recipientDepartmentNr;
    }

    public void setRecipientDepartmentName(String recipientDepartmentName) {
        this.recipientDepartmentName = recipientDepartmentName;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public void setRecipientDivisionId(BigDecimal recipientDivisionId) {
        this.recipientDivisionId = recipientDivisionId;
    }

    public void setRecipientDivisionName(String recipientDivisionName) {
        this.recipientDivisionName = recipientDivisionName;
    }

    public void setRecipientPositionId(BigDecimal recipientPositionId) {
        this.recipientPositionId = recipientPositionId;
    }

    public void setRecipientPositionName(String recipientPositionName) {
        this.recipientPositionName = recipientPositionName;
    }
}
