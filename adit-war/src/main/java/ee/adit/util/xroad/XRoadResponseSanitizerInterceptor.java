package ee.adit.util.xroad;

import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import ee.webmedia.xtee.client.util.XTeeResponseSanitizerInterceptor;

/**
 * The only reason this class exists is to enable usage of latest Spring Web Services library
 * where the new {@link ClientInterceptor#afterCompletion(MessageContext, Exception) afterCompletion()}
 * method is defined, but the old xtee library has no related implementation.
 * 
 * @author Levan Kekelidze
 */
public class XRoadResponseSanitizerInterceptor extends XTeeResponseSanitizerInterceptor {

	@Override
	public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {}

}
