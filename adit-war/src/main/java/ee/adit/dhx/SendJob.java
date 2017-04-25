package ee.adit.dhx;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.service.DocumentService;
import ee.adit.util.Constants;

/**
 * Sends documents to DHX client.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class SendJob extends QuartzJobBean {
	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_DHX_SEND;
    private static Logger logger = LogManager.getLogger(SendJob.class);

    private DocumentService documentService;
    private MaintenanceJobDAO maintenanceJobDAO;

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        try {
        	if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
        		try {
		        	logger.info("Executing scheduled job: Send documents to DHX");

		            // Send documents to DHX Client database
		            int sentDocumentsCount = this.getDocumentService().sendDocumentsToDHX();

		            logger.debug("Documents sent to DHX (" + sentDocumentsCount + ")");
        		} finally {
        			maintenanceJobDAO.setJobRunningStatus(jobId, false);
        		}
        	}
        } catch (Exception e) {
            logger.error("Error executing scheduled DHX sending: ", e);
        }
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

	public MaintenanceJobDAO getMaintenanceJobDAO() {
		return maintenanceJobDAO;
	}

	public void setMaintenanceJobDAO(MaintenanceJobDAO maintenanceJobDAO) {
		this.maintenanceJobDAO = maintenanceJobDAO;
	}
}
