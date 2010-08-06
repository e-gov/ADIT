package ee.adit.ws.endpoint.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetJoinedRequest;
import ee.adit.pojo.GetJoinedResponse;
import ee.adit.pojo.GetJoinedResponseAttachment;
import ee.adit.pojo.GetJoinedResponseAttachmentUser;
import ee.adit.pojo.Message;
import ee.adit.pojo.SetNotificationsResponse;
import ee.adit.pojo.UserList;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.JoinResponse;
import ee.adit.pojo.Success;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.util.XMLUtil;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getJoined", version = "v1")
@Component
public class GetJoinedEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetJoinedEndpoint.class);

	private UserService userService;
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		LOG.debug("GetJoinedEndpoint.v1 invoked.");
		
		GetJoinedResponse response = new GetJoinedResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		
		try {
			
			// Check configuration
			checkConfiguration();
			
			GetJoinedRequest request = (GetJoinedRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Check request
			checkRequest(request);
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
			
			if(applicationRegistered) {
				
				// Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid näha
				int accessLevel = this.getUserService().getAccessLevel(applicationName);
				
				if(accessLevel >= 1) {
					
					// Kontrollime, kas küsitud kirjete arv jääb maksimaalse lubatud vahemiku piiresse
					BigInteger maxResults = request.getMaxResults();
					BigInteger configurationMaxResults = this.getConfiguration().getGetJoinedMaxResults();
					BigInteger startIndex = request.getStartIndex();
					
					if(maxResults == null) {
						maxResults = configurationMaxResults;
					}
					
					if(startIndex == null) {
						startIndex = BigInteger.ZERO;
					}
					
					if(maxResults.intValue() <= configurationMaxResults.intValue()) {
						
						// Teeme andmebaasist väljavõtte vastavalt offset-ile ja maksimaalsele ridade arvule
						List<AditUser> userList = this.getUserService().listUsers(startIndex, maxResults);
						
						if(userList != null && userList.size() > 0) {
							LOG.debug("Number of users found: " + userList.size());
							
							// 1. Convert java list to XML string and output to file
							String xmlFile = outputToFile(userList);
							
							// 3. GZip and Base64 encode the temporary file
							String gzipFileName = Util.gzipAndBase64Encode(xmlFile, this.getConfiguration().getTempDir(), this.getConfiguration().getDeleteTemporaryFilesAsBoolean());

							// 4. Add as an attachment
							String contentID = addAttachment(gzipFileName);
							UserList getJoinedResponseUserList = new UserList();
							getJoinedResponseUserList.setHref("cid:" + contentID);
							response.setUserList(getJoinedResponseUserList);
							
							String message = this.getMessageSource().getMessage("request.getJoined.success", new Object[] { userList.size() }, Locale.ENGLISH);
							response.setSuccess(new Success(true));
							messages.addMessage(new Message("en", message));
							
						} else {
							LOG.warn("No users were found.");
							String message = this.getMessageSource().getMessage("request.getJoined.noUsersFound", new Object[] { }, Locale.ENGLISH);
							throw new AditException(message);
						}						
						
					} else {
						String errorMessage = this.getMessageSource().getMessage("request.getJoined.maxResults.tooLarge", new Object[] { configurationMaxResults.toString() }, Locale.ENGLISH);
						throw new AditException(errorMessage);
					}
				} else {
					String errorMessage = this.getMessageSource().getMessage("application.insufficientPrivileges.read", new Object[] { applicationName }, Locale.ENGLISH);
					throw new AditException(errorMessage);
				}				
			} else {
				String errorMessage = this.getMessageSource().getMessage("application.notRegistered", new Object[] { applicationName }, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
			
			response.setMessages(messages);
			
		} catch (Exception e) {
			LOG.error("Exception: ", e);
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
		
		return response;
	}
	
	@Override
	protected Object getResultForGenericException(Exception ex) {
		LOG.debug("Constructing result for generic exception...");
		GetJoinedResponse response = new GetJoinedResponse();
		response.setSuccess(new Success(false));
		ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
		arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
		response.setMessages(arrayOfMessage);
		LOG.debug("Returning generic exception response");
		return response;
	}

	private String outputToFile(List<AditUser> userList) throws XmlMappingException, IOException, ParserConfigurationException, TransformerException {
		List<GetJoinedResponseAttachmentUser> getJoinedResponseAttachmentUserList = new ArrayList<GetJoinedResponseAttachmentUser>();
		
		for(int i = 0; i < userList.size(); i++) {
			AditUser aditUser = userList.get(i);
			GetJoinedResponseAttachmentUser getJoinedResponseAttachmentUser = new GetJoinedResponseAttachmentUser();
			getJoinedResponseAttachmentUser.setCode(aditUser.getUserCode());
			getJoinedResponseAttachmentUser.setName(aditUser.getFullName());
			getJoinedResponseAttachmentUser.setType(aditUser.getUsertype().getShortName());
			getJoinedResponseAttachmentUserList.add(getJoinedResponseAttachmentUser);
		}
		GetJoinedResponseAttachment getJoinedResponseAttachment = new GetJoinedResponseAttachment();
		getJoinedResponseAttachment.setUsers(getJoinedResponseAttachmentUserList);
		getJoinedResponseAttachment.setTotal(getJoinedResponseAttachmentUserList.size());

		return marshal(getJoinedResponseAttachment);
	}
	
	private void checkRequest(GetJoinedRequest request) {
		String errorMessage = null; 
		if(request != null) {
			if(request.getMaxResults() != null && request.getMaxResults().longValue() <= 0) {
				errorMessage = this.getMessageSource().getMessage("request.getJoined.body.invalid.maxResults", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			} else if(request.getStartIndex() != null && request.getStartIndex().longValue() < 0) {
				errorMessage = this.getMessageSource().getMessage("request.getJoined.body.invalid.startIndex", new Object[] {}, Locale.ENGLISH);
				throw new AditException(errorMessage);
			}
		} else {
			errorMessage = this.getMessageSource().getMessage("request.body.empty", new Object[] {}, Locale.ENGLISH);
			throw new AditException(errorMessage);
		}
	}
	
	private void checkConfiguration() throws AditInternalException {
		if(this.getConfiguration() == null) {
			throw new AditInternalException("Configuration not initialized - check servlet configuration.");
		} else {
			if(this.getConfiguration().getGetJoinedMaxResults() == null) {
				throw new AditInternalException("Configuration not properly initialized (parameter 'getJoinedMaxResults' is undefined) - check servlet configuration.");
			}
			if(this.getConfiguration().getDeleteTemporaryFiles() == null) {
				throw new AditInternalException("Configuration not properly initialized (parameter 'deleteTemporaryFiles' is undefined) - check servlet configuration.");
			}
			if(this.getConfiguration().getTempDir() == null) {
				
				throw new AditInternalException("Configuration not properly initialized (parameter 'tempDir' is undefined) - check servlet configuration.");
			} else {
				try {
					boolean tempDirExists = (new File(this.getConfiguration().getTempDir())).exists();
					if(!tempDirExists) {
						throw new FileNotFoundException("Directory does not exist: " + this.getConfiguration().getTempDir());
					}
				} catch (Exception e) {
					throw new AditInternalException("Configuration not properly initialized (parameter 'tempDir' not properly configured) - check servlet configuration.", e);
				}
			}
		}
	}
	
	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}