package ee.adit.util.xroad;

import java.util.Collection;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.ws.client.core.WebServiceTemplate;

import ee.webmedia.xtee.client.service.XTeeAttachment;
import ee.webmedia.xtee.client.service.XTeeServiceConfiguration;

/**
 * Custom XTee service consumer class.
 *  
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class CustomXRoadConsumer {

    /**
     * Log4J logger.
     */
    private static Logger logger = LogManager.getLogger(CustomXRoadConsumer.class);

    /**
     * Web-service template.
     */
    private WebServiceTemplate webServiceTemplate;
    
    /**
     * Message callback factory.
     */
    private CustomMessageCallbackFactory msgCallbackFactory;
    
    /**
     * Service configuration.
     */
    private XTeeServiceConfiguration serviceConfiguration;

    /**
     * Sets the central class for client-side Web services.
     * 
     * @param webServiceTemplate
     *     Web service template
     */
    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    /**
     * Sets the factory that is used to produce
     * <code>WebServiceMessageCallback</code> when web service invocation is
     * made.
     * 
     * @param msgCallbackFactory
     *     Message callback factory
     */
    public void setMsgCallbackFactory(CustomMessageCallbackFactory msgCallbackFactory) {
        this.msgCallbackFactory = msgCallbackFactory;
    }

    /**
     * Sets service configurator, which overrides values taken from spring
     * configuration if possible.
     * 
     * @param serviceConfigurator
     *     Service configuration as {@link XTeeServiceConfiguration} object
     */
    public void setServiceConfiguration(XTeeServiceConfiguration serviceConfigurator) {
        this.serviceConfiguration = serviceConfigurator;
    }

    /**
     * Get service configuration.
     * @return service configuration
     */
    public XTeeServiceConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }

    /**
     * Get web-service template.
     * @return web-service template
     */
    public WebServiceTemplate getWebServiceTemplate() {
        return webServiceTemplate;
    }

    /**
     * Send request.
     * 
     * @param <T> type
     * @param t request
     * @return response
     */
    public <T> Object sendRequest(T t) {
        return sendRealRequest(t, serviceConfiguration, null);
    }

    /**
     * Send request.
     * 
     * @param <T> type
     * @param t request
     * @param attachments
     *     request attachments
     * @return response
     */
    public <T> Object sendRequest(T t, Collection<XTeeAttachment> attachments) {
        return sendRealRequest(t, serviceConfiguration, attachments);
    }

    /**
     * Send request.
     * 
     * @param <T> type
     * @param t request
     * @param xteeServiceConfigurator service configuration
     * @return response
     */
    public <T> Object sendRequest(T t, XTeeServiceConfiguration xteeServiceConfigurator) {
        return sendRealRequest(t, xteeServiceConfigurator, null);
    }

    /**
     * Send request.
     * 
     * @param <T> type
     * @param t request
     * @param xteeServiceConfigurator service configuration
     * @param attachments attachments
     * @return response
     */
    public <T> Object sendRequest(T t, XTeeServiceConfiguration xteeServiceConfigurator,
            Collection<XTeeAttachment> attachments) {
        return sendRealRequest(t, xteeServiceConfigurator, attachments);
    }

    /**
     * Send request.
     * 
     * @param <T> type
     * @param t request
     * @param xteeServiceConfigurator service configuration
     * @param attachments attachments
     * @return response
     */
    private <T> Object sendRealRequest(T t, XTeeServiceConfiguration xteeServiceConfigurator,
            Collection<XTeeAttachment> attachments) {
        logger.debug("Sending request using CustomXRoadConsumer...");
        return webServiceTemplate.marshalSendAndReceive(serviceConfiguration.getSecurityServer(), t,
                getCurrentMessageCallbackFactory().create(xteeServiceConfigurator, attachments));
    }

    /**
     * Get current message callback factory.
     * 
     * @return message callback factory
     */
    protected CustomMessageCallbackFactory getCurrentMessageCallbackFactory() {
        return msgCallbackFactory != null ? msgCallbackFactory : new CustomMessageCallbackFactory();
    }
}
