package ee.adit.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

/**
 * Scheduler interceptor. Corrects SOAP array instances.
 *  
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class SchedulerSoapArrayInterceptor implements ClientInterceptor {
    
    /**
     * Log4J logger.
     */
    private static Logger logger = Logger.getLogger(SchedulerSoapArrayInterceptor.class);
    
    /**
     * Notification calendar namespace.
     */
    public static final String NS_TK = "http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender";
    
    /**
     * XML Schema namespace.
     */
    public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    
    /**
     * SOAP Encoding namespace .
     */
    public static final String NS_SOAPENC = "http://schemas.xmlsoap.org/soap/encoding/";

    /**
     * Handle fault.
     * @param mc message context
     * @return success
     */
    public boolean handleFault(MessageContext mc) throws WebServiceClientException {
        return true;
    }
    
    /**
     * Handle request.
     * 
     * @param mc message context
     * @return success
     */
    public boolean handleRequest(MessageContext mc) throws WebServiceClientException {
        if (mc.getRequest() instanceof SaajSoapMessage) {
            SOAPMessage msg = ((SaajSoapMessage) mc.getRequest()).getSaajMessage();
            try {
                addSoapEncAttributes(msg);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        return true;
    }

    /**
     * Handle response.
     * @param mc message context
     * @return success
     */
    public boolean handleResponse(MessageContext mc) throws WebServiceClientException {
        return true;
    }

    /**
     * Add SOAP encoding attributes.
     * 
     * @param msg SOAP message
     * @throws SOAPException
     */
    @SuppressWarnings("unchecked")
    public void addSoapEncAttributes(SOAPMessage msg) throws SOAPException {
        if (msg == null) {
            logger.debug("Original message is NULL!");
            return;
        }

        SOAPBody body = msg.getSOAPBody();
        if (body == null) {
            logger.debug("SOAP body is NULL!");
            return;
        }

        Node operationNode = body.getFirstChild();
        if (operationNode == null) {
            logger.debug("Operation node is NULL!");
            return;
        }
        while ((operationNode != null) && (operationNode.getNodeType() != Node.ELEMENT_NODE)) {
            operationNode = operationNode.getNextSibling();
        }
        if (operationNode == null) {
            logger.debug("Operation node is NULL!");
            return;
        }

        Node xroadKehaNode = operationNode.getFirstChild();
        if (xroadKehaNode == null) {
            logger.debug("Keha node is NULL!");
            return;
        }
        while ((xroadKehaNode != null) && (xroadKehaNode.getNodeType() != Node.ELEMENT_NODE)) {
            xroadKehaNode = xroadKehaNode.getNextSibling();
        }
        if (xroadKehaNode == null) {
            logger.debug("Keha node is NULL!");
            return;
        }

        reformatDateInXml((Element) xroadKehaNode, "algus");
        reformatDateInXml((Element) xroadKehaNode, "lopp");

        NodeList lugejadNodeList = ((Element) xroadKehaNode).getElementsByTagNameNS(
                "http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender", "lugejad");
        if ((lugejadNodeList == null) || (lugejadNodeList.getLength() != 1)) {
            logger.debug("lugejad node not found!");
            return;
        }
        Element lugejadElement = (Element) lugejadNodeList.item(0);
        NodeList kasutajadNodeList = lugejadElement.getElementsByTagNameNS(
                "http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender", "kasutaja");
        if ((kasutajadNodeList != null) && (kasutajadNodeList.getLength() > 0)) {
            String xsiPrefix = "";
            String soapEncPrefix = "";
            String tkPrefix = "";
            boolean hasXsdPrefix = false;

            Iterator<String> it = msg.getSOAPPart().getEnvelope().getNamespacePrefixes();
            while (it.hasNext()) {
                String prefix = it.next();
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
                msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(tkPrefix,
                        "http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender");
            }
            if (xsiPrefix.length() < 1) {
                xsiPrefix = "xsi";
                msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(xsiPrefix,
                        "http://www.w3.org/2001/XMLSchema-instance");
            }
            if (soapEncPrefix.length() < 1) {
                soapEncPrefix = "soap-enc";
                msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(soapEncPrefix,
                        "http://schemas.xmlsoap.org/soap/encoding/");
            }

            // Workaround for an error in x-road client library
            // which registers XmlSchema namespace with "xs" prefix
            // and later attmpts to use it with "xsd" prefix.
            if (!hasXsdPrefix) {
                msg.getSOAPPart().getEnvelope().addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
            }

            lugejadElement.setAttribute(xsiPrefix + ":type", soapEncPrefix + ":Array");
            lugejadElement.setAttribute(soapEncPrefix + ":arrayType", tkPrefix + ":kasutaja["
                    + kasutajadNodeList.getLength() + "]");
        }
    }

    /**
     * Reformat dates in XML.
     * 
     * @param xroadKehaElement XTee body element
     * @param tagName tag name
     */
    private void reformatDateInXml(Element xroadKehaElement, String tagName) {
        NodeList nl = xroadKehaElement.getElementsByTagNameNS(NS_TK, tagName);
        if ((nl != null) && (nl.getLength() > 0)) {
            Element e = (Element) nl.item(0);
            Date dateValue = getDateFromXML(e.getTextContent());
            String modifiedDate = formatDate(dateValue);
            if ((modifiedDate != null) && (modifiedDate.length() > 0)) {
                e.setTextContent(modifiedDate);
            }
        } else {
            logger.debug(tagName + " node not found!");
        }
    }

    /**
     * Get date from XML date string.
     * 
     * @param xmlDate XML date string
     * @return date
     */
    private Date getDateFromXML(String xmlDate) {
        Date result = null;
        if ((xmlDate != null) && !xmlDate.equalsIgnoreCase("")) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            df.setLenient(false);
            try {
                result = df.parse(xmlDate);
            } catch (ParseException e1) {
                df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ");
                df.setLenient(false);
                try {
                    result = df.parse(xmlDate);
                } catch (ParseException e2) {
                    df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");
                    df.setLenient(false);
                    try {
                        result = df.parse(xmlDate);
                    } catch (ParseException e3) {
                        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                        df.setLenient(false);
                        try {
                            result = df.parse(xmlDate);
                        } catch (ParseException e4) {
                            df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            df.setLenient(false);
                            try {
                                result = df.parse(xmlDate);
                            } catch (ParseException e5) {
                                if (xmlDate.contains("T")) {
                                    return null;
                                }
                                df = new SimpleDateFormat("yyyy-MM-ddZ");
                                df.setLenient(false);
                                try {
                                    result = df.parse(xmlDate);
                                } catch (ParseException e6) {
                                    df = new SimpleDateFormat("yyyy-MM-dd");
                                    df.setLenient(false);
                                    try {
                                        result = df.parse(xmlDate);
                                    } catch (ParseException e7) {
                                        result = null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Format date to XML date string.
     * 
     * @param date date
     * @return XML date string
     */
    private String formatDate(Date date) {
        try {
            if (date == null) {
                return "";
            }

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String result = format.format(date);
            return result;
        } catch (Exception ex) {
            logger.error(ex);
            return "";
        }
    }
}
