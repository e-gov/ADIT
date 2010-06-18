package ee.adit.ws.endpoint;

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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
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
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetJoinedRequest;
import ee.adit.pojo.GetJoinedResponse;
import ee.adit.pojo.GetJoinedResponseAttachment;
import ee.adit.pojo.GetJoinedResponseAttachmentUser;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.JoinResponse;
import ee.adit.pojo.Success;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.util.XMLUtil;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getJoined", version = "v1")
@Component
public class GetJoinedEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetJoinedEndpoint.class);

	private UserService userService;

	private MessageSource messageSource;

	private Configuration configuration;
	
	
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		LOG.debug("GetJoinedEndpoint.v1 invoked.");
		
		GetJoinedResponse response = new GetJoinedResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		
		try {
			GetJoinedRequest request = (GetJoinedRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);
			
			if(applicationRegistered) {
				
				// Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid näha
				int accessLevel = this.getUserService().getAccessLevel(applicationName);
				
				if(accessLevel >= 1) {
					
					// Kontrollime, kas küsitud kirjete arv jääb maksimaalse lubatud vahemiku piiresse
					BigInteger maxResults = request.getMaxResults();
					BigInteger configurationMaxResults = configuration.getGetJoinedMaxResults();
					
					if(maxResults.intValue() <= configurationMaxResults.intValue()) {
						
						// Teeme andmebaasist väljavõtte vastavalt offset-ile ja maksimaalsele ridade arvule
						List<AditUser> userList = this.getUserService().listUsers(request.getStartIndex(), maxResults);
						
						if(userList != null && userList.size() > 0) {
							LOG.debug("Number of users found: " + userList.size());
							
							// 1. Convert java list to XML string using Castor
							Node rootNode = getXML(userList);
							
							// 2. Output the XML to a temporary file
							String temporaryFile = Util.outputToTemporaryFile(rootNode, this.getConfiguration());
							
							// 3. GZip and Base64 encode the temporary file
							String gzipFileName = Util.gzipAndBase64Encode(temporaryFile, this.getConfiguration().getTempDir());

							// 4. Add as an attachment
							addAttachment(gzipFileName);
							
						} else {
							LOG.warn("No users were found.");
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

	private Node getXML(List<AditUser> userList) throws XmlMappingException, IOException, ParserConfigurationException {
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
	
	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}