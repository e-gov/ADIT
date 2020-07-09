package ee.adit.service;

import com.google.gson.Gson;
import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.DocumentType;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.exception.AditInternalException;
import ee.adit.service.dhx.RuuterDhxProcessingErrorRequest;
import ee.adit.service.dhx.RuuterDhxProcessingErrorRequestsBuilder;
import ee.adit.util.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Provides services for accessing Ruuter component endpoints
 */
@Service
public class RuuterService implements InitializingBean {

    private static Logger logger = LogManager.getLogger(RuuterService.class);
    private static final String RUUTER_DHX_ERROR_ENDPOINT = "ADIT_DHXDOCUMENTERROR";

    private Configuration configuration;

    private DocumentTypeDAO documentTypeDAO;

    public void forwardDhxProcessingErrorToRouter(String temporaryFile, Exception ex) {
        // This method is already called within exception handling block.
        // try-catch ensures all exceptions generated here get logged as well.
        try {
            ContainerVer2_1 containerVer2_1 = ContainerVer2_1.parseFile(temporaryFile);

            List<Recipient> recipients = containerVer2_1.getRecipient();
            if (recipients.size() > 1) {
                /*
                 * If container with multiple recipients fails it is probably due to an exception thrown, when validating
                 * one of those recipients. When container processing fails in such way, none of the recipients receive
                 * the document, error is sent for each of them.
                 */
                logger.warn("Container with multiple recipients failed. " +
                        "Cause may not be specified for all outgoing requests.");
            }

            List<DocumentType> aditDocumentTypes = documentTypeDAO.listDocumentTypes();
            List<RuuterDhxProcessingErrorRequest> dhxProcessingErrorRequests =
                    new RuuterDhxProcessingErrorRequestsBuilder(containerVer2_1, ex, aditDocumentTypes).build();
            logger.info("Forwarding DHX processing error ({}) to Ruuter as {} requests.",
                    ex.getMessage(), dhxProcessingErrorRequests.size());

            sendDhxProcessingErrorRequests(dhxProcessingErrorRequests);
        } catch (Exception e) {
            logger.error("Forwarding DHX processing error to Ruuter failed.", e);
            e.printStackTrace();
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

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setDocumentTypeDAO(DocumentTypeDAO documentTypeDAO) {
        this.documentTypeDAO = documentTypeDAO;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(configuration.getRuuterServiceUrl())) {
            throw new IllegalStateException(RuuterService.class + " bean must have ruuterServiceUrl set in configuration");
        }
    }
}
