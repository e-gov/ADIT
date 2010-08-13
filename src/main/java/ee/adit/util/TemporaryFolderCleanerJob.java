package ee.adit.util;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class TemporaryFolderCleanerJob extends QuartzJobBean {
	private static Logger LOG = Logger.getLogger(TemporaryFolderCleanerJob.class);
	private Configuration configuration;
	
	protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
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
		                        }
		                    } catch (Exception ex) {
		                        LOG.warn("Error deleting temporary file: " + files[i].getName(), ex);
		                    }
		                }
		            }
		        }
			}
		} catch (Exception e) {
			LOG.error("Error deleting temporary files: ", e);
		}
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

}