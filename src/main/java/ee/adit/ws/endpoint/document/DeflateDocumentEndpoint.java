package ee.adit.ws.endpoint.document;

import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.DeflateDocumentRequest;
import ee.adit.pojo.DeflateDocumentResponse;
import ee.adit.pojo.Success;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "deflateDocument", version = "v1")
@Component
public class DeflateDocumentEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(DeflateDocumentEndpoint.class);
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
		DeflateDocumentResponse response = new DeflateDocumentResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		
		try {
			LOG.debug("deflateDocument.v1 invoked.");
			DeflateDocumentRequest request = (DeflateDocumentRequest) requestObject;
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
			if (!applicationRegistered) {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringu käivitanud infosüsteem tohib
			// andmeid muuta (või üldse näha)
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if(accessLevel != 2) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.write", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Kontrollime, kas päringus märgitud isik on teenuse kasutaja
			String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader().getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader().getIsikukood(); 
			AditUser user = this.getUserService().getUserByID(userCode);
			if(user == null) {
				String errorMessage = this.getMessageSource().getMessage("user.nonExistent", new Object[] { userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Kontrollime, et kasutajakonto ligipääs poleks peatatud (kasutaja lahkunud)
			if((user.getActive() == null) || !user.getActive()) {
				String errorMessage = this.getMessageSource().getMessage("user.inactive", new Object[] { userCode }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
				
			Document doc = this.documentService.getDocumentDAO().getDocument(request.getDocumentId());
			
			// Kontrollime, kas ID-le vastav dokument on olemas
			if (doc != null) {
				boolean saveDocument = false;
				
				// Kontrollime, kas dokument kuulub päringu käivitanud kasutajale
				if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
					// Kontrollime, ega dokument ei ole juba tühjendatud.
					if (!doc.getDeflated()) {
						// TODO: Failide sisu asendamine failide MD5 räsikoodiga
						
						// Märgime dokumendi tühjendatuks ja lukustatuks
						doc.setDeflated(true);
						doc.setDeflateDate(new Date());
						doc.setLocked(true);
						doc.setLockingDate(new Date());
						saveDocument = true;
					} else {
						String errorMessage = this.getMessageSource().getMessage("request.deflateDocument.document.alreadyDeflated", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
				} else {
					String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
				
				// Salvestame dokumendi
				if (saveDocument) {
					// Lisame kustutamise ajaloosündmuse
					DocumentHistory historyEvent = new DocumentHistory();
					historyEvent.setRemoteApplicationName(applicationName);
					historyEvent.setDocumentId(doc.getId());
					historyEvent.setDocumentHistoryType(DocumentService.HistoryType_Deflate);
					historyEvent.setEventDate(new Date());
					historyEvent.setUserCode(userCode);
					doc.getDocumentHistories().add(historyEvent);
					
					// Salvestame tehtud muudatused
					this.documentService.getDocumentDAO().save(doc, null);
				}
			} else {
				String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			// Set response messages
			response.setSuccess(new Success(true));
			messages.addMessage(this.getMessageSource().getMessage("request.deflateDocument.success", new Object[] { }, Locale.ENGLISH));
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
		
		return response;
	}
	
	private void checkHeader(CustomXTeeHeader header) throws Exception {
		String errorMessage = null;
		if(header != null) {
			if ((header.getIsikukood() == null) || (header.getIsikukood().length() < 1)) {
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
	
	private void checkRequest(DeflateDocumentRequest request) {
		String errorMessage = null; 
		if(request != null) {
			if(request.getDocumentId() <= 0) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}
	
	private static void printRequest(DeflateDocumentRequest request) {
		LOG.debug("-------- DeflateDocumentRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		LOG.debug("--------------------------------------");
	}
}
