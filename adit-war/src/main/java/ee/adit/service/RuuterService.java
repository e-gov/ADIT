package ee.adit.service;

import com.google.gson.Gson;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.exception.AditInternalException;
import ee.adit.exception.AditUserInactiveException;
import ee.adit.service.dhx.DhxProcessingErrorType;
import ee.adit.service.dhx.DhxRecipientUserType;
import ee.adit.service.dhx.RuuterDhxProcessingErrorRequest;
import ee.adit.util.Configuration;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides services for accessing Ruuter component endpoints
 */
@Service
public class RuuterService {

    private static Logger logger = LogManager.getLogger(RuuterService.class);

    private static final String RUUTER_DHX_ERROR_ENDPOINT = "ADIT_DHXDOCUMENTERROR";

    private Configuration configuration;

    public void forwardDhxProcessingErrorToRouter(String temporaryFile, Exception ex) {
        ContainerVer2_1 containerVer2_1 = ContainerVer2_1.parseFile(temporaryFile);

        List<Recipient> recipients = containerVer2_1.getRecipient();
        if (recipients.size() > 1) {
            /*
             * If container with multiple recipients fails it is probably due to an exception thrown, when validating
             * one of those recipients. When container processing fails in such way, none of the recipients receive
             * the document, error is sent for each of them.
             */
            logger.warn("Container with multiple recipients failed. Cause may not be specified for all.");
        }

        List<RuuterDhxProcessingErrorRequest> dhxProcessingErrorRequests =
                buildRuuterDhxProcessingErrorRequests(containerVer2_1, ex);
        logger.info("Forwarding DHX processing error ({}) to Ruuter as {} requests.", ex.getMessage(), dhxProcessingErrorRequests.size());
        sendDhxProcessingErrorRequests(dhxProcessingErrorRequests);
    }

    private List<RuuterDhxProcessingErrorRequest> buildRuuterDhxProcessingErrorRequests(ContainerVer2_1 containerVer2_1, Exception ex) {
        List<RuuterDhxProcessingErrorRequest> dhxProcessingErrorRequests = new ArrayList<>();

        for (Recipient recipient : containerVer2_1.getRecipient()) {
            RuuterDhxProcessingErrorRequest request = new RuuterDhxProcessingErrorRequest();

            // Set recipient specific fields
            parseRecipientSpecificErrorDetails(request, recipient);
            // Set error specific fields
            parseErrorType(ex, recipient, request);

            request.setContainerVer2_1(containerVer2_1);
            dhxProcessingErrorRequests.add(request);
        }
        return dhxProcessingErrorRequests;
    }

    private void parseErrorType(Exception ex, Recipient recipient, RuuterDhxProcessingErrorRequest request) {
        DhxProcessingErrorType errorType = DhxProcessingErrorType.UNSPECIFIED;

        if (AditUserInactiveException.class.isInstance(ex)) {
            String inactiveUserPersonalCode = ((AditUserInactiveException) ex).getInactiveUserPersonalCode();
            if (recipient.getPerson() != null && inactiveUserPersonalCode.equals(recipient.getPerson().getPersonalIdCode())) {
                errorType = DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND;
            }
        }
        request.setErrorCode(errorType);
    }

    private void parseRecipientSpecificErrorDetails(RuuterDhxProcessingErrorRequest request, Recipient recipient) {
        if (recipient.getPerson() != null) {
            request.setRecipientUserType(DhxRecipientUserType.PERSON);
            request.setRecipientCode(recipient.getPerson().getPersonalIdCode());
            request.setRecipientUserName(recipient.getPerson().getName());
        } else if (recipient.getOrganisation() != null) {
            request.setRecipientUserType(DhxRecipientUserType.ORGANISATION);
            request.setRecipientCode(recipient.getOrganisation().getOrganisationCode());
            request.setRecipientUserName(recipient.getOrganisation().getName());
        } else {
            logger.error("Could not identify userType from the container");
        }
    }

    private void sendDhxProcessingErrorRequests(List<RuuterDhxProcessingErrorRequest> dhxProcessingErrorRequests) {
        for (RuuterDhxProcessingErrorRequest request : dhxProcessingErrorRequests) {
            sendDhxProcessingErrorRequest(request);
        }
    }

    private void sendDhxProcessingErrorRequest(RuuterDhxProcessingErrorRequest request) {
        String ruuterEndpointUrl = configuration.getRuuterServiceUrl() + "/" + RUUTER_DHX_ERROR_ENDPOINT;
        String entityJson = new Gson().toJson(request);

        HttpPost httpPost = new HttpPost(ruuterEndpointUrl);

        StringEntity requestEntity = new StringEntity(entityJson, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        httpPost.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setHeader("Content-type", ContentType.APPLICATION_JSON.getMimeType());

        logger.info("Forwarding DHX error to Ruuter.");
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpPost)) {
            logger.info("DHX error forwarded to Ruuter. Response: {}", response);
        } catch (IOException e) {
            throw new AditInternalException("Failed to forward DHX processing error to Ruuter", e);
        }

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
