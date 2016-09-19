package ee.adit.util.xroad.messageprotocol;

/**
 * This enumeration class models X-Road object types as defined in the related
 * <a href="http://x-road.eu/xsd/identifiers.xsd">XML schema</a>.
 * 
 * @author Levan Kekelidze
 */
public enum XRoadObjectType {

	MEMBER("MEMBER"),
	SUBSYSTEM("SUBSYSTEM"),
	SERVER("SERVER"),
	GLOBALGROUP("GLOBALGROUP"),
	LOCALGROUP("LOCALGROUP"),
	SECURITYCATEGORY("SECURITYCATEGORY"),
	SERVICE("SERVICE"),
	CENTRAL_SERVICE("CENTRALSERVICE");
	
	private final String name;
	
	private XRoadObjectType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
