package ee.adit.ws.endpoint;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;

import org.apache.log4j.Logger;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.mapping.AbstractQNameEndpointMapping;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import ee.adit.exception.AditInternalException;
import ee.adit.util.Configuration;
import ee.adit.util.Util;
import ee.adit.util.xroad.XRoadQueryName;

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
    @SuppressWarnings("unchecked")
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

        	QName requestQName = PayloadRootUtils.getPayloadRootQName(
                messageContext.getRequest().getPayloadSource(), transformerFactory);

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

                QName xteeRequestNameHeaderQName = new QName(Util.XTEE_NAMESPACE, XTEE_REQUEST_NAME_HEADER);

                logger.info("soapHeaderIterator.hasNext(): " + soapHeaderIterator.hasNext());

                while (soapHeaderIterator.hasNext()) {
                    SoapHeaderElement header = soapHeaderIterator.next();

                    if ((header != null) && xteeRequestNameHeaderQName.equals(header.getName())) {
                        String requestNameHeaderValue = header.getText();
                        logger.debug("Found X-Road request name header: " + requestNameHeaderValue);
                        requestNameHeaderFound = true;
                        String localName = requestQName.getLocalPart();
                        XRoadQueryName queryName = Util.extractQueryName(requestNameHeaderValue);

                        logger.debug("extracted queryname: " + queryName);

                        if (queryName == null || queryName.getName() == null) {
                            throw new AditInternalException(
                                "X-Road query header name does not match the required format '"
                            	+ configuration.getXteeProducerName() + ".[methodName].v[versionNumber]': "
                                + requestNameHeaderValue);
                        }

                        if (!queryName.getName().equalsIgnoreCase(localName)) {
                            throw new AditInternalException(
                                    "X-Road query header name does not match SOAP body payload. Query name: '"
                                            + queryName.getName() + "', payload name: '" + localName + "'.");
                        }
                    }
                }

                if (!requestNameHeaderFound) {
                    throw new AditInternalException("X-Road header 'nimi' not found.");
                }
            }

            result = PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(),
                    transformerFactory);

        } catch (Exception e) {
            logger.error("Error while determining endpoint for request: ", e);

            if (e instanceof AditInternalException) {
                throw (AditInternalException) e;
            }
        }

        logger.debug("result: " + result);

        return result;
    }
}
