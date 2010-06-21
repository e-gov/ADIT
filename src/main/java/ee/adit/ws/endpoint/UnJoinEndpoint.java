package ee.adit.ws.endpoint;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.JoinResponse;
import ee.adit.pojo.Success;
import ee.adit.pojo.UnJoinRequest;
import ee.adit.pojo.UnJoinResponse;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "unJoin", version = "v1")
@Component
public class UnJoinEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(UnJoinEndpoint.class);

	private UserService userService;
	
	@Override
	protected void invokeInternal(Document requestKeha, Element responseKeha, CustomXTeeHeader xteeHeader) throws Exception {
		
		
		
		
		
		
		
	}

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		
		UnJoinResponse response = new UnJoinResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		
		try {
			LOG.debug("UnJoinEndpoint.v1 invoked.");
			
			UnJoinRequest request = (UnJoinRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Log request
			Util.printHeader(header);
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
						
			if (applicationRegistered) {
				
				// Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid muuta (või üldse näha)
				int accessLevel = this.getUserService().getAccessLevel(applicationName);
				
				// Application has write permission
				if(accessLevel == 2) {
					
					// Kontrollime, kas päringu käivitanud kasutaja eksisteerib
					AditUser aditUser = userService.getUserByID(header.getIsikukood());
					
					if(aditUser != null) {
						
						// TODO: Kontrollime, kas infosüsteem tohib antud kasutaja andmeid muuta
						int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, aditUser);
						
						if(applicationAccessLevelForUser == 2) {
							LOG.info("Deactivating user.");
							
							// Märgime kasutaja lahkunuks
							this.getUserService().deactivateUser(aditUser);
							
							String message = this.getMessageSource().getMessage("request.unJoin.success", new Object[] { aditUser.getUserCode() }, Locale.ENGLISH);
							messages.addMessage(message);
							
						} else {
							String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.forUser.write", new Object[] { applicationName, aditUser.getUserCode() }, Locale.ENGLISH);
							throw new AditException(errorMessage);
						}
					} else {
						String errorMessage = this.getMessageSource().getMessage("user.nonExistent", new Object[] { header.getIsikukood() }, Locale.ENGLISH);
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
				arrayOfMessage.getMessage().add("Service error");
			}
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		return null;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}