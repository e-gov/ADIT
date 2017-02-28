package ee.adit.dvk.api.container.v1;

import java.io.IOException;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ee.adit.dvk.api.container.Container;
import ee.adit.dvk.api.container.Metaxml;
import ee.adit.dvk.api.ml.Util;

public class ContainerVer1 extends Container
{
	private Metainfo metainfo;
	private SignedDoc signedDoc;
	private Transport transport;
	private Metaxml metaxml;
	private Ajalugu ajalugu;
	public static final String NameSpaceURI_DHL = "http://www.riik.ee/schemas/dhl";
	public static final String Xmlns_DHL = "xmlns:dhl=\"" + NameSpaceURI_DHL + "\"";
	public static final String PrefixDHL = "dhl";
	public static final String NameSpaceURI_MM = "http://www.riik.ee/schemas/dhl-meta-manual";
	public static final String Xmlns_MM = "xmlns:mm=\"" + NameSpaceURI_MM + "\"";
	public static final String PrefixMM = "mm";
	public static final String NameSpaceURI_MA = "http://www.riik.ee/schemas/dhl-meta-automatic";
	public static final String Xmlns_MA = "xmlns:ma=\"" + NameSpaceURI_MA + "\"";
	public static final String PrefixMA = "ma";
	public static final String NameSpaceURI_DDOC = "http://www.sk.ee/DigiDoc/v1.3.0#";
	public static final String Xmlns_DDOC = "xmlns=\"" + NameSpaceURI_DDOC + "\"";
	public static final String PrefixDDOC = "";
	public static final String NameSpaceURI_RKEL = "http://www.riik.ee/schemas/dhl/rkel_letter";
	public static final String Xmlns_RKEL = "xmlns:rkel=\"" + NameSpaceURI_RKEL + "\"";
	public static final String PrefixRKEL = "rkel";

	public Metainfo getMetainfo() {
		return metainfo;
	}

	public void setMetainfo(Metainfo metainfo) {
		this.metainfo = metainfo;
	}

	public SignedDoc getSignedDoc() {
		return signedDoc;
	}

	public void setSignedDoc(SignedDoc signedDoc) {
		this.signedDoc = signedDoc;
	}

