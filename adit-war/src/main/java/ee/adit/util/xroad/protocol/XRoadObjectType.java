package ee.adit.util.xroad.protocol;

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
