package ee.adit.util.xroad.messageprotocol;

/**
 * This class models XRoadIdentifierType as defined in the related
 * <a href="http://x-road.eu/xsd/identifiers.xsd">XML schema</a>.
 * 
 * @author Levan Kekelidze
 *
 */
public abstract class XRoadIdentifier {
	
	public static final String NAMESPACE_URI = "http://x-road.eu/xsd/identifiers";
	
    public static final String NAMESPACE_PREFIX = "id";
    
    public static final String OBJECT_TYPE_ATTRIBUTE = "objectType";

	protected String xRoadInstance;
	
	protected String memberClass;
	
	protected String memberCode;
	
	protected String subsystemCode;
	
	protected String groupCode;
	
	protected String serviceCode;
	
	protected String serviceVersion;
	
	protected String securityCategoryCode;
	
	protected String serverCode;
	
}
