package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.endpoint.AbstractXTeeBaseEndpoint;

@Component
public class GetDocumentEndpoint extends AbstractXTeeBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetDocumentEndpoint.class);

	@Override
	protected void invokeInternal(Document requestKeha, Element responseKeha,
			XTeeHeader xteeHeader) throws Exception {
		
		this.
		
		LOG.debug("GetDocumentEndpoint invoked.");
		
	}
	
	

}
