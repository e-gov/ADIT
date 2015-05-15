package ee.adit.util;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.WebServiceMessageCallback;

import ee.webmedia.xtee.client.factory.StandardXTeeMessageCallbackFactory;
import ee.webmedia.xtee.client.factory.XTeeMessageCallbackFactory;
import ee.webmedia.xtee.client.service.XTeeAttachment;
import ee.webmedia.xtee.client.service.XTeeServiceConfiguration;

public class CustomMessageCallbackFactory implements XTeeMessageCallbackFactory {
	
	private static Logger LOG = Logger.getLogger(CustomMessageCallbackFactory.class);
	
	@Override
	public WebServiceMessageCallback create(XTeeServiceConfiguration conf, Collection<XTeeAttachment> attachments) {
		LOG.debug("Creating WebServiceMessageCallback...");
		return new CustomXTeeMessageCallback((CustomXTeeServiceConfiguration) conf, attachments);
	}
	
}
