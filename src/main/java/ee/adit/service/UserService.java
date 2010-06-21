package ee.adit.service;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.RemoteApplicationDAO;
import ee.adit.dao.UsertypeDAO;
import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.RemoteApplication;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.GetUserInfoRequestAttachmentUserList;

public class UserService {

	private static Logger LOG = Logger.getLogger(UserService.class);
	
	private RemoteApplicationDAO remoteApplicationDAO;
	
	private UsertypeDAO usertypeDAO;
	
	private AditUserDAO aditUserDAO;
	
	private DocumentDAO documentDAO;
	
	private static final String USERTYPE_PERSON = "PERSON";
	private static final String USERTYPE_INSTITUTION = "INSTITUTION";
	private static final String USERTYPE_COMPANY = "COMPANY";
	
	private static final String ACCESS_RESTRICTION_WRITE = "WRITE";
	private static final String ACCESS_RESTRICTION_READ = "READ";
	
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
	 * 
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
	/**
	 * Determines the level of access on user for this application:
	 * 0 - no access
	 * 1 - read access
	 * 2 - write acces (full access) 
	 *  
	 * @param remoteApplicationShortName
	 * @param aditUser
	 * @return
	 */
	public int getAccessLevelForUser(String remoteApplicationShortName, AditUser aditUser) {
		int result = 2;
		
		RemoteApplication remoteApplication = this.getRemoteApplicationDAO().getByShortName(remoteApplicationShortName);
		List<AccessRestriction> accessRestrictons = this.getAditUserDAO().getAccessRestrictionsForUser(aditUser);
		LOG.debug("Number of access restrictions for (" + aditUser.getUserCode() + "): " + accessRestrictons.size());
		Iterator<AccessRestriction> i = accessRestrictons.iterator();
		
		while(i.hasNext()) {
			AccessRestriction accessRestriction = i.next();
			LOG.debug("Access restriction: " + accessRestriction.getRestriction());
			if(accessRestriction.getRemoteApplication() != null && accessRestriction.getRemoteApplication().getShortName() != null && accessRestriction.getRemoteApplication().getShortName().equals(remoteApplication.getShortName())) {
				// If the restriction restricts this application to read this user's data
				if(ACCESS_RESTRICTION_READ.equalsIgnoreCase(accessRestriction.getRestriction())) {
					LOG.debug("Found READ access restriction for user: " + aditUser.getUserCode());
					result = 0;
				} else if(ACCESS_RESTRICTION_WRITE.equalsIgnoreCase(accessRestriction.getRestriction())) {
					LOG.debug("Found WRITE access restriction for user: " + aditUser.getUserCode());
					result = 1;
				}
			}
		}
		
		return result;
	}
	
	public Usertype getUsertypeByID(String usertypeShortName) {
		Usertype result = null;
		
		try {
			result = this.getUsertypeDAO().getByShortName(usertypeShortName);
		} catch (Exception e) {
			LOG.error("Error while fetching Usertype by sgort name: ", e);
		}
		
		return result;
	}
	
	public AditUser getUserByID(String userRegCode) {
		LOG.debug("Getting user by ID: " + userRegCode);
		AditUser result = null;
		
		try {
			result = this.getAditUserDAO().getUserByID(userRegCode);
			if(result != null) {
				LOG.debug("Found user with ID: " + result.getUserCode() + ", name: " + result.getFullName());
			} else {
				LOG.debug("Did not find user.");
			}
		} catch (Exception e) {
			LOG.error("Error while fetching AditUser by ID: ", e);
		}
		
		return result;
	}
	
	public void addUser(String username, Usertype usertype, String institutionCode, String personalCode) throws AditInternalException {
		if(USERTYPE_PERSON.equalsIgnoreCase(usertype.getShortName())) {
			addUser(username, personalCode, usertype);
		} else if(USERTYPE_INSTITUTION.equalsIgnoreCase(usertype.getShortName()) || USERTYPE_COMPANY.equalsIgnoreCase(usertype.getShortName())) {
			addUser(username, institutionCode, usertype);
		} else {
			throw new AditInternalException("Unknown usertype");
		}
	}
	
