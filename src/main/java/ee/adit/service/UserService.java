package ee.adit.service;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ee.adit.dao.AccessRestrictionDAO;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.NotificationDAO;
import ee.adit.dao.NotificationTypeDAO;
import ee.adit.dao.RemoteApplicationDAO;
import ee.adit.dao.UsertypeDAO;
import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.NotificationType;
import ee.adit.dao.pojo.RemoteApplication;
import ee.adit.dao.pojo.UserNotification;
import ee.adit.dao.pojo.UserNotificationId;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfNotification;
import ee.adit.pojo.GetUserInfoRequestAttachmentUserList;
import ee.adit.pojo.GetUserInfoResponseAttachmentUser;
import ee.adit.pojo.Notification;

public class UserService {

	private static Logger LOG = Logger.getLogger(UserService.class);
	
	private RemoteApplicationDAO remoteApplicationDAO;
	
	private UsertypeDAO usertypeDAO;
	
	private NotificationTypeDAO notificationTypeDAO;
	
	private AditUserDAO aditUserDAO;
	
	private DocumentDAO documentDAO;
	
	private AccessRestrictionDAO accessRestrictionDAO;
	
	private NotificationDAO notificationDAO;
	
	public static final String USERTYPE_PERSON = "PERSON";
	public static final String USERTYPE_INSTITUTION = "INSTITUTION";
	public static final String USERTYPE_COMPANY = "COMPANY";
	
	public static final String ACCESS_RESTRICTION_WRITE = "WRITE";
	public static final String ACCESS_RESTRICTION_READ = "READ";
	
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
		if (remoteApplication != null) {
			if ((remoteApplication.getCanWrite() != null) && remoteApplication.getCanWrite()) {
				result = 2;
			} else if ((remoteApplication.getCanRead() != null) && remoteApplication.getCanRead()) {
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
		this.getAccessRestrictionDAO().setSessionFactory(this.getRemoteApplicationDAO().getSessionFactory());
		List<AccessRestriction> accessRestrictons = this.getAccessRestrictionDAO().getAccessRestrictionsForUser(aditUser);
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
		aditUser.setActive(true);
		this.getAditUserDAO().saveOrUpdate(aditUser);
	}
	
	/**
	 * 
	 * @param aditUser
	 * @param username
	 * @param usertype
	 * @return true if the user was reactivated
	 * @throws AditInternalException
	 */
	public boolean modifyUser(AditUser aditUser, String username, Usertype usertype) throws AditInternalException {
		boolean result = false;
		// Activate the user account if needed
		if(!aditUser.getActive()) {
			aditUser.setActive(true);
			aditUser.setDeactivationDate(null);
			result = true;
		}
		
		if(USERTYPE_PERSON.equalsIgnoreCase(usertype.getShortName())) {
			modifyUser(aditUser, username);
		} else if(USERTYPE_INSTITUTION.equalsIgnoreCase(usertype.getShortName()) || USERTYPE_COMPANY.equalsIgnoreCase(usertype.getShortName())) {
			modifyUser(aditUser, username);
		} else {
			throw new AditInternalException("Unknown usertype");
		}
		
		return result;
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
	
	public List<GetUserInfoResponseAttachmentUser> getUserInfo(GetUserInfoRequestAttachmentUserList userList) {
		List<GetUserInfoResponseAttachmentUser> result = new ArrayList<GetUserInfoResponseAttachmentUser>();
		
		List<String> userCodes = userList.getCodes();
		
		for(String userCode : userCodes) {
			
			GetUserInfoResponseAttachmentUser userInfo = getUserInfo(userCode);
			result.add(userInfo);
		}
		
		return result;
	}
	
	public GetUserInfoResponseAttachmentUser getUserInfo(String userCode) {
		
		GetUserInfoResponseAttachmentUser result = new GetUserInfoResponseAttachmentUser();
		
		AditUser user = this.getAditUserDAO().getUserByID(userCode);
		
		Long diskquota = null;
		long usedSpace;
		Long unusedSpace = null;
		boolean usesDVK = false;
		boolean canRead = true;
		boolean canWrite = true;
		boolean hasJoined = false;
		
		if(user != null) {
			// User has joined the service
			LOG.debug("User has joined the service: " + userCode);
			hasJoined = true;
			
			usedSpace = this.getDocumentDAO().getUsedSpaceForUser(userCode);
			LOG.debug("Information for user (" + userCode + "): ");
			LOG.debug("UsedSpace for user: " + usedSpace);
			
			if(user.getDiskQuota() != null && user.getDiskQuota() > 0) {
				// Disk quota defined in user table
				user.getDiskQuota();				
			} else {
				// User disk quota not defined in user table - check usertype for quota
				Usertype usertype = this.getUsertypeDAO().getUsertype(user);
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

			// Construct the holder object
			result.setUserCode(userCode);
			result.setHasJoined(hasJoined);
			result.setFreeSpace(unusedSpace);
			result.setUsedSpace(usedSpace);
			result.setCanRead(canRead);
			result.setCanWrite(canWrite);
			result.setUsesDVK(usesDVK);
			
		} else {
			// User has not joined the service
			LOG.debug("User has not joined the service: " + userCode);
		}		

		return result;
		
	}
	
	public void deactivateUser(AditUser user) {		
		user.setActive(false);
		user.setDeactivationDate(new Date());
		this.getAditUserDAO().saveOrUpdate(user);		
	}
	
	public long getRemainingDiskQuota(AditUser user) {
		long result = 0;
		
		if(user.getDiskQuota() != null) {
			result = user.getDiskQuota();
		} else {
			Usertype usertype = this.getUsertypeDAO().getUsertype(user);
			if(usertype != null) {
				result = usertype.getDiskQuota();
			} else {
				throw new AditInternalException("Error getting remaining disk quota for user: " + user.getUserCode());
			}
		}
		
		return result;
	}
	
	public void setNotifications(final String userCode, final List<Notification> notifications) {
		this.getAditUserDAO().getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				AditUser user = (AditUser)session.get(AditUser.class, userCode);
				
				for (Notification item : notifications) {
					if (item.isActive() && (findNotification(user.getUserNotifications(), item.getType()) == null)) {
						NotificationType type = (NotificationType)session.get(NotificationType.class, item.getType());
						
						UserNotification notification = new UserNotification();
						UserNotificationId notificationId = new UserNotificationId();
						notificationId.setUserCode(userCode);
						notificationId.setNotificationType(item.getType());
						notification.setId(notificationId);
						notification.setNotificationType(type);
						
						user.getUserNotifications().add(notification);
						LOG.debug("Adding notification \""+ item.getType() +"\" to user " + userCode);
					} else if (!item.isActive()) {
						UserNotification notification = findNotification(user.getUserNotifications(), item.getType());
						if (notification != null) {
							session.delete(notification);
							user.getUserNotifications().remove(notification);
							//notification.getId().setUserCode(null);
							LOG.debug("Removing notification \""+ item.getType() +"\" from user " + userCode);
						}
					}
				}
				
				session.saveOrUpdate(user);
				return null;
			}
		});
	}
	
