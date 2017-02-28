package ee.adit.dvk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.adit.dao.MaintenanceJobDAO;
import ee.adit.service.DocumentService;
import ee.adit.util.Constants;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.ws.service.AddressService;

public class AddresseeRenewJob extends QuartzJobBean {

	/**
	 * Unique ID of current job. This is used for maintenance job
	 * synchronization between cluster nodes. Must have a corresponding record
	 * in database table "MAINTENANCE_JOB".
	 */
	private static long jobId = Constants.JOB_ID_DHX_ADDRESSEES_RENEW;
	private static Logger logger = LogManager.getLogger(AddresseeRenewJob.class);

	private AddressService addressService;
	private MaintenanceJobDAO maintenanceJobDAO;

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		try {
			if (maintenanceJobDAO.setJobRunningStatus(jobId, true)) {
				try {
					logger.debug("updating address DHX list automatically");
					addressService.renewAddressList();
				} catch (DhxException ex) {
					logger.error("Error occured while renewing addresslist. " + ex.getMessage(), ex);
				} finally {
					maintenanceJobDAO.setJobRunningStatus(jobId, false);
				}
			}
		} catch (Exception e) {
			logger.error("Error executing scheduled DHX addressee renew: ", e);
		}
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
