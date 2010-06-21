package ee.adit.ws.endpoint.document;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.SaveDocumentResponse;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.adit.ws.endpoint.user.JoinEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "saveDocument", version = "v1")
@Component
public class SaveDocumentEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(SaveDocumentEndpoint.class);
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		
		SaveDocumentResponse response = new SaveDocumentResponse();
		
		try {
			
			LOG.debug("JoinEndpoint.v1 invoked.");
			JoinRequest request = (JoinRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
		} catch (Exception e) {
			
		}
		
		
		return response;
	}

}
