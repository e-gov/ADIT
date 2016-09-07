package ee.adit.util.xroad.protocol;

public enum XRoadProtocolHeaderField {

	CLIENT("client"),
	SERVICE("service"),
	CENTRAL_SERVICE("centralService"),
	ID("id"),
	USER_ID("userId"),
	ISSUE("issue"),
	PROTOCOL_VERSION("protocolVersion"),
	REQUEST_HASH("requestHash"),
	REQUEST_HASH_ALGORITHM_ID("requestHash/@algorithmId");
	
	private final String value;
	
	private XRoadProtocolHeaderField(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
}
