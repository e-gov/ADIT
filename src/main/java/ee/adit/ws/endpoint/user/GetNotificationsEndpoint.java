package ee.adit.ws.endpoint.user;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.ArrayOfNotification;
import ee.adit.pojo.GetNotificationsResponse;
import ee.adit.pojo.Message;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getNotifications", version = "v1")
@Component
public class GetNotificationsEndpoint extends AbstractAditBaseEndpoint {
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
		GetNotificationsResponse response = new GetNotificationsResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Date requestDate = Calendar.getInstance().getTime();
		String additionalInformationForLog = null;
		
		try {
			LOG.debug("GetNotificationsEndpoint.v1 invoked.");
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Log request
			Util.printHeader(header);

			// Check header for required fields
			checkHeader(header);
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis
			// registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
			if (!applicationRegistered) {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid näha
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel < 1) {
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
			
			// Set notification data
			ArrayOfNotification notificationList = this.getUserService().getNotifications(user.getUserCode());
			response.setNotifications(notificationList);
			
			// TODO: Teavituste üldine staatus teavitusteenusest
			// TODO: Teavituste e-posti aadresside nimekiri
			
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.getNotifications.success", new Object[] { }, Locale.ENGLISH)));
			
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
}
