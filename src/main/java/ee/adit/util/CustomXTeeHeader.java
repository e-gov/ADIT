package ee.adit.util;

import javax.xml.namespace.QName;

import ee.webmedia.xtee.XTeeHeader;

public class CustomXTeeHeader extends XTeeHeader {

	private static final long serialVersionUID = 1L;

	public static final String ADIT_NS_PREFIX = "adit";
	
	public static final String ADIT_NS_URI = "http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid";
	
	public static final QName INFOSYSTEEM = new QName(ADIT_NS_URI, "infosysteem");
	
	public String getInfosysteem() {
		if(this.getElemendid() != null) {
			return this.getElemendid().get(INFOSYSTEEM);
		} else {
			return null;
		}
	}
	
}
