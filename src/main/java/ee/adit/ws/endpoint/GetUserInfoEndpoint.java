package ee.adit.ws.endpoint;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetJoinedRequest;
import ee.adit.pojo.GetJoinedResponse;
import ee.adit.pojo.GetUserInfoRequest;
import ee.adit.pojo.GetUserInfoResponse;
import ee.adit.pojo.Success;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getUserInfo", version = "v1")
@Component
public class GetUserInfoEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetUserInfoEndpoint.class);

	private UserService userService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		LOG.debug("GetUserInfoEndpoint.v1 invoked.");

		GetUserInfoResponse response = new GetUserInfoResponse();
		ArrayOfMessage messages = new ArrayOfMessage();

		try {

			GetUserInfoRequest request = (GetUserInfoRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();

			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);

			if(applicationRegistered) {
				
				// TODO: Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid näha

				
			} else {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
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
}