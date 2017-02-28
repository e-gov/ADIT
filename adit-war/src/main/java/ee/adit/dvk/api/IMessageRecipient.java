package ee.adit.dvk.api;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author User
 *         Delegate for work with a record from the table DHL_MESSAGE_RECIPIENT.
 *         Serves as relation between message and recipient.
 */
public interface IMessageRecipient extends IMessageRecipientObserver, IDvkElement {
    /**
     * Sets recipient's organization name of this relation between message and recipient.
     *
     * @param recipientOrgName recipient's organization name {@link String}
     */
    void setRecipientOrgName(String recipientOrgName);

    /**
     * Sets recipient's name of this relation between message and recipient.
     *
     * @param recipientName recipient's name {@link String}
     */
    void setRecipientName(String recipientName);

    /**
     * Sets the date of sending of this relation between message and recipient.
     *
     * @param sendingDate the date of the sending {@link Date}
     */
    void setSendingDate(Date sendingDate);

    /**
     * Sets the date of receiving of this relation between message and recipient.
     *
     * @param receivedDate the date of receiving {@link Date}
     */
    void setReceivedDate(Date receivedDate);

    /**
     * Sets recipient's status ID of this relation between message and recipient.
     *
     * @param recipientStatusId recipient's status ID {@link Long}
     */
    void setRecipientStatusId(Long recipientStatusId);

    /**
     * Sets fault code of this relation between message and recipient.
     *
     * @param faultCode fault code {@link String}
     */
    void setFaultCode(String faultCode);

    /**
     * Sets fault actor of this relation between message and recipient.
     *
     * @param faultActor fault actor {@link String}
     */
    void setFaultActor(String faultActor);

    /**
     * Sets fault's description of this relation between message and recipient.
     *
     * @param faultString fault's description {@link String}
     */
    void setFaultString(String faultString);

    /**
     * Sets fault's detail of this relation between message and recipient.
     *
     * @param faultDetail fault's detail {@link String}
     */
    void setFaultDetail(String faultDetail);

    /**
     * Sets meta-xml data of this relation between message and recipient.
     *
     * @param metaxml meta-xml data {@link String}
     */
    void setMetaxml(String metaxml);

    /**
     * Sets DHL ID of this relation between message and recipient.
     *
     * @param dhlId DHL ID {@link BigDecimal}
     */
    void setDhlId(BigDecimal dhlId);

    /**
     * Sets query ID of this relation between message and recipient.
     *
     * @param queryId query ID {@link String}
     */
    void setQueryId(String queryId);

    /**
     * Sets producer's name of this relation between message and recipient.
     *
     * @param producerName producer's name {@link String}
     */
    void setProducerName(String producerName);

    /**
     * Sets service URL of this relation between message and recipient.
     *
     * @param serviceUrl service URL {@link String}
     */
    void setServiceUrl(String serviceUrl);

    /**
     * Sets recipient's division name of this relation between message and recipient.
     *
     * @param recipientDivisionName recipient's division name {@link String}
     */
    void setRecipientDivisionName(String recipientDivisionName);

    /**
     * Sets recipient's position name of this relation between message and recipient.
     *
     * @param recipientPositionName recipient's position name {@link String}
     */
    void setRecipientPositionName(String recipientPositionName);

    /**
     * Returns immutable proxy-object of this relation between message and recipient containing actual
     * values directly from the data storage.
     *
     * @return {@link IMessageRecipientObserver}
     */
    IMessageRecipientObserver getOrigin();
}
