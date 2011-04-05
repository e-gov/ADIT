package ee.adit.dvk;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.DocumentService;
import ee.adit.util.Configuration;
import ee.adit.util.Util;

/**
 * Receives documents from DVK client.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class ReceiveJob extends QuartzJobBean {

    private static Logger logger = Logger.getLogger(ReceiveJob.class);

    private Configuration configuration;
    private DocumentService documentService;
    private String digidocConfigurationFile;

    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {

        try {
            logger.info("Executing scheduled job: Receive documents from DVK");

            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(getDigidocConfigurationFile());
            String jdigidocCfgTmpFile = Util.createTemporaryFile(input, getConfiguration().getTempDir());
            
            // Receive documents from DVK Client database
            int receivedDocumentsCount = this.getDocumentService().receiveDocumentsFromDVK(jdigidocCfgTmpFile);

            logger.debug("Documents received from DVK (" + receivedDocumentsCount + ")");

        } catch (Exception e) {
            logger.error("Error executing scheduled DVK receiving: ", e);
        }

    }

    public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

	public String getDigidocConfigurationFile() {
		return digidocConfigurationFile;
	}

	public void setDigidocConfigurationFile(String digidocConfigurationFile) {
		this.digidocConfigurationFile = digidocConfigurationFile;
	}

}
