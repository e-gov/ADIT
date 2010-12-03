package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for DeflateDocumentRequest complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DeflateDocumentRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="document_id" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeflateDocumentRequest", propOrder = {"documentId" })
public class DeflateDocumentRequest {

    @XmlElement(name = "document_id", required = true)
    protected long documentId;

    /**
     * Gets the value of the documentId property.
     * 
     * @return document id
     * 
     */
    public long getDocumentId() {
        return documentId;
    }

    /**
     * Sets the value of the documentId property.
     * 
     * @param value
     *            document id
     * 
     */
    public void setDocumentId(long value) {
        this.documentId = value;
    }
}
