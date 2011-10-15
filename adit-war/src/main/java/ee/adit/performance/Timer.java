package ee.adit.performance;

import org.apache.log4j.Logger;

public class Timer {
	private static Logger logger = Logger.getLogger(Timer.class);
    private long startTime;

    public Timer() {
        reset();
    }

    public void reset() {
        startTime = System.currentTimeMillis();
    }

    public void logElapsedTime(String message) {
        String elapsed = String.valueOf(System.currentTimeMillis() - startTime) + " ms";
        logger.info(message + ": " + elapsed);
    }
}