	public ArrayOfNotification getNotifications(final String userCode) {
		final NotificationTypeDAO notTypeDao = this.getNotificationTypeDAO();
		
		return (ArrayOfNotification)this.getAditUserDAO().getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ArrayOfNotification innerResult = new ArrayOfNotification();
				innerResult.setNotification(new ArrayList<Notification>());
				
				AditUser user = (AditUser)session.get(AditUser.class, userCode);
				List<NotificationType> types = notTypeDao.getNotificationTypeList();
				
				for (NotificationType type : types) {
					boolean notificationActive = false;
					if (findNotification(user.getUserNotifications(), type.getShortName()) != null) {
						notificationActive = true;
					}
					Notification item = new Notification();
					item.setActive(notificationActive);
					item.setType(type.getShortName());
					innerResult.getNotification().add(item);
				}

				return innerResult;
			}
		});
	}
	
	public UserNotification findNotification(final Set notifications, final String notificationType) {
		UserNotification result = null;
		
		Iterator it = notifications.iterator();
		while (it.hasNext()) {
			UserNotification item = (UserNotification)it.next();
			if (item.getId().getNotificationType().equalsIgnoreCase(notificationType)) {
				result = item;
				break;
			}
		}
		
		return result;
	}
	
	public long addNotification(
		long id,
		long documentId,
		String notificationType,
		String userCode,
		Date eventDate,
		String notificationText,
		Long notificationId,
		Date notificationSendingDate) {
		
		if (this.notificationDAO != null) {
			ee.adit.dao.pojo.Notification notification = new ee.adit.dao.pojo.Notification();
			notification.setId(id);
			notification.setUserCode(userCode);
			notification.setDocumentId(documentId);
			notification.setEventDate(eventDate);
			notification.setNotificationType(notificationType);
			notification.setNotificationText(notificationText);
			notification.setNotificationId(notificationId);
			notification.setNotificationSendingDate(notificationSendingDate);
			
			return this.notificationDAO.save(notification);
		} else {
			LOG.debug("Cannot save snotification, notificationDAO object is NULL!");
			return 0;
		}
	}
	
	public List<Usertype> listUsertypes() {
		try {
			return this.getUsertypeDAO().listUsertypes();
		} catch (Exception e) {
			LOG.error("Error while listing usertypes: ", e);
			return null;
		}
	}
	
	public String getUsertypesString() {
		List<Usertype> usertypes = this.listUsertypes();
		StringBuffer result = new StringBuffer();
		
		if(usertypes == null || usertypes.size() == 0) {
			return null;
		} else {
			for(int i = 0; i < usertypes.size(); i++) {
				Usertype usertype = usertypes.get(i);
				if(i > 0) {
					result.append(" / ");
				}
				result.append(usertype.getShortName());
			}
		}
		
		return result.toString();
	}
	
	public String getNotificationTypesString() {
		List<NotificationType> notificationTypes = this.getNotificationTypeDAO().getNotificationTypeList();
		StringBuffer result = new StringBuffer();
		
		if(notificationTypes == null || notificationTypes.size() == 0) {
			return null;
		} else {
			for(int i = 0; i < notificationTypes.size(); i++) {
				NotificationType notificationType = notificationTypes.get(i);
				if(i > 0) {
					result.append(" / ");
				}
				result.append(notificationType.getShortName());
			}
		}
		
		return result.toString();
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

	public NotificationTypeDAO getNotificationTypeDAO() {
		return notificationTypeDAO;
	}

	public void setNotificationTypeDAO(NotificationTypeDAO notificationTypeDAO) {
		this.notificationTypeDAO = notificationTypeDAO;
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

	public AccessRestrictionDAO getAccessRestrictionDAO() {
		return accessRestrictionDAO;
	}

	public void setAccessRestrictionDAO(AccessRestrictionDAO accessRestrictionDAO) {
		this.accessRestrictionDAO = accessRestrictionDAO;
	}

	public NotificationDAO getNotificationDAO() {
		return notificationDAO;
	}

	public void setNotificationDAO(NotificationDAO notificationDAO) {
		this.notificationDAO = notificationDAO;
	}	
	
}
