package ee.adit.ws.endpoint.document;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.MarkDocumentViewedRequest;
import ee.adit.pojo.MarkDocumentViewedResponse;
import ee.adit.pojo.Message;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "markDocumentViewed", version = "v1")
@Component
public class MarkDocumentViewedEndpoint extends AbstractAditBaseEndpoint {
	
	private static Logger LOG = Logger.getLogger(MarkDocumentViewedEndpoint.class);
	private UserService userService;
	private DocumentService documentService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		MarkDocumentViewedResponse response = new MarkDocumentViewedResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		Long documentId = null;

		try {
			LOG.debug("markDocumentViewed.v1 invoked.");
			MarkDocumentViewedRequest request = (MarkDocumentViewedRequest) requestObject;
			if (request != null) {
				documentId = request.getDocumentId();
			}
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
			// andmeid näha
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel < 1) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.read", new Object[] { applicationName }, Locale.ENGLISH);
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

			Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());

			// Kontrollime, kas ID-le vastav dokument on olemas
			if (doc != null) {
				if ((doc.getDeleted() == null) || (!doc.getDeleted())) {
					if ((doc.getDeflated() == null) || (!doc.getDeflated())) {
						boolean saveDocument = false;
						
						// Document can be marked as viewed only if:
						// a) document belongs to user
						// b) document is sent or shared to user
						boolean userIsDocOwner = false;
						if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
							userIsDocOwner = true;
						} else {
							if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
								Iterator it = doc.getDocumentSharings().iterator();
								while (it.hasNext()) {
									DocumentSharing sharing = (DocumentSharing)it.next();
									if (sharing.getUserCode().equalsIgnoreCase(userCode)) {
										userIsDocOwner = true;
										
										if (sharing.getLastAccessDate() == null) {
											sharing.setLastAccessDate(new Date());
											saveDocument = true;
										}
										
										break;
									}
								}
							}
						}
						
						// Kui kasutaja tohib dokumendile ligi pääseda, siis tagastame failid
						if (userIsDocOwner) {
							// If document has not been viewed by current user before then mark it viewed.
							boolean isViewed = false;
							if ((doc.getDocumentHistories() != null) && (!doc.getDocumentHistories().isEmpty())) {
								Iterator it = doc.getDocumentHistories().iterator();
								while (it.hasNext()) {
									DocumentHistory event = (DocumentHistory)it.next();
									if (event.getDocumentHistoryType().equalsIgnoreCase(DocumentService.HistoryType_MarkViewed)
										&& event.getUserCode().equalsIgnoreCase(userCode)) {
										isViewed = true;
										break;
									}
								}
							}
							
							if (!isViewed) {
								// Add first viewing history event
								DocumentHistory historyEvent = new DocumentHistory();
								historyEvent.setRemoteApplicationName(applicationName);
								historyEvent.setDocumentId(doc.getId());
								historyEvent.setDocumentHistoryType(DocumentService.HistoryType_MarkViewed);
								historyEvent.setEventDate(requestDate.getTime());
								historyEvent.setUserCode(userCode);
								doc.getDocumentHistories().add(historyEvent);
								saveDocument = true;
							}
							
							if (saveDocument) {
								this.documentService.getDocumentDAO().save(doc, null, null);
							}
							
							// Teavituskalendri kaudu teavituse saatmine
							if (!isViewed && (userService.findNotification(user.getUserNotifications(), ScheduleClient.EventType_View) != null)) {
								ScheduleClient.addEvent(
									user,
									this.getMessageSource().getMessage("scheduler.message.view", new Object[] { doc.getTitle(), userCode }, Locale.ENGLISH),
									this.getConfiguration().getSchedulerEventTypeName(),
									requestDate,
									doc.getId());
							}
							
						} else {
							LOG.debug("Requested document does not belong to user. Document ID: " + request.getDocumentId() + ", User ID: " + userCode);
							String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
							throw new AditException(errorMessage);
						}
					} else {
						LOG.debug("Requested document is deflated. Document ID: " + request.getDocumentId());
						String errorMessage = this.getMessageSource().getMessage("document.deflated", new Object[] { doc.getDeflateDate() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
				} else {
					LOG.debug("Requested document is deleted. Document ID: " + request.getDocumentId());
					String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
			} else {
				LOG.debug("Requested document does not exist. Document ID: " + request.getDocumentId());
				String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() },	Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.markDocumentViewed.success",	new Object[] { request.getDocumentId(), user.getUserCode() }, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
			additionalInformationForLog = "Request failed: " + e.getMessage();
			LOG.error("Exception: ", e);
			response.setSuccess(false);
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

			if (e instanceof AditException) {
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

	
	private void checkHeader(CustomXTeeHeader header) throws Exception {
		String errorMessage = null;
		if (header != null) {
			if ((header.getIsikukood() == null)	|| (header.getIsikukood().length() < 1)) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.personalCode", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if ((header.getInfosysteem() == null) || (header.getInfosysteem().length() < 1)) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.systemName", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if ((header.getAsutus() == null) || (header.getAsutus().length() < 1)) {
				errorMessage = this.getMessageSource().getMessage("request.header.undefined.institution", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		}
	}

	private void checkRequest(MarkDocumentViewedRequest request) {
		String errorMessage = null;
		if (request != null) {
			if ((request.getDocumentId() == null) || (request.getDocumentId() <= 0)) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {},	Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}

	private static void printRequest(MarkDocumentViewedRequest request) {
		LOG.debug("-------- MarkDocumentViewedRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		LOG.debug("------------------------------------------");
	}
}
