package ee.adit.ws.endpoint.user;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.NotificationType;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.Notification;
import ee.adit.pojo.SetNotificationsRequest;
import ee.adit.pojo.SetNotificationsResponse;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "setNotifications" web method (web service request).
 * Contains request input validation, request-specific workflow
 * and response composition.  
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "setNotifications", version = "v1")
@Component
public class SetNotificationsEndpoint extends AbstractAditBaseEndpoint {
	private static Logger LOG = Logger.getLogger(JoinEndpoint.class);
	private UserService userService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	protected Object invokeInternal(Object requestObject, int version) throws Exception {
		LOG.debug("setNotifications invoked. Version: " + version);

		if (version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
	}
	
	/**
	 * Executes "V1" version of "setNotifications" request.
	 * 
	 * @param requestObject		Request body object
	 * @return					Response body object
	 */
	protected Object v1(Object requestObject) {
		SetNotificationsResponse response = new SetNotificationsResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		
		try {
			LOG.debug("SetNotificationsEndpoint.v1 invoked.");
			SetNotificationsRequest request = (SetNotificationsRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Log request
			Util.printHeader(header);
			printRequest(request);

			// Check header for required fields
			checkHeader(header);
			
			// Check request body
			checkRequest(request);
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis
			// registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
			if (!applicationRegistered) {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid muuta
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel != 2) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.write", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringus märgitud isik on teenuse kasutaja
			String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader().getIsikukood();
			AditUser user = this.getUserService().getUserByID(userCode);
			if (user == null) {
				String errorMessage = this.getMessageSource().getMessage("user.nonExistent", new Object[] { userCode },	Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
			if ((user.getActive() == null) || !user.getActive()) {
				String errorMessage = this.getMessageSource().getMessage("user.inactive", new Object[] { userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check whether or not the application has rights to
			// modify current user's data.
			int applicationAccessLevelForUser = userService.getAccessLevelForUser(applicationName, user);
			if(applicationAccessLevelForUser != 2) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.forUser.write", new Object[] { applicationName, user.getUserCode() }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Check whether or not all given notification types really exist
			List<NotificationType> existingTypes = this.userService.getNotificationTypeDAO().getNotificationTypeList();
			String incorrectNotificationTypes = "";
			for (Notification item : request.getNotifications().getNotification()) {
				if (NotificationType.findFromList(existingTypes, item.getType()) == null) {
					incorrectNotificationTypes += item.getType() + "  ";
				}
			}
			if (incorrectNotificationTypes.length() > 0) {	
				String correctTypes = this.getUserService().getNotificationTypesString();
				incorrectNotificationTypes = incorrectNotificationTypes.trim().replace("  ", ", ");
				String errorMessage = this.getMessageSource().getMessage("request.setNotifications.incorrectNotificationTypesProvided", new Object[] { incorrectNotificationTypes, correctTypes }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Set notification data
			this.getUserService().setNotifications(user.getUserCode(), request.getNotifications().getNotification());
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.setNotifications.success", new Object[] { }, Locale.ENGLISH)));
			
			// Set response messages
			response.setMessages(messages);
			response.setSuccess(true);
		} catch (Exception e) {
			LOG.error("Exception: ", e);
			additionalInformationForLog = "Request failed: " + e.getMessage();
			super.logError(null, requestDate.getTime(), LogService.ErrorLogLevel_Error, e.getMessage());
			
			response.setSuccess(false);
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditCodedException) {
				LOG.debug("Adding exception messages to response object.");
				arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
			} else if(e instanceof AditException) {
				LOG.debug("Adding exception message to response object.");
				arrayOfMessage.getMessage().add(new Message("en", e.getMessage()));
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
		SetNotificationsResponse response = new SetNotificationsResponse();
		response.setSuccess(false);
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		return response;
	}
	
	/**
	 * Validates request body and makes sure that all
	 * required fields exist and are not empty.
	 * <br><br>
	 * Throws {@link AditCodedException} if any errors in request data are found.
	 * 
	 * @param request				Request body as {@link SetNotificationsRequest} object.
	 * @throws AditCodedException	Exception describing error found in requet body.
	 */
	private void checkRequest(SetNotificationsRequest request) throws AditCodedException {
		if(request != null) {
			if ((request.getNotifications() == null) || (request.getNotifications().getNotification() == null) || request.getNotifications().getNotification().isEmpty()) {
				throw new AditCodedException("request.body.undefined.notifications");
			}
		} else {
			throw new AditCodedException("request.body.empty");
		}
	}
	
	/**
	 * Writes request parameters to application DEBUG log.
	 * 
	 * @param request	Request body as {@link SetNotificationsRequest} object.
	 */
	private void printRequest(SetNotificationsRequest request) {
		LOG.debug("-------- SetNotificationsRequest -------");
		if (request != null) {
			if ((request.getNotifications() != null) && (request.getNotifications().getNotification() != null) && !request.getNotifications().getNotification().isEmpty()) {
				for (Notification item : request.getNotifications().getNotification()) {
					LOG.debug("Type: " + item.getType());
					LOG.debug("Active: " + item.isActive());
				}
			} else {
				LOG.debug("Notification data was not supplied!");
			}
		} else {
			LOG.debug("Request is NULL!");
		}
		LOG.debug("----------------------------------------");
	}
	
}
