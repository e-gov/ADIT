package ee.adit.util.xroad;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ee.adit.util.xroad.messageprotocol.XRoadClient;
import ee.adit.util.xroad.messageprotocol.XRoadProtocolVersion;
import ee.adit.util.xroad.messageprotocol.XRoadService;
import ee.webmedia.xtee.XTeeHeader;

/**
 * Class for holding all the SOAP headers data.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class CustomXRoadHeader extends XTeeHeader {
	
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = Logger.getLogger(CustomXRoadHeader.class);
    
    public static final String XROAD_NS_URI = "http://x-road.eu/xsd/xroad.xsd";
    public static final String XROAD_NS_PREFIX = "xrd";
    
    public static final String IDENTIFIERS_NS_URI = "http://x-road.eu/xsd/identifiers";
    public static final String IDENTIFIERS_NS_PREFIX = "id";

    // X-Road protocol version 4.0 SOAP header elements
    public static final QName CLIENT = new QName(XROAD_NS_URI, "client");
    public static final QName SERVICE = new QName(XROAD_NS_URI, "service");
    public static final QName USER_ID = new QName(XROAD_NS_URI, "userId");
    public static final QName ID = new QName(XROAD_NS_URI, "id");
    public static final QName PROTOCOL_VERSION = new QName(XROAD_NS_URI, "protocolVersion");
    
    private String id;
    private String userId;
    private String issue;
    
	// Fields specific to X-Road protocol version 4.0
    private XRoadClient xRoadClient;
	private XRoadService xRoadService;
	private XRoadProtocolVersion protocolVersion;
	

    public CustomXRoadHeader(XRoadProtocolVersion protocolVersion) {
    	this.protocolVersion = protocolVersion;
    }
    
    /**
     * A constructor for X-Road protocol version 4.0+
     * 
     * @param xRoadClient
     * @param xRoadService
     * @param id
     * @param userId
     * @param issue
     */
    public CustomXRoadHeader(XRoadClient xRoadClient, XRoadService xRoadService, String id, String userId, String issue, XRoadProtocolVersion protocolVersion) {
    	this.xRoadClient = xRoadClient;
    	this.xRoadService = xRoadService;
    	this.id = id;
    	this.userId = userId;
    	this.issue = issue;
    	this.protocolVersion = protocolVersion;
    }
    
    public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public XRoadClient getXRoadClient() {
		return xRoadClient;
	}

	public void setXRoadClient(XRoadClient xRoadClient) {
		this.xRoadClient = xRoadClient;
	}

	public XRoadService getXRoadService() {
		return xRoadService;
	}

	public void setXRoadService(XRoadService xRoadService) {
		this.xRoadService = xRoadService;
	}

	public XRoadProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(XRoadProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
     * Retrieves the value of the {@code INFOSYSTEEM} SOAP header.
     *
     * @param producerName X-Road producer name of current application/database.
     * @return Value of {@code INFOSYSTEEM} SOAP header
     */
    public String getInfosysteem(final String producerName) {
        String producerNsUri = String.format("http://producers.%s.xtee.riik.ee/producer/%s", producerName, producerName);
        QName infosysteem = new QName(producerNsUri, "infosysteem");

    	if (this.getElemendid() != null) {
            return this.getElemendid().get(infosysteem);
        } else {
            logger.info("Producer name: " + producerName);
            logger.info("Namespace: " + producerNsUri);
            
            return null;
        }
    }
    
    
    /*********************************************************************
     * The below getters are overridden for backward compatibility
     * and to support both X-Road message protocol versions (2.0 and 4.0).
     ********************************************************************/
    
    @Override
    public String getAsutus() {
    	String consumer = "";
    	if (protocolVersion.equals(XRoadProtocolVersion.V2_0)) {
    		consumer = super.getAsutus();
    	} else {
    		consumer = xRoadClient.getMemberCode();
    	}
    	
		return consumer;
	}

    @Override
	public String getAndmekogu() {
    	String producer = "";
    	if (protocolVersion.equals(XRoadProtocolVersion.V2_0)) {
    		producer = super.getAndmekogu();
    	} else {
    		producer = xRoadService.getSubsystemCode();
    	}
    	
    	return producer;
	}

    @Override
	public String getXteeDatabase() {
    	return getAndmekogu();
	}

    @Override
	public String getIsikukood() {
    	String isikukood = "";
    	if (protocolVersion.equals(XRoadProtocolVersion.V2_0)) {
    		isikukood = super.getIsikukood();
    	} else {
    		isikukood = getUserId();
    	}
    	
		return isikukood;
	}
	
    @Override
	public String getId() {
    	String requestId = "";
    	if (protocolVersion.equals(XRoadProtocolVersion.V2_0)) {
    		requestId = super.getId();
    	} else {
    		requestId = this.id;
    	}
    	
		return requestId;
	}
	
    @Override
	public String getNimi() {
    	String serviceName = "";
    	if (protocolVersion.equals(XRoadProtocolVersion.V2_0)) {
    		serviceName = super.getNimi();
    	} else {
    		serviceName = new StringBuilder().
    				append(xRoadService.getSubsystemCode()).append(".").
    				append(xRoadService.getServiceCode()).append(".").
    				append(!StringUtils.isBlank(xRoadService.getServiceVersion()) ? xRoadService.getServiceVersion() : "1").
    				toString();
    	}
    	
		return serviceName;
	}

    @Override
	public String getAmet() {
    	String position = "";
    	if (protocolVersion.equals(XRoadProtocolVersion.V2_0)) {
    		position = super.getAmet();
    	}
    	
    	// There is no equivalent of the "position" ("amet") in the X-Road message protocol version 4.0
    	
		return position;
	}
	
    @Override
	public String getAmetniknimi() {
    	String userName = "";
    	if (protocolVersion.equals(XRoadProtocolVersion.V2_0)) {
    		userName = super.getAmetniknimi();
    	}
    	
    	// There is no equivalent of the "userName" ("ametniknimi") in the X-Road message protocol version 4.0
    	
		return userName;
	}
	
    @Override
	public String getAllasutus() {
    	String unit = "";
    	if (protocolVersion.equals(XRoadProtocolVersion.V2_0)) {
    		unit = super.getAllasutus();
    	}
    	
    	// There is no equivalent of the "unit" ("allasutus") in the X-Road message protocol version 4.0
    	
		return unit;
	}
    
}
