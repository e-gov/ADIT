package ee.adit.util;

/**
 * Contains globally used constants.
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public final class Constants {
	/**
	 * Default constructor.
	 */
	private Constants() {
	}

	/**
	 * Unique ID of maintenance job: Send documents to DVK.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	public static final long JOB_ID_DVK_SEND = 1L;

	/**
	 * Unique ID of maintenance job: Receive documents from DVK.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	public static final long JOB_ID_DVK_RECEIVE = 2L;

	/**
	 * Unique ID of maintenance job: Update document status from DVK.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	public static final long JOB_ID_DVK_UPDATE_STATUS_FROM_DVK = 3L;

	/**
	 * Unique ID of maintenance job: Update document status to DVK.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	public static final long JOB_ID_DVK_UPDATE_STATUS_TO_DVK = 4L;

	/**
	 * Unique ID of maintenance job: Delete documents from DVK.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	public static final long JOB_ID_DVK_DELETE = 5L;

	/**
	 * Unique ID of maintenance job: Synchronize users with DVK.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	public static final long JOB_ID_DVK_USER_SYNC = 6L;

	/**
	 * Unique ID of maintenance job: Send notifications.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	public static final long JOB_ID_NOTIFICATION_SEND = 7L;

	/**
	 * Unique ID of maintenance job: Clean temporary files.
	 * Must have a corresponding record in database table "MAINTENANCE_JOB".
	 */
	public static final long JOB_ID_TEMPORARY_FOLDER_CLEAN = 8L;
}
