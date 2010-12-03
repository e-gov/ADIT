package ee.adit.util;

import org.apache.log4j.Logger;

public class NagiosLogger {

    private static Logger logger = Logger.getLogger(NagiosLogger.class);

    public void log(String message) {
        logger.info(message);
    }

    public void log(String message, Exception e) {
        logger.error(message + e.getMessage());
    }

}
