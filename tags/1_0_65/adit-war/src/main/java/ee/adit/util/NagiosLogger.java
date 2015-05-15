package ee.adit.util;

import org.apache.log4j.Logger;

/**
 * Monitor result holder class.
 *  
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class NagiosLogger {

    /**
     * Log4J logger.
     */
    private static Logger logger = Logger.getLogger(NagiosLogger.class);

    /**
     * Log message.
     * @param message log message
     */
    public void log(String message) {
        logger.info(message);
    }

    /**
     * Log message with cause.
     * 
     * @param message message
     * @param e cause
     */
    public void log(String message, Exception e) {
        logger.error(message + e.getMessage());
    }

}
