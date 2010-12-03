package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.DocumentService;

/**
 * Receives documents from DVK client.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class ReceiveJob extends QuartzJobBean {

    private static Logger logger = Logger.getLogger(ReceiveJob.class);

    private DocumentService documentService;

    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {

        try {
            logger.info("Executing scheduled job: Receive documents from DVK");

            // Receive documents from DVK Client database
            int receivedDocumentsCount = this.getDocumentService().receiveDocumentsFromDVK();

            logger.debug("Documents received from DVK (" + receivedDocumentsCount + ")");

        } catch (Exception e) {
            logger.error("Error executing scheduled DVK receiving: ", e);
        }

    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

}
