package ee.adit.ws.endpoint;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.log4j.Logger;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.mapping.AbstractQNameEndpointMapping;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ee.adit.exception.AditInternalException;
import ee.adit.util.Configuration;
import ee.adit.util.Util;
import ee.adit.util.xroad.XRoadQueryName;
import ee.adit.util.xroad.messageprotocol.XRoadIdentifierType;
import ee.adit.util.xroad.messageprotocol.XRoadProtocolHeaderField;
import ee.adit.util.xroad.messageprotocol.XRoadProtocolVersion;

/**
 * Custom web-service endpoint mapping implementation. Maps the incoming SOAP
 * message to one of the endpoints registered in the configuration.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class AditEndpointMapping extends AbstractQNameEndpointMapping {

    /**
     * Log4J logger.
     */
    private static Logger logger = Logger.getLogger(AditEndpointMapping.class);

    /**
     * The name of the SOAP header that specifies the X-Tee request name.
     */
    private static final String XTEE_REQUEST_NAME_HEADER = "nimi";

    /**
     * Transformer.
     */
    private static TransformerFactory transformerFactory;

    static {
        transformerFactory = TransformerFactory.newInstance();
    }

    /**
     * Configuration.
     */
    private Configuration configuration;

    /**
     * Retrieves the configuration.
     *
     * @return Application configuration as {@link Configuration} object.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration.
     *
     * @param configuration
     *            Application configuration as {@link Configuration} object.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Resolves the qualified name of the SOAP body payload element for this
     * message. The SOAP body payload element is compared to the X-Tee specific
     * SOAP header {@code XTEE_REQUEST_NAME_HEADER} and if the names do not
     * match, an {@code AditInternalException} is thrown.
     *
     * @param messageContext
     *            message context
     * @return the qualified name of the SOAP body payload element.
     * @throws Exception
     */
    @Override
    protected QName resolveQName(MessageContext messageContext) throws Exception {
        QName result = null;
        boolean requestNameHeaderFound = false;

        try {
            if (messageContext == null) {
            	throw new AditInternalException("Message context is NULL!");
            }
            if (messageContext.getRequest() == null) {
            	throw new AditInternalException("Request is NULL!");
            }
            if (messageContext.getRequest().getPayloadSource() == null) {
            	throw new AditInternalException("Payload source of request is NULL!");
            }

        	QName requestQName = PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerFactory);

            logger.debug("Resolved request payload qualified name: " + requestQName);

            SaajSoapMessage request = (SaajSoapMessage) messageContext.getRequest();

            // If listMethods method, don't expect SOAP headers
            if (requestQName != null && "listMethods".equalsIgnoreCase(requestQName.getLocalPart())) {
                logger.debug("Mapping to listMethods method. Ignoring SOAP headers.");
            } else {
                logger.info("request" + request);
                logger.info("request soap_header: " + request.getSoapHeader());
                logger.info("request.envelope.header" + request.getEnvelope().getHeader());

                if (request.getSoapHeader() == null) {
                	throw new AditInternalException("Request has no SOAP headers!");
                }
                
                Iterator<SoapHeaderElement> soapHeaderIterator = request.getSoapHeader().examineAllHeaderElements();

                QName xRoadProtocol_V2_0_ServiceName = new QName(Util.XTEE_NAMESPACE, XTEE_REQUEST_NAME_HEADER);
                QName xRoadProtocol_V4_0_ServiceName = new QName(XRoadProtocolVersion.V4_0.getNamespaceURI(), XRoadProtocolHeaderField.SERVICE.getValue());

                logger.info("soapHeaderIterator.hasNext(): " + soapHeaderIterator.hasNext());

                while (soapHeaderIterator.hasNext()) {
                    SoapHeaderElement header = soapHeaderIterator.next();

                    if (header != null) {
                    	if (xRoadProtocol_V2_0_ServiceName.equals(header.getName())) {
                    		requestNameHeaderFound = true;
                    		
                    		String requestNameHeaderValue = header.getText();
                    		logger.debug("Found X-Road messge protocol version 2.0 service name header: " + requestNameHeaderValue);
                    		
                    		checkRequestedService(requestNameHeaderValue, requestQName.getLocalPart());
                    	} else if (xRoadProtocol_V4_0_ServiceName.equals(header.getName())) {
                    		String subsystemCode = null;
                    		String serviceCode = null;
                    		String serviceVersion = "v1";
                    		
                    		Node serviceNode = ((DOMSource) header.getSource()).getNode();
                    		
                    		NodeList serviceNodeList = serviceNode.getChildNodes();
                    		for (int i = 0; i < serviceNodeList.getLength(); i++) {
                    			Node serviceNodeNestedElement = serviceNodeList.item(i);
                    			if (serviceNodeNestedElement.getNodeType() == Node.ELEMENT_NODE) {
                    				if (serviceNodeNestedElement.getLocalName().equals(XRoadIdentifierType.SUBSYSTEM_CODE.getName())) {
                    					subsystemCode = serviceNodeNestedElement.getTextContent();
                    				} else if (serviceNodeNestedElement.getLocalName().equals(XRoadIdentifierType.SERVICE_CODE.getName())) {
                    					serviceCode = serviceNodeNestedElement.getTextContent();
                    				} else if (serviceNodeNestedElement.getLocalName().equals(XRoadIdentifierType.SERVICE_VERSION.getName())) {
                    					serviceVersion = serviceNodeNestedElement.getTextContent();
                    				}
                    			}
                    		}
                    		
                    		if (subsystemCode != null && serviceCode != null) {
                    			requestNameHeaderFound = true;
                    			
                    			String backwardCompatibleServiceName = new StringBuilder().
                    					append(subsystemCode).append(".").
                    					append(serviceCode).append(".").
                    					append(serviceVersion).toString();
                    			
                    			logger.debug("Found X-Road message protocol version 4.0 service name header ([subsystemCode].serviceCode.[serviceVersion]): " +
                    					backwardCompatibleServiceName);
                        		
                        		checkRequestedService(backwardCompatibleServiceName, requestQName.getLocalPart());
                    		}
                    	}
                    }
                }

                if (!requestNameHeaderFound) {
                    throw new AditInternalException("X-Road header for the service name was not found.");
                }
            }

            result = PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerFactory);
        } catch (Exception e) {
            logger.error("Error while determining endpoint for request: ", e);

            if (e instanceof AditInternalException) {
                throw (AditInternalException) e;
            }
        }

        logger.debug("result: " + result);

        return result;
    }
    
    private void checkRequestedService(String requestServiceNameFromSoapHeader, String requestServiceNameFromSoapBody) {
    	XRoadQueryName queryName = Util.extractQueryName(requestServiceNameFromSoapHeader);
		
		logger.debug("extracted queryname: " + queryName.getName());
		
		if (queryName == null || queryName.getName() == null) {
			throw new AditInternalException("X-Road query header name does not match the required format '" + 
					configuration.getXteeProducerName() + ".[methodName].v[versionNumber]': " + requestServiceNameFromSoapHeader);
		}
		
		if (!queryName.getName().equalsIgnoreCase(requestServiceNameFromSoapBody)) {
			throw new AditInternalException("X-Road query header name does not match SOAP body payload. Query name: '" +
					queryName.getName() + "', payload name: '" + requestServiceNameFromSoapBody + "'.");
		}
    }
    
}