	public void addUser(String username, String usercode, Usertype usertype) {
		AditUser aditUser = new AditUser();
		aditUser.setUserCode(usercode);
		aditUser.setFullName(username);
		aditUser.setUsertype(usertype);
		this.getAditUserDAO().saveOrUpdate(aditUser);
	}
	
	public void modifyUser(AditUser aditUser, String username, Usertype usertype) throws AditInternalException {
		if(USERTYPE_PERSON.equalsIgnoreCase(usertype.getShortName())) {
			modifyUser(aditUser, username);
		} else if(USERTYPE_INSTITUTION.equalsIgnoreCase(usertype.getShortName()) || USERTYPE_COMPANY.equalsIgnoreCase(usertype.getShortName())) {
			modifyUser(aditUser, username);
		} else {
			throw new AditInternalException("Unknown usertype");
		}
	}
	
	public void modifyUser(AditUser aditUser, String username) {
		aditUser.setFullName(username);
		this.getAditUserDAO().saveOrUpdate(aditUser);
	}
	
	public List<AditUser> listUsers(BigInteger startIndex, BigInteger maxResults) throws Exception {
		List <AditUser> result = null;
		
		result = this.getAditUserDAO().listUsers(startIndex.intValue(), maxResults.intValue());
		
		return result;
	}
	
	public void getUserInfo(GetUserInfoRequestAttachmentUserList userList) {
		
		List<String> userCodes = userList.getCodes();
		
		for(String userCode : userCodes) {
			
			getUserInfo(userCode);
			
		}
		
	}
	
	public void getUserInfo(String userCode) {
		
		
		
		AditUser user = this.getAditUserDAO().getUserByID(userCode);
		
		Long diskquota = null;
		long usedSpace;
		Long unusedSpace = null;
		boolean usesDVK = false;
		boolean canRead = true;
		boolean canWrite = true;
		
		if(user != null) {
			LOG.debug("User has joined the service: " + userCode);
			// User has joined the service
			usedSpace = this.getDocumentDAO().getUsedSpaceForUser(userCode);
			LOG.debug("Information for user (" + userCode + "): ");
			LOG.debug("UsedSpace for user: " + usedSpace);
			
			
			
			if(user.getDiskQuota() != null && user.getDiskQuota() > 0) {
				// Disk quota defined in user table
				user.getDiskQuota();				
			} else {
				// User disk quota not defined in user table - check usertype for quota
				Usertype usertype = user.getUsertype();
				if(usertype != null && usertype.getDiskQuota() != null) {
					diskquota = usertype.getDiskQuota();
				}
			}
			
			if(diskquota != null) {
				// Calculate the unused space for this user
				unusedSpace = diskquota.longValue() - usedSpace;				
			} else {
				throw new AditInternalException("User disk quota not defined by user/usertype data.");
			}
			
			if(user.getDvkOrgCode() != null && !"".equalsIgnoreCase(user.getDvkOrgCode().trim())) {
				usesDVK = true;
				canWrite = false;
			}
			
			
		} else {
			// User has not joined the service
			LOG.debug("User has not joined the service: " + userCode);
		}
		
		// Construct the holder object
		
		
	}
	
	public RemoteApplicationDAO getRemoteApplicationDAO() {
		return remoteApplicationDAO;
	}

	public void setRemoteApplicationDAO(RemoteApplicationDAO remoteApplicationDAO) {
		this.remoteApplicationDAO = remoteApplicationDAO;
	}

	public UsertypeDAO getUsertypeDAO() {
		return usertypeDAO;
	}

	public void setUsertypeDAO(UsertypeDAO usertypeDAO) {
		this.usertypeDAO = usertypeDAO;
	}

	public AditUserDAO getAditUserDAO() {
		return aditUserDAO;
	}

	public void setAditUserDAO(AditUserDAO aditUserDAO) {
		this.aditUserDAO = aditUserDAO;
	}

	public DocumentDAO getDocumentDAO() {
		return documentDAO;
	}

	public void setDocumentDAO(DocumentDAO documentDAO) {
		this.documentDAO = documentDAO;
	}	
	
}
