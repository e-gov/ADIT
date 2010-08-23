package ee.adit.ws.endpoint.user;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditCodedException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.pojo.UnJoinResponse;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "unJoin", version = "v1")
@Component
public class UnJoinEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(UnJoinEndpoint.class);

	private UserService userService;

	@Override
	protected Object invokeInternal(Object requestObject, int version) throws Exception {
		
		UnJoinResponse response = new UnJoinResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		
		try {
			LOG.debug("UnJoinEndpoint.v1 invoked.");
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
						
						// Kontrollime, kas infosüsteem tohib antud kasutaja andmeid muuta
						int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, aditUser);
						
						if(applicationAccessLevelForUser == 2) {
							LOG.info("Deactivating user.");
							
							// Kontrollime, kas kasutaja on aktiivne
							if(aditUser.getActive()) {
								// Märgime kasutaja lahkunuks
								this.getUserService().deactivateUser(aditUser);
								LOG.info("User (" + aditUser.getUserCode() + ") deactivated.");
								response.setSuccess(new Success(true));
								messages.setMessage(this.getMessageService().getMessages("request.unJoin.success", new Object[] { aditUser.getUserCode() }));
								
							} else {
								AditCodedException aditCodedException = new AditCodedException("request.unJoin.alreadyUnJoined");
								aditCodedException.setParameters(new Object[] { aditUser.getUserCode() });
								throw aditCodedException;
							}
						} else {
							AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.forUser.write");
							aditCodedException.setParameters(new Object[] { applicationName, aditUser.getUserCode() });
							throw aditCodedException;
						}
					} else {
						AditCodedException aditCodedException = new AditCodedException("user.nonExistent");
						aditCodedException.setParameters(new Object[] { header.getIsikukood() });
						throw aditCodedException;
					}
				} else {
					AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.write");
					aditCodedException.setParameters(new Object[] { applicationName });
					throw aditCodedException;
				}				
			} else {
				AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
				aditCodedException.setParameters(new Object[] { applicationName });
				throw aditCodedException;
			}
			
			// Set response messages
			response.setMessages(messages);
			
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			additionalInformationForLog = "Request failed: " + e.getMessage();
			super.logError(null, requestDate.getTime(), LogService.ErrorLogLevel_Error, e.getMessage());
			
			response.setSuccess(new Success(false));
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditCodedException) {
				LOG.debug("Adding exception messages to response object.");
				arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
			} else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
			}
			
			LOG.debug("Adding exception messages to response object.");
			response.setMessages(arrayOfMessage);
		}
		
		super.logCurrentRequest(null, requestDate.getTime(), additionalInformationForLog);
		return response;
	}

	@Override
	protected Object getResultForGenericException(Exception ex) {
		super.logError(null, Calendar.getInstance().getTime(), LogService.ErrorLogLevel_Fatal, ex.getMessage());
		UnJoinResponse response = new UnJoinResponse();
		response.setSuccess(new Success(false));
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		return response;
	}
	
	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}