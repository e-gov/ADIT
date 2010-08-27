package ee.adit.ws.endpoint.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetJoinedRequest;
import ee.adit.pojo.GetJoinedResponse;
import ee.adit.pojo.GetJoinedResponseAttachment;
import ee.adit.pojo.GetJoinedResponseAttachmentUser;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.pojo.UserList;
import ee.adit.service.LogService;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.Util;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getJoined", version = "v1")
@Component
public class GetJoinedEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(GetJoinedEndpoint.class);

	private UserService userService;
	
	@Override
	protected Object invokeInternal(Object requestObject, int version) throws Exception {

		LOG.debug("JoinEndpoint invoked. Version: " + version);
		
		if(version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
		
	}
	
	protected Object v1(Object requestObject) {
		GetJoinedResponse response = new GetJoinedResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		Calendar requestDate = Calendar.getInstance();
		String additionalInformationForLog = null;
		
		try {
			
			// Check configuration
			checkConfiguration();
			
			GetJoinedRequest request = (GetJoinedRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// Check header for required fields
			checkHeader(header);
			
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
							
							// 2. GZip the temporary file
							// Base64 encoding will be done at SOAP envelope level
							String gzipFileName = Util.gzipFile(xmlFile, this.getConfiguration().getTempDir());

							// 3. Add as an attachment
							String contentID = addAttachment(gzipFileName);
							UserList getJoinedResponseUserList = new UserList();
							getJoinedResponseUserList.setHref("cid:" + contentID);
							response.setUserList(getJoinedResponseUserList);
							
							response.setSuccess(new Success(true));							
							messages.setMessage(this.getMessageService().getMessages("request.getJoined.success", new Object[] { userList.size() }));
							
						} else {
							LOG.warn("No users were found.");
							throw new AditCodedException("request.getJoined.noUsersFound");
						}						
						
					} else {
						AditCodedException aditCodedException = new AditCodedException("request.getJoined.maxResults.tooLarge");
						aditCodedException.setParameters(new Object[] { configurationMaxResults.toString() });
						throw aditCodedException;
					}
				} else {
					AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.read");
					aditCodedException.setParameters(new Object[] { applicationName });
					throw aditCodedException;
				}				
			} else {
				AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
				aditCodedException.setParameters(new Object[] { applicationName });
				throw aditCodedException;
			}
			
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
		if(request != null) {
			if(request.getMaxResults() != null && request.getMaxResults().longValue() <= 0) {
				throw new AditCodedException("request.getJoined.body.invalid.maxResults");
			} else if(request.getStartIndex() != null && request.getStartIndex().longValue() < 0) {
				throw new AditCodedException("request.getJoined.body.invalid.startIndex");
			}
		} else {
			throw new AditCodedException("request.body.empty");
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