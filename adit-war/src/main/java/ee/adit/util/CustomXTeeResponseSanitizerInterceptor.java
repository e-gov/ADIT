package ee.adit.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ee.adit.exception.AditInternalException;
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
		StringWriter stringWriter = new StringWriter();
		
		try {
			DOMResult res = (DOMResult) message.getPayloadResult();
			
			Source source = new DOMSource(res.getNode());
	        
			
	        Result result = new StreamResult(stringWriter);
	        TransformerFactory factory = TransformerFactory.newInstance();
	        Transformer transformer = factory.newTransformer();
	        transformer.transform(source, result);
	        String responseXML = stringWriter.getBuffer().toString();
	        
	        LOG.debug("Notifications service response: " + responseXML);
	        
	        if(responseXML.contains("lisaSyndmusResponse")) {
	        	// Remove namespace prefixes
	        	/*String startString = "<tkal:lisaSyndmusResponse>";
	        	String endString = "</tkal:lisaSyndmusResponse>";
	        	int startIndex = responseXML.indexOf(startString);
	        	int endIndex = responseXML.indexOf(endString);
	        	
	        	String subString = responseXML.substring(startIndex, endIndex + endString.length());
	        	LOG.debug("SubString: '" + subString);*/
	        	
		        responseXML.replace("<tkal:lisaSyndmusResponse>", "<tkal:lisaSyndmusResponse xmlns:tkal=\"http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender\">");
	        	
		        if (message instanceof SaajSoapMessage) {
					SOAPMessage ssm = ((SaajSoapMessage) message).getSaajMessage();
					
					try {
						
						SOAPBody body = ssm.getSOAPBody();
						body.setTextContent(responseXML);							
						
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
	        	
	        } else {
	        	throw new AditInternalException("Notification service response message did not contain the element: 'tkal:lisaSyndmusResponse'");
	        }
	        
	        /*DocumentBuilderFactory dbf = new DocumentBuilderFactoryImpl();
	        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
	        
	        ByteArrayInputStream bis = new ByteArrayInputStream(responseXML.getBytes("UTF-8"));
	        
	        Document doc = docBuilder.parse(bis);
	        
	        NodeList nl = doc.getElementsByTagName("tkal:lisaSyndmusResponse");
	        
	        if(nl.getLength() > 0) {
	        	Node node = nl.item(0);
	        	
	        	
	        	
	        } else {
	        	throw new AditInternalException("Notification service response message did not contain the element: 'tkal:lisaSyndmusResponse'");
	        }*/
	        
		} catch(Exception e) {
			LOG.error("Error while getting DOMResult: ", e);
		}
		
		
		
		return true;
	}

}