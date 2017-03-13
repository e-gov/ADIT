package ee.adit.dhx;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.service.UserService;
import ee.adit.util.Constants;
import ee.adit.util.DHXUserSyncResult;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.ws.service.AddressService;

/**
 * Refreshes the DHX adress list and synchronizes users from DHX to ADIT.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class UserSyncJob extends QuartzJobBean {
	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_DHX_USER_SYNC;
    private static Logger logger = LogManager.getLogger(UserSyncJob.class);

    private UserService userService;
    private MaintenanceJobDAO maintenanceJobDAO;
    
    private AddressService addressService;

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        try {
        	if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
        		try {
    				logger.debug("updating address DHX list automatically");
    				addressService.renewAddressList();
		            logger.info("Executing scheduled job: DHX user synchronization");

		            DHXUserSyncResult result = this.getUserService().synchroinzeDVKUsers();

		            logger.info("Users added: " + result.getAdded());
		            logger.info("Users modified: " + result.getModified());
		            logger.info("Users deactivated: " + result.getDeactivated());
        		} finally {
        			maintenanceJobDAO.setJobRunningStatus(jobId, false);
        		}
        	}
        } catch (Exception e) {
            logger.error("Error executing scheduled DHX user synchronization: ", e);
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

	public MaintenanceJobDAO getMaintenanceJobDAO() {
		return maintenanceJobDAO;
	}

	public void setMaintenanceJobDAO(MaintenanceJobDAO maintenanceJobDAO) {
		this.maintenanceJobDAO = maintenanceJobDAO;
	}

	/**
	 * @return the addressService
	 */
	public AddressService getAddressService() {
		return addressService;
	}

	/**
	 * @param addressService the addressService to set
	 */
	public void setAddressService(AddressService addressService) {
		this.addressService = addressService;
	}
}
