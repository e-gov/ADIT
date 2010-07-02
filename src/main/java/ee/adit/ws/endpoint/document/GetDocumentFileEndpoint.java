package ee.adit.ws.endpoint.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.exception.AditException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetDocumentFileRequest;
import ee.adit.pojo.GetDocumentFileResponse;
import ee.adit.pojo.GetDocumentFileResponseAttachment;
import ee.adit.pojo.GetDocumentFileResponseAttachmentFile;
import ee.adit.pojo.GetDocumentFileResponseFiles;
import ee.adit.pojo.Message;
import ee.adit.service.DocumentService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getDocumentFile", version = "v1")
@Component
public class GetDocumentFileEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetDocumentFileEndpoint.class);
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
		GetDocumentFileResponse response = new GetDocumentFileResponse();
		ArrayOfMessage messages = new ArrayOfMessage();

		try {
			LOG.debug("getDocumentFile.v1 invoked.");
			GetDocumentFileRequest request = (GetDocumentFileRequest) requestObject;
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
			// andmeid muuta (või üldse näha)
			int accessLevel = this.getUserService().getAccessLevel(applicationName);
			if (accessLevel != 2) {
				String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.write", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Kontrollime, kas päringus märgitud isik on teenuse kasutaja
			String userCode = ((this.getHeader().getAllasutus() != null) && (this
					.getHeader().getAllasutus().length() > 0)) ? this
					.getHeader().getAllasutus() : this.getHeader()
					.getIsikukood();
			AditUser user = this.getUserService().getUserByID(userCode);
			if (user == null) {
				String errorMessage = this.getMessageSource().getMessage(
						"user.nonExistent", new Object[] { userCode },
						Locale.ENGLISH);
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
				// Kontrollime, kas dokument kuulub päringu käivitanud kasutajale
				// TODO: Siin sobib ka kasutajale välja jagatud dokument
				if (doc.getCreatorCode().equalsIgnoreCase(userCode)) {
					if ((doc.getDocumentFiles() != null) && (doc.getDocumentFiles().size() > 0)) {
						LOG.debug("Document has " + doc.getDocumentFiles().size()  + " files.");
						
						List<DocumentFile> filesList = new ArrayList<DocumentFile>(doc.getDocumentFiles());
						
						// 1. Convert java list to XML string and output to file
						String xmlFile = outputToFile(filesList);
						
						// 2. GZip and Base64 encode the temporary file
						String gzipFileName = Util.gzipAndBase64Encode(xmlFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());

						// 3. Add as an attachment
						String contentID = addAttachment(gzipFileName);
						GetDocumentFileResponseFiles files = new GetDocumentFileResponseFiles();
						files.setHref("cid:" + contentID);
						response.setFiles(files);
					} else {
						LOG.debug("Document has no files!");
					}					
				} else {
					String errorMessage = this.getMessageSource().getMessage("document.doesNotBelongToUser", new Object[] { request.getDocumentId(), userCode }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}
			} else {
				String errorMessage = this.getMessageSource().getMessage("document.nonExistent", new Object[] { request.getDocumentId() },	Locale.ENGLISH);
				throw new AditException(errorMessage);
			}

			// Set response messages
			response.setSuccess(true);
			messages.addMessage(new Message("en", this.getMessageSource().getMessage("request.getDocumentFile.success",	new Object[] {}, Locale.ENGLISH)));
			response.setMessages(messages);
		} catch (Exception e) {
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

		return response;
	}

	private String outputToFile(List<DocumentFile> filesList) throws XmlMappingException, IOException, ParserConfigurationException, TransformerException {
		List<GetDocumentFileResponseAttachmentFile> result = new ArrayList<GetDocumentFileResponseAttachmentFile>();
		
		for (int i = 0; i < filesList.size(); i++) {
			DocumentFile docFile = filesList.get(i);
			GetDocumentFileResponseAttachmentFile fileData = new GetDocumentFileResponseAttachmentFile();
			fileData.setId(docFile.getId());
			fileData.setName(docFile.getFileName());
			fileData.setDescription(docFile.getDescription());
			fileData.setContentType(docFile.getContentType());
			fileData.setSizeBytes(docFile.getFileSizeBytes());
			result.add(fileData);
		}
		GetDocumentFileResponseAttachment attachment = new GetDocumentFileResponseAttachment();
		attachment.setFiles(result);

		return marshal(attachment);
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

	private void checkRequest(GetDocumentFileRequest request) {
		String errorMessage = null;
		if (request != null) {
			if (request.getDocumentId() <= 0) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.documentId", new Object[] {},	Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if ((request.getFileIdList() == null) || (request.getFileIdList().getFileId() == null) || (request.getFileIdList().getFileId().size() < 1)) {
				errorMessage = this.getMessageSource().getMessage("request.body.undefined.fileId", new Object[] {},	Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}

	private static void printRequest(GetDocumentFileRequest request) {
		LOG.debug("-------- GetDocumentFileRequest -------");
		LOG.debug("Document ID: " + String.valueOf(request.getDocumentId()));
		if ((request.getFileIdList() != null) && (request.getFileIdList().getFileId() != null)) {
			for (long fileId : request.getFileIdList().getFileId()) {
				LOG.debug("File ID: " + String.valueOf(fileId));
			}
		}
		LOG.debug("---------------------------------------");
	}
}
