package ee.adit.util.xroad.messageprotocol;

/**
 * This class models XRoadClientIdentifierType as defined in the related
 * <a href="http://x-road.eu/xsd/identifiers.xsd">XML schema</a>.
 * 
 * @author Levan Kekelidze
 *
 */
public class XRoadClient extends XRoadIdentifier {
	
	/**
	 * A constructor for X-Road client with <em>mandatory</em> parameters.
	 * 
	 * @param xRoadInstance 
	 * @param memberClass
	 * @param memberCode
	 */
	public XRoadClient(String xRoadInstance, String memberClass, String memberCode) {
		this.xRoadInstance = xRoadInstance;
		this.memberClass = memberClass;
		this.memberCode = memberCode;
	}
	
	/**
	 * A constructor for X-Road client.
	 * 
	 * <p>
	 * NOTE:<br>
	 * According to the XRoadServiceIdentifierType XSD definition {@code subsystemCode} is <em>optional</em>.
	 * </p>
	 * 
	 * @param xRoadInstance
	 * @param memberClass
	 * @param memberCode
	 * @param subsytemCode
	 */
	public XRoadClient(String xRoadInstance, String memberClass, String memberCode, String subsytemCode) {
		this(xRoadInstance, memberClass, memberCode);
		
		this.subsystemCode = subsytemCode;
		
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
	
}
