package ee.adit.service;

import com.google.gson.Gson;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.exception.AditUserInactiveException;
import ee.adit.service.dhx.DhxProcessingErrorType;
import ee.adit.service.dhx.RuuterDhxProcessingErrorRequest;
import ee.adit.util.Configuration;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Provides services for accessing Ruuter component endpoints
 */
@Service
public class RuuterService {

    private static Logger logger = LogManager.getLogger(RuuterService.class);

    private static final String RUUTER_DHX_ERROR_ENDPOINT = "ADIT_DHXDOCUMENTERROR";

    private Configuration configuration;

    public void forwardDhxProcessingErrorToRouter(String temporaryFile, Exception ex) {
        DhxProcessingErrorType errorType = DhxProcessingErrorType.UNSPECIFIED;

        if (AditUserInactiveException.class.isInstance(ex)) {
            errorType = DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND;
        }

        AditUserInactiveException aditCodedException = (AditUserInactiveException) ex;
        String inactiveUserPersonalCode = aditCodedException.getInactiveUserPersonalCode();
        ContainerVer2_1 containerVer2_1 = ContainerVer2_1.parseFile(temporaryFile);

        RuuterDhxProcessingErrorRequest request = new RuuterDhxProcessingErrorRequest();
        request.setErrorCode(errorType);
        request.setContainerVer2_1(containerVer2_1);


        Gson gson = new Gson();
        CloseableHttpClient client = HttpClients.createDefault();
        String ruuterEndpointUrl = configuration.getRuuterServiceUrl() + "/" + RUUTER_DHX_ERROR_ENDPOINT;
        HttpPost httpPost = new HttpPost(ruuterEndpointUrl);

        try {
            String json = gson.toJson(request);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            logger.info("Forwarding DHX error to Ruuter.");
            CloseableHttpResponse execute = client.execute(httpPost);
            client.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
