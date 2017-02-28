package ee.adit.dvk.api;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author User
 *         Delegate for work with a record from the table DHL_MESSAGE
 */
public interface IMessage extends IMessageObserver, IDvkElement {
    /**
     * Sets the indication that message is incoming or not.
     *
     * @param isIncoming boolean flag.
     */
    void setIsIncoming(boolean isIncoming);

    /**
     * Sets the data of the message.
     *
     * @param data message data as {@link String}.
     */
    void setData(String data);

    /**
     * Sets the title of the message.
     *
     * @param title title text.
     */
    void setTitle(String title);

    /**
     * Sets sender organization code of this message.
     *
     * @param senderOrgCode organization code.
     */
    void setSenderOrgCode(String senderOrgCode);

    /**
     * Sets sender organization name of this message.
     *
     * @param senderOrgName organization name
     */
    void setSenderOrgName(String senderOrgName);

    /**
     * Sets sender person code of this message.
     *
     * @param senderPersonCode person code
     */
    void setSenderPersonCode(String senderPersonCode);

    /**
     * Sets sender's name of this massage.
     *
     * @param senderName sender's name
     */
    void setSenderName(String senderName);

    /**
     * Sets recipient's organization code of this massage.
     *
     * @param recipientOrgCode recipient's organization code
     */
    void setRecipientOrgCode(String recipientOrgCode);

    /**
     * Sets recipient's organization name of this massage.
     *
     * @param recipientOrgName recipient's organization name
     */
    void setRecipientOrgName(String recipientOrgName);

    /**
     * Sets recipient's person code of this massage.
     *
     * @param recipientPersonCode recipient's person code
     */
    void setRecipientPersonCode(String recipientPersonCode);

    /**
     * Sets recipient's name of this massage.
     *
     * @param recipientName recipient's name
     */
    void setRecipientName(String recipientName);

    /**
     * Sets case name of this massage.
     *
     * @param caseName case name
     */
    void setCaseName(String caseName);

    /**
     * Sets DHL folder name of this massage.
     *
     * @param dhlFolderName DHL folder name
     */
    void setDhlFolderName(String dhlFolderName);

    /**
     * Sets sending status ID of this massage.
     *
     * @param sendingStatusId sending status ID
     */
    void setSendingStatusId(long sendingStatusId);

    /**
     * Sets unit ID of this massage.
     *
     * @param unitId unit ID
     */
    void setUnitId(long unitId);

    /**
     * Sets DHL ID of this massage.
     *
     * @param dhlId DHL ID
     */
    void setDhlId(Long dhlId);

    /**
     * Sets the date the message has been sent
     *
     * @param sendingDate the date of sending
     */
    void setSendingDate(Date sendingDate);

    /**
     * Sets the date the message has been received
     *
     * @param receivedDate the date of receiving
     */
    void setReceivedDate(Date receivedDate);

    /**
     * Sets the local item ID of the message
     *
     * @param localItemId local item ID
     */
    void setLocalItemId(Long localItemId);

    /**
     * Sets recipient's status ID of the message.
     *
     * @param recipientStatusId recipient's status ID
     */
    void setRecipientStatusId(Long recipientStatusId);

    /**
     * Sets fault code of the message.
     *
     * @param faultCode fault code
     */
    void setFaultCode(String faultCode);

    /**
     * Sets fault actor of the message.
     *
     * @param faultActor fault actor
     */
    void setFaultActor(String faultActor);

    /**
     * Sets fault's description of the message.
     *
     * @param faultString fault's description
     */
    void setFaultString(String faultString);

    /**
     * Sets fault's detail of the message.
     *
     * @param faultDetail fault's detail
     */
    void setFaultDetail(String faultDetail);

    /**
     * Sets status needs for update value of the message.
     *
     * @param statusUpdateNeeded status needs for update value
     */
    void setStatusUpdateNeeded(Long statusUpdateNeeded);

    /**
     * Sets meta-xml data of the message.
     *
     * @param metaxml meta-xml data
     */
    void setMetaxml(String metaxml);

    /**
     * Sets query ID of the message.
     *
     * @param queryId query's ID
     */
    void setQueryId(String queryId);

    /**
     * Sets proxy's organization code of the message.
     *
     * @param proxyOrgCode proxy's organization code
     */
    void setProxyOrgCode(String proxyOrgCode);

    /**
     * Sets proxy's organization name of the message.
     *
     * @param proxyOrgName proxy's organization name
     */
    void setProxyOrgName(String proxyOrgName);

    /**
     * Sets proxy's person code of the message.
     *
     * @param proxyPersonCode proxy's person code
     */
    void setProxyPersonCode(String proxyPersonCode);

    /**
     * Sets proxy's name of the message.
     *
     * @param proxyName proxy's name
     */
    void setProxyName(String proxyName);

    /**
     * Sets recipient's department number of the message.
     *
     * @param recipientDepartmentNr recipient's department number
     */
    void setRecipientDepartmentNr(String recipientDepartmentNr);

    /**
     * Sets recipient's department name of the message.
     *
     * @param recipientDepartmentName recipient's department name
     */
    void setRecipientDepartmentName(String recipientDepartmentName);

    /**
     * Sets recipient's e-mail of the message.
     *
     * @param recipientEmail recipient's e-mail
     */
    void setRecipientEmail(String recipientEmail);

    /**
     * Sets recipient's division ID of the message.
     *
     * @param recipientDivisionId recipient's division ID
     */
    void setRecipientDivisionId(BigDecimal recipientDivisionId);

    /**
     * Sets recipient's division name of the message.
     *
     * @param recipientDivisionName recipient's division name
     */
    void setRecipientDivisionName(String recipientDivisionName);

    /**
     * Sets recipient's position ID of the message.
     *
     * @param recipientPositionId recipient's position ID
     */
    void setRecipientPositionId(BigDecimal recipientPositionId);

    /**
     * Sets recipient's position name of the message.
     *
     * @param recipientPositionName recipient's position name
     */
    void setRecipientPositionName(String recipientPositionName);

    /**
     * Sets proxy's DHL GUID
     *
     * @param guid
     */
    void setDhlGuid(String guid);

    /**
     * Adds an existing message recipient to a pending list of message recipients if it doesn't
     * contain such a message recipient yet and will change its message DHL ID after message's or
     * message recipient's save method will be called.
     *
     * @param recipient message recipient
     * @return true if the message contains no such a recipient yet
     */
    boolean addRecipient(IMessageRecipient recipient);

    /**
     * Creates a new message recipient and puts it to the pending list of message recipients and will
     * save it after message's or message recipient's save method will be called.
     *
     * @param createArgs mandatory arguments
     * @return {@link IMessageRecipient}
     */
    IMessageRecipient createMessageRecipient(MessageRecipientCreateArgs createArgs);

    /**
     * Returns immutable proxy-object of this message containing actual
     * values directly from the data storage.
     *
     * @return {@link IMessageObserver}
     */
    IMessageObserver getOrigin();
}
