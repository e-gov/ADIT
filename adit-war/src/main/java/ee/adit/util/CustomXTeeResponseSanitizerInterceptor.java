package ee.adit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
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
import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Attr;
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

	public boolean handleResponse(MessageContext mc) throws WebServiceClientException {
		WebServiceMessage message = mc.getResponse();
		if (mc.getResponse() instanceof SaajSoapMessage) {
			OutputStream out = new ByteArrayOutputStream();
			try {
				((SaajSoapMessage) mc.getResponse()).writeTo(out);
				String responseXML = out.toString();
				LOG.debug("responseXML: " + responseXML);
				
				if (responseXML.contains("lisaSyndmusResponse")) {

					responseXML.replace("<tkal:lisaSyndmusResponse>", "<tkal:lisaSyndmusResponse xmlns:tkal=\"http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender\">");

					if (message instanceof SaajSoapMessage) {
						SOAPMessage ssm = ((SaajSoapMessage) message).getSaajMessage();

						try {
							
							removeNamespacePrefixes(ssm.getSOAPBody());
						    
							mc.setResponse((WebServiceMessage) ssm);
							
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					
				} else {
					throw new AditInternalException("Notification service response message did not contain the element: 'tkal:lisaSyndmusResponse'");
				}

			} catch (Exception e) {
				// not important
			}
		}
		return true;
	}
	
	private void removeNamespacePrefixes(SOAPBody body) throws ParserConfigurationException {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		
		document.appendChild(body.getFirstChild());
		
		String ns = "http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender";
		NodeList nl = document.getElementsByTagNameNS(ns, "lisaSyndmusResponse");
		
		if(nl.getLength() > 0) {
			Node responseNode = nl.item(0);
			// <tkal:lisaSyndmusResponse>
			
			Attr attribute = document.createAttribute("xmlns:tkal");
			responseNode.appendChild(attribute);
			
			body.removeContents();
			body.appendChild(document.getFirstChild());
		}
		
	}

}