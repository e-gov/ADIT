package ee.adit.dvk.api;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author User
 *         Delegate for a certain message entry which contains actual values taken directly
 *         from the data storage but not from the cache using that message's identification
 *         attribute(s).
 */
public interface IMessageObserver extends IElementObserver {
    /**
     * Returns DHL message ID
     *
     * @return DHL message ID
     */
    long getDhlMessageId();

    /**
     * Returns indicating flag is message incoming or not.
     *
     * @return boolean flag
     */
    boolean isIsIncoming();

    /**
     * Returns message data.
     *
     * @return {@link String}
     */
    String getData();

    /**
     * Returns title text of this message.
     *
     * @return {@link String}
     */
    String getTitle();

    /**
     * Returns sender's organization code of this message.
     *
     * @return {@link String}
     */
    String getSenderOrgCode();

    /**
     * Returns sender's organization name of this message.
     *
     * @return {@link String}
     */
    String getSenderOrgName();

    /**
     * Returns sender's person code of this message.
     *
     * @return {@link String}
     */
    String getSenderPersonCode();

    /**
     * Returns sender's name of this message.
     *
     * @return {@link String}
     */
    String getSenderName();

    /**
     * Returns recipient's organization code of this message.
     *
     * @return {@link String}
     */
    String getRecipientOrgCode();

    /**
     * Returns recipient's organization name of this message.
     *
     * @return {@link String}
     */
    String getRecipientOrgName();

    /**
     * Returns recipient's organization name of this message.
     *
     * @return {@link String}
     */
    String getRecipientPersonCode();

    /**
     * Returns recipient's name of this message.
     *
     * @return {@link String}
     */
    String getRecipientName();

    /**
     * Returns case name of this message.
     *
     * @return {@link String}
     */
    String getCaseName();

    /**
     * Returns DHL folder name of this message.
     *
     * @return {@link String}
     */
    String getDhlFolderName();

    /**
     * Returns sending status ID of this message.
     *
     * @return {@link long}
     */
    long getSendingStatusId();

    /**
     * Returns unit ID of this message.
     *
     * @return {@link long}
     */
    long getUnitId();

    /**
     * Returns DHL ID of this message.
     *
     * @return {@link Long}
     */
    Long getDhlId();

    /**
     * Returns the date of sending of this message.
     *
     * @return {@link Date}
     */
    Date getSendingDate();

    /**
     * Returns the date of receiving of this message.
     *
     * @return {@link Date}
     */
    Date getReceivedDate();

    /**
     * Returns local item's ID of this message.
     *
     * @return {@link Long}
     */
    Long getLocalItemId();

    /**
     * Returns recipient's status ID of this message.
     *
     * @return {@link Long}
     */
    Long getRecipientStatusId();

    /**
     * Returns fault code of this message.
     *
     * @return {@link String}
     */
    String getFaultCode();

    /**
     * Returns fault actor of this message.
     *
     * @return {@link String}
     */
    String getFaultActor();

    /**
     * Returns fault description of this message.
     *
     * @return {@link String}
     */
    String getFaultString();

    /**
     * Returns fault detail of this message.
     *
     * @return {@link String}
     */
    String getFaultDetail();

    /**
     * Returns fault code of this message.
     *
     * @return {@link Long}
     */
    Long getStatusUpdateNeeded();

    /**
     * Returns meta-xml data of this message.
     *
     * @return {@link String}
     */
    String getMetaxml();

    /**
     * Returns query ID of this message.
     *
     * @return {@link String}
     */
    String getQueryId();

    /**
     * Returns proxy-organization code of this message.
     *
     * @return {@link String}
     */
    String getProxyOrgCode();

    /**
     * Returns proxy-organization name of this message.
     *
     * @return {@link String}
     */
    String getProxyOrgName();

    /**
     * Returns proxy-person code of this message.
     *
     * @return {@link String}
     */
    String getProxyPersonCode();

    /**
     * Returns proxy name of this message.
     *
     * @return {@link String}
     */
    String getProxyName();

    /**
     * Returns recipient's department number of this message.
     *
     * @return {@link String}
     */
    String getRecipientDepartmentNr();

    /**
     * Returns recipient's department name of this message.
     *
     * @return {@link String}
     */
    String getRecipientDepartmentName();

    /**
     * Returns recipient's e-mail of this message.
     *
     * @return {@link String}
     */
    String getRecipientEmail();

    /**
     * Returns recipient's division ID of this message.
     *
     * @return {@link BigDecimal}
     */
    BigDecimal getRecipientDivisionId();

    /**
     * Returns recipient's division name of this message.
     *
     * @return {@link String}
     */
    String getRecipientDivisionName();

    /**
     * Returns recipient's position ID of this message.
     *
     * @return {@link BigDecimal}
     */
    BigDecimal getRecipientPositionId();

    /**
     * Returns recipient's position name of this message.
     *
     * @return {@link String}
     */
    String getRecipientPositionName();

    /**
     * @return
     */
    String getDhlGuid();

}
