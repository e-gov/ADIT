//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.07.09 at 04:42:08 PM EEST 
//

package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for GetDocumentListResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="GetDocumentListResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="success" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="messages" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}ArrayOfMessage"/>
 *         &lt;element name="document_list">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="href" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetDocumentListResponse", propOrder = {"success", "messages", "documentList" })
public class GetDocumentListResponse {

    private boolean success;
    @XmlElement(required = true)
    private ArrayOfMessage messages;
    @XmlElement(name = "document_list", required = true)
    private GetDocumentListResponseList documentList;

    /**
     * Gets the value of the success property.
     * 
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the value of the success property.
     * 
     */
    public void setSuccess(boolean value) {
        this.success = value;
    }

    /**
     * Gets the value of the messages property.
     * 
     * @return possible object is {@link ArrayOfMessage }
     * 
     */
    public ArrayOfMessage getMessages() {
        return messages;
    }

    /**
     * Sets the value of the messages property.
     * 
     * @param value
     *            allowed object is {@link ArrayOfMessage }
     * 
     */
    public void setMessages(ArrayOfMessage value) {
        this.messages = value;
    }

    /**
     * Gets the value of the documentList property.
     * 
     * @return possible object is {@link GetDocumentListResponseList }
     * 
     */
    public GetDocumentListResponseList getDocumentList() {
        return documentList;
    }

    /**
     * Sets the value of the documentList property.
     * 
     * @param value
     *            allowed object is {@link GetDocumentListResponseList }
     * 
     */
    public void setDocumentList(GetDocumentListResponseList value) {
        this.documentList = value;
    }
}
