
package ee.adit.pojo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Array of document identificators
 * 					
 * 
 * <p>Java class for arrayOfDocumentId complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="arrayOfDocumentId">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="document_id" type="{http://www.w3.org/2001/XMLSchema}integer" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "arrayOfDocumentId", propOrder = {
    "documentId"
})
public class ArrayOfDocumentId {

    @XmlElement(name = "document_id")
    protected List<Long> documentId;

    /**
     * Gets the value of the documentId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the documentId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocumentId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     * 
     * 
     */
    public List<Long> getDocumentId() {
        if (documentId == null) {
            documentId = new ArrayList<Long>();
        }
        return this.documentId;
    }

    public String toString () {
    	StringBuilder sb = new StringBuilder();
    	boolean isFirst = true;
    	for (Long Id : documentId)
    	{
    		if (!isFirst) {
    			sb.append(", ");
    		} else {
    			isFirst = false;
    		}
    	    sb.append(Id);
    	}
    	return sb.toString();
    }
}
