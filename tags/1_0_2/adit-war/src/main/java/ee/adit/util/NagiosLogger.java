package ee.adit.util;

import org.apache.log4j.Logger;

public class NagiosLogger {
	
	private static Logger LOG = Logger.getLogger(NagiosLogger.class);
	
	public void log(String message) {
		LOG.info(message);
	}
	
	public void log(String message, Exception e) {
		LOG.error(message + e.getMessage());
	}
	
}
