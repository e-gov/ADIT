/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package ee.adit.ws.endpoint;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.NodeType;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.webmedia.soap.SOAPUtil;
import ee.webmedia.xtee.XTeeUtil;

/**
 * Base class for web-service endpoints. Wraps the X-Tee specific operations and
 * data manipulation. Web-service endpoints extending this class will not have
 * to be aware of the X-Tee specific SOAP envelope.
 * 
 * The class is a modified version of the XRoad java library class {@code
 * ee.webmedia.xtee.endpoint.AbstractXTeeBaseEndpoint}.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public abstract class XteeCustomEndpoint implements MessageEndpoint {

	/**
	 * Log4J logger.
	 */
	private static Logger LOG = Logger.getLogger(XteeCustomEndpoint.class);

	/**
	 * Response element's suffix.
	 */
	public final static String RESPONSE_SUFFIX = "Response";

	/**
	 * Indicates if this is a metaservice call.
	 */
	private boolean metaService = false;

	/**
	 * Response message.
	 */
	private SaajSoapMessage responseMessage;

	/**
	 * Request message.
	 */
	private SaajSoapMessage requestMessage;

	/**
	 * If true then SOAP attachment headers will not be corrected before sending
	 * query response. This is useful when returning original attachments within
	 * an error message.
	 */
	private boolean ignoreAttachmentHeaders;

	/**
	 * The entry point for web-service call. Extracts the X-Tee operation node
	 * and passes it to the
	 * {@link #getResponse(CustomXTeeHeader, Document, SOAPMessage, SOAPMessage, Document)}
	 * method for futher processing.
	 * 
	 * @param messageContext
	 *            the message context
	 * @throws Exception
	 *             if an exception occurs while processing the request.
	 */
	@SuppressWarnings("unchecked")
	public final void invoke(MessageContext messageContext) throws Exception {

		try {

			// Extract request / response
			SOAPMessage paringMessage = SOAPUtil.extractSoapMessage(messageContext.getRequest());
			SOAPMessage responseMessage = SOAPUtil.extractSoapMessage(messageContext.getResponse());
			this.setResponseMessage(new SaajSoapMessage(responseMessage));
			this.setRequestMessage(new SaajSoapMessage(paringMessage));

			// Check if metaservice
			try {
				Iterator i = paringMessage.getSOAPBody().getChildElements(new QName(Util.XTEE_NAMESPACE, "listMethods"));
				if (i.hasNext()) {
					metaService = true;
				}
			} catch (Exception e) {
				LOG.error("Error while trying to determine if metaservice query: ", e);
			}

			// meta-service does not need 'header' element
			if (metaService) {
				responseMessage.getSOAPHeader().detachNode();
			}

			CustomXTeeHeader pais = metaService ? null : parseXteeHeader(paringMessage);
			Document paring = metaService ? null : parseQuery(paringMessage);

			// Extract the operation node (copy namespaces for it to remain
			// valid)
			Node operationNode = null;
			Iterator i = paringMessage.getSOAPBody().getChildElements();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				if (Node.ELEMENT_NODE == n.getNodeType()) {
					operationNode = n;
				}
			}

			Document operationDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			operationNode = operationDocument.importNode(operationNode, true);
			operationDocument.appendChild(operationNode);

			// Copy namespace declarations from SOAP message to
			// body XML document.
			// This is useful for example if request body contains SOAP
			// arrays (in which case the necessary namespace declarations
			// are likely to be found in SOAP envelope header.
			SOAPEnvelope env = paringMessage.getSOAPPart().getEnvelope();
			Iterator it = env.getNamespacePrefixes();
			while (it.hasNext()) {
				String prefix = (String) it.next();
				String uri = env.getNamespaceURI(prefix);
				LOG.debug("Attempting to add namespace declaration xmlns:" + prefix + "=\"" + uri + "\"");
				try {
					operationDocument.getDocumentElement().setAttribute("xmlns:" + prefix, uri);
					LOG.debug("Namespace declaration xmlns:" + prefix + "=\"" + uri + "\" was copied from SOAP envelope to request document.");
				} catch (Exception ex) {
					LOG.warn("Failed to copy namespace declaration xmlns:" + prefix + "=\"" + uri + "\" from SOAP envelope to request document.", ex);
				}
			}

			getResponse(pais, paring, responseMessage, paringMessage, operationDocument);
		} catch (Exception e) {
			LOG.error("Exception while processing request: ", e);
			throw new Exception("Service error");
		}
	}

	/**
	 * Parses the X-Tee headers.
	 * 
	 * @param paringMessage
	 *            the request message
	 * @return X-Tee header object
	 * @throws SOAPException
	 */
	@SuppressWarnings("unchecked")
	private CustomXTeeHeader parseXteeHeader(SOAPMessage paringMessage) throws SOAPException {
		CustomXTeeHeader pais = new CustomXTeeHeader();
		SOAPHeader header = paringMessage.getSOAPHeader();
		for (Iterator<Node> headerElemendid = header.getChildElements(); headerElemendid.hasNext();) {
			Node headerElement = headerElemendid.next();
			if (!SOAPUtil.isTextNode(headerElement)) {
				LOG.debug("Parsing XTee header element: " + headerElement.getLocalName() + " (value=" + headerElement.getTextContent() + ")");
				pais.addElement(new QName(headerElement.getNamespaceURI(), headerElement.getLocalName()), headerElement.getTextContent());
			}
		}
		return pais;
	}

	/**
	 * Parses the query - constructs a new {@link} Document} from the X-Tee
	 * request body element.
	 * 
	 * @param queryMsg
	 *            query message
	 * @return document representing X-Tee specific query data
	 * @throws Exception
	 */
	private Document parseQuery(SOAPMessage queryMsg) throws Exception {
		Node bodyNode = findBodyNode(queryMsg.getSOAPBody());

		if (bodyNode == null) {
			throw new IllegalStateException("Service is not metaservice, but query is missing mandatory body ('//keha\')");
		}

		Document query = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		bodyNode = query.importNode(bodyNode, true);
		query.appendChild(bodyNode);

		return query;
	}

	/**
	 * Finds "keha" element from X-Tee message in namespace-unaware fashion.<br>
	 * This is useful if service consumer has placed "keha" element in some
	 * namespace. In such case namespace-aware solutions are likely to fail.
	 * 
	 * @param soapBody
	 *            Body part of request SOAP envelope as {@link SOAPBody}
	 * @return {@link Node} representing "keha" element (or null if "keha" was
	 *         not found).
	 */
	private Node findBodyNode(SOAPBody soapBody) {
		Node result = null;
		Node marker = soapBody.getFirstChild();
		while ((marker != null) && (marker.getNodeType() != NodeType.ELEMENT)) {
			LOG.debug(marker.getNodeName() + " is not the right place to look for \"keha\". Checking next sibling...");
			marker = marker.getNextSibling();
		}
		if ((marker != null) && (marker.getNodeType() == NodeType.ELEMENT)) {
			LOG.debug("Attempting to find \"keha\" from element " + marker.getNodeName());
			marker = marker.getFirstChild();
			while ((marker != null) && !((marker.getNodeType() == NodeType.ELEMENT) && "keha".equalsIgnoreCase(marker.getLocalName()))) {
				marker = marker.getNextSibling();
			}
			if (marker != null) {
				result = marker;
			}
		}
		return result;
	}

	/**
	 * Copies the request query and headers to the response message, invokes the
	 * underlying web-service endpoint in {@link AbstractAditBaseEndpoint}. If
	 * the query is a metaservice (listMethods) query, then the query and
	 * headers are not copied.
	 * 
	 * @param header
	 *            X-Tee header
	 * @param query
	 *            query document
	 * @param responseMessage
	 *            response message
	 * @param requestMessage
	 *            request message
	 * @param operationNode
	 *            X-Tee specific operation node
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void getResponse(CustomXTeeHeader header, Document query, SOAPMessage responseMessage, SOAPMessage requestMessage, Document operationNode) throws Exception {
		SOAPElement teenusElement = createXteeMessageStructure(requestMessage, responseMessage);
		if (!metaService)
			copyParing(query, teenusElement);
		invokeInternal(operationNode, teenusElement, header);
		if (!metaService)
			addHeader(header, responseMessage);

		if ((responseMessage != null) && !this.isIgnoreAttachmentHeaders()) {
			Iterator it = responseMessage.getAttachments();
			if (it != null) {
				while (it.hasNext()) {
					AttachmentPart at = (AttachmentPart) it.next();
					at.setMimeHeader("Content-Transfer-Encoding", "base64");
					at.setMimeHeader("Content-Encoding", "gzip");
				}
			}
		}
	}

	/**
	 * Creates X-Tee specific structure for SOAP message: adds MIME headers,
	 * base namespaces.
	 * 
	 * @param requestMessage
	 *            request SOAP message
	 * @param responseMessage
	 *            response SOAP message
	 * @return the service element of the SOAP response message
	 * @throws Exception
	 */
	private SOAPElement createXteeMessageStructure(SOAPMessage requestMessage, SOAPMessage responseMessage) throws Exception {
		SOAPUtil.addBaseMimeHeaders(responseMessage);
		SOAPUtil.addBaseNamespaces(responseMessage);
		responseMessage.getSOAPPart().getEnvelope().setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");

		Node teenusElement = SOAPUtil.getFirstNonTextChild(requestMessage.getSOAPBody());

		if (teenusElement.getPrefix() == null || teenusElement.getNamespaceURI() == null) {
			throw new IllegalStateException("Service request is missing namespace.");
		}
		SOAPUtil.addNamespace(responseMessage, teenusElement.getPrefix(), teenusElement.getNamespaceURI());
		return responseMessage.getSOAPBody().addChildElement(teenusElement.getLocalName() + RESPONSE_SUFFIX, teenusElement.getPrefix(), teenusElement.getNamespaceURI());
	}

	/**
	 * Copies the request data to the response message - the request message's
	 * <keha> element contents is copied to the response message's <paring>
	 * element.
	 * 
	 * @param paring
	 *            request data
	 * @param response
	 *            response
	 * @throws Exception
	 */
	private void copyParing(Document paring, Node response) throws Exception {
		Node paringElement = response.appendChild(response.getOwnerDocument().createElement("paring"));
		Node kehaNode = response.getOwnerDocument().importNode(paring.getDocumentElement(), true);

		NamedNodeMap attrs = kehaNode.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			paringElement.getAttributes().setNamedItem(attrs.item(i));
		}

		while (kehaNode.hasChildNodes()) {
			paringElement.appendChild(kehaNode.getFirstChild());
		}
	}

	/**
	 * Adds headers from the {@code CustomXTeeHeader} to the SOAP message.
	 * 
	 * @param pais
	 *            headers
	 * @param message
	 *            SOAP message
	 * @throws SOAPException
	 */
	private void addHeader(CustomXTeeHeader pais, SOAPMessage message) throws SOAPException {
		XTeeUtil.addXteeNamespace(message);
		for (QName qname : pais.getElemendid().keySet()) {
			if (qname.getNamespaceURI().equals(XTeeUtil.XTEE_NS_URI)) {
				XTeeUtil.addHeaderElement(message.getSOAPHeader(), qname.getLocalPart(), pais.getElemendid().get(qname));
			}
		}
	}

	/**
	 * If true, request will be processed like meta-request (example of the
	 * meta-query is <code>listMethods</code>).
	 */
	public void setMetaService(boolean metaService) {
		this.metaService = metaService;
	}

	/** Returns <code>true</code>, if this is a meta service. */
	public boolean isMetaService() {
		return metaService;
	}

	/**
	 * Sets the property to ignore SOAP attachment headers.
	 */
	public void setIgnoreAttachmentHeaders(boolean ignoreAttachmentHeaders) {
		this.ignoreAttachmentHeaders = ignoreAttachmentHeaders;
	}

	/**
	 * Retrieves the response message.
	 * 
	 * @return	Response message as {@link SoapMessage} object.
	 */
	public SoapMessage getResponseMessage() {
		return responseMessage;
	}

	/**
	 * Retrieves the request message.
	 * 
	 * @return	Request message as {@link SoapMessage} object.
	 */
	public SoapMessage getRequestMessage() {
		return requestMessage;
	}

	/**
	 * Sets the response message.
	 * 
	 * @param responseMessage	Response message as {@link SaajSoapMessage} object.
	 */
	public void setResponseMessage(SaajSoapMessage responseMessage) {
		this.responseMessage = responseMessage;
	}

	/**
	 * Sets the request message.
	 * 
	 * @param requestMessage	Request message as {@link SaajSoapMessage} object.
	 */
	public void setRequestMessage(SaajSoapMessage requestMessage) {
		this.requestMessage = requestMessage;
	}

	/**
	 * Indicates whether SOAP attachments headers are to be corrected.
	 * 
	 * @return	<code>true</code> if this request should not attempt to correct response attachment headers.  
	 */
	public boolean isIgnoreAttachmentHeaders() {
		return ignoreAttachmentHeaders;
	}

	/**
	 * Method which must implement the service logic, receives
	 * <code>requestKeha</code>, <code>responseKeha<code>
	 * and <code>CustomXTeeHeader</code>
	 * 
	 * @param requestKeha
	 *            query body
	 * @param responseKeha
	 *            response body
	 * @param xteeHeader
	 *            query header
	 */
	protected abstract void invokeInternal(Document requestKeha, Element responseElement, CustomXTeeHeader xTeeHeader) throws Exception;

}
