package ee.adit.util.xroad.messageprotocol;

/**
 * This enumeration class models X-Road identifier types as defined in the related
 * <a href="http://x-road.eu/xsd/identifiers.xsd">XML schema</a>.
 * 
 * @author Levan Kekelidze
 */
public enum XRoadIdentifierType {

	/**
	 * Identifies the X-Road instance. This field is applicable to all identifier types.
	 */
	XROAD_INSTANCE("xRoadInstance"),
	
	/**
	 * Type of the member (company, government institution, private person, etc.)
	 */
	MEMBER_CLASS("memberClass"),
	
	/**
	 * Code that uniquely identifies a member of given member type.
	 */
	MEMBER_CODE("memberCode"),
	
	/**
	 * Code that uniquely identifies a subsystem of given X-Road member.
	 */
	SUBSYSTEM_CODE("subsystemCode"),
	
	/**
	 * Code that uniquely identifies a global group in given X-Road instance.
	 */
	GROUP_CODE("groupCode"),
	
	/**
	 * Code that uniquely identifies a service offered by given X-Road member or subsystem.
	 */
	SERVICE_CODE("serviceCode"),
	
	/**
	 * Version of the service.
	 */
	SERVICE_VERSION("serviceVersion"),
	
	/**
	 * Code that uniquely identifies security category in a given X-Road instance.
	 */
	SECURITY_CATEGORY_CODE("securityCategoryCode"),
	
	/**
	 * Code that uniquely identifies security server offered by a given X-Road member or subsystem.<
	 */
	SERVER_CODE("serverCode");
	
	private final String name;
	
	private XRoadIdentifierType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
