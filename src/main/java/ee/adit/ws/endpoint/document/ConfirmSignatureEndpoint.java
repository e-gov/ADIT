package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Component;

import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "confirmSignature", version = "v1")
@Component
public class ConfirmSignatureEndpoint extends AbstractAditBaseEndpoint {

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		Date requestDate = Calendar.getInstance().getTime();
		String additionalInformationForLog = null;
		Long documentId = null;
		
		// TODO Auto-generated method stub
		super.logCurrentRequest(documentId, requestDate, additionalInformationForLog);
		return null;
	}

}
