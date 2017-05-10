package ee.adit.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "item", propOrder = {"dhxReceiptId" })
public class GetSendStatusRequestAttachmentV2 {
    
    @XmlElement(name = "dhx_receipt_id")
    private List<String> dhxReceiptIds;

	/**
	 * @return the dhxReceiptIds
	 */
	public List<String> getDhxReceiptIds() {
		return dhxReceiptIds;
	}

	/**
	 * @param dhxReceiptIds the dhxReceiptIds to set
	 */
	public void setDhxReceiptIds(List<String> dhxReceiptIds) {
		this.dhxReceiptIds = dhxReceiptIds;
	}

    

}
