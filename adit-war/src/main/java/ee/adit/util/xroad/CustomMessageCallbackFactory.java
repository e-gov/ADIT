package ee.adit.util.xroad;

import java.util.Collection;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.ws.client.core.WebServiceMessageCallback;

import ee.webmedia.xtee.client.factory.XTeeMessageCallbackFactory;
import ee.webmedia.xtee.client.service.XTeeAttachment;
import ee.webmedia.xtee.client.service.XTeeServiceConfiguration;

/**
 * Custom message callback factory class. Required to use CustomXRoadMessageCallback.
 *  
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class CustomMessageCallbackFactory implements XTeeMessageCallbackFactory {

    /**
     * Log4J logger.
     */
    private static Logger logger = LogManager.getLogger(CustomMessageCallbackFactory.class);

    /**
     * Creates a new WebServiceMessageCallback.
     * 
     * @param conf XTee service configuration
     * @param attachments message attachments
     * 
     * @return callback
     */
    @Override
    public WebServiceMessageCallback create(XTeeServiceConfiguration conf, Collection<XTeeAttachment> attachments) {
        logger.debug("Creating WebServiceMessageCallback...");
        return new CustomXRoadMessageCallback((CustomXRoadServiceConfiguration) conf, attachments);
    }

}
