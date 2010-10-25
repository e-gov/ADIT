package ee.adit.util;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import ee.webmedia.xtee.client.exception.XTeeException;
import ee.webmedia.xtee.client.util.XTeeConverter;

public class CustomXTeeResponseSanitizerInterceptor implements ClientInterceptor {

	private static Logger LOG = Logger.getLogger(CustomXTeeResponseSanitizerInterceptor.class);

	public boolean handleFault(MessageContext context) throws WebServiceClientException {
		throw new XTeeException();
	}

	public boolean handleRequest(MessageContext context) throws WebServiceClientException {
		return true;
	}

	/**
	 * Handles response messages.
	 */
	public boolean handleResponse(MessageContext mc) throws WebServiceClientException {
		WebServiceMessage message = mc.getResponse();
		
		StreamResult res = (StreamResult) message.getPayloadResult();
		StringWriter strWrt = new StringWriter();
		res.setWriter(strWrt);
		
		LOG.debug("StringWriter.toString: 1: " + strWrt.toString());
		
		if (message instanceof SaajSoapMessage) {
			SOAPMessage ssm = ((SaajSoapMessage) message).getSaajMessage();
			
			LOG.debug("StringWriter.toString: 2: " + strWrt.toString());
			LOG.debug("Handling response with interceptor. SaajSoapMessage created.");
			
			try {

				
				LOG.debug("SOAP Header: " + ssm.getSOAPHeader());
				LOG.debug("SOAP Body: " + ssm.getSOAPBody());

				if(ssm.getSOAPHeader() == null) {
					ssm.getSOAPPart().getEnvelope().addHeader();
					SOAPHeaderElement headerElement = ssm.getSOAPHeader().addHeaderElement(new QName("http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender", "nimi"));
					headerElement.setValue("teavituskalender.lisaSyndmus.v1");
				}
					
				XTeeConverter converter = new XTeeConverter();
				mc.clearResponse();
				mc.setResponse(converter.convert(ssm));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return true;
	}

}