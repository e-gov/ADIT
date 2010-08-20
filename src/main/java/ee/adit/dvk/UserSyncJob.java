package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.DocumentService;

public class UserSyncJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(UserSyncJob.class);
	
	private DocumentService documentService;
	
	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		
		try {
			LOG.info("Executing scheduled job: DVK user synchronization");
			
			// Send documents to DVK Client database
			int sentDocumentsCount = this.getDocumentService().sendDocumentsToDVK();

			LOG.debug("Users synchronized (" + sentDocumentsCount + ")");
			
		} catch (Exception e) {
			LOG.error("Error executing scheduled DVK user synchronization: ", e);
		}

	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
	
}