	public Transport getTransport() {
		return transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	public Metaxml getMetaxml() {
		return metaxml;
	}

	public void setMetaxml(Metaxml metaxml) {
		this.metaxml = metaxml;
	}

	public Ajalugu getAjalugu() {
		return ajalugu;
	}

	public void setAjalugu(Ajalugu ajalugu) {
		this.ajalugu = ajalugu;
	}

	public static ContainerVer1 parse(String xml) throws MappingException, MarshalException, ValidationException, IOException {
		if (Util.isEmpty(xml)) {
			return null;
		}

		/*
		int indx1 = xml.indexOf("?>");
		if (indx1 < 0) {
		    Pattern documentRootPattern = Pattern.compile("<([\\w]+:)?dokument", Pattern.DOTALL | Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);
		    Matcher documentRootMatcher = documentRootPattern.matcher(xml);
		    if (documentRootMatcher.find()) {
		        indx1 = documentRootMatcher.start();
		    }
		    documentRootMatcher = null;
		    documentRootPattern = null;
		} else {
		    indx1 += 2;
		}

		int indx2 = xml.indexOf(">", indx1) + 1;// root element tag end

		String docRootTag = "";
		if ((indx1 >= 0) && (indx2 > indx1)) {
		    docRootTag = xml.substring(indx1, indx2);
		}

		if (!docRootTag.contains("konteineri_versioon")) {
			final String xmlBlockStart = "<dhl:metainfo";
			final String endTag = "</dhl:metainfo>";
			indx1 = xml.indexOf(xmlBlockStart);
			indx2 = xml.indexOf(endTag, indx1);
			//
			if (indx1 > -1 && indx2 > -1) {
				final String xmlBlockStartWithDHL = "<dhl:metainfo " + Xmlns_DHL + " ";
				indx2 = indx2 + endTag.length();
				String metainfoXml = xml.substring(indx1, indx2).replace(xmlBlockStart, xmlBlockStartWithDHL);
				String metainfo = transformMetainfo(metainfoXml);

				if (metainfo != null) {
					StringBuilder sb = new StringBuilder(xml);
					sb.replace(indx1, indx2, metainfo);
					xml = sb.toString();
				}
			}
		}
        */
		StringReader in = new StringReader(xml);

		try {
			return (ContainerVer1) Container.marshal(in, Version.Ver1);
		} finally {
			in.close();
		}
	}

	@Override
	public String getContent() throws MarshalException, ValidationException, IOException, MappingException {
		StringWriter sw = new StringWriter();

		try {
			Marshaller marshaller = createMarshaller(sw);
			marshaller.marshal(this);

			StringBuffer buff = sw.getBuffer();

			String s = "<dhl:dokument";
			int start = buff.indexOf(s);

			if (start > -1) {
				int end = buff.indexOf(">", start);

				if (end > -1) {
					buff.replace(start, end + 1, String.format("<dhl:dokument\n %s \n %s \n %s \n %s>", Xmlns_DHL, Xmlns_MA, Xmlns_MM,
						Xmlns_RKEL));
				}
			}

			return sw.toString();
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		} finally {
			sw.close();
		}
	}

	@Override
	public Version getInternalVersion() {
		return Version.Ver1;
	}

	static String transformMetainfo(String xml) {
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagNameNS(NameSpaceURI_DHL, "metainfo");

			if (nodes.getLength() == 0) {
				return null;
			}

			Element metainfo = (Element) nodes.item(0);

			nodes = metainfo.getElementsByTagNameNS(NameSpaceURI_MM, "metainfo");
			if (nodes.getLength() == 0) {
				// is not transformed
				processNamespaceMetaManual(metainfo);
			}

			nodes = metainfo.getElementsByTagNameNS(NameSpaceURI_MA, "metainfo");
			if (nodes.getLength() == 0) {
				// is not transformed
				processNamespaceMetaAutomatic(metainfo);
			}

			removeEmptyTextNodes(metainfo);

			String strResult = Util.transformXml2String(doc);
			int indx = strResult.indexOf("?>");

			if (indx > -1) {
				indx = strResult.indexOf("<", indx);

				if (indx > -1) {
					strResult = strResult.substring(indx); // cut off xml header
				}
			}

			return strResult;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	private static void processNamespaceMetaAutomatic(Element metainfo) {
		NodeList nodes = metainfo.getElementsByTagNameNS(NameSpaceURI_MA, "*");

		if (nodes.getLength() == 0) {
			return;
		}

		// process mm
		int indx = 0;
		Element maMetainfo = null;
		Document doc = metainfo.getOwnerDocument();

		while (nodes.getLength() > indx) {
			Node node = nodes.item(indx);

			if (node.getNodeType() != Node.ELEMENT_NODE) {
				++indx;
				continue;
			}

			String value = trimNodeText(node.getTextContent());
			metainfo.removeChild(node);

			if (value == null) {
				continue;
			}

			if (maMetainfo == null) {
				maMetainfo = doc.createElementNS(NameSpaceURI_MA, PrefixMA + ":metainfo");
			}

			maMetainfo.appendChild(node);
		}

		if (maMetainfo != null && maMetainfo.hasChildNodes()) {
			metainfo.appendChild(maMetainfo);
		}
	}

	private static void processNamespaceMetaManual(Element metainfo) {
		NodeList nodes = metainfo.getElementsByTagNameNS(NameSpaceURI_MM, "*");

		if (nodes.getLength() == 0) {
			return;
		}

		// process mm
		int indx = 0;
		final String SaatjaDefineeritud = PrefixMM + ":saatja_defineeritud";
		Element mmMetainfo = null;
		Element saatjad = null;
		Document doc = metainfo.getOwnerDocument();

		while (nodes.getLength() > indx) {
			Node node = nodes.item(indx);

			if (node.getNodeType() != Node.ELEMENT_NODE) {
				++indx;
				continue;
			}

			String value = trimNodeText(node.getTextContent());
			metainfo.removeChild(node);

			if (value == null) {
				continue;
			}

			if (mmMetainfo == null) {
				mmMetainfo = doc.createElementNS(NameSpaceURI_MM, PrefixMM + ":metainfo");
			}

			if (SaatjaDefineeritud.equals(node.getNodeName())) {
				if (saatjad == null) {
					saatjad = doc.createElementNS(NameSpaceURI_MM, SaatjaDefineeritud);
					mmMetainfo.appendChild(saatjad);
				}
				saatjad.appendChild(node);
			} else {
				mmMetainfo.appendChild(node);
			}
		}

		if (mmMetainfo != null && mmMetainfo.hasChildNodes()) {
			metainfo.appendChild(mmMetainfo);
		}
	}

	private static void removeEmptyTextNodes(Node node) {
		NodeList nodes = node.getChildNodes();

		int indx = 0;

		while (nodes.getLength() > indx) {
			Node childNode = nodes.item(indx);

			if (childNode.getNodeType() == Node.TEXT_NODE) {
				String value = trimNodeText(childNode.getTextContent());

				if (value == null) {
					node.removeChild(childNode);
				} else {
					++indx;
				}
			} else {
				++indx;
			}
		}
	}

	private static String trimNodeText(String s) {
		if (Util.isEmpty(s)) {
			return null;
		}

		s = s.trim();

		return s.length() == 0 ? null : s;
	}
}
