package ee.adit.ws.endpoint;

import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;

import org.apache.log4j.Logger;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping;
import org.springframework.ws.server.endpoint.mapping.AbstractMapBasedEndpointMapping;
import org.springframework.ws.server.endpoint.mapping.AbstractQNameEndpointMapping;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import ee.adit.exception.AditInternalException;
import ee.adit.util.Util;

public class AditEndpointMapping extends AbstractQNameEndpointMapping {

	private static Logger LOG = Logger.getLogger(AditEndpointMapping.class);

	private static final String XTEE_REQUEST_NAME_HEADER = "nimi";

	private static TransformerFactory transformerFactory;

	static {
		transformerFactory = TransformerFactory.newInstance();
	}

	/*
	 * @Override protected Object getEndpointInternal(MessageContext
	 * messageContext) throws AditInternalException { boolean
	 * requestNameHeaderFound = false;
	 * 
	 * try { QName requestQName = PayloadRootUtils.getPayloadRootQName(
	 * messageContext.getRequest().getPayloadSource(), transformerFactory);
	 * 
	 * LOG.debug("Resolved request payload qualified name: " + requestQName);
	 * 
	 * 
	 * 
	 * SaajSoapMessage request = (SaajSoapMessage) messageContext .getRequest();
	 * Iterator<SoapHeaderElement> soapHeaderIterator = request
	 * .getSoapHeader().examineAllHeaderElements();
	 * 
	 * QName xteeRequestNameHeaderQName = new QName(Util.XTEE_NAMESPACE,
	 * XTEE_REQUEST_NAME_HEADER);
	 * 
	 * while (soapHeaderIterator.hasNext()) { SoapHeaderElement header =
	 * soapHeaderIterator.next();
	 * 
	 * if (xteeRequestNameHeaderQName.equals(header.getName())) { String
	 * requestNameHeaderValue = header.getText();
	 * LOG.debug("Found X-Road request name header: " + requestNameHeaderValue);
	 * requestNameHeaderFound = true; String localName =
	 * requestQName.getLocalPart();
	 * 
	 * // Comapre the 'requestNameHeaderValue' with 'localName' String queryName
	 * = extractQueryName(requestNameHeaderValue);
	 * 
	 * if(queryName.equalsIgnoreCase(localName)) {
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * if(!requestNameHeaderFound) { throw new
	 * AditInternalException("X-Road header 'nimi' not found."); }
	 * 
	 * } catch (Exception e) {
	 * LOG.error("Error while determining endpoint for request: ", e);
	 * 
	 * if(e instanceof AditInternalException) { throw (AditInternalException) e;
	 * } }
	 * 
	 * return null; }
	 */

	private static String extractQueryName(String fullQueryName) {
		String result = fullQueryName;

		StringTokenizer st = new StringTokenizer(fullQueryName, ".");

		for (int i = 0; st.hasMoreTokens(); i++) {
			if (i == 1) {
				result = st.nextToken();
			} else {
				st.nextToken();
			}
		}

		return result;
	}

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
					String queryName = extractQueryName(requestNameHeaderValue);

					if (!queryName.equalsIgnoreCase(localName)) {
						throw new AditInternalException("X-Road query header name does not match SOAP body payload. Query name: '" + queryName + "', payload name: '" + localName + "'.");
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
