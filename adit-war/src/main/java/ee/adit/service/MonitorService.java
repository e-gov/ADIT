package ee.adit.service;

import java.util.Date;

import org.apache.log4j.Logger;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.util.Configuration;
import ee.adit.util.NagiosLogger;

/**
 * 
 * Provides monitoring services. Monitoring consist of the following:
 * 
 * 1. Check database read
 * 2. Check database write
 * 3. Check application settings (temporary folder)
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * 
 */
public class MonitorService {

	private static final String OK = "OK";
	private static final String FAIL = "FAIL";
	private static final String MS = "ms";
	private static final String SECONDS = "seconds";
	
	private static final String ADIT_DB_CONNECTION = "ADIT_DB_CONNECTION";
	private static final String ADIT_UK_CONNECTION = "ADIT_UK_CONNECTION";
	private static final String ADIT_APP = "ADIT_APP";
	
	private static Logger LOG = Logger.getLogger(MonitorService.class);
	
	private DocumentDAO documentDAO; 
	
	private DvkDAO dvkDAO;
	
	private Configuration configuration;
	
	private NagiosLogger nagiosLogger;
	
	public void check() {
		LOG.info("ADIT monitor - Checking database and application.");
		
		checkApplication();
		
		checkDBRead(this.getConfiguration().getTestDocumentID());
		checkDBWrite();
		
	}
	
	/**
	 * Check if ADIT database read is successful.
	 */
	public void checkDBRead(long documentID) {
		LOG.info("ADIT monitor - Checking database READ.");
		
		try {
			
			double duration = 0;
			long duration_ms = 0;
			
			try {
				Date start = new Date();
				long startTime = start.getTime();
				
				getDocumentDAO().getDocument(documentID);
				
				Date end = new Date();
				long endTime = end.getTime();
				duration_ms = endTime - startTime;
				duration = (endTime - startTime) / 1000;
				
			} catch (Exception e) {
				LOG.info("Error occurred while accessing ADIT database READ function: ", e);
				String message = ADIT_DB_CONNECTION + " " + FAIL;
				this.getNagiosLogger().log(message, e);
				return;
			}
			
			this.getNagiosLogger().log(ADIT_DB_CONNECTION + " " + OK + " " + duration + " (" + duration_ms + " " + MS + ") " + SECONDS);
			
		} catch(Exception e) {
			LOG.error("Error while checking database READ: ", e);
		}
		
		
	}
	
	/**
	 * Check if ADIT database write is successful. 
	 */
	public void checkDBWrite() {
		LOG.info("ADIT monitor - Checking database WRITE.");
	}
	
	/**
	 * Check application configuration parameters.
	 */
	public void checkApplication() {
		LOG.info("ADIT monitor - Checking application.");
	}

	/**
	 * Handle application exception.
	 */
	public void handleException(Exception e) {
		LOG.info("ADIT monitor - Handling exception: " + e.getMessage());
	}
	
	public DocumentDAO getDocumentDAO() {
		return documentDAO;
	}

	public void setDocumentDAO(DocumentDAO documentDAO) {
		this.documentDAO = documentDAO;
	}

	public DvkDAO getDvkDAO() {
		return dvkDAO;
	}

	public void setDvkDAO(DvkDAO dvkDAO) {
		this.dvkDAO = dvkDAO;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public NagiosLogger getNagiosLogger() {
		return nagiosLogger;
	}

	public void setNagiosLogger(NagiosLogger nagiosLogger) {
		this.nagiosLogger = nagiosLogger;
	}
	
}
