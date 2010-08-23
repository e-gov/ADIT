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

	private static ExtractQueryNameResult extractQueryName(String fullQueryName) {
		ExtractQueryNameResult result = new ExtractQueryNameResult();
		result.setName(fullQueryName);
		result.setVersion(1);
		
		StringTokenizer st = new StringTokenizer(fullQueryName, ".");

		for (int i = 0; st.hasMoreTokens(); i++) {
			if (i == 1) {
				result.setName(st.nextToken());
			} else if(i == 2) {
				try {
					String tmpVersion = st.nextToken();
					tmpVersion = tmpVersion.substring(1);
					result.setVersion(Integer.parseInt(tmpVersion));
				} catch (Exception e) {
					LOG.error("Error while trying to parse X-Road request name version part: ", e);
				}
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
					ExtractQueryNameResult queryName = extractQueryName(requestNameHeaderValue);

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

class ExtractQueryNameResult {
	
	private String name;
	
	private int version;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
}