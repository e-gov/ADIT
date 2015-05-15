package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.service.DocumentService;
import ee.adit.util.Constants;

/**
 * Scheduled job that deletes unnecessary (sent / aborted / received) documents
 * from DVK client database.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class DeleteDocumentsFromDVKJob extends QuartzJobBean {
	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_DVK_DELETE;
    private static Logger logger = Logger.getLogger(DeleteDocumentsFromDVKJob.class);

    private DocumentService documentService;
    private MaintenanceJobDAO maintenanceJobDAO;

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        try {
        	if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
        		try {
		            logger.info("Executing scheduled job: Delete sent / received / failed documents from DVK");

		            // Delete sent documents from DVK Client database
		            int deletedSentDocumentsCount = this.getDocumentService().deleteSentDocumentsFromDVK();

		            // Delete failed / received documents from DVK Client database
		            int deletedReceivedDocumentsCount = this.getDocumentService().deleteReceivedDocumentsFromDVK();

		            logger.debug("Documents (sent) deleted from DVK (" + deletedSentDocumentsCount + ")");
		            logger.debug("Documents (received / aborted) deleted from DVK (" + deletedReceivedDocumentsCount + ")");
        		} finally {
        			maintenanceJobDAO.setJobRunningStatus(jobId, false);
        		}
        	}
        } catch (Exception e) {
            logger.error("Error executing scheduled DVK deleting: ", e);
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
