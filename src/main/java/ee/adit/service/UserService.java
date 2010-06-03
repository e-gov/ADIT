package ee.adit.service;

import org.apache.log4j.Logger;

import ee.adit.dao.RemoteApplicationDAO;
import ee.adit.dao.pojo.RemoteApplication;

public class UserService {

	private static Logger LOG = Logger.getLogger(UserService.class);
	
	private RemoteApplicationDAO remoteApplicationDAO;
	
	public boolean isApplicationRegistered(String remoteApplicationShortName) {
		boolean result = false;
		LOG.debug("Checking if application '" + remoteApplicationShortName + "' is registered.");
		
		if(this.getRemoteApplicationDAO() == null) {
			LOG.error("remoteApplicationDAO not initialized");
		} else {
			RemoteApplication remoteApplication = this.getRemoteApplicationDAO().getByShortName(remoteApplicationShortName);
			
			if(remoteApplication != null) {
				result = true;
			}
		}
		
		LOG.debug("Application '" + remoteApplicationShortName + "' is registered?: " + result);
		return result;
	}

	/**
	 * Determines the access level for this application:
	 * 0 - no access
	 * 1 - read access
	 * 2 - write acces (full access) 
	 * @return
	 */
	public int getAccessLevel(String remoteApplicationShortName) {
		int result = 0;
		
		RemoteApplication remoteApplication = this.getRemoteApplicationDAO().getByShortName(remoteApplicationShortName);
		if(remoteApplication != null) {
			if(remoteApplication.getCanWrite()) {
				result = 2;
			} else if(remoteApplication.getCanRead()) {
				result = 1;
			}
		}
		
		return result;
	}
	
	public boolean userTypeExists(String userTypeShortName) {
		boolean result = false;
		
		
		
		return result;
	}
	
	public RemoteApplicationDAO getRemoteApplicationDAO() {
		return remoteApplicationDAO;
	}

	public void setRemoteApplicationDAO(RemoteApplicationDAO remoteApplicationDAO) {
		this.remoteApplicationDAO = remoteApplicationDAO;
	}
	
	
}
