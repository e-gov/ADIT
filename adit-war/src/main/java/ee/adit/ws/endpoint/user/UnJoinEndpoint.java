package ee.adit.ws.endpoint.user;

import java.util.Calendar;
import java.util.Locale;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.dao.pojo.AditUser;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.Message;
import ee.adit.pojo.Success;
import ee.adit.pojo.UnJoinResponse;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.service.UserService;
import ee.adit.util.Util;
import ee.adit.util.xroad.CustomXRoadHeader;
import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

/**
 * Implementation of "unJoin" web method (web service request). Contains request
 * input validation, request-specific workflow and response composition.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
@XTeeService(name = "unJoin", version = "v1")
@Component
public class UnJoinEndpoint extends AbstractAditBaseEndpoint {

    private static Logger logger = LogManager.getLogger(UnJoinEndpoint.class);

    private UserService userService;

    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        logger.debug("unJoin invoked. Version: " + version);

        if (version == 1) {
            return v1(requestObject);
        } else {
            throw new AditInternalException("This method does not support version specified: " + version);
        }
    }

    /**
     * Executes "V1" version of "unJoin" request.
     *
     * @param requestObject
     *            Request body object
     * @return Response body object
     */
    protected Object v1(Object requestObject) {
        UnJoinResponse response = new UnJoinResponse();
        ArrayOfMessage messages = new ArrayOfMessage();
        Calendar requestDate = Calendar.getInstance();
        String additionalInformationForLog = null;

        try {
            logger.debug("UnJoinEndpoint.v1 invoked.");
            CustomXRoadHeader header = this.getHeader();
            String applicationName = header.getInfosysteem(this.getConfiguration().getXteeProducerName());

            // Log request
            Util.printHeader(header, this.getConfiguration());

            // Check header for required fields
            checkHeader(header);

            // Kontrollime, kas päringu käivitanud infosüsteem on ADITis
            // registreeritud
            boolean applicationRegistered = this.getUserService().isApplicationRegistered(applicationName);

            if (applicationRegistered) {

                // Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid
                // muuta (või üldse näha)
                int accessLevel = this.getUserService().getAccessLevel(applicationName);

                // Application has write permission
                if (accessLevel == 2) {

                    // Kontrollime, kas päringu käivitanud kasutaja eksisteerib
                    AditUser aditUser = Util.getAditUserFromXroadHeader(this.getHeader(), this.getUserService());

                    // Kontrollime, kas infosüsteem tohib antud kasutaja
                    // andmeid muuta
                    int applicationAccessLevelForUser = userService
                            .getAccessLevelForUser(applicationName, aditUser);

                    if (applicationAccessLevelForUser == 2) {
                        logger.info("Deactivating user.");

                        // Kontrollime, kas kasutaja on aktiivne
                        if (aditUser.getActive()) {
                            // Märgime kasutaja lahkunuks
                            this.getUserService().deactivateUser(aditUser);
                            logger.info("User (" + aditUser.getUserCode() + ") deactivated.");
                            response.setSuccess(new Success(true));
                            messages.setMessage(this.getMessageService().getMessages("request.unJoin.success",
                                    new Object[] {aditUser.getUserCode() }));

                            String additionalMessage = this.getMessageService().getMessage(
                                    "request.unJoin.success", new Object[] {aditUser.getUserCode() },
                                    Locale.ENGLISH);
                            additionalInformationForLog = LogService.REQUEST_LOG_SUCCESS + ": " + additionalMessage;

                        } else {
                            AditCodedException aditCodedException = new AditCodedException(
                                    "request.unJoin.alreadyUnJoined");
                            aditCodedException.setParameters(new Object[] {aditUser.getUserCode() });
                            throw aditCodedException;
                        }
                    } else {
                        AditCodedException aditCodedException = new AditCodedException(
                                "application.insufficientPrivileges.forUser.write");
                        aditCodedException.setParameters(new Object[] {applicationName, aditUser.getUserCode() });
                        throw aditCodedException;
                    }
                } else {
                    AditCodedException aditCodedException = new AditCodedException(
                            "application.insufficientPrivileges.write");
                    aditCodedException.setParameters(new Object[] {applicationName });
                    throw aditCodedException;
                }
            } else {
                AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
                aditCodedException.setParameters(new Object[] {applicationName });
                throw aditCodedException;
            }

            // Set response messages
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
        UnJoinResponse response = new UnJoinResponse();
        response.setSuccess(new Success(false));
        ArrayOfMessage arrayOfMessage = new ArrayOfMessage();
        arrayOfMessage.getMessage().add(new Message("en", ex.getMessage()));
        response.setMessages(arrayOfMessage);
        return response;
    }

    /**
     * Gets user service.
     *
     * @return user service
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
