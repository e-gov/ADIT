package ee.adit.ws.endpoint.user;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.JoinResponse;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "join", version = "v1")
@Component
public class JoinEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(JoinEndpoint.class);

	private UserService userService;
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {

		JoinResponse response = new JoinResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;
		
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
			
			// Kontrollime, kas p�ringu k�ivitanud infos�steem on ADITis registreeritud
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
						String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader().getIsikukood();
						AditUser aditUser = userService.getUserByID(userCode);
						
						// Lisame kasutaja või muudame olemasolevat
						if(aditUser != null) { 
							// Muudame olemasolevat kasutajat
							// Kontrollime, kas infosüsteemil on õigus kasutaja andmeid muuta
							int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, aditUser);
							
							if(applicationAccessLevelForUser == 2) {
								LOG.info("Modifying existing user.");
								boolean userReactivated = userService.modifyUser(aditUser, request.getUserName(), usertype);
								
								String message =  null;
								if(userReactivated) {
									message = this.getMessageSource().getMessage("request.join.success.userReactivated", new Object[] { request.getUserType() }, Locale.ENGLISH);
								} else {
									message = this.getMessageSource().getMessage("request.join.success.userModified", new Object[] { request.getUserType() }, Locale.ENGLISH);
								}
								
								response.setSuccess(new Success(true));
								messages.addMessage(new Message("en", message));
								additionalInformationForLog = "SUCCESS: " + message;
								
							} else {
								String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.forUser.write", new Object[] { applicationName, aditUser.getUserCode() }, Locale.ENGLISH);
								throw new AditException(errorMessage);
							}
							
						} else {
							// Lisame uue kasutaja
							LOG.info("Adding new user.");
							userService.addUser(request.getUserName(), usertype, header.getAllasutus(), header.getIsikukood());
							response.setSuccess(new Success(true));
							String message = this.getMessageSource().getMessage("request.join.success.userAdded", new Object[] { request.getUserType() }, Locale.ENGLISH);
							messages.addMessage(new Message("en", message));
							additionalInformationForLog = "SUCCESS: " + message;
						}
						
					} else {
						String usertypes = this.getUserService().getUsertypesString();
						String errorMessage = this.getMessageSource().getMessage("usertype.nonExistent", new Object[] { request.getUserType(), usertypes }, Locale.ENGLISH);
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
			additionalInformationForLog = "ERROR: " + e.getMessage();
			super.logError(documentId, requestDate.getTime(), LogService.ErrorLogLevel_Error, e.getMessage());
			
			response.setSuccess(new Success(false));
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
		
		super.logCurrentRequest(documentId, requestDate.getTime(), additionalInformationForLog);
		return response;
	}

	@Override
	protected Object getResultForGenericException(Exception ex) {
		JoinResponse response = new JoinResponse();
		response.setSuccess(new Success(false));
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
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
			if(request.getUserType() == null || "".equalsIgnoreCase(request.getUserType().trim())) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.usertype", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if(request.getUserName() == null || "".equalsIgnoreCase(request.getUserName().trim())) {
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
	
}