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

import dvk.api.ml.PojoOrganization;
import ee.adit.dao.AccessRestrictionDAO;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.NotificationDAO;
import ee.adit.dao.NotificationTypeDAO;
import ee.adit.dao.RemoteApplicationDAO;
import ee.adit.dao.UsertypeDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AccessRestriction;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.NotificationType;
import ee.adit.dao.pojo.RemoteApplication;
import ee.adit.dao.pojo.UserNotification;
import ee.adit.dao.pojo.UserNotificationId;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.ArrayOfNotification;
import ee.adit.pojo.GetUserInfoRequestAttachmentUserList;
import ee.adit.pojo.GetUserInfoResponseAttachmentUser;
import ee.adit.pojo.Notification;
import ee.adit.util.DVKUserSyncResult;

/**
 * Provides services for manipulating and retrieving user data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class UserService {

    private static Logger logger = Logger.getLogger(UserService.class);

    private RemoteApplicationDAO remoteApplicationDAO;

    private UsertypeDAO usertypeDAO;

    private NotificationTypeDAO notificationTypeDAO;

    private AditUserDAO aditUserDAO;

    private DocumentDAO documentDAO;

    private AccessRestrictionDAO accessRestrictionDAO;

    private NotificationDAO notificationDAO;

    private DvkDAO dvkDAO;

    /**
     * Usertype PERSON
     */
    public static final String USERTYPE_PERSON = "PERSON";
    
    /**
     * Usertype INSTITUTION
     */
    public static final String USERTYPE_INSTITUTION = "INSTITUTION";
    
    /**
     * Usertype COMPANY
     */
    public static final String USERTYPE_COMPANY = "COMPANY";

    /**
     * Usertype WRITE
     */
    public static final String ACCESS_RESTRICTION_WRITE = "WRITE";
    
    /**
     * Usertype READ
     */
    public static final String ACCESS_RESTRICTION_READ = "READ";

    /**
     * Checks if the application is registered.
     * 
     * @param remoteApplicationShortName
     *            remote application short name
     * @return true, if the application is registered
     */
    public boolean isApplicationRegistered(String remoteApplicationShortName) {
        boolean result = false;
        logger.debug("Checking if application '" + remoteApplicationShortName + "' is registered.");

        if (this.getRemoteApplicationDAO() == null) {
            logger.error("remoteApplicationDAO not initialized");
        } else {
            RemoteApplication remoteApplication = this.getRemoteApplicationDAO().getByShortName(
                    remoteApplicationShortName);

            if (remoteApplication != null) {
                result = true;
            }
        }

        logger.debug("Application '" + remoteApplicationShortName + "' is registered?: " + result);
        return result;
    }

    /**
     * Determines the access level for this application:<br>
     * 0 - no access<br>
     * 1 - read access<br>
     * 2 - write acces (full access)
     * 
     * @param remoteApplicationShortName
     *            Short name of application that executed curent request
     * @return Access level for specified application
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
     * Determines the level of access on user for this application:<br>
     * 0 - no access<br>
     * 1 - read access<br>
     * 2 - write acces (full access)
     * 
     * @param remoteApplicationShortName
     *            Short name of application that executed current request.
     * @param aditUser
     *            User as {@link AditUser} object.
     * @return Access level for specified user and application
     */
    public int getAccessLevelForUser(String remoteApplicationShortName, AditUser aditUser) {
        int result = 2;

        RemoteApplication remoteApplication = this.getRemoteApplicationDAO().getByShortName(remoteApplicationShortName);
        this.getAccessRestrictionDAO().setSessionFactory(this.getRemoteApplicationDAO().getSessionFactory());
        List<AccessRestriction> accessRestrictons = this.getAccessRestrictionDAO().getAccessRestrictionsForUser(
                aditUser);
        logger.debug("Number of access restrictions for (" + aditUser.getUserCode() + "): " + accessRestrictons.size());
        Iterator<AccessRestriction> i = accessRestrictons.iterator();

        while (i.hasNext()) {
            AccessRestriction accessRestriction = i.next();
            logger.debug("Access restriction: " + accessRestriction.getRestriction());
            if (accessRestriction.getRemoteApplication() != null
                    && accessRestriction.getRemoteApplication().getShortName() != null
                    && accessRestriction.getRemoteApplication().getShortName().equals(remoteApplication.getShortName())) {
                // If the restriction restricts this application to read this
                // user's data
                if (ACCESS_RESTRICTION_READ.equalsIgnoreCase(accessRestriction.getRestriction())) {
                    logger.debug("Found READ access restriction for user: " + aditUser.getUserCode());
                    result = 0;
                } else if (ACCESS_RESTRICTION_WRITE.equalsIgnoreCase(accessRestriction.getRestriction())) {
                    logger.debug("Found WRITE access restriction for user: " + aditUser.getUserCode());
                    result = 1;
                }
            }
        }

        return result;
    }

    /**
     * Retrieves the usertype object specified by the usertype short name.
     * 
     * @param usertypeShortName
     *            usertype short name
     * @return usertype object
     */
    public Usertype getUsertypeByID(String usertypeShortName) {
        Usertype result = null;

        try {
            result = this.getUsertypeDAO().getByShortName(usertypeShortName);
        } catch (Exception e) {
            logger.error("Error while fetching Usertype by sgort name: ", e);
        }

        return result;
    }

    /**
     * Retrieves user.
     * 
     * @param userRegCode
     *            user code
     * @return user object, null if not found
     */
    public AditUser getUserByID(String userRegCode) {
        logger.debug("Getting user by ID: " + userRegCode);
        AditUser result = null;

        try {
            result = this.getAditUserDAO().getUserByID(userRegCode);
            if (result != null) {
                logger.debug("Found user with ID: " + result.getUserCode() + ", name: " + result.getFullName());
            } else {
                logger.debug("Did not find user.");
            }
        } catch (Exception e) {
            logger.error("Error while fetching AditUser by ID: ", e);
        }

        return result;
    }

    /**
     * Adds a user.
     * 
     * @param username
     *            user full name
     * @param usertype
     *            usertype
     * @param institutionCode
     *            institution code - used if usertype is {@code
     *            USERTYPE_INSTITUTION} or {@code USERTYPE_COMPANY}
     * @param personalCode
     *            personal code - used if usertype is {@code USERTYPE_PERSON}
     * @throws AditInternalException
     */
    public void addUser(String username, Usertype usertype, String institutionCode, String personalCode)
            throws AditInternalException {
        if (USERTYPE_PERSON.equalsIgnoreCase(usertype.getShortName())) {
            addUser(username, personalCode, usertype);
        } else if (USERTYPE_INSTITUTION.equalsIgnoreCase(usertype.getShortName())
                || USERTYPE_COMPANY.equalsIgnoreCase(usertype.getShortName())) {
            addUser(username, institutionCode, usertype);
        } else {
            throw new AditInternalException("Unknown usertype");
        }
    }

    /**
     * Adds a user.
     * 
     * @param username
     *            user full name
     * @param usercode
     *            user code
     * @param usertype
     *            usertype
     */
    public void addUser(String username, String usercode, Usertype usertype) {
        AditUser aditUser = new AditUser();
        aditUser.setUserCode(usercode);
        aditUser.setFullName(username);
        aditUser.setUsertype(usertype);
        aditUser.setActive(true);
        this.getAditUserDAO().saveOrUpdate(aditUser);
    }

    /**
     * Updates / modifies a user. User reactivated if not presently active.
     * 
     * @param aditUser
     *            user
     * @param username
     *            new username
     * @param usertype
     *            new usertype
     * @return true if the user was reactivated
     * @throws AditInternalException
     */
    public boolean modifyUser(AditUser aditUser, String username, Usertype usertype) throws AditInternalException {
        boolean result = false;
        // Activate the user account if needed
        if (!aditUser.getActive()) {
            aditUser.setActive(true);
            aditUser.setDeactivationDate(null);
            result = true;
        }

        if (USERTYPE_PERSON.equalsIgnoreCase(usertype.getShortName())) {
            modifyUser(aditUser, username);
        } else if (USERTYPE_INSTITUTION.equalsIgnoreCase(usertype.getShortName())
                || USERTYPE_COMPANY.equalsIgnoreCase(usertype.getShortName())) {
            modifyUser(aditUser, username);
        } else {
            throw new AditInternalException("Unknown usertype");
        }

        return result;
    }

    /**
     * Updates user name.
     * 
     * @param aditUser
     *            user
     * @param username
     *            new user full name
     */
    public void modifyUser(AditUser aditUser, String username) {
        aditUser.setFullName(username);
        this.getAditUserDAO().saveOrUpdate(aditUser);
    }

    /**
     * Retrieves the userlist.
     * 
     * @param startIndex
     *            start index (offset)
     * @param maxResults
     *            number of maximum results
     * @return userlist
     * @throws Exception
     */
    public List<AditUser> listUsers(BigInteger startIndex, BigInteger maxResults) throws Exception {
        List<AditUser> result = null;

        result = this.getAditUserDAO().listUsers(startIndex.intValue(), maxResults.intValue());

        return result;
    }

    /**
     * Retrieves user information.
     * 
     * @param userList
     *            userlist
     * @return list of users information
     */
    public List<GetUserInfoResponseAttachmentUser> getUserInfo(GetUserInfoRequestAttachmentUserList userList) {
        List<GetUserInfoResponseAttachmentUser> result = new ArrayList<GetUserInfoResponseAttachmentUser>();

        List<String> userCodes = userList.getCodes();

        for (String userCode : userCodes) {

            GetUserInfoResponseAttachmentUser userInfo = getUserInfo(userCode);
            result.add(userInfo);
        }

        return result;
    }

    /**
     * Gets user info for a single user.
     * 
     * @param userCode
     *            user code
     * @return user information
     */
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

        if (user != null) {
            // User has joined the service
            logger.debug("User has joined the service: " + userCode);
            hasJoined = true;

            usedSpace = this.getAditUserDAO().getUsedSpaceForUser(userCode);
            logger.debug("Information for user (" + userCode + "): ");
            logger.debug("UsedSpace for user: " + usedSpace);

            if (user.getDiskQuota() != null && user.getDiskQuota() > 0) {
                // Disk quota defined in user table
                user.getDiskQuota();
            } else {
                // User disk quota not defined in user table - check usertype
                // for quota
                Usertype usertype = this.getUsertypeDAO().getUsertype(user);
                if (usertype != null && usertype.getDiskQuota() != null) {
                    diskquota = usertype.getDiskQuota();
                }
            }

            if (diskquota != null) {
                // Calculate the unused space for this user
                unusedSpace = diskquota.longValue() - usedSpace;
            } else {
                throw new AditInternalException("User disk quota not defined by user/usertype data.");
            }

            if (user.getDvkOrgCode() != null && !"".equalsIgnoreCase(user.getDvkOrgCode().trim())) {
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
            result.setUserCode(userCode);
            // User has not joined the service
            logger.debug("User has not joined the service: " + userCode);
        }

        return result;

    }

    /**
     * Deactivates user.
     * 
     * @param user
     */
    public void deactivateUser(AditUser user) {
        user.setActive(false);
        user.setDeactivationDate(new Date());
        this.getAditUserDAO().saveOrUpdate(user);
    }

    /**
     * Retrieves the remaining disk quota for the specified user.
     * 
     * @param user
     * @param globalDiskQuota
     *            global disk quota
     * @return
     */
    public long getRemainingDiskQuota(AditUser user, long globalDiskQuota) {

        logger.info("Finding disk quota for user: " + user.getUserCode());

        if (user != null) {
            Long usedDiskSpace = user.getDiskQuotaUsed();
            if (usedDiskSpace == null) {
            	usedDiskSpace = 0L;
            }
            
            long totalDiskQuota = getTotalDiskQuota(user, globalDiskQuota);
            long result = totalDiskQuota - usedDiskSpace;
            logger.debug("Remaining disk quota for user \"" + user.getUserCode() + "\" is " + result + " (total: "
                    + totalDiskQuota + ", used: " + usedDiskSpace + ")");
            return result;
        } else {
            throw new AditInternalException("Could not find remaining disk quota for user: user is null");
        }

    }

    /**
     * Determines the disk quota for the user specified.
     * 
     * @param user
     * @param globalDiskQuota
     *            global disk quota
     * @return
     */
    public long getTotalDiskQuota(AditUser user, long globalDiskQuota) {
        long result = 0;

        if (user.getDiskQuota() != null) {
            result = user.getDiskQuota();
        } else {
            Usertype usertype = this.getUsertypeDAO().getUsertype(user);
            if (usertype != null) {
                if (usertype.getDiskQuota() != null) {
                    result = usertype.getDiskQuota();
                } else {
                    result = globalDiskQuota;
                }
            } else {
                throw new AditInternalException("Error getting total disk quota for user: " + user.getUserCode());
            }
        }

        return result;
    }

    /**
     * Sets notifications settings for the specified user.
     * 
     * @param userCode
     *            user code
     * @param notifications
     *            notifications list
     */
    public void setNotifications(final String userCode, final List<Notification> notifications) {
        this.getAditUserDAO().getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                AditUser user = (AditUser) session.get(AditUser.class, userCode);

                for (Notification item : notifications) {
                    if (item.isActive() && (findNotification(user.getUserNotifications(), item.getType()) == null)) {
                        NotificationType type = (NotificationType) session.get(NotificationType.class, item.getType());

                        UserNotification notification = new UserNotification();
                        UserNotificationId notificationId = new UserNotificationId();
                        notificationId.setUserCode(userCode);
                        notificationId.setNotificationType(item.getType());
                        notification.setId(notificationId);
                        notification.setNotificationType(type);

                        user.getUserNotifications().add(notification);
                        logger.debug("Adding notification \"" + item.getType() + "\" to user " + userCode);
                    } else if (!item.isActive()) {
                        UserNotification notification = findNotification(user.getUserNotifications(), item.getType());
                        if (notification != null) {
                            session.delete(notification);
                            user.getUserNotifications().remove(notification);
                            // notification.getId().setUserCode(null);
                            logger.debug("Removing notification \"" + item.getType() + "\" from user " + userCode);
                        }
                    }
                }

                session.saveOrUpdate(user);
                return null;
            }
        });
    }

    /**
     * Retrieves the notifications settings for the user specified.
     * 
     * @param userCode
     *            user code
     * @return notifications settings list
     */
    public ArrayOfNotification getNotifications(final String userCode) {
        final NotificationTypeDAO notTypeDao = this.getNotificationTypeDAO();

        return (ArrayOfNotification) this.getAditUserDAO().getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                ArrayOfNotification innerResult = new ArrayOfNotification();
                innerResult.setNotification(new ArrayList<Notification>());

                AditUser user = (AditUser) session.get(AditUser.class, userCode);
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

    /**
     * Finds notification by type.
     * 
     * @param notifications
     *            notifications settings list
     * @param notificationType
     *            notification type
     * @return notification, null if not found
     */
    public UserNotification findNotification(final Set<UserNotification> notifications, final String notificationType) {
        UserNotification result = null;

        Iterator<UserNotification> it = notifications.iterator();
        while (it.hasNext()) {
            UserNotification item = it.next();
            if (item.getId().getNotificationType().equalsIgnoreCase(notificationType)) {
                result = item;
                break;
            }
        }

        return result;
    }

    /**
     * Adds a notification for the specified user.
     * 
     * @param id
     *            notification ID
     * @param documentId
     *            document ID
     * @param notificationType
     *            notification type
     * @param userCode
     *            user code
     * @param eventDate
     *            event date
     * @param notificationText
     *            notification text
     * @param notificationId
     *            notification ID (X-tee notification calendar)
     * @param notificationSendingDate
     *            sending date for this notification
     * @return
     */
    public long addNotification(long id, long documentId, String notificationType, String userCode, Date eventDate,
            String notificationText, Long notificationId, Date notificationSendingDate) {

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
            logger.debug("Cannot save snotification, notificationDAO object is NULL!");
            return 0;
        }
    }

    /**
     * Retrieves usertypes list.
     * 
     * @return
     */
    public List<Usertype> listUsertypes() {
        try {
            return this.getUsertypeDAO().listUsertypes();
        } catch (Exception e) {
            logger.error("Error while listing usertypes: ", e);
            return null;
        }
    }

    /**
     * Retrieves the usertypes list and converts it to a string.
     * 
     * @return
     */
    public String getUsertypesString() {
        List<Usertype> usertypes = this.listUsertypes();
        StringBuffer result = new StringBuffer();

        if (usertypes == null || usertypes.size() == 0) {
            return null;
        } else {
            for (int i = 0; i < usertypes.size(); i++) {
                Usertype usertype = usertypes.get(i);
                if (i > 0) {
                    result.append(" / ");
                }
                result.append(usertype.getShortName());
            }
        }

        return result.toString();
    }

    /**
     * Retrieves notification types and returns them as a string.
     * 
     * @return
     */
    public String getNotificationTypesString() {
        List<NotificationType> notificationTypes = this.getNotificationTypeDAO().getNotificationTypeList();
        StringBuffer result = new StringBuffer();

        if (notificationTypes == null || notificationTypes.size() == 0) {
            return null;
        } else {
            for (int i = 0; i < notificationTypes.size(); i++) {
                NotificationType notificationType = notificationTypes.get(i);
                if (i > 0) {
                    result.append(" / ");
                }
                result.append(notificationType.getShortName());
            }
        }

        return result.toString();
    }

    /**
     * Checks if application is registered.
     * 
     * @param applicationName
     *            application short name
     * @throws AditCodedException
     */
    public void checkApplicationRegistered(String applicationName) throws AditCodedException {
        boolean applicationRegistered = isApplicationRegistered(applicationName);
        if (!applicationRegistered) {
            AditCodedException aditCodedException = new AditCodedException("application.notRegistered");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }
    }

    /**
     * Checks if application has the overall 'write' privilege.
     * 
     * @param applicationName remote application name
     * @throws if application does not have the 'write' privilege.
     */
    public void checkApplicationWritePrivilege(String applicationName) throws AditCodedException {
        int accessLevel = getAccessLevel(applicationName);
        if (accessLevel != 2) {
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.write");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }
    }

    /**
     * Checks if application has the overall 'read' privilege.
     * 
     * @param applicationName remote application name
     * @throws if application does not have the 'read' privilege.
     */
    public void checkApplicationReadPrivilege(String applicationName) throws AditCodedException {
        int accessLevel = getAccessLevel(applicationName);
        if (accessLevel < 1) {
            AditCodedException aditCodedException = new AditCodedException("application.insufficientPrivileges.read");
            aditCodedException.setParameters(new Object[] {applicationName });
            throw aditCodedException;
        }
    }

    /**
     * Synchronize DVK users with ADIT user accounts: DVK -> ADIT only 1. Get
     * DVK users 2. Check if user exists in ADIT 3. Check is user data changed
     * 
     * @return result
     */
    public DVKUserSyncResult synchroinzeDVKUsers() {

        DVKUserSyncResult result = new DVKUserSyncResult();

        int deactivated = 0;
        int added = 0;
        int modified = 0;

        try {
            List<PojoOrganization> dvkUsers = this.getDvkDAO().getUsers();
            logger.info("Number of DVK users found: " + dvkUsers.size());

            Iterator<PojoOrganization> dvkUserIterator = dvkUsers.iterator();

            List<AditUser> aditUsers = this.getAditUserDAO().listDVKUsers();
            logger.info("Number of ADIT users found: " + aditUsers.size());

            List<AditUser> aditUsersCopy = new ArrayList<AditUser>(aditUsers);

            Usertype institutionUsertype = this.getUsertypeDAO().getByShortName(UserService.USERTYPE_INSTITUTION);

            if (institutionUsertype == null) {
                throw new AditInternalException("Could not find usertype '" + UserService.USERTYPE_INSTITUTION
                        + "' in ADIT database.");
            }

            while (dvkUserIterator.hasNext()) {

                PojoOrganization dvkUser = dvkUserIterator.next();
                boolean found = false;
                Iterator<AditUser> aditUserIterator = aditUsers.iterator();

                logger.debug("Finding match for dvkUser: " + dvkUser.getOrgCode());
                while (!found && aditUserIterator.hasNext()) {
                    AditUser aditUser = aditUserIterator.next();

                    // Match

                    logger.debug("Matching: " + aditUser.getDvkOrgCode());
                    if (aditUser.getDvkOrgCode() != null
                            && aditUser.getDvkOrgCode().trim().equalsIgnoreCase(dvkUser.getOrgCode())) {
                        found = true;
                        logger.debug("Matched!");

                        aditUsersCopy.remove(aditUser);

                        // Check if user's name has changed in DVK
                        if (dvkUser.getName() != null && !dvkUser.getName().equalsIgnoreCase(aditUser.getFullName())) {
                            aditUser.setFullName(dvkUser.getName());
                            this.getAditUserDAO().saveOrUpdate(aditUser);
                            logger.info("User '" + aditUser.getUserCode() + "' has modified name. Updated in ADIT.");
                            modified++;
                        }

                    }

                }

                // Add new user to ADIT
                if (!found) {
                    logger.info("Adding new user to ADIT (DVK user): " + dvkUser.getOrgCode() + ", " + dvkUser.getName());
                    AditUser newAditUser = new AditUser();
                    newAditUser.setDvkOrgCode(dvkUser.getOrgCode());
                    newAditUser.setActive(new Boolean(true));
                    newAditUser.setFullName(dvkUser.getName());
                    newAditUser.setUserCode("EE" + dvkUser.getOrgCode());
                    newAditUser.setUsertype(institutionUsertype);
                    this.getAditUserDAO().saveOrUpdate(newAditUser);
                    added++;
                }

            }

            // For those users that remained in the list - delete (because they
            // don't exist in DVK anymore)

            if (aditUsersCopy != null && aditUsersCopy.size() > 0) {
                Iterator<AditUser> deletedUserIterator = aditUsersCopy.iterator();

                while (deletedUserIterator.hasNext()) {
                    AditUser deletedUser = deletedUserIterator.next();
                    if (deletedUser.getActive()) {
                        logger.info("Deactivating DVK user in ADIT: " + deletedUser.getUserCode());
                        deletedUser.setActive(new Boolean(false));
                        deletedUser.setDeactivationDate(new Date());
                        this.getAditUserDAO().saveOrUpdate(deletedUser);
                        deactivated++;
                    }
                }

            }

        } catch (Exception e) {
            logger.error("Error while synchronizing DVK users: ", e);
            result.setAdded(added);
            result.setDeactivated(deactivated);
            result.setModified(modified);
        }

        result.setAdded(added);
        result.setDeactivated(deactivated);
        result.setModified(modified);

        return result;
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

    public DvkDAO getDvkDAO() {
        return dvkDAO;
    }

    public void setDvkDAO(DvkDAO dvkDAO) {
        this.dvkDAO = dvkDAO;
    }

}
