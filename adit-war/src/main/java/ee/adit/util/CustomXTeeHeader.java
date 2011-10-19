package ee.adit.util;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import ee.adit.ws.endpoint.user.GetJoinedEndpoint;
import ee.webmedia.xtee.XTeeHeader;

/**
 * Class for holding all the SOAP headers data.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class CustomXTeeHeader extends XTeeHeader {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(CustomXTeeHeader.class);

    /**
     * Retieves the value of the {@code INFOSYSTEEM} SOAP header.
     *
     * @param producerName
     * 		X-Road producer name of current application/database.
     * @return Value of {@code INFOSYSTEEM} SOAP header
     */
    public String getInfosysteem(final String producerName) {
        String producerNsUri = String.format("http://producers.%s.xtee.riik.ee/producer/%s", producerName, producerName);
        QName infosysteem = new QName(producerNsUri, "infosysteem");

    	if (this.getElemendid() != null) {
            return this.getElemendid().get(infosysteem);
        } else {
            logger.info("Producer name: " + producerName);
            logger.info("Namespace: " + producerNsUri);
            return null;
        }
    }
}
