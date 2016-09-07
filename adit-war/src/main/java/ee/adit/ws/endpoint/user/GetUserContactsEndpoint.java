package ee.adit.ws.endpoint.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import ee.adit.pojo.GetUserContactsRequest;
import ee.adit.pojo.GetUserContactsResponse;
import ee.adit.pojo.GetUserContactsResponseAttachment;
import ee.adit.pojo.GetUserContactsResponseAttachmentUserContact;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.pojo.UserContactList;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.Util;
import ee.adit.util.xroad.CustomXRoadHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "getUserContacts" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * @author Dmitri Timofejev, Finestmedia, dmitri.timoefejev@fonestmedia.ee
 */
@XTeeService(name = "getUserContacts", version = "v1")
@Component
public class GetUserContactsEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(GetUserContactsEndpoint.class);

    private UserService userService;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("getUserContacts invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }

    }

    /**
     * Executes "V1" version of "getUserContacts" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        GetUserContactsResponse response = new GetUserContactsResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;

        try {
            // Check configuration
            checkConfiguration();

            GetUserContactsRequest request = (GetUserContactsRequest) requestObject;
            CustomXRoadHeader header = this.getHeader();
            String applicationName = header.getInfosysteem(this.getConfiguration().getXteeProducerName());

            // Check header for required fields
            checkHeader(header);

            // Check request
            checkRequest(request);

            // Kontrollime, kas päringu käivitanud infosüsteem on ADITis
            // registreeritud
            boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);

            if (applicationRegistered) {

                // Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid
                // näha
                int accessLevel = this.getUserService().getAccessLevel(applicationName);

                if (accessLevel >= 1) {
                	
                    String userCode = ((this.getHeader().getAllasutus() != null) && (this.getHeader()
                            .getAllasutus().length() > 0)) ? this.getHeader().getAllasutus() : this.getHeader()
                            .getIsikukood();
                    AditUser aditUser = userService.getUserByID(userCode);

                	
                    // Teeme andmebaasist väljavõtte 
                	
                	//List<AditUser> userList = new ArrayList();
                	
                	//TODO:Query for user contacts
                    List<AditUser> userList = this.getUserService().getUserContacts(aditUser);

                    if (userList != null /*&& userContactList.size() > 0*/) {
                        logger.debug("Number of user contacts found: " + userList.size());

                        // 1. Convert java list to XML string and output to
                        // file
                        String xmlFile = outputToFile(userList);

                        // 2. GZip the temporary file
                        // Base64 encoding will be done at SOAP envelope
                        // level
                        String gzipFileName = Util.gzipFile(xmlFile, this.getConfiguration().getTempDir());

                        // 3. Add as an attachment
                        String contentID = addAttachment(gzipFileName);
                        UserContactList getUserContactsResponseUserContactList = new UserContactList();
                        getUserContactsResponseUserContactList.setHref("cid:" + contentID);
                        response.setUserContactList(getUserContactsResponseUserContactList);

                        response.setSuccess(new Success(true));
                        messages.setMessage(this.getMessageService().getMessages("request.getUserContacts.success",
                                new Object[] {new Integer(userList.size()).toString() }));

                        // Additional information for request log
                        String additionalMessage = this.getMessageService().getMessage("request.getUserContacts.success",
                                new Object[] {new Integer(userList.size()).toString() }, Locale.ENGLISH);
                        additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

                    } else {
                        logger.warn("No user contacts were found.");
                        throw new AditCodedException("request.getUserContacts.noUsersFound");
                    }

                } else {
                    AditCodedException aditCodedException = new AditCodedException(
                            "application.insufficientPrivileges.read");
                    aditCodedException.setParameters(new Object[] {applicationName });
                    throw aditCodedException;
                }
            } else {
                AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
                aditCodedException.setParameters(new Object[] {applicationName });
                throw aditCodedException;
            }

            response.setMessages(messages);

        } catch (Exception e) {
            logger.error("Exception: ", e);
            String errorMessage = null;
            response.setSuccess(new Success(false));
            ArrayOfMessage arrayOfMessage = new ArrayOfMessage();

            if (e instanceof AditCodedException) {
                logger.debug("Adding exception messages to response object.");
                arrayOfMessage.setMessage(this.getMessageService().getMessages((AditCodedException) e));

                errorMessage = this.getMessageService().getMessage(e.getMessage(),
                        ((AditCodedException) e).getParameters(), Locale.ENGLISH);
                errorMessage = "ERROR: " + errorMessage;

            } else {
            	arrayOfMessage.setMessage(this.getMessageService().getMessages(MessageService.GENERIC_ERROR_CODE, new Object[]{}));
                errorMessage = "ERROR: " + e.getMessage();
            }

            additionalInformationForLog = errorMessage;
            super.logError(null, requestDate.getTime(), LogService.ERROR_LOG_LEVEL_ERROR, errorMessage);

            logger.debug("Adding exception messages to response object.");
            response.setMessages(arrayOfMessage);
        }

        super.logCurrentRequest(null, requestDate.getTime(), additionalInformationForLog);
        return response;
    }

    @Override
    protected Object getResultForGenericException(Exception ex) {
        super.logError(null, Calendar.getInstance().getTime(), LogService.ERROR_LOG_LEVEL_FATAL, "ERROR: "
                + ex.getMessage());
        logger.debug("Constructing result for generic exception...");
        GetJoinedResponse response = new GetJoinedResponse();
        response.setSuccess(new Success(false));
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        logger.debug("Returning generic exception response");
        return response;
    }

    /**
     * Outputs the user contact list to a temporary file.
     *
     * @param userContactList
     *            users list
     * @return absolute path to temporary file created
     * @throws XmlMappingException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    private String outputToFile(List<AditUser> userContactList) throws XmlMappingException, IOException,
            ParserConfigurationException, TransformerException {
        List<GetUserContactsResponseAttachmentUserContact> getUsercontactsResponseAttachmentUserContactsList = new ArrayList<GetUserContactsResponseAttachmentUserContact>();

        for (int i = 0; i < userContactList.size(); i++) {
            AditUser aditUser = userContactList.get(i);
            GetUserContactsResponseAttachmentUserContact getUserContactsResponseAttachmentUserContact = new GetUserContactsResponseAttachmentUserContact();
            getUserContactsResponseAttachmentUserContact.setCode(aditUser.getUserCode());
            getUserContactsResponseAttachmentUserContact.setName(aditUser.getFullName());
            getUserContactsResponseAttachmentUserContact.setType(aditUser.getUsertype().getShortName());
            getUsercontactsResponseAttachmentUserContactsList.add(getUserContactsResponseAttachmentUserContact);
        }
        GetUserContactsResponseAttachment getUserContactsResponseAttachment = new GetUserContactsResponseAttachment();
        getUserContactsResponseAttachment.setUsers(getUsercontactsResponseAttachmentUserContactsList);
        getUserContactsResponseAttachment.setTotal(getUsercontactsResponseAttachmentUserContactsList.size());

        return marshal(getUserContactsResponseAttachment);
    }

    /**
     * Validates request body and makes sure that all required fields exist and
     * are not empty. <br>
     * <br>
     * Throws {@link AditCodedException} if any errors in request data are
     * found.
     *
     * @param request
     *            Request body as {@link GetJoinedRequest} object.
     * @throws AditCodedException
     *             Exception describing error found in requet body.
     */
    private void checkRequest(GetUserContactsRequest request) throws AditCodedException {
        if (request == null) {
            throw new AditCodedException("request.body.empty");
        }
    }

    /**
     * Checks servlet configuration parameters and validates them.
     *
     * @throws AditInternalException
     *             thrown if errors exist in configuration parameters
     */
    private void checkConfiguration() throws AditInternalException {
        if (this.getConfiguration() == null) {
            throw new AditInternalException("Configuration not initialized - check servlet configuration.");
        } else {
            if (this.getConfiguration().getGetJoinedMaxResults() == null) {
                throw new AditInternalException(
                        "Configuration not properly initialized (parameter 'getJoinedMaxResults' is undefined) - check servlet configuration.");
            }
            if (this.getConfiguration().getDeleteTemporaryFiles() == null) {
                throw new AditInternalException(
                        "Configuration not properly initialized (parameter 'deleteTemporaryFiles' is undefined) - check servlet configuration.");
            }
            if (this.getConfiguration().getTempDir() == null) {

                throw new AditInternalException(
                        "Configuration not properly initialized (parameter 'tempDir' is undefined) - check servlet configuration.");
            } else {
                try {
                    boolean tempDirExists = (new File(this.getConfiguration().getTempDir())).exists();
                    if (!tempDirExists) {
                        throw new FileNotFoundException("Directory does not exist: "
                                + this.getConfiguration().getTempDir());
                    }
                } catch (Exception e) {
                    throw new AditInternalException(
                            "Configuration not properly initialized (parameter 'tempDir' not properly configured) - check servlet configuration.",
                            e);
                }
            }
        }
    }

    /**
     * Gets user service.
     *
     * @return
     *     User service
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * Sets user service.
     *
     * @param userService
     *     User service
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
