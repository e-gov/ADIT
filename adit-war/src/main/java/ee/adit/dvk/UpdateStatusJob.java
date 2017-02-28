package ee.adit.dvk;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.service.DocumentService;
import ee.adit.util.Constants;

/**
 * Updates document statuses from DVK client.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class UpdateStatusJob extends QuartzJobBean {
	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_DVK_UPDATE_STATUS_FROM_DVK;
    private static Logger logger = LogManager.getLogger(UpdateStatusJob.class);

    private DocumentService documentService;
    private MaintenanceJobDAO maintenanceJobDAO;

    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
        try {
        	if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
        		try {
		            logger.info("Executing scheduled job: Updating document statuses from DVK");

		            // Update document statuses from DVK
		            int updatedDocumentsCount = this.getDocumentService().updateDocumentsFromDVK();

		            logger.debug("Document statuses updated from DVK (" + updatedDocumentsCount + ")");
        		} finally {
        			maintenanceJobDAO.setJobRunningStatus(jobId, false);
        		}
        	}
        } catch (Exception e) {
            logger.error("Error executing scheduled DVK statuses update: ", e);
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
