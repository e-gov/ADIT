package ee.adit.ws.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.ws.mime.Attachment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetJoinedRequest;
import ee.adit.pojo.GetJoinedResponse;
import ee.adit.pojo.GetUserInfoRequest;
import ee.adit.pojo.GetUserInfoRequestAttachmentUserList;
import ee.adit.pojo.GetUserInfoResponse;
import ee.adit.pojo.GetUserInfoResponseAttachmentUser;
import ee.adit.pojo.Success;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getUserInfo", version = "v1")
@Component
public class GetUserInfoEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetUserInfoEndpoint.class);

	private UserService userService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		LOG.debug("GetUserInfoEndpoint.v1 invoked.");

		GetUserInfoResponse response = new GetUserInfoResponse();
		ArrayOfMessage messages = new ArrayOfMessage();

		try {

			GetUserInfoRequest request = (GetUserInfoRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();

			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);

			if(applicationRegistered) {
				
				// Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid näha
				int accessLevel = this.getUserService().getAccessLevel(applicationName);
				
				if(accessLevel >= 1) {
					
					Iterator<Attachment> i = this.getRequestMessage().getAttachments();
					
					int attachmentCount = 0;
					while(i.hasNext()) {
						if(attachmentCount == 0) {
							Attachment attachment = i.next();
							LOG.debug("Attachment: " + attachment.getContentId());
							
							// Extract the SOAP message to a temporary file
							String base64EncodedFile = extractXML(attachment);
							
							// Base64 decode and unzip the temporary file
							String xmlFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());
							LOG.debug("Attachment unzipped to temporary file: " + xmlFile);
							
							// Unmarshal the XML from the temporary file
							Object unmarshalledObject = unMarshal(xmlFile);
							
							// Check if the marshalling result is what we expected
							if(unmarshalledObject != null) {
								LOG.debug("XML unmarshalled to type: " + unmarshalledObject.getClass());
								if(unmarshalledObject instanceof GetUserInfoRequestAttachmentUserList) {
									
									GetUserInfoRequestAttachmentUserList userList = (GetUserInfoRequestAttachmentUserList) unmarshalledObject;
									// TODO: Query database
									
									List<GetUserInfoResponseAttachmentUser> userInfoList = this.getUserService().getUserInfo(userList);
									
									// TODO: Construct response
									
								} else {
									throw new AditInternalException("Unmarshalling returned wrong type. Expected " + GetUserInfoRequestAttachmentUserList.class + ", got " + unmarshalledObject.getClass());
								}
								
							} else {
								throw new AditInternalException("Unmarshalling failed for XML in file: " + xmlFile);
							}
							
						} else {
							String errorMessage = this.getMessageSource().getMessage("request.attachments.tooMany", new Object[] { applicationName }, Locale.ENGLISH);
							throw new AditException(errorMessage);
						}
						attachmentCount++;
					}					
				} else {
					String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.read", new Object[] { applicationName }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}	
				
			} else {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
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
	
	public String extractXML(Attachment attachment) throws IOException {
		String result = Util.createTemporaryFile(attachment.getInputStream(), this.getConfiguration().getTempDir());
		LOG.debug("Attachment extracted to temporary file: " + result);
		return result;
	}
}