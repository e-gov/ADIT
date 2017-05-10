package ee.adit.util;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;

/**
 * Clears the running statuses on jobs.
 * Mean only run once after application restart to fix any state related issues.
 *
 * @author Hendrik Pärna
 * @since 15.07.14
 */
public class JobStatusesCleaner extends QuartzJobBean {
    private static Logger logger = LogManager.getLogger(JobStatusesCleaner.class);
    private MaintenanceJobDAO maintenanceJobDAO;

    @Override
    protected void executeInternal(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("JobStatusesCleaner started.");
        maintenanceJobDAO.initializeJobRunningStatuses();
        logger.info("JobStatusesCleaner ended.");
    }

    public void setMaintenanceJobDAO(final MaintenanceJobDAO maintenanceJobDAO) {
        this.maintenanceJobDAO = maintenanceJobDAO;
    }
}
