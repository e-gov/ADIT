
package ee.adit.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Array of document action statuses
 * 					
 * 
 * <p>Java class for arrayOfDocumentActionStatus complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="arrayOfDocumentActionStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="document" type="{http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid}documentActionStatus" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "arrayOfDocumentActionStatus", propOrder = {
    "document"
})
public class ArrayOfDocumentActionStatus {

    protected List<DocumentActionStatus> document;

    /**
     * Gets the value of the document property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the document property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocument().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DocumentActionStatus }
     * 
     * 
     */
    public List<DocumentActionStatus> getDocument() {
        if (document == null) {
            document = new ArrayList<DocumentActionStatus>();
        }
        return this.document;
    }
    

    public void setDocument(List<DocumentActionStatus> doc) {
        this.document = doc;
    }
    
    public void addDocument(DocumentActionStatus status) {
        if (this.document == null) {
            this.document = new ArrayList<DocumentActionStatus>();
        }
        this.document.add(status);
    }

}
