package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;

public class GetDocumentEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetDocumentEndpoint.class);
	
	
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		
		LOG.debug("GetDocumentEndpoint invoked");
		
		org.springframework.ws.transport.http.MessageDispatcherServlet w;
		
		return null;
	}

}
