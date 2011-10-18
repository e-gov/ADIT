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
     * Retieves the value of the {@code INFOSYSTEEM} SOAP header.
     *
     * @param producerName
     * 		X-Road producer name of current application/database.
     * @return Value of {@code INFOSYSTEEM} SOAP header
     */
    public String getInfosysteem(final String producerName) {
        String producerNsUri = String.format("http://producers.{0}.xtee.riik.ee/producer/{0}", producerName);
        QName infosysteem = new QName(producerNsUri, "infosysteem");

    	if (this.getElemendid() != null) {
            return this.getElemendid().get(infosysteem);
        } else {
            return null;
        }
    }
}
