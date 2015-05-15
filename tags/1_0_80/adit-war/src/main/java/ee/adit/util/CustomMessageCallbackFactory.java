package ee.adit.util;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.WebServiceMessageCallback;

import ee.webmedia.xtee.client.factory.XTeeMessageCallbackFactory;
import ee.webmedia.xtee.client.service.XTeeAttachment;
import ee.webmedia.xtee.client.service.XTeeServiceConfiguration;

/**
 * Custom message callback factory class. Required to use CustomXTeeMessageCallback.
 *  
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class CustomMessageCallbackFactory implements XTeeMessageCallbackFactory {

    /**
     * Log4J logger.
     */
    private static Logger logger = Logger.getLogger(CustomMessageCallbackFactory.class);

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
        return new CustomXTeeMessageCallback((CustomXTeeServiceConfiguration) conf, attachments);
    }

}
