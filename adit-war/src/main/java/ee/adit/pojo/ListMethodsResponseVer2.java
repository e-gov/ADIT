package ee.adit.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ee.adit.generated.xroad.XRoadServiceIdentifierType;

/**
 * This class is used for OXM when preparing a response using X-Road message protocol version 4.0
 * 
 * @author Levan Kekelidze
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListMethodsResponse", propOrder = {"service" })
public class ListMethodsResponseVer2 {
	
    @XmlElement(name = "service")
    private List<XRoadServiceIdentifierType> services;

    public List<XRoadServiceIdentifierType> getServices() {
        if (services == null) {
            services = new ArrayList<XRoadServiceIdentifierType>();
        }
        
        return this.services;
    }

    public void setServices(List<XRoadServiceIdentifierType> services) {
        this.services = services;
    }

    public void addServices(XRoadServiceIdentifierType service) {
        if (this.services == null) {
            this.services = new ArrayList<XRoadServiceIdentifierType>();
        }
        
        this.services.add(service);
    }
}
