package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.DocumentService;

public class DeleteDocumentsFromDVKJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(DeleteDocumentsFromDVKJob.class);
	
	private DocumentService documentService;
	
	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		
		try {
			LOG.info("Executing scheduled job: Delete sent / received / failed documents from DVK");
			
			// Send documents to DVK Client database
			int deletedDocumentsCount = this.getDocumentService().deleteDocumentsFromDVK();

			LOG.debug("Documents deleted from DVK (" + deletedDocumentsCount + ")");
			
		} catch (Exception e) {
			LOG.error("Error executing scheduled DVK deleting: ", e);
		}

	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
	
}
