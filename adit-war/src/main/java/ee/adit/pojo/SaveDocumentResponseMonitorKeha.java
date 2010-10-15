package ee.adit.pojo;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlElement;

public class SaveDocumentResponseMonitorKeha {
	
	protected Success success;
    @XmlElement(required = true)
    protected ArrayOfMessage messages;
    @XmlElement(name = "document_id", required = true)
    protected long documentId;
    protected SaveDocumentRequestDocument document;

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
     * Gets the value of the documentId property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public long getDocumentId() {
        return documentId;
    }

    /**
     * Sets the value of the documentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDocumentId(long value) {
        this.documentId = value;
    }

	public Success getSuccess() {
		return success;
	}

	public void setSuccess(Success success) {
		this.success = success;
	}

    /**
     * Gets the value of the document property.
     * 
     * @return
     *     possible object is
     *     {@link SaveDocumentRequestDocument }
     *     
     */
    public SaveDocumentRequestDocument getDocument() {
        return document;
    }

    /**
     * Sets the value of the document property.
     * 
     * @param value
     *     allowed object is
     *     {@link SaveDocumentRequestDocument }
     *     
     */
    public void setDocument(SaveDocumentRequestDocument value) {
        this.document = value;
    }
	
}
