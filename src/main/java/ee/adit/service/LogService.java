package ee.adit.service;

import java.util.Date;

import org.apache.log4j.Logger;

import ee.adit.dao.DownloadRequestLogDAO;
import ee.adit.dao.ErrorLogDAO;
import ee.adit.dao.RequestLogDAO;
import ee.adit.dao.pojo.DownloadRequestLog;
import ee.adit.dao.pojo.ErrorLog;
import ee.adit.dao.pojo.RequestLog;

public class LogService {
	private static Logger LOG = Logger.getLogger(LogService.class);
	
	public static final String ErrorLogLevel_Warn = "WARN";
	public static final String ErrorLogLevel_Error = "ERROR";
	public static final String ErrorLogLevel_Fatal = "FATAL";
	
	private RequestLogDAO requestLogDAO;
	private ErrorLogDAO errorLogDAO;
	private DownloadRequestLogDAO downloadRequestLogDAO;
	
	public RequestLogDAO getRequestLogDAO() {
		return requestLogDAO;
	}

	public void setRequestLogDAO(RequestLogDAO requestLogDAO) {
		this.requestLogDAO = requestLogDAO;
	}
	
	public Long addRequestLogEntry(RequestLog requestLogEntry) {
		return this.requestLogDAO.save(requestLogEntry);
	}
	
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
	
	public ErrorLogDAO getErrorLogDAO() {
		return errorLogDAO;
	}

	public void setErrorLogDAO(ErrorLogDAO errorLogDAO) {
		this.errorLogDAO = errorLogDAO;
	}

	public DownloadRequestLogDAO getDownloadRequestLogDAO() {
		return downloadRequestLogDAO;
	}

	public void setDownloadRequestLogDAO(DownloadRequestLogDAO downloadRequestLogDAO) {
		this.downloadRequestLogDAO = downloadRequestLogDAO;
	}
		
	
}
