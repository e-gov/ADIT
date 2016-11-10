package ee.adit.util.xroad;

import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;

/**
 * Custom XTee service configuration. Adds the element "infosysteem" to
 * existing XTee headers.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class CustomXRoadServiceConfiguration
    extends SimpleXTeeServiceConfiguration {

    private static final long serialVersionUID = 1L;

    /**
     * Remote application name.
     */
    private String infosysteem;

    /**
     * Get remote application name.
     * @return remote application name
     */
    public String getInfosysteem() {
        return infosysteem;
    }

    /**
     * Set remote application name.
     * @param infosysteem remote application name
     */
    public void setInfosysteem(String infosysteem) {
        this.infosysteem = infosysteem;
    }

}
