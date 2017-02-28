package ee.adit.dvk;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.service.DocumentService;
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
    private static Logger logger = LogManager.getLogger(ReceiveJob.class);

    private Configuration configuration;
    private DocumentService documentService;
    private String digidocConfigurationFile;
    private MaintenanceJobDAO maintenanceJobDAO;

    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
        try {
        	if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
        		try {
		            logger.info("Executing scheduled job: Receive documents from DVK");

		            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(getDigidocConfigurationFile());
		            String jdigidocCfgTmpFile = Util.createTemporaryFile(input, getConfiguration().getTempDir());

		            // Receive documents from DVK Client database
		            int receivedDocumentsCount = this.getDocumentService().receiveDocumentsFromDVK(jdigidocCfgTmpFile);

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
}
