package ee.adit.util;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

public class CustomClientInterceptor implements ClientInterceptor {

	private static Logger LOG = Logger.getLogger(CustomClientInterceptor.class);
	
	private String tmpFile;
	
	private Configuration configuration;
	
	@Override
	public boolean handleFault(MessageContext arg0)
			throws WebServiceClientException {
		return true;
	}

	@Override
	public boolean handleRequest(MessageContext arg0)
			throws WebServiceClientException {
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext arg0)
			throws WebServiceClientException {
		
		LOG.debug("CustomClientInterceptor invoked...");
		boolean result = false;
		
		try {
		
			WebServiceMessage response = arg0.getResponse();
			SaajSoapMessage responseMessage = (SaajSoapMessage) response;
			
			Iterator<Attachment> i = responseMessage.getAttachments();
			
			Attachment a = null;
			if(i.hasNext())
				a = i.next();
			
			if(a != null) {
				String rawTmpFile = Util.createTemporaryFile(a.getInputStream(), this.getConfiguration().getTempDir());
				LOG.debug("Raw data saved to temporary file: " + rawTmpFile);
				String decodedTmpFile = Util.unzip(rawTmpFile, this.getConfiguration().getTempDir());
				this.setTmpFile(decodedTmpFile);
				result = true;
			}
			
		} catch(Exception e) {
			LOG.error("Exception while handling response with interceptor: ", e);
		}
		
		return result;
	}

	public String getTmpFile() {
		return tmpFile;
	}

	public void setTmpFile(String tmpFile) {
		this.tmpFile = tmpFile;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

}
