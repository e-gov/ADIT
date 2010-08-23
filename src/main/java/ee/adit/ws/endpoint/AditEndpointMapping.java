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
import ee.adit.util.Util;
import ee.adit.util.XRoadQueryName;

/**
 * Custom web-service endpoint mapping implementation. Maps the incoming SOAP message to one of the 
 * endpoints registered in the configuration.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class AditEndpointMapping extends AbstractQNameEndpointMapping {

	/**
	 * Log4J logger.
	 */
	private static Logger LOG = Logger.getLogger(AditEndpointMapping.class);

	/**
	 * The name of the SOAP header that specifies the X-Tee request name
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
	 * Resolves the qualified name of the SOAP body payload element for this message.
	 * The SOAP body payload element is compared to the X-Tee specific SOAP header {@code XTEE_REQUEST_NAME_HEADER}
	 * and if the names do not match, an {@code AditInternalException} is thrown.
	 * 
	 * @param messageContext message context
	 * @return the qualified name of the SOAP body payload element. 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected QName resolveQName(MessageContext messageContext)
			throws Exception {
		QName result = null;
		boolean requestNameHeaderFound = false;

		try {
			QName requestQName = PayloadRootUtils.getPayloadRootQName(
					messageContext.getRequest().getPayloadSource(),
					transformerFactory);

			LOG.debug("Resolved request payload qualified name: "
					+ requestQName);

			SaajSoapMessage request = (SaajSoapMessage) messageContext
					.getRequest();
			Iterator<SoapHeaderElement> soapHeaderIterator = request
					.getSoapHeader().examineAllHeaderElements();

			QName xteeRequestNameHeaderQName = new QName(Util.XTEE_NAMESPACE,
					XTEE_REQUEST_NAME_HEADER);

			while (soapHeaderIterator.hasNext()) {
				SoapHeaderElement header = soapHeaderIterator.next();

				if (xteeRequestNameHeaderQName.equals(header.getName())) {
					String requestNameHeaderValue = header.getText();
					LOG.debug("Found X-Road request name header: "
							+ requestNameHeaderValue);
					requestNameHeaderFound = true;
					String localName = requestQName.getLocalPart();
					XRoadQueryName queryName = Util.extractQueryName(requestNameHeaderValue);

					if(queryName == null || queryName.getName() == null) {
						throw new AditInternalException("X-Road query header name does not match the required format 'ametlikud-dokumendid.[methodName].v[versionNumber]': " + requestNameHeaderValue);
					}
					
					if (!queryName.getName().equalsIgnoreCase(localName)) {
						throw new AditInternalException("X-Road query header name does not match SOAP body payload. Query name: '" + queryName.getName() + "', payload name: '" + localName + "'.");
					}
				}
			}

			if (!requestNameHeaderFound) {
				throw new AditInternalException(
						"X-Road header 'nimi' not found.");
			}

			result = PayloadRootUtils.getPayloadRootQName(messageContext
					.getRequest().getPayloadSource(), transformerFactory);

		} catch (Exception e) {
			LOG.error("Error while determining endpoint for request: ", e);

			if (e instanceof AditInternalException) {
				throw (AditInternalException) e;
			}
		}

		return result;
	}
	
}

