package ee.adit.util.xroad;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ee.webmedia.soap.SOAPUtil;

/**
 * This class adds some helper methods taken from the new J-road library
 * to the {@link SOAPUtil} class from old {@code x-tee} library.
 *
 */
public class CustomSOAPUtil extends SOAPUtil {

	/**
	 * Returns the text content of a given Node.
	 *
	 * @param node {@link Node} instance
	 * @return text content of a node
	 */
	public static String getTextContent(Node node) {
		if (node == null) {
			return null;
		}

		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node childNode = nl.item(i);

			if (isTextNode(childNode)) {
				return childNode.getNodeValue();
			}
		}

		return null;
	}

	/**
	 * Returns child elements according to name and namespace
	 * 
	 * @param root
	 * @param name
	 * @param ns
	 * @return
	 */
	public static NodeList getNsElements(Element root, String name, String ns) {
		if (root == null) {
			return null;
		}
		return root.getElementsByTagNameNS(ns, name);
	}

	/**
	 * Returns child element according to name and namespace
	 * 
	 * @param root
	 * @param name
	 * @param ns
	 * @return
	 */
	public static Element getNsElement(Element root, String name, String ns) {
		NodeList nl = getNsElements(root, name, ns);
		if (nl == null || nl.getLength() != 1) {
			return null;
		}
		return (Element) nl.item(0);
	}

	/**
	 * Returns child element value according to name and namespace
	 * 
	 * @param root
	 * @param name
	 * @param ns
	 * @return
	 */
	public static String getNsElementValue(Element root, String name, String ns) {
		return getTextContent(getNsElement(root, name, ns));
	}
	
	public static void addBaseSoapNamespace(SOAPMessage message) throws SOAPException {
		SOAPUtil.addNamespace(message, "SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
	}

}
