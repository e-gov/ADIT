package ee.adit.util;

import javax.xml.namespace.QName;

import ee.webmedia.xtee.XTeeHeader;

/**
 * Class for holding all the SOAP headers data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class CustomXTeeHeader extends XTeeHeader {

    private static final long serialVersionUID = 1L;

    /**
     * The default namespace prefix used for ADIT namespace
     */
    public static final String ADIT_NS_PREFIX = "adit";

    /**
     * ADIT namespace URI.
     */
    public static final String ADIT_NS_URI = "http://producers.ametlikud-dokumendid.xtee.riik.ee/producer/ametlikud-dokumendid";

    /**
     * ADIT specific SOAP header "infosysteem" that specifies the
     * remote_application invoking the web-service
     */
    public static final QName INFOSYSTEEM = new QName(ADIT_NS_URI, "infosysteem");

    /**
     * Retieves the value of the {@code INFOSYSTEEM} SOAP header
     * 
     * @return Value of {@code INFOSYSTEEM} SOAP header
     */
    public String getInfosysteem() {
        if (this.getElemendid() != null) {
            return this.getElemendid().get(INFOSYSTEEM);
        } else {
            return null;
        }
    }

}
