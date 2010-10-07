package ee.adit.service;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.util.Configuration;
import ee.adit.util.NagiosLogger;
import ee.adit.util.Util;

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
	private static final String ADIT_DB_CONNECTION_READ = "ADIT_DB_CONNECTION_READ";
	private static final String ADIT_DB_CONNECTION_WRITE = "ADIT_DB_CONNECTION_WRITE";
	
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
		checkDBWrite(this.getConfiguration().getTestDocumentID());
		
	}
	
	/**
	 * Check database connectivity.
	 */
	public void checkDBConnection() {
		
		Session session = null;
		double duration = 0;
		
		try {
			
			SessionFactory sessionFactory = this.getDocumentDAO().getSessionFactory();
			
			try {
				
				Date start = new Date();
				long startTime = start.getTime();
				
				session = sessionFactory.openSession();
				session.close();
				
				Date end = new Date();
				long endTime = end.getTime();
				duration = (endTime - startTime) / 1000.0;
				
				DecimalFormat df = new DecimalFormat("0.000");
				this.getNagiosLogger().log(ADIT_DB_CONNECTION + " " + OK + " " + df.format(duration) + SECONDS);
				
			} catch(Exception e) {
				LOG.info("Error occurred while accessing ADIT database: ", e);
				String message = ADIT_DB_CONNECTION + " " + FAIL;
				this.getNagiosLogger().log(message, e);
				return;
			} finally {
				if(session != null)
					session.close();
			}
			
		} catch(Exception e) {
			LOG.error("Error while checking database connectivity: ", e);
		}
		
	}
	
	/**
	 * Check database read.
	 */
	public void checkDBRead(long documentID) {
		LOG.info("ADIT monitor - Checking database READ.");
		
		try {
			
			double duration = 0;
			
			try {
				Date start = new Date();
				long startTime = start.getTime();
				
				getDocumentDAO().getDocument(documentID);
				
				Date end = new Date();
				long endTime = end.getTime();
				duration = (endTime - startTime) / 1000.0;
				
				
			} catch (Exception e) {
				LOG.info("Error occurred while accessing ADIT database READ function: ", e);
				String message = ADIT_DB_CONNECTION_READ + " " + FAIL;
				this.getNagiosLogger().log(message, e);
				return;
			}
			
			DecimalFormat df = new DecimalFormat("0.000");
			this.getNagiosLogger().log(ADIT_DB_CONNECTION_READ + " " + OK + " " + df.format(duration) + SECONDS);
			
		} catch(Exception e) {
			LOG.error("Error while checking database READ: ", e);
		}
		
	}
	
	/**
	 * Check database write. 
	 */
	public void checkDBWrite(long documentID) {
		LOG.info("ADIT monitor - Checking database WRITE.");
		
		try {
			
			double duration = 0;
			
			try {
				Date start = new Date();
				long startTime = start.getTime();
				
				Document document = getDocumentDAO().getDocument(documentID);
				
				if(document != null) {
					document.setLastModifiedDate(new Date());
					getDocumentDAO().update(document);
				}
				
				Date end = new Date();
				long endTime = end.getTime();
				duration = (endTime - startTime) / 1000.0;
				
				
			} catch (Exception e) {
				LOG.info("Error occurred while accessing ADIT database WRITE function: ", e);
				String message = ADIT_DB_CONNECTION_WRITE + " " + FAIL;
				this.getNagiosLogger().log(message, e);
				return;
			}
			
			DecimalFormat df = new DecimalFormat("0.000");
			this.getNagiosLogger().log(ADIT_DB_CONNECTION_WRITE + " " + OK + " " + df.format(duration) + SECONDS);
			
		} catch(Exception e) {
			LOG.error("Error while checking database READ: ", e);
		}
	}
	
	/**
	 * Check application configuration parameters.
	 */
	public void checkApplication() {
		LOG.info("ADIT monitor - Checking application.");
		List<String> errorMessages = new ArrayList<String>();
		
		// 1. Check temporary folder
		try {
			
			String tempDir = this.getConfiguration().getTempDir();
			File tempDirFile = new File(tempDir);
			String randomFileName = Util.generateRandomFileName();
			File temporaryFile = new File(tempDirFile.getAbsolutePath() + File.separator + randomFileName);
			temporaryFile.createNewFile();		
			
		} catch (Exception e) {
			LOG.error("Error checking application - temporary directory not defined or not writable: ", e);
			errorMessages.add("Error checking application - temporary directory not defined or not writable: " + e.getMessage());
		}
		
		// 2. Check DVK response message stylesheet
		try {
			
			String styleSheet = this.getConfiguration().getDvkResponseMessageStylesheet();
			File styleSheetFile = new File(styleSheet);
			if(!styleSheetFile.exists()) {
				throw new Exception("File does not exist: " + styleSheet);
			}
			
		} catch (Exception e) {
			LOG.error("Error checking application - DVK response message stylesheet not defined or file does not exist: ", e);
			errorMessages.add("Error checking application - DVK response message stylesheet not defined or file does not exist: " + e.getMessage());
		}
		
		// 3. Check test document ID
		try {
			
			Long testDocumentID = this.getConfiguration().getTestDocumentID();
			if(testDocumentID == null) {
				throw new Exception("Test document ID not defined.");
			}
			
		} catch (Exception e) {
			LOG.error("Error checking application - test document ID not defined.");
			errorMessages.add("Error checking application - test document ID not defined.");
		}
		
		// Errors were detected
		if(errorMessages.size() > 0) {
			String combinedErrorMessage = "";
			for(int i = 0; i < errorMessages.size(); i++) {
				if(i != 0)
					combinedErrorMessage = combinedErrorMessage + ", ";
				combinedErrorMessage = combinedErrorMessage + errorMessages.get(i);
			}
			
			this.getNagiosLogger().log(ADIT_APP + " " + FAIL + " " + "Errors found: " + combinedErrorMessage);
		}
		
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
