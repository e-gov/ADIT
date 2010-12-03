package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.DocumentService;

/**
 * Sends documents to DVK client.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class SendJob extends QuartzJobBean {

    private static Logger logger = Logger.getLogger(SendJob.class);

    private DocumentService documentService;

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {

        try {
            logger.info("Executing scheduled job: Send documents to DVK");

            // Send documents to DVK Client database
            int sentDocumentsCount = this.getDocumentService().sendDocumentsToDVK();

            logger.debug("Documents sent to DVK (" + sentDocumentsCount + ")");

        } catch (Exception e) {
            logger.error("Error executing scheduled DVK sending: ", e);
        }

    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

}
