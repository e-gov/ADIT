package ee.adit.dvk.api;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author User
 *         Delegate for a certain relation between message and recipient entry which contains actual
 *         values taken directly from the data storage but not from the cache using that relation's
 *         identification attribute(s).
 */
public interface IMessageRecipientObserver extends IElementObserver {
    /**
     * Returns recipient's organization name of this relation between message and recipient.
     *
     * @return recipient's organization name {@link String}
     */
    String getRecipientOrgName();

    /**
     * Returns recipient's name of this relation between message and recipient.
     *
     * @return recipient's name {@link String}
     */
    String getRecipientName();

    /**
     * Returns the date of sending of this relation between message and recipient.
     *
     * @return the date of sending {@link Date}
     */
    Date getSendingDate();

    /**
     * Returns the date of receiving of this relation between message and recipient.
     *
     * @return the date of receiving {@link Date}
     */
    Date getReceivedDate();

    /**
     * Returns sending status ID of this relation between message and recipient.
     *
     * @return sending status ID {@link long}
     */
    long getSendingStatusId();

    /**
     * Returns recipient's status ID of this relation between message and recipient.
     *
     * @return recipient's status ID {@link Long}
     */
    Long getRecipientStatusId();

    /**
     * Returns fault code of this relation between message and recipient.
     *
     * @return fault code {@link String}
     */
    String getFaultCode();

    /**
     * Returns fault actor of this relation between message and recipient.
     *
     * @return fault actor {@link String}
     */
    String getFaultActor();

    /**
     * Returns fault description of this relation between message and recipient.
     *
     * @return fault description {@link String}
     */
    String getFaultString();

    /**
     * Returns fault detail of this relation between message and recipient.
     *
     * @return fault detail {@link String}
     */
    String getFaultDetail();

    /**
     * Returns meta-xml data of this relation between message and recipient.
     *
     * @return meta-xml data {@link String}
     */
    String getMetaxml();

    /**
     * Returns DHL ID of this relation between message and recipient.
     *
     * @return DHL ID {@link BigDecimal}
     */
    BigDecimal getDhlId();

    /**
     * Returns query's ID of this relation between message and recipient.
     *
     * @return query's ID {@link String}
     */
    String getQueryId();

    /**
     * Returns producer's name of this relation between message and recipient.
     *
     * @return producer's name {@link String}
     */
    String getProducerName();

    /**
     * Returns service URL of this relation between message and recipient.
     *
     * @return service URL {@link String}
     */
    String getServiceUrl();

    /**
     * Returns recipient's division name of this relation between message and recipient.
     *
     * @return recipient's division name {@link String}
     */
    String getRecipientDivisionName();

    /**
     * Returns recipient's position name of this relation between message and recipient.
     *
     * @return recipient's position name {@link String}
     */
    String getRecipientPositionName();

    /**
     * Returns DHL message ID of this relation between message and recipient.
     *
     * @return DHL message ID {@link Long}
     */
    Long getDhlMessageId();

    /**
     * Returns recipient's organization code of this relation between message and recipient.
     *
     * @return recipient's organization code {@link String}
     */
    String getRecipientOrgCode();

    /**
     * Returns recipient's person code of this relation between message and recipient.
     *
     * @return recipient's person code {@link String}
     */
    String getRecipientPersonCode();

    /**
     * Returns recipient's division ID of this relation between message and recipient.
     *
     * @return recipient's division ID {@link BigDecimal}
     */
    BigDecimal getRecipientDivisionId();

    /**
     * Returns recipient's position ID of this relation between message and recipient.
     *
     * @return recipient's position ID {@link BigDecimal}
     */
    BigDecimal getRecipientPositionId();
}
