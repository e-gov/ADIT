package ee.adit.dvk;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.pojo.Document;

public class SendJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(SendJob.class);

	private DocumentDAO documentDAO;
	
	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		
		LOG.info("Executing scheduled job: Send documents to DVK");
		
		// TODO: Fetch all the documents that have document_sharing records that have type "send_dvk" and dvk_status_id is null or "100" (puudub)
		List<Document> documents = this.getDocuments();
		
		
		// TODO: Construct a DVK XML container for every document that is found
		
		// TODO: Save the document in DVK Client database (including the DVK XML container and recipient data)
		
		// TODO: Save the document DVK_ID to ADIT database
		

	}

	private List<Document> getDocuments() {
		return this.getDocumentDAO().getDocumentsForDVK();
	}

	public DocumentDAO getDocumentDAO() {
		return documentDAO;
	}

	public void setDocumentDAO(DocumentDAO documentDAO) {
		this.documentDAO = documentDAO;
	}
	
}
