package ee.adit.ws.endpoint.document;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.SaveDocumentResponse;
import ee.adit.pojo.Success;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.adit.ws.endpoint.user.JoinEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "saveDocument", version = "v1")
@Component
public class SaveDocumentEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(SaveDocumentEndpoint.class);
	
	private UserService userService;
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		
		SaveDocumentResponse response = new SaveDocumentResponse();
		
		try {
			
			LOG.debug("SaveDocumentEndpoint.v1 invoked.");
			JoinRequest request = (JoinRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// TODO: implement
			
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			response.setSuccess(new Success(false));
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(e.getMessage());
			} else {
				arrayOfMessage.getMessage().add("Service error");
			}
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		
		return response;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
