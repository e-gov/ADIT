package ee.adit.service;

import java.util.Date;

import org.apache.log4j.Logger;

import ee.adit.dao.DownloadRequestLogDAO;
import ee.adit.dao.ErrorLogDAO;
import ee.adit.dao.MetadataRequestLogDAO;
import ee.adit.dao.RequestLogDAO;
import ee.adit.dao.pojo.DownloadRequestLog;
import ee.adit.dao.pojo.ErrorLog;
import ee.adit.dao.pojo.MetadataRequestLog;
import ee.adit.dao.pojo.RequestLog;

/**
 * Logging service. Provides methods for inserting log records to database.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class LogService {
    /**
     * Default logger.
     */
    private static Logger logger = Logger.getLogger(LogService.class);

    /**
     * Error log level WARN.
     */
    public static final String ERROR_LOG_LEVEL_WARN = "WARN";

    /**
     * Error log level ERROR.
     */
    public static final String ERROR_LOG_LEVEL_ERROR = "ERROR";

    /**
     * Error log level FATAL.
     */
    public static final String ERROR_LOG_LEVEL_FATAL = "FATAL";

    /**
     * Request log success message.
     */
    public static final String REQUEST_LOG_SUCCESS = "Success";

    /**
     * Request log success message.
     */
    public static final String REQUEST_LOG_FAIL = "ERROR: ";

    /**
     * Data access object for reading and writing request log data.
     */
    private RequestLogDAO requestLogDAO;

    /**
     * Data access object for reading and writing error log data.
     */
    private ErrorLogDAO errorLogDAO;

    /**
     * Data access object for reading and writing download request log data.
     */
    private DownloadRequestLogDAO downloadRequestLogDAO;

    /**
     * Data access object for reading and writing metadata request log data.
     */
    private MetadataRequestLogDAO metadataRequestLogDAO;

    /**
     * Adds a record to request log.
     *
     * @param requestName
     *            request name
     * @param documentId
     *            document ID of the document associated with the request
     * @param requestDate
     *            request invocation date
     * @param remoteApplicationShortName
     *            name of the remote application that sent the request
     * @param userCode
     *            code of the user who sent the request
     * @param organizationCode
     *            the organization that sent the request
     * @param additionalInformation
     *            additional information
     * @return ID of added log entry
     */
    public final Long addRequestLogEntry(
        final String requestName, final Long documentId, final Date requestDate,
        final String remoteApplicationShortName, final String userCode,
        final String organizationCode, final String additionalInformation) {

        RequestLog logEntry = new RequestLog();
        logEntry.setAdditionalInformation(additionalInformation);
        logEntry.setDocumentId(documentId);
        logEntry.setOrganizationCode(organizationCode);
        logEntry.setRemoteApplicationShortName(remoteApplicationShortName);
        logEntry.setRequest(requestName);
        logEntry.setRequestDate(requestDate);
        logEntry.setUserCode(userCode);

        return this.requestLogDAO.save(logEntry);
    }

    /**
     * Adds a record to the error log.
     *
     * @param actionName
     *            the name of the action that caused the error
     * @param documentId
     *            document ID associated with the error
     * @param errorDate
     *            the time when the error occurred
     * @param remoteApplicationShortName
     *            remote application
     * @param userCode
     *            code of the user who sent the request
     * @param errorLevel
     *            error level
     * @param errorMessage
     *            error message
     *
     * @return log entry ID
     */
    public final Long addErrorLogEntry(
        final String actionName, final Long documentId, final Date errorDate,
        final String remoteApplicationShortName, final String userCode,
        final String errorLevel, final String errorMessage) {

        ErrorLog logEntry = new ErrorLog();
        logEntry.setActionName(actionName);
        logEntry.setDocumentId(documentId);
        logEntry.setErrorDate(errorDate);
        logEntry.setErrorLevel(errorLevel);
        logEntry.setErrorMessage(errorMessage);
        logEntry.setRemoteApplicationShortName(remoteApplicationShortName);
        logEntry.setUserCode(userCode);

        return this.errorLogDAO.save(logEntry);
    }

    /**
     * Adds a record to the download log.
     *
     * @param documentId
     *            document ID associated with the request
     * @param documentFileId
     *            document file ID associated with the request
     * @param requestDate
     *            request invocation date
     * @param appName
     *            remote application name
     * @param userCode
     *            code of the user who sent the request
     * @param orgCode
     *            the code of the organization that sent the request
     * @return log entry ID
     */
    public final Long addDownloadRequestLogEntry(
        final Long documentId, final Long documentFileId,
        final Date requestDate, final String appName, final String userCode,
        final String orgCode) {

        Long result = 0L;
        try {
            DownloadRequestLog logEntry = new DownloadRequestLog();
            logEntry.setDocumentId(documentId);
            logEntry.setDocumentFileId(documentFileId);
            logEntry.setRequestDate(requestDate);
            logEntry.setRemoteApplicationShortName(appName);
            logEntry.setUserCode(userCode);
            logEntry.setOrganizationCode(orgCode);
            result = this.downloadRequestLogDAO.save(logEntry);
        } catch (Exception ex) {
            // Do not throw exception if logging fails!
            logger.warn("Failed logging document/file download!", ex);
        }
        return result;
    }

    /**
     * Adds a record to the metadata log.
     *
     * @param documentId
     *            document ID associated with the request
     * @param requestDate
     *            request invocation date
     * @param appName
     *            remote application name
     * @param userCode
     *            code of the user who sent the request
     * @param orgCode
     *            the code of the organization that sent the request
     * @return log entry ID
     */
    public final Long addMetadataRequestLogEntry(
        final Long documentId, final Date requestDate, final String appName,
        final String userCode, final String orgCode) {

        Long result = 0L;
        try {
            MetadataRequestLog logEntry = new MetadataRequestLog();
            logEntry.setDocumentId(documentId);
            logEntry.setRequestDate(requestDate);
            logEntry.setRemoteApplicationShortName(appName);
            logEntry.setUserCode(userCode);
            logEntry.setOrganizationCode(orgCode);
            result = this.metadataRequestLogDAO.save(logEntry);
        } catch (Exception ex) {
            // Do not throw exception if logging fails!
            logger.warn("Failed logging document/file download!", ex);
        }
        return result;
    }

    /**
     * Retrieves the request log DAO.
     *
     * @return {@link RequestLogDAO} object that is used for saving
     *         {@link RequestLog} entries to database
     */
    public final RequestLogDAO getRequestLogDAO() {
        return requestLogDAO;
    }

    /**
     * Sets the request log DAO.
     *
     * @param value
     *     {@link RequestLogDAO} object that will be used for saving
     *     {@link RequestLog} entries to database
     */
    public final void setRequestLogDAO(final RequestLogDAO value) {
        this.requestLogDAO = value;
    }

    /**
     * Adds a request log entry.
     *
     * @param requestLogEntry
     *     Log entry as {@link RequestLog} object.
     * @return ID of added log entry
     */
    public final Long addRequestLogEntry(final RequestLog requestLogEntry) {
        return this.requestLogDAO.save(requestLogEntry);
    }

    /**
     * Gets data access object for reading and writing error log data.
     *
     * @return
     *     Data access object for reading and writing error log data.
     */
    public final ErrorLogDAO getErrorLogDAO() {
        return errorLogDAO;
    }

    /**
     * Sets data access object for reading and writing error log data.
     *
     * @param value
     *     {@link ErrorLogDAO} object that will be used for saving
     *     {@link ErrorLog} entries to database
     */
    public final void setErrorLogDAO(final ErrorLogDAO value) {
        this.errorLogDAO = value;
    }

    /**
     * Gets data access object for reading and writing download
     * request log data.
     *
     * @return
     *     Data access object for reading and writing download
     *     request log data.
     */
    public final DownloadRequestLogDAO getDownloadRequestLogDAO() {
        return downloadRequestLogDAO;
    }

    /**
     * Sets data access object for reading and writing download
     * request log data.
     *
     * @param value
     *     {@link DownloadRequestLogDAO} object that will be used for saving
     *     {@link DownloadRequestLog} entries to database
     */
    public final void setDownloadRequestLogDAO(
        final DownloadRequestLogDAO value) {
        this.downloadRequestLogDAO = value;
    }

    /**
     * Gets data access object for reading and writing metadata
     * request log data.
     *
     * @return
     *     Data access object for reading and writing metadata
     *     request log data.
     */
    public final MetadataRequestLogDAO getMetadataRequestLogDAO() {
        return metadataRequestLogDAO;
    }

    /**
     * Sets data access object for reading and writing metadata
     * request log data.
     *
     * @param value
     *     {@link MetadataRequestLogDAO} object that will be used for saving
     *     {@link MetadataRequestLog} entries to database
     */
    public final void setMetadataRequestLogDAO(
        final MetadataRequestLogDAO value) {
        this.metadataRequestLogDAO = value;
    }
}
