package ee.adit.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "item", propOrder = {"dhlId" })
public class GetSendStatusRequestAttachment {
    
    @XmlElement(name = "dhl_id")
    private List<Long> dhlIds;

	public List<Long> getDhlIds() {
		return dhlIds;
	}

	public void setDhlIds(List<Long> dhlIds) {
		this.dhlIds = dhlIds;
	}

}
