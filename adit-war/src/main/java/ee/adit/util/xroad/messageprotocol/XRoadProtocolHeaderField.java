package ee.adit.util.xroad.messageprotocol;

/**
 * This enumeration class models SOAP headers that are used by the X-Road system.
 * These SOAP headers are defined in the related
 * <a href="http://x-road.eu/docs/x-road_message_protocol_v4.0.pdf">technical specification</a>.
 * 
 * @author Levan Kekelidze
 */
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
