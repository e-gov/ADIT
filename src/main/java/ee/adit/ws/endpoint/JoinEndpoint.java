package ee.adit.ws.endpoint;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.JoinResponse;
import ee.adit.pojo.Success;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "join", version = "v1")
@Component
public class JoinEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(JoinEndpoint.class);

	private UserService userService;

	private MessageSource messageSource;

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {

		JoinResponse response = new JoinResponse();
		
		try {

			LOG.debug("JoinEndpoint invoked.");
			JoinRequest request = (JoinRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String systemName = header.getInfosysteem();
			
			// Log the input parameters
			Util.printHeader(header);
			printRequest(request);

			// TODO: check header and request objects - not null, not empty strings and so on.

			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(systemName);

			if (applicationRegistered) {
				
				// Kontrollime, kas päringu käivitanud infosüsteem tohib
				// andmeid muuta (või üldse näha)
				int accessLevel = this.getUserService().getAccessLevel(systemName);
				
				// Application has write 
				if(accessLevel == 2) {
					
					// TODO: Kontrollime, kas etteantud kasutajatüüp eksisteerib
					Usertype usertype = this.getUserService().getUsertypeByID(request.getUserType());
					
					if(usertype != null) {

						// TODO: Kontrollime, kas kasutaja juba eksisteerib
						// s.t. kas lisame uue kasutaja või muudame olemasolevat
						AditUser aditUser = userService.getUserByID(header.getIsikukood());
						
						// TODO: Lisame kasutaja või muudame olemasolevat
						if(aditUser != null) { 
							// Muudame olemasolevat kasutajat
							LOG.info("Modifying existing user.");
						} else {
							// Lisame uue kasutaja
							LOG.info("Adding new user.");
						}
						
					} else {
						String errorMessage = this.getMessageSource().getMessage("usertype.nonExistent", new Object[] { request.getUserType() }, Locale.ENGLISH);
						throw new AditException(errorMessage, null);
					}
					
				} else {
					String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.write", new Object[] { systemName }, Locale.ENGLISH);
					throw new AditException(errorMessage, null);
				}
				
			} else {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { systemName }, Locale.ENGLISH);
				throw new AditException(errorMessage, null);
			}
			
		} catch (Exception e) {
			
			LOG.error("Exception: ", e);
			response.setSuccess(new Success(false));
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(e.getMessage());
			} else {
				arrayOfMessage.getMessage().add("Could not register the user.");
			}
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		
		return response;
	}

	private static void printRequest(JoinRequest request) {

		LOG.debug("-------- JoinRequest -------");
		LOG.debug("UserName: " + request.getUserName());
		LOG.debug("UserType: " + request.getUserType());
		LOG.debug("----------------------------");

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
	
}