package ee.adit.ws.endpoint;

import java.math.BigInteger;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetJoinedRequest;
import ee.adit.pojo.GetJoinedResponse;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.JoinResponse;
import ee.adit.pojo.Success;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;
import ee.adit.util.CustomXTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getJoined", version = "v1")
@Component
public class GetJoinedEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetJoinedEndpoint.class);

	private UserService userService;

	private MessageSource messageSource;

	private Configuration configuration;
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		LOG.debug("GetJoinedEndpoint.v1 invoked.");
		
		GetJoinedResponse response = new GetJoinedResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		
		try {
			LOG.debug("JoinEndpoint invoked.");
			GetJoinedRequest request = (GetJoinedRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
			
			if(applicationRegistered) {
				
				// Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid näha
				int accessLevel = this.getUserService().getAccessLevel(applicationName);
				
				if(accessLevel == 1) {
					
					// Kontrollime, kas küsitud kirjete arv jääb maksimaalse lubatud vahemiku piiresse
					BigInteger maxResults = request.getMaxResults();
					BigInteger configurationMaxResults = configuration.getGetJoinedMaxResults();
					
					if(maxResults.intValue() <= configurationMaxResults.intValue()) {
						
						// TODO: teeme andmebaasist väljavõtte vastavalt offset-ile ja maksimaalsele ridade arvule
						
						
					} else {
						// TODO: tagasta veateade
						String errorMessage = this.getMessageSource().getMessage("request.getJoined.maxResults.tooLarge", new Object[] { configurationMaxResults.toString() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
					
				} else {
					String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.read", new Object[] { applicationName }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}				
			} else {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
		} catch (Exception e) {
			LOG.error("Exception: ", e);
		}
		
		return response;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}