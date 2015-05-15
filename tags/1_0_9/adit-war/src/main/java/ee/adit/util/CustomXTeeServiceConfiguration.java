package ee.adit.util;

import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;

public class CustomXTeeServiceConfiguration extends SimpleXTeeServiceConfiguration {

    private static final long serialVersionUID = 1L;

    private String infosysteem;

    public String getInfosysteem() {
        return infosysteem;
    }

    public void setInfosysteem(String infosysteem) {
        this.infosysteem = infosysteem;
    }

}
