package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class SendJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(SendJob.class);

	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		
		LOG.info("Executing scheduled job: Send documents to DVK");
		
		// TODO: Fetch all the documents that have document_sharing records that have type "send_dvk" and dvk_status_id is null or "100" (puudub)
		
		// TODO: Construct a DVK XML container for every document that is found
		
		// TODO: Save the document in DVK Client database (including the DVK XML container and recipient data)
		
		// TODO: Save the document DVK_ID to ADIT database
		

	}

}
