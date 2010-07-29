package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.service.DocumentService;

public class ReceiveJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(ReceiveJob.class);
	
	private DocumentService documentService;
	
	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		
		try {
			LOG.info("Executing scheduled job: Receive documents from DVK");
			
			// TODO: receive documents from DVK Client database		
			int receivedDocumentsCount = this.getDocumentService().receiveDocumentsFromDVK();

			LOG.debug("Documents received from DVK (" + receivedDocumentsCount + ")");
			
		} catch (Exception e) {
			LOG.error("Error executing scheduled DVK receiving: ", e);
		}
		
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

}
