package ee.adit.ws.endpoint.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

/**
 * Implementation of "getJoined" web method (web service request).
 * Contains request input validation, request-specific workflow
 * and response composition.  
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "getJoined", version = "v1")
@Component
public class GetJoinedEndpoint extends AbstractAditBaseEndpoint {

	/**
	 * Log4J logger
	 */
	private static Logger LOG = Logger.getLogger(GetJoinedEndpoint.class);

	/**
	 * User service
	 */
	private UserService userService;
	
	@Override
	protected Object invokeInternal(Object requestObject, int version) throws Exception {
		LOG.debug("getJoined invoked. Version: " + version);
		
		if(version == 1) {
			return v1(requestObject);
		} else {
			throw new AditInternalException("This method does not support version specified: " + version);
		}
		
	}
	
	/**
	 * Executes "V1" version of "getJoined" request.
	 * 
	 * @param requestObject		Request body object
	 * @return					Response body object
	 */
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
							messages.setMessage(this.getMessageService().getMessages("request.getJoined.success", new Object[] { new Integer(userList.size()).toString() }));
							
							// Additional information for request log
							String additionalMessage = this.getMessageService().getMessage("request.getJoined.success", new Object[] { new Integer(userList.size()).toString() }, Locale.ENGLISH);
							additionalInformationForLog = LogService.RequestLog_Success + ": " + additionalMessage;
							
							
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
			additionalInformationForLog = e.getMessage();
			String errorMessage = null;
			response.setSuccess(new Success(false));
			ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
			
			if(e instanceof AditCodedException) {
				LOG.debug("Adding exception messages to response object.");
				arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));
				
				errorMessage = this.getMessageService().getMessage(e.getMessage(), ((AditCodedException) e).getParameters(), Locale.ENGLISH);
				errorMessage = "ERROR: " + errorMessage;
				
			} else {
				arrayOfMessage.getMessage().add(new Message("en", "Service error"));
				errorMessage = "ERROR: " + e.getMessage();
			}
			
			super.logError(null, requestDate.getTime(), LogService.ErrorLogLevel_Error, errorMessage);
			
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

	/**
	 * Outputs the user list to a temporary file. 
	 * 
	 * @param userList users list
	 * @return absolute path to temporary file created
	 * @throws XmlMappingException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
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
	
	/**
	 * Validates request body and makes sure that all
	 * required fields exist and are not empty.
	 * <br><br>
	 * Throws {@link AditCodedException} if any errors in request data are found.
	 * 
	 * @param request				Request body as {@link GetJoinedRequest} object.
	 * @throws AditCodedException	Exception describing error found in requet body.
	 */
	private void checkRequest(GetJoinedRequest request) throws AditCodedException {
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
	
	/**
	 * Checks servlet configuration parameters and validates them.
	 * 
	 * @throws AditInternalException thrown if errors exist in configuration parameters
	 */
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
	
	/**
	 * Gets user service.
	 * @return
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * Sets user service
	 * @param userService
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}