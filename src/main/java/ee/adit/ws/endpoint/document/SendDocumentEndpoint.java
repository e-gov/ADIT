package ee.adit.ws.endpoint.document;

import org.springframework.stereotype.Component;

import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "sendDocument", version = "v1")
@Component
public class SendDocumentEndpoint extends AbstractAditBaseEndpoint {

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}