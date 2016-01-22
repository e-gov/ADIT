package ee.adit.dvk;

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.schedule.ScheduleClient;
import ee.adit.service.DocumentService;
import ee.adit.service.NotificationService;
import ee.adit.util.Configuration;
import ee.adit.util.Constants;
import ee.adit.util.Util;

/**
 * Receives documents from DVK client.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class ReceiveJob extends QuartzJobBean {
	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_DVK_RECEIVE;
    private static Logger logger = Logger.getLogger(ReceiveJob.class);

    private Configuration configuration;
    private DocumentService documentService;
    private MaintenanceJobDAO maintenanceJobDAO;
    
    private String digidocConfigurationFile;
    
    private NotificationService notificationService;

    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
        try {
        	if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
        		try {
		            logger.info("Executing scheduled job: Receive documents from DVK");

		            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(getDigidocConfigurationFile());
		            String jdigidocCfgTmpFile = Util.createTemporaryFile(input, getConfiguration().getTempDir());

		            // Receive documents from DVK Client database
		            List<DispatchReport> dispatchReports = this.getDocumentService().receiveDocumentsFromDVK(jdigidocCfgTmpFile);

		            int receivedDocumentsCount = 0;
		            for (DispatchReport dispatchReport : dispatchReports) {
		            	if (dispatchReport.isSuccess()) {
		            		receivedDocumentsCount++;
		            		
		            		notificationService.sendNotification(dispatchReport.getDocument(), dispatchReport.getRecipient(), ScheduleClient.NOTIFICATION_TYPE_SEND);
		            	}
		            }
		            
		            logger.info("Documents received from DVK (" + receivedDocumentsCount + ")");
        		} finally {
        			maintenanceJobDAO.setJobRunningStatus(jobId, false);
        		}
        	}
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

	public MaintenanceJobDAO getMaintenanceJobDAO() {
		return maintenanceJobDAO;
	}

	public void setMaintenanceJobDAO(MaintenanceJobDAO maintenanceJobDAO) {
		this.maintenanceJobDAO = maintenanceJobDAO;
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}
	
}
