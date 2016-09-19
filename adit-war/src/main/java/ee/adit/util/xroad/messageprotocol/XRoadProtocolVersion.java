package ee.adit.util.xroad.messageprotocol;

/**
 * This enumeration class contains data related to X-Road message protocol versions.
 * 
 * @author Levan Kekelidze
 */
public enum XRoadProtocolVersion {

	/**
	 * Supported by X-Road security server v1 - v4
	 */
	V1_0("1.0", "", ""),
	
	/**
	 * Supported by X-Road security server v2 - v5
	 */
	V2_0("2.0", "http://x-tee.riik.ee/xsd/xtee.xsd", "xtee"),
	
	/**
	 * Supported by X-Road security server v5
	 */
	V3_0("3.0", "http://x-rd.net/xsd/xroad.xsd", "xrd"),
	
	/**
	 * Supported by X-Road security server v5
	 */
	V3_1("3.1", "http://x-road.ee/xsd/x-road.xsd", "xrd"),
	
	/**
	 * Supported by X-Road security server v6
	 */
	V4_0("4.0", "http://x-road.eu/xsd/xroad.xsd", "xrd");
	
	private final String value;
	
	private final String namespaceURI;
	
	private final String namespacePrefix;
	
	private XRoadProtocolVersion(String value, String namespaceURI, String namespacePrefix) {
		this.value = value;
		this.namespaceURI = namespaceURI;
		this.namespacePrefix = namespacePrefix;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getNamespaceURI() {
		return namespaceURI;
	}
	
	public String getNamespacePrefix() {
		return namespacePrefix;
	}
	
}
