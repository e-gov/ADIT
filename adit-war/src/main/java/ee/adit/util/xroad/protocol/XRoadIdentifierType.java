package ee.adit.util.xroad.protocol;

public enum XRoadIdentifierType {

	XROAD_INSTANCE("xRoadInstance"),
	MEMBER_CLASS("memberClass"),
	MEMBER_CODE("memberCode"),
	SUBSYSTEM_CODE("subsystemCode"),
	SERVICE_CODE("serviceCode"),
	SERVICE_VERSION("serviceVersion");
	
	private final String name;
	
	private XRoadIdentifierType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
