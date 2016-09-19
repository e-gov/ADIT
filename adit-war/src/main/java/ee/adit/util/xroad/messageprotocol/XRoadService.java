package ee.adit.util.xroad.messageprotocol;

/**
 * This class models XRoadServiceIdentifierType as defined in the related
 * <a href="http://x-road.eu/xsd/identifiers.xsd">XML schema</a>.
 * 
 * @author Levan Kekelidze
 *
 */
public class XRoadService extends XRoadIdentifier {
	
	public XRoadService() {}
	
	/**
	 * A constructor for XRoad service with <em>mandatory</em> parameters.
	 * 
	 * @param xRoadInstance
	 * @param memberClass
	 * @param memberCode
	 * @param serviceCode
	 */
	public XRoadService(String xRoadInstance, String memberClass, String memberCode, String serviceCode) {
		this.xRoadInstance = xRoadInstance;
		this.memberClass = memberClass;
		this.memberCode = memberCode;
		this.serviceCode = serviceCode;
	}
	
	/**
	 * A constructor for XRoad service.
	 * 
	 * <p>
	 * NOTE:<br>
	 * According to the XRoadServiceIdentifierType XSD definition {@code subsystemCode} and {@code serviceVersion} are <em>optional</em>.
	 * </p>
	 * 
	 * @param xRoadInstance
	 * @param memberClass
	 * @param memberCode
	 * @param subsystemCode
	 * @param serviceCode
	 * @param serviceVersion
	 */
	public XRoadService(String xRoadInstance, String memberClass, String memberCode, String subsystemCode, String serviceCode, String serviceVersion) {
		this(xRoadInstance, memberClass, memberCode, serviceCode);
		
		this.subsystemCode = subsystemCode;
		this.serviceVersion = serviceVersion;
	}
	
	public String getxRoadInstance() {
		return xRoadInstance;
	}

	public void setxRoadInstance(String xRoadInstance) {
		this.xRoadInstance = xRoadInstance;
	}

	public String getMemberClass() {
		return memberClass;
	}

	public void setMemberClass(String memberClass) {
		this.memberClass = memberClass;
	}

	public String getMemberCode() {
		return memberCode;
	}

	public void setMemberCode(String memberCode) {
		this.memberCode = memberCode;
	}

	public String getSubsystemCode() {
		return subsystemCode;
	}

	public void setSubsystemCode(String subsystemCode) {
		this.subsystemCode = subsystemCode;
	}
	
	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
	
}
