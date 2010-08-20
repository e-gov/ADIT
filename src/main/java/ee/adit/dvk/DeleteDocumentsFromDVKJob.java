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
			
			// Delete sent documents from DVK Client database
			int deletedSentDocumentsCount = this.getDocumentService().deleteSentDocumentsFromDVK();

			// Delete failed / received documents from DVK Client database
			int deletedReceivedDocumentsCount = this.getDocumentService().deleteReceivedDocumentsFromDVK();
			
			LOG.debug("Documents (sent) deleted from DVK (" + deletedSentDocumentsCount + ")");
			LOG.debug("Documents (received / aborted) deleted from DVK (" + deletedReceivedDocumentsCount + ")");
			
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
