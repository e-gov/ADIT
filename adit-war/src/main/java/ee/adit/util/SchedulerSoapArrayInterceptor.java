package ee.adit.util;

import java.util.Iterator;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SchedulerSoapArrayInterceptor implements ClientInterceptor {
	private static Logger LOG = Logger.getLogger(SchedulerSoapArrayInterceptor.class);
	public final String NS_TK = "http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender";
	public final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	public final String NS_SOAPENC = "http://schemas.xmlsoap.org/soap/encoding/";
	
	public boolean handleFault(MessageContext mc) throws WebServiceClientException {
		return true;
	}

	public boolean handleRequest(MessageContext mc) throws WebServiceClientException {
		LOG.debug("handleRequest() started.");
		if (mc.getRequest() instanceof SaajSoapMessage) {
			SOAPMessage msg = ((SaajSoapMessage) mc.getRequest()).getSaajMessage();
			try {
				addSoapEncAttributes(msg); 
			} catch (Exception ex) {
				LOG.error(ex);
			}
		}
		return true;
	}

	public boolean handleResponse(MessageContext mc) throws WebServiceClientException {
		return true;
	}
	
	public void addSoapEncAttributes(SOAPMessage msg) throws SOAPException {
		if (msg == null) {
			LOG.debug("Original message is NULL!");
			return;
		}
		  
		SOAPBody body = msg.getSOAPBody();
		if (body == null) {
			LOG.debug("SOAP body is NULL!");
			return;
		}
		  
		Node operationNode = body.getFirstChild();
		if (operationNode == null) {
			LOG.debug("Operation node is NULL!");
			return;
		}
		while ((operationNode != null) && (operationNode.getNodeType() != Node.ELEMENT_NODE)) {
			operationNode = operationNode.getNextSibling();
		}
		if (operationNode == null) {
			LOG.debug("Operation node is NULL!");
			return;
		}
		  
		Node xroadKehaNode = operationNode.getFirstChild();
		if (xroadKehaNode == null) {
			LOG.debug("Keha node is NULL!");
			return;
		}
		while ((xroadKehaNode != null) && (xroadKehaNode.getNodeType() != Node.ELEMENT_NODE)) {
			xroadKehaNode = xroadKehaNode.getNextSibling();
		}
		if (xroadKehaNode == null) {
			LOG.debug("Keha node is NULL!");
			return;
		}
		
		NodeList lugejadNodeList = ((Element)xroadKehaNode).getElementsByTagNameNS("http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender", "lugejad");
		if ((lugejadNodeList == null) || (lugejadNodeList.getLength() != 1)) {
			LOG.debug("lugejad node not found!");
			return;
		}
		Element lugejadElement = (Element)lugejadNodeList.item(0);
		NodeList kasutajadNodeList = lugejadElement.getElementsByTagNameNS("http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender", "kasutaja");
		if ((kasutajadNodeList != null) && (kasutajadNodeList.getLength() > 0)) {
			String xsiPrefix = "";
			String soapEncPrefix = "";
			String tkPrefix = "";
			boolean hasXsdPrefix = false;
			  
			Iterator it = msg.getSOAPPart().getEnvelope().getNamespacePrefixes();
			while (it.hasNext()) {
				String prefix = it.next().toString();
				if ("xsd".equalsIgnoreCase(prefix)) {
					hasXsdPrefix = true;
				}
				
				String uri = msg.getSOAPPart().getEnvelope().getNamespaceURI(prefix);
				if (NS_TK.equalsIgnoreCase(uri)) {
					tkPrefix = prefix;
				} else if (NS_XSI.equalsIgnoreCase(uri)) {
					xsiPrefix = prefix;
				} else if (NS_SOAPENC.equalsIgnoreCase(uri)) {
					soapEncPrefix = prefix;
				}
			}
			
			
			if (tkPrefix.length() < 1) {
				tkPrefix = "teav";
				msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(tkPrefix, "http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender");
			}
			if (xsiPrefix.length() < 1) {
				xsiPrefix = "xsi";
				msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(xsiPrefix, "http://www.w3.org/2001/XMLSchema-instance");
			}
			if (soapEncPrefix.length() < 1) {
				soapEncPrefix = "soap-enc";
				msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(soapEncPrefix, "http://schemas.xmlsoap.org/soap/encoding/");
			}
			
			// Workaround for an error in x-road client library
			// which registers XmlSchema namespace with "xs" prefix
			// and later attmpts to use it with "xsd" prefix.
			if (!hasXsdPrefix) {
				msg.getSOAPPart().getEnvelope().addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
			}
			
			lugejadElement.setAttribute(xsiPrefix + ":type", soapEncPrefix + ":Array");
			lugejadElement.setAttribute(soapEncPrefix + ":arrayType", tkPrefix + ":kasutaja["+ kasutajadNodeList.getLength() +"]");
		}
	}
}
