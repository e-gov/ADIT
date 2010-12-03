package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for DeleteDocumentFileRequest complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DeleteDocumentFileRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="document_id" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="file_id" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeleteDocumentFileRequest", propOrder = {"documentId", "fileId" })
public class DeleteDocumentFileRequest {

    @XmlElement(name = "document_id", required = true)
    protected long documentId;
    @XmlElement(name = "file_id", required = true)
    protected long fileId;

    /**
     * Gets the value of the documentId property.
     * 
     * @return possible long
     * 
     */
    public long getDocumentId() {
        return documentId;
    }

    /**
     * Sets the value of the documentId property.
     * 
     * @param value
     *            allowed long
     * 
     */
    public void setDocumentId(long value) {
        this.documentId = value;
    }

    /**
     * Gets the value of the fileId property.
     * 
     * @return possible long
     * 
     */
    public long getFileId() {
        return fileId;
    }

    /**
     * Sets the value of the fileId property.
     * 
     * @param value
     *            allowed long
     * 
     */
    public void setFileId(long value) {
        this.fileId = value;
    }
}
