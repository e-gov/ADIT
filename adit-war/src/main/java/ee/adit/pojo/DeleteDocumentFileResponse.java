package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeleteDocumentFileResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeleteDocumentFileResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="success" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="messages" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}ArrayOfMessage"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeleteDocumentFileResponse", propOrder = {
    "success",
    "messages"
})
public class DeleteDocumentFileResponse {

    protected Success success;
    @XmlElement(required = true)
    protected ArrayOfMessage messages;

    /**
     * Gets the value of the messages property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMessage }
     *     
     */
    public ArrayOfMessage getMessages() {
        return messages;
    }

    /**
     * Sets the value of the messages property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMessage }
     *     
     */
    public void setMessages(ArrayOfMessage value) {
        this.messages = value;
    }

	public Success getSuccess() {
		return success;
	}

	public void setSuccess(Success success) {
		this.success = success;
	}
}
