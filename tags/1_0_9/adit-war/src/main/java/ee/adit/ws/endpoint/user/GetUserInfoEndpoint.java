package ee.adit.ws.endpoint.user;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.GetUserInfoRequest;
import ee.adit.pojo.GetUserInfoRequestAttachmentUserList;
import ee.adit.pojo.GetUserInfoResponse;
import ee.adit.pojo.GetUserInfoResponseAttachment;
import ee.adit.pojo.GetUserInfoResponseAttachmentUser;
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
 * Implementation of "getUserInfo" web method (web service request). Contains
 * request input validation, request-specific workflow and response composition.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "getUserInfo", version = "v1")
@Component
public class GetUserInfoEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = Logger.getLogger(GetUserInfoEndpoint.class);

    private UserService userService;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("getUserInfo invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "getUserInfo" request.
     * 
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        GetUserInfoResponse response = new GetUserInfoResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;

        try {

            GetUserInfoRequest request = (GetUserInfoRequest) requestObject;
            CustomXTeeHeader header = this.getHeader();
            String applicationName = header.getInfosysteem();

            // Check header for required fields
            checkHeader(header);

            // Kontrollime, kas päringu käivitanud infosüsteem on ADITis
            // registreeritud
            boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);

            if (applicationRegistered) {

                // Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid
                // näha
                int accessLevel = this.getUserService().getAccessLevel(applicationName);

                if (accessLevel >= 1) {

                    String attachmentID = null;
                    // Check if the attachment ID is specified
                    if (request.getUserList() != null && request.getUserList().getHref() != null
                            && !request.getUserList().getHref().trim().equals("")) {
                        attachmentID = Util.extractContentID(request.getUserList().getHref());
                    } else {
                        throw new AditCodedException("request.saveDocument.attachment.id.notSpecified");
                    }

                    // All primary checks passed.
                    logger.debug("Processing attachment with id: '" + attachmentID + "'");
                    // Extract the SOAP message to a temporary file
                    String base64EncodedFile = extractAttachmentXML(this.getRequestMessage(), attachmentID);

                    // Base64 decode and unzip the temporary file
                    String xmlFile = Util.base64DecodeAndUnzip(base64EncodedFile, this.getConfiguration().getTempDir(),
                            this.getConfiguration().getDeleteTemporaryFilesAsBoolean());
                    logger.debug("Attachment unzipped to temporary file: " + xmlFile);

                    // Unmarshal the XML from the temporary file
                    Object unmarshalledObject = null;
                    try {
                        unmarshalledObject = unMarshal(xmlFile);
                    } catch (Exception e) {
                        logger.error("Error while unmarshalling SOAP attachment: ", e);
                        throw new AditCodedException("request.attachments.invalidFormat");
                    }

                    // Check if the marshalling result is what we
                    // expected
                    if (unmarshalledObject != null) {
                        logger.debug("XML unmarshalled to type: " + unmarshalledObject.getClass());
                        if (unmarshalledObject instanceof GetUserInfoRequestAttachmentUserList) {

                            GetUserInfoRequestAttachmentUserList userList = (GetUserInfoRequestAttachmentUserList) unmarshalledObject;

                            List<GetUserInfoResponseAttachmentUser> userInfoList = this.getUserService().getUserInfo(
                                    userList);
                            GetUserInfoResponseAttachment responseAttachment = new GetUserInfoResponseAttachment();
                            responseAttachment.setUserList(userInfoList);

                            String responseAttachmentXMLFile = this.marshal(responseAttachment);

                            // Compress response attachment
                            // Base64 encoding will be done at SOAP
                            // envelope level
                            String attachmentFile = Util.gzipFile(responseAttachmentXMLFile, this.getConfiguration()
                                    .getTempDir());

                            // Add as an attachment
                            String contentID = addAttachment(attachmentFile);
                            UserList getUserInfoResponseUserList = new UserList();
                            getUserInfoResponseUserList.setHref("cid:" + contentID);
                            response.setUserList(getUserInfoResponseUserList);
                            response.setSuccess(new Success(true));
                            messages.setMessage(this.getMessageService().getMessages("request.getUserInfo.success",
                                    new Object[] {}));

                            String additionalMessage = this.getMessageService().getMessage(
                                    "request.getUserInfo.success", new Object[] {}, Locale.ENGLISH);
                            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

                        } else {
                            throw new AditInternalException("Unmarshalling returned wrong type. Expected "
                                    + GetUserInfoRequestAttachmentUserList.class + ", got "
                                    + unmarshalledObject.getClass());
                        }

                    } else {
                        throw new AditInternalException("Unmarshalling failed for XML in file: " + xmlFile);
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

            response.setSuccess(new Success(true));
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
                arrayOfMessage.getMessage().add(new Message("en", "Service error"));
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
        GetUserInfoResponse response = new GetUserInfoResponse();
        response.setSuccess(new Success(false));
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        return response;
    }

}
