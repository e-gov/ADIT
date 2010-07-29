package ee.adit.ws.endpoint.user;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.NotificationType;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.Notification;
import ee.adit.pojo.SetNotificationsRequest;
import ee.adit.pojo.SetNotificationsResponse;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

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
	protected Object invokeInternal(Object requestObject) throws Exception {
		SetNotificationsResponse response = new SetNotificationsResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Date requestDate = Calendar.getInstance().getTime();
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
			
			// Check whether or not all given notification types really exist
			List<NotificationType> existingTypes = this.userService.getNotificationTypeDAO().getNotificationTypeList();
			String incorrectNotificationTypes = "";
			for (Notification item : request.getNotifications().getNotification()) {
				if (NotificationType.findFromList(existingTypes, item.getType()) == null) {
					incorrectNotificationTypes += item.getType() + "  ";
				}
			}
			if (incorrectNotificationTypes.length() > 0) {
				incorrectNotificationTypes = incorrectNotificationTypes.trim().replace("  ", ", ");
				messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.setNotifications.incorrectNotificationTypesProvided", new Object[] { incorrectNotificationTypes }, Locale.ENGLISH)));
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
		
		super.logCurrentRequest(null, requestDate, additionalInformationForLog);
		return response;
	}

	@Override
	protected Object getResultForGenericException(Exception ex) {
		SetNotificationsResponse response = new SetNotificationsResponse();
		response.setSuccess(false);
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
	
	private void checkRequest(SetNotificationsRequest request) {
		String errorMessage = null; 
		if(request != null) {
			if ((request.getNotifications() == null) || (request.getNotifications().getNotification() == null) || request.getNotifications().getNotification().isEmpty()) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.notifications", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}
	
	private static void printRequest(SetNotificationsRequest request) {
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
