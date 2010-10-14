package ee.adit.util;

import java.util.Collection;

import org.springframework.ws.client.core.WebServiceMessageCallback;

import ee.webmedia.xtee.client.factory.StandardXTeeMessageCallbackFactory;
import ee.webmedia.xtee.client.factory.XTeeMessageCallbackFactory;
import ee.webmedia.xtee.client.service.XTeeAttachment;
import ee.webmedia.xtee.client.service.XTeeServiceConfiguration;

public class CustomMessageCallbackFactory extends StandardXTeeMessageCallbackFactory {
	
	@Override
	public WebServiceMessageCallback create(XTeeServiceConfiguration conf, Collection<XTeeAttachment> attachments) {
		return new CustomXTeeMessageCallback(conf, attachments);
	}
	
}
