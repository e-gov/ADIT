package ee.adit.util;

import java.util.Collection;

import org.springframework.ws.client.core.WebServiceTemplate;

import ee.webmedia.xtee.client.factory.StandardXTeeMessageCallbackFactory;
import ee.webmedia.xtee.client.service.XTeeAttachment;
import ee.webmedia.xtee.client.service.XTeeServiceConfiguration;

/**
 * Standard class for consuming X-Tee services.
 * 
 * @author Rando Mihkelsaar
 */
public class CustomXTeeConsumer
{
  protected WebServiceTemplate webServiceTemplate;
  protected CustomMessageCallbackFactory msgCallbackFactory;
  private XTeeServiceConfiguration serviceConfiguration;

  /**
   * Sets the central class for client-side Web services
   */
  public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
    this.webServiceTemplate = webServiceTemplate;
  }

  /**
   * Sets the factory that is used to produce <code>WebServiceMessageCallback</code>
   * when web service invocation is made.
   */
  public void setMsgCallbackFactory(CustomMessageCallbackFactory msgCallbackFactory) {
	this.msgCallbackFactory = msgCallbackFactory;
  }

  /**
   * Sets service configurator, which overrides values taken from spring configuration if possible.
   * 
   * @param serviceConfiguration
   */
  public void setServiceConfiguration(XTeeServiceConfiguration serviceConfigurator) {
    this.serviceConfiguration = serviceConfigurator;
  }

  public XTeeServiceConfiguration getServiceConfiguration() {
    return serviceConfiguration;
  }

  public WebServiceTemplate getWebServiceTemplate() {
    return webServiceTemplate;
  }

  public <T> Object sendRequest(T t) {
    return sendRealRequest(t, serviceConfiguration, null);
  }

  public <T> Object sendRequest(T t, Collection<XTeeAttachment> attachments) {
    return sendRealRequest(t, serviceConfiguration, attachments);
  }

  public <T> Object sendRequest(T t, XTeeServiceConfiguration xteeServiceConfigurator) {
    return sendRealRequest(t, xteeServiceConfigurator, null);
  }

  public <T> Object sendRequest(T t, XTeeServiceConfiguration xteeServiceConfigurator, Collection<XTeeAttachment> attachments) {
    return sendRealRequest(t, xteeServiceConfigurator, attachments);
  }
  
  private <T> Object sendRealRequest(T t, XTeeServiceConfiguration xteeServiceConfigurator, Collection<XTeeAttachment> attachments) {
    return webServiceTemplate.marshalSendAndReceive(
    		serviceConfiguration.getSecurityServer(), 
    		t, 
    		getCurrentMessageCallbackFactory().create(xteeServiceConfigurator, attachments));
  }

  protected CustomMessageCallbackFactory getCurrentMessageCallbackFactory() {
	return msgCallbackFactory != null ? msgCallbackFactory : new CustomMessageCallbackFactory();
  }
}
