package ee.adit.ws.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import ee.adit.generated.xroad.XRoadServiceIdentifierType;
import ee.adit.pojo.ListMethodsResponse;
import ee.adit.pojo.ListMethodsResponseVer2;
import ee.adit.util.Util;
import ee.adit.util.xroad.CustomXRoadHeader;
import ee.adit.util.xroad.messageprotocol.XRoadProtocolVersion;

/**
 * Web-service endpoint for "listMethods" service. Returns the list of services provided by ADIT.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 *
 */
public class ListMethodsEndpoint extends AbstractAditBaseEndpoint implements InitializingBean {

	/**
	 * ADIT's service method names in the format suitable for X-Road message protocol version 2.0
	 */
	private List<String> xteeServiceMethodsNames = new ArrayList<String>();
	
	/**
	 * ADIT's service method names in the format suitable for X-Road message protocol version 4.0
	 */
	private List<XRoadServiceIdentifierType> xRoadServices = new ArrayList<XRoadServiceIdentifierType>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		String producerName = this.getConfiguration().getXteeProducerName();
		
		xteeServiceMethodsNames.add(producerName + ".confirmSignature.v1");
		xteeServiceMethodsNames.add(producerName + ".deflateDocument.v1");
		xteeServiceMethodsNames.add(producerName + ".deleteDocument.v1");
		xteeServiceMethodsNames.add(producerName + ".deleteDocuments.v1");
		xteeServiceMethodsNames.add(producerName + ".deleteDocumentFile.v1");
		xteeServiceMethodsNames.add(producerName + ".getDocument.v1");
		xteeServiceMethodsNames.add(producerName + ".getDocument.v2");
		xteeServiceMethodsNames.add(producerName + ".getDocumentFile.v1");
		xteeServiceMethodsNames.add(producerName + ".getDocumentHistory.v1");
		xteeServiceMethodsNames.add(producerName + ".getDocumentList.v1");
		xteeServiceMethodsNames.add(producerName + ".getJoined.v1");
		xteeServiceMethodsNames.add(producerName + ".getNotifications.v1");
		xteeServiceMethodsNames.add(producerName + ".getSendStatus.v1");
		xteeServiceMethodsNames.add(producerName + ".getUserInfo.v1");
		xteeServiceMethodsNames.add(producerName + ".join.v1");
		xteeServiceMethodsNames.add(producerName + ".markDocumentViewed.v1");
		xteeServiceMethodsNames.add(producerName + ".modifyStatus.v1");
		xteeServiceMethodsNames.add(producerName + ".prepareSignature.v1");
		xteeServiceMethodsNames.add(producerName + ".saveDocument.v1");
		xteeServiceMethodsNames.add(producerName + ".saveDocumentFile.v1");
		xteeServiceMethodsNames.add(producerName + ".sendDocument.v1");
		xteeServiceMethodsNames.add(producerName + ".setNotifications.v1");
		xteeServiceMethodsNames.add(producerName + ".shareDocument.v1");
		xteeServiceMethodsNames.add(producerName + ".unJoin.v1");
		xteeServiceMethodsNames.add(producerName + ".unShareDocument.v1");
		xteeServiceMethodsNames.add(producerName + ".getUserContacts.v1");
		xteeServiceMethodsNames.add(producerName + ".listMethods");
		
		xRoadServices.add(Util.populateAditXRoadService("confirmSignature", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("deflateDocument", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("deleteDocument", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("deleteDocuments", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("deleteDocumentFile", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getDocument", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getDocument", "v2", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getDocumentFile", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getDocumentHistory", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getDocumentList", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getJoined", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getNotifications", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getSendStatus", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getUserInfo", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("join", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("markDocumentViewed", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("modifyStatus", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("prepareSignature", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("saveDocument", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("saveDocumentFile", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("sendDocument", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("setNotifications", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("shareDocument", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("unJoin", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("unShareDocument", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getUserContacts", "v1", getConfiguration()));
		xRoadServices.add(Util.populateAditXRoadService("getUserContacts", "v1", getConfiguration()));
		// In accordance with X-Road Service Metadata Protocol (quote): "The response SHALL NOT contain names of the metainfo services."
	}
	
    /**
     * Invocation method (entry point).
     *
     * @param requestObject request object (not used)
     * @param version query version (not used)
     * @return response object
     */
    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        return getResponse();
    }

    /**
     * Gets the response object if an exception occurs.
     * @param ex Exception that occurred
     * @return response object
     */
    @Override
    protected Object getResultForGenericException(Exception ex) {
        return getResponse();
    }

    /**
     * Constructs a response object containing all the query names that ADIT publishes.
     *
     * @return "listMethods" web method result as {@link ListMethodsResponse} object
     */
    private Object getResponse() {
    	Object response = null;
    	
    	CustomXRoadHeader xRoadHeader = getHeader();
    	
    	if (xRoadHeader.getProtocolVersion().equals(XRoadProtocolVersion.V2_0)) {
    		ListMethodsResponse listMethodsResponse = new ListMethodsResponse();
    		for (String serviceMethod : xteeServiceMethodsNames) {
    			listMethodsResponse.addItem(serviceMethod);
    		}
    		
    		response = listMethodsResponse;
    	} else if (xRoadHeader.getProtocolVersion().equals(XRoadProtocolVersion.V4_0)) {
    		ListMethodsResponseVer2 listMethodsResponseVer2 = new ListMethodsResponseVer2();
    		for (XRoadServiceIdentifierType xRoadService : xRoadServices) {
    			listMethodsResponseVer2.addServices(xRoadService);
    		}
    		
    		response = listMethodsResponseVer2;
    	}

        return response;
    }
    
}
