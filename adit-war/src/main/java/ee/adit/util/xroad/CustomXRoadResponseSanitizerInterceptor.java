package ee.adit.util.xroad;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ee.adit.exception.AditInternalException;
import ee.webmedia.xtee.client.exception.XTeeException;

/**
 * Custom XTee response message sanitizer (cleaner). Acts as an interceptor.
 *  
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class CustomXRoadResponseSanitizerInterceptor implements ClientInterceptor {

    /**
     * Log4J logger.
     */
    private static Logger logger = Logger.getLogger(CustomXRoadResponseSanitizerInterceptor.class);

    /**
     * Handle fault.
     * 
     * @param context message context
     * @return success
     */
    public boolean handleFault(MessageContext context) throws WebServiceClientException {
        throw new XTeeException();
    }

    /**
     * Handle request.
     * 
     * @param context message context
     * @return success
     */
    public boolean handleRequest(MessageContext context) throws WebServiceClientException {
        return true;
    }

    /**
     * Handle response.
     * 
     * @param mc message context
     * @return success
     */
    public boolean handleResponse(MessageContext mc) throws WebServiceClientException {
        WebServiceMessage message = mc.getResponse();
        if (mc.getResponse() instanceof SaajSoapMessage) {
            OutputStream out = new ByteArrayOutputStream();
            try {
                ((SaajSoapMessage) mc.getResponse()).writeTo(out);
                String responseXML = out.toString();
                logger.debug("responseXML: " + responseXML);

                if (responseXML.contains("lisaSyndmusResponse")) {

                    responseXML
                            .replace("<tkal:lisaSyndmusResponse>",
                                    "<tkal:lisaSyndmusResponse xmlns:tkal=\"http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender\">");

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
                    throw new AditInternalException(
                            "Notification service response message did not contain the element: 'tkal:lisaSyndmusResponse'");
                }

            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        }
        return true;
    }

    /**
     * Remove namespace prefixes from SOAP body.
     * 
     * @param body SOAP body
     * @throws ParserConfigurationException
     */
    private void removeNamespacePrefixes(SOAPBody body) throws ParserConfigurationException {

        logger.debug("Removing namespace prefixes...");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        logger.debug("Removing namespace prefixes1...");
        DocumentBuilder builder = factory.newDocumentBuilder();
        logger.debug("Removing namespace prefixes2...");
        Document document = builder.newDocument();
        logger.debug("Removing namespace prefixes3...");

        Node firstChild = body.getFirstChild();
        logger.debug("firstChild: " + firstChild);

        if (firstChild != null) {
            logger.debug("firstChild.name: " + firstChild.getLocalName());
            logger.debug("firstChild.textContent: " + firstChild.getTextContent());
            logger.debug("firstChild.value: " + firstChild.getNodeValue());
        }

        document.appendChild(firstChild);
        logger.debug("Removing namespace prefixes4...");
        String ns = "http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender";
        NodeList nl = document.getElementsByTagNameNS(ns, "lisaSyndmusResponse");

        logger.debug("NodeList retrieved: size: " + nl.getLength());

        if (nl.getLength() > 0) {
            Node responseNode = nl.item(0);
            // <tkal:lisaSyndmusResponse>

            logger.debug("Adding namespaces");
            Attr attribute = document.createAttribute("xmlns:tkal");
            attribute.setValue("http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender");
            Attr attribute2 = document.createAttribute("xmlns:xsi");
            attribute2.setValue("http://www.w3.org/2001/XMLSchema-instance");
            responseNode.appendChild(attribute);
            responseNode.appendChild(attribute2);

            logger.debug("Removing contents");
            body.removeContents();
            logger.debug("Appending document...");
            body.appendChild(document.getFirstChild());
        }

    }

	@Override
	public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {}

}
