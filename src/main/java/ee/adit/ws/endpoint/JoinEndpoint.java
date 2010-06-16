package ee.adit.ws.endpoint;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

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
		ArrayOfMessage messages = new ArrayOfMessage();
		
		try {

			LOG.debug("JoinEndpoint.v1 invoked.");
			JoinRequest request = (JoinRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Log request
			Util.printHeader(header);
			printRequest(request);

			// Check header for required fields
			checkHeader(header);
			
			// Check request body
			checkRequest(request);
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);

			if (applicationRegistered) {
				
				// Kontrollime, kas päringu käivitanud infosüsteem tohib
				// andmeid muuta (või üldse näha)
				int accessLevel = this.getUserService().getAccessLevel(applicationName);
				
				// Application has write permission
				if(accessLevel == 2) {
					
					// Kontrollime, kas etteantud kasutajatüüp eksisteerib
					Usertype usertype = this.getUserService().getUsertypeByID(request.getUserType());
					
					if(usertype != null) {

						// Kontrollime, kas kasutaja juba eksisteerib
						// s.t. kas lisame uue kasutaja või muudame olemasolevat
						LOG.debug("Checking if user already exists...");
						AditUser aditUser = userService.getUserByID(header.getIsikukood());
						
						// Lisame kasutaja või muudame olemasolevat
						if(aditUser != null) { 
							// Muudame olemasolevat kasutajat
							LOG.info("Modifying existing user.");
							userService.modifyUser(aditUser, request.getUserName(), usertype);
							response.setSuccess(new Success(true));
							String message = this.getMessageSource().getMessage("request.join.success.userModified", new Object[] { request.getUserType() }, Locale.ENGLISH);
							messages.addMessage(message);
						} else {
							// Lisame uue kasutaja
							LOG.info("Adding new user.");
							userService.addUser(request.getUserName(), usertype, header.getAsutus(), header.getIsikukood());
							response.setSuccess(new Success(true));
							String message = this.getMessageSource().getMessage("request.join.success.userAdded", new Object[] { request.getUserType() }, Locale.ENGLISH);
							messages.addMessage(message);
						}
						
					} else {
						String errorMessage = this.getMessageSource().getMessage("usertype.nonExistent", new Object[] { request.getUserType() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
					
				} else {
					String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.write", new Object[] { applicationName }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
				
			} else {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Set response messages
			response.setMessages(messages);
			
		} catch (Exception e) {
			
			LOG.error("Exception: ", e);
			response.setSuccess(new Success(false));
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(e.getMessage());
			} else {
				arrayOfMessage.getMessage().add("Could not register the user - service error.");
			}
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		return response;
	}

	private void checkHeader(CustomXTeeHeader header) throws Exception {
		String errorMessage = null;
		if(header != null) {
			if(header.getIsikukood() == null) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.personalCode", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if(header.getInfosysteem() == null) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.systemName", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if(header.getAsutus() == null) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.institution", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		}
	}
	
	private void checkRequest(JoinRequest request) {
		String errorMessage = null; 
		if(request != null) {
			if(request.getUserType() == null) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.usertype", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if(request.getUserName() == null) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.username", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
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