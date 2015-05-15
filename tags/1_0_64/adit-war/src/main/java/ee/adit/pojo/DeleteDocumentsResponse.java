
package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * "deleteDocuments" request
 * 						response
 * 
 * <p>Java class for deleteDocumentsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deleteDocumentsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="success" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}success"/>
 *         &lt;element name="messages" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}arrayOfMessage"/>
 *         &lt;element name="documents" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}arrayOfDocumentActionStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deleteDocumentsResponse", propOrder = {
    "success",
    "messages",
    "documents"
})
public class DeleteDocumentsResponse {

    protected Success success;
    @XmlElement(required = true)
    protected ArrayOfMessage messages;
    @XmlElement(required = true)
    protected ArrayOfDocumentActionStatus documents;

    /**
     * Gets the value of the success property.
     * 
     */
    public boolean isSuccess() {
        return success.isSuccess();
    }

    /**
     * Sets the value of the success property.
     * 
     */
    public void setSuccess(Success value) {
        this.success = value;
    }
    
    public Success getSuccess() {
        return success;
    }
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

    /**
     * Gets the value of the documents property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfDocumentActionStatus }
     *     
     */
    public ArrayOfDocumentActionStatus getDocuments() {
        return documents;
    }

    /**
     * Sets the value of the documents property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfDocumentActionStatus }
     *     
     */
    public void setDocuments(ArrayOfDocumentActionStatus value) {
        this.documents = value;
    }

}
