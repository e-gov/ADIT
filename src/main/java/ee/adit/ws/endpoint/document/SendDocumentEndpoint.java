package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.SaveDocumentRequest;
import ee.adit.pojo.SendDocumentRequest;
import ee.adit.pojo.SendDocumentResponse;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "sendDocument", version = "v1")
@Component
public class SendDocumentEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(SendDocumentEndpoint.class);
	
	private UserService userService;
	
	private DocumentService documentService;
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		SendDocumentResponse response = new SendDocumentResponse();
		Date requestDate = Calendar.getInstance().getTime();
		String additionalInformationForLog = null;
		Long documentId = null;
		
		try {
		
		LOG.debug("SendDocumentEndpoint.v1 invoked.");
		SendDocumentRequest request = (SendDocumentRequest) requestObject;
		CustomXTeeHeader header = this.getHeader();
		String applicationName = header.getInfosysteem();
		
		super.logCurrentRequest(documentId, requestDate, additionalInformationForLog);
		
		// TODO: application registered?
		
		
		
		// TODO: user registered?
		
		// TODO: application access level for user
		
		// TODO: business logic
		
		} catch(Exception e) {
			LOG.error("Exception: ", e);
			response.setSuccess(false);
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(new Message("en", e.getMessage()));
			} else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
			}
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		return null;
	}

}
