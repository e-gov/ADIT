package ee.adit.util;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Quartz job for cleaning up applications temporary files folder.<br>
 * Enables periodic deletion of old and unnecessary files. This job is required
 * because some temporary files cannot be deleted in real time (for example
 * attachment files of outgoing SOAP messages). <br>
 * <br>
 * Quartz library: http://www.quartz-scheduler.org
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class TemporaryFolderCleanerJob extends QuartzJobBean {
	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_TEMPORARY_FOLDER_CLEAN;

	/**
     * Log4j logger.
     */
    private static Logger logger = LogManager.getLogger(TemporaryFolderCleanerJob.class);

    /**
     * Application configuration as {@link Configuration} object. Wraps
     * configuration values defined in servlet context file.
     */
    private Configuration configuration;

    /**
     * Deletes old files from applications temporary folder.
     * @param ctx job execution context
     */
    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        logger.debug("Starting to delete temporary files...");
        int deletedCount = 0;

        try {
            // If temporary file deleting is allowed then delete all temporary
            // files that are older than 1 hour.
            if ((configuration != null) && configuration.getDeleteTemporaryFilesAsBoolean()) {
                File tempPath = new File(configuration.getTempDir());
                if ((tempPath != null) && tempPath.exists() && tempPath.isDirectory()) {
                    FilenameFilter filter = new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".adit");
                        }
                    };

                    File[] files = tempPath.listFiles(filter);
                    if ((files != null) && (files.length > 0)) {
                        for (int i = 0; i < files.length; ++i) {
                            try {
                                if ((System.currentTimeMillis() - files[i].lastModified()) > (1000 * 60 * 60)) {
                                    files[i].delete();
                                    deletedCount++;
                                }
                            } catch (Exception ex) {
                                logger.warn("Error deleting temporary file: " + files[i].getName(), ex);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error deleting temporary files: ", e);
        }

        if (deletedCount > 0) {
            logger.info("Number of temporary files deleted: " + deletedCount);
        } else {
            logger.debug("No temporary files were deleted.");
        }
    }

    /**
     * Gets current configuration.
     *
     * @return Current configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets configuration.
     *
     * @param configuration
     *            {@link Configuration} object to be used in current objects
     *            methods
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
