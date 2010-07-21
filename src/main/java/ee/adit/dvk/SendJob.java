package ee.adit.dvk;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class SendJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(SendJob.class);

	private int timeout;

	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {

		// TODO: actual work implementation
		LOG.info("Executing scheduled job: Send documents to DVK");

	}

	/**
	 * Setter called after the ExampleJob is instantiated with the value from
	 * the JobDetailBean (5)
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getTimeout() {
		return timeout;
	}

}
