package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class UpdateStatusJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(UpdateStatusJob.class);
	
	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		
		// TODO: update document status from DVK
		
		
	}

}
