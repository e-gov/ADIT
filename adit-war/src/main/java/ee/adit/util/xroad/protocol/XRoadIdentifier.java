package ee.adit.util.xroad.protocol;

/**
 * This class models XRoadIdentifierType as defined in the related
 * <a href="http://x-road.eu/xsd/identifiers.xsd">XML schema</a>.
 * 
 * @author Levan Kekelidze
 *
 */
public abstract class XRoadIdentifier {

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
