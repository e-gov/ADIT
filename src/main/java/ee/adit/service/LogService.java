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
	 * Log4J logger
	 */
	private static Logger LOG = Logger.getLogger(LogService.class);
	
	/**
	 * Error log level WARN
	 */
	public static final String ErrorLogLevel_Warn = "WARN";
	
	/**
	 * Error log level ERROR
	 */
	public static final String ErrorLogLevel_Error = "ERROR";
	
	/**
	 * Error log level FATAL
	 */
	public static final String ErrorLogLevel_Fatal = "FATAL";
	
	/**
	 * Request log Data Access Object
	 */
	private RequestLogDAO requestLogDAO;
	
	/**
	 * Error log Data Access Object
	 */
	private ErrorLogDAO errorLogDAO;
	
	/**
	 * Download log Data Access Object
	 */
	private DownloadRequestLogDAO downloadRequestLogDAO;
	
	/**
	 * Metadata log Data Access Object
	 */
	private MetadataRequestLogDAO metadataRequestLogDAO;
	
	/**
	 * Adds a record to request log.
	 * 
	 * @param requestName request name
	 * @param documentId document ID of the document associated with the request
	 * @param requestDate request invocation date
	 * @param remoteApplicationShortName name of the remote application that sent the request
	 * @param userCode code of the user who sent the request
	 * @param organizationCode the organization that sent the request
	 * @param additionalInformation additional information
	 * @return	ID of added log entry
	 */
	public Long addRequestLogEntry(
			String requestName,
			Long documentId,
			Date requestDate,
			String remoteApplicationShortName,
			String userCode,
			String organizationCode,
			String additionalInformation) {
		
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
	 * @param actionName the name of the action that caused the error 
	 * @param documentId document ID associated with the error
	 * @param errorDate the time when the error occurred
	 * @param remoteApplicationShortName remote application
	 * @param userCode code of the user who sent the request
	 * @param errorLevel error level
	 * @param errorMessage error message
	 * 
	 * @return log entry ID
	 */
	public Long addErrorLogEntry(
			String actionName,
			Long documentId,
			Date errorDate,
			String remoteApplicationShortName,
			String userCode,
			String errorLevel,
			String errorMessage) {
		
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
	 * @param documentId document ID associated with the request
	 * @param documentFileId document file ID associated with the request
	 * @param requestDate request invocation date
	 * @param appName remote application name
	 * @param userCode code of the user who sent the request
	 * @param orgCode the code of the organization that sent the request
	 * @return log entry ID
	 */
	public Long addDownloadRequestLogEntry(
			Long documentId,
			Long documentFileId,
			Date requestDate,
			String appName,
			String userCode,
			String orgCode) {
		
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
			LOG.warn("Failed logging document/file download!", ex);
		}
		return result;
	}
	
	/**
	 * Adds a record to the metadata log.
	 * 
	 * @param documentId document ID associated with the request
	 * @param requestDate request invocation date
	 * @param appName remote application name
	 * @param userCode code of the user who sent the request
	 * @param orgCode the code of the organization that sent the request
	 * @return log entry ID
	 */
	public Long addMetadataRequestLogEntry(
			Long documentId,
			Date requestDate,
			String appName,
			String userCode,
			String orgCode) {
		
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
			LOG.warn("Failed logging document/file download!", ex);
		}
		return result;
	}

	/**
	 * Retrieves the request log DAO
	 * @return	{@link RequestLogDAO} object that is used for saving {@link RequestLog} entries to database
	 */
	public RequestLogDAO getRequestLogDAO() {
		return requestLogDAO;
	}

	/**
	 * Sets the request log DAO
	 * @param requestLogDAO	{@link RequestLogDAO} object that will be used for saving {@link RequestLog} entries to database
	 */
	public void setRequestLogDAO(RequestLogDAO requestLogDAO) {
		this.requestLogDAO = requestLogDAO;
	}
	
	/**
	 * Adds a request log entry,
	 * 
	 * @param requestLogEntry	Log entry as {@link RequestLog} object.
	 * @return	ID of added log entry
	 */
	public Long addRequestLogEntry(RequestLog requestLogEntry) {
		return this.requestLogDAO.save(requestLogEntry);
	}
	
	/**
	 * Retrieves the error log DAO
	 * @return	{@link ErrorLogDAO} object that is used for saving {@link ErrorLog} entries to database
	 */
	public ErrorLogDAO getErrorLogDAO() {
		return errorLogDAO;
	}

	/**
	 * Sets the error log DAO
	 * @param errorLogDAO	{@link ErrorLogDAO} object that will be used for saving {@link ErrorLog} entries to database
	 */
	public void setErrorLogDAO(ErrorLogDAO errorLogDAO) {
		this.errorLogDAO = errorLogDAO;
	}

	/**
	 * Retrieves the download log DAO
	 * @return	{@link DownloadRequestLogDAO} object that is used for saving
	 * 			{@link DownloadRequestLog} entries to database
	 */
	public DownloadRequestLogDAO getDownloadRequestLogDAO() {
		return downloadRequestLogDAO;
	}
	
	/**
	 * Sets the download log DAO
	 * @param downloadRequestLogDAO	{@link DownloadRequestLogDAO} object that will be used for saving
	 * 								{@link DownloadRequestLog} entries to database
	 */
	public void setDownloadRequestLogDAO(DownloadRequestLogDAO downloadRequestLogDAO) {
		this.downloadRequestLogDAO = downloadRequestLogDAO;
	}

	/**
	 * Retrieves the metadata log DAO
	 * @return	{@link MetadataRequestLogDAO} object that is used for saving
	 * 			{@link MetadataRequestLog} entries to database
	 */
	public MetadataRequestLogDAO getMetadataRequestLogDAO() {
		return metadataRequestLogDAO;
	}

	/**
	 * Sets the metadata log DAO
	 * @param metadataRequestLogDAO	{@link MetadataRequestLogDAO} object that will be used for saving
	 * 								{@link MetadataRequestLog} entries to database
	 */
	public void setMetadataRequestLogDAO(MetadataRequestLogDAO metadataRequestLogDAO) {
		this.metadataRequestLogDAO = metadataRequestLogDAO;
	}
}
