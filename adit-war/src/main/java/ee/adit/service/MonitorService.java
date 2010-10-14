package ee.adit.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import dvk.api.ml.PojoMessage;
import dvk.api.ml.PojoSettings;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.monitor.MonitorResult;
import ee.adit.pojo.SaveDocumentRequest;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveDocumentRequestDocument;
import ee.adit.pojo.SaveDocumentResponse;
import ee.adit.util.Configuration;
import ee.adit.util.CustomMessageCallbackFactory;
import ee.adit.util.NagiosLogger;
import ee.adit.util.Util;
import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;
import ee.webmedia.xtee.client.service.StandardXTeeConsumer;
import ee.webmedia.xtee.client.service.XTeeAttachment;
import ee.webmedia.xtee.client.service.XTeeServiceConfiguration;

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
	private static final String ADIT_UK_CONNECTION_READ = "ADIT_UK_CONNECTION_READ";
	private static final String ADIT_UK_CONNECTION_WRITE = "ADIT_UK_CONNECTION_WRITE";
	
	private static final String ADIT_APP = "ADIT_APP";
	
	private static Logger LOG = Logger.getLogger(MonitorService.class);
	
	private DocumentDAO documentDAO; 
	
	private DvkDAO dvkDAO;
	
	private Configuration configuration;
	
	private NagiosLogger nagiosLogger;
	
	private DocumentService documentService;
	
	/**
	 * Marshaller - required to convert Java objects to XML.
	 */
	private Marshaller marshaller;

	/**
	 * Unmarshaller - required to convert XML to Java objects.
	 */
	private Unmarshaller unmarshaller;
	
	public void check() {
		LOG.info("ADIT monitor - Checking database and application.");
		
		checkApplication();
		
		checkDBConnection();
		checkDBRead(this.getConfiguration().getTestDocumentID());
		checkDBWrite(this.getConfiguration().getTestDocumentID());
		
		checkDVKconnection();
		checkDVKRead(this.getConfiguration().getDvkTestDocumentID());
		checkDVKWrite(this.getConfiguration().getDvkTestDocumentID());
		
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
				this.getNagiosLogger().log(ADIT_DB_CONNECTION + " " + OK + " " + df.format(duration) + " " + SECONDS);
				
			} catch(Exception e) {
				LOG.info("Error occurred while accessing ADIT database: ", e);
				String message = ADIT_DB_CONNECTION + " " + FAIL;
				this.getNagiosLogger().log(message, e);
				return;
			} finally {
				if(session != null && session.isOpen())
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
				String message = ADIT_DB_CONNECTION_READ + " " + FAIL + " ";
				this.getNagiosLogger().log(message, e);
				return;
			}
			
			DecimalFormat df = new DecimalFormat("0.000");
			this.getNagiosLogger().log(ADIT_DB_CONNECTION_READ + " " + OK + " " + df.format(duration) + " " + SECONDS);
			
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
					getDocumentService().save(document, getConfiguration().getGlobalDiskQuota());
				}
				
				Date end = new Date();
				long endTime = end.getTime();
				duration = (endTime - startTime) / 1000.0;
				
				
			} catch (Exception e) {
				LOG.info("Error occurred while accessing ADIT database WRITE function: ", e);
				String message = ADIT_DB_CONNECTION_WRITE + " " + FAIL + " ";
				this.getNagiosLogger().log(message, e);
				return;
			}
			
			DecimalFormat df = new DecimalFormat("0.000");
			this.getNagiosLogger().log(ADIT_DB_CONNECTION_WRITE + " " + OK + " " + df.format(duration) + " " + SECONDS);
			
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
		double duration = 0;
		DecimalFormat df = new DecimalFormat("0.000");
		
		Date start = new Date();
		long startTime = start.getTime();
		
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
		
		Date end = new Date();
		long endTime = end.getTime();
		duration = (endTime - startTime) / 1000.0;
		
		// Errors were detected
		if(errorMessages.size() > 0) {
			String combinedErrorMessage = "";
			for(int i = 0; i < errorMessages.size(); i++) {
				if(i != 0)
					combinedErrorMessage = combinedErrorMessage + ", ";
				combinedErrorMessage = combinedErrorMessage + errorMessages.get(i);
			}
			
			this.getNagiosLogger().log(ADIT_APP + " " + FAIL + " ", new Exception("Errors found: " + combinedErrorMessage));
		} else {
			this.getNagiosLogger().log(ADIT_APP + " " + OK + " " + df.format(duration) + " " + SECONDS);
		}
		
	}

	/**
	 * Check DVK database connectivity.
	 */
	public void checkDVKconnection() {
		Session session = null;
		double duration = 0;
		
		try {
			
			SessionFactory sessionFactory = this.getDvkDAO().getSessionFactory();
			
			try {
				
				Date start = new Date();
				long startTime = start.getTime();
				
				session = sessionFactory.openSession();
				session.close();
				
				Date end = new Date();
				long endTime = end.getTime();
				duration = (endTime - startTime) / 1000.0;
				
				DecimalFormat df = new DecimalFormat("0.000");
				this.getNagiosLogger().log(ADIT_UK_CONNECTION + " " + OK + " " + df.format(duration) + " " + SECONDS);
				
			} catch(Exception e) {
				LOG.info("Error occurred while accessing DVK database: ", e);
				String message = ADIT_UK_CONNECTION + " " + FAIL;
				this.getNagiosLogger().log(message, e);
				return;
			} finally {
				if(session != null && session.isOpen())
					session.close();
			}
			
		} catch(Exception e) {
			LOG.error("Error while checking DVK database connectivity: ", e);
		}
	}
	
	/**
	 * Check DVK database read.
	 */
	public void checkDVKRead(long messageDhlId) {
		LOG.info("ADIT monitor - Checking DVK database READ.");
		
		try {
			
			double duration = 0;
			
			try {
				Date start = new Date();
				long startTime = start.getTime();
				
				this.getDvkDAO().testRead(messageDhlId);
				
				Date end = new Date();
				long endTime = end.getTime();
				duration = (endTime - startTime) / 1000.0;
				
				
			} catch (Exception e) {
				LOG.info("Error occurred while accessing DVK database READ function: ", e);
				String message = ADIT_UK_CONNECTION_READ + " " + FAIL + " ";
				this.getNagiosLogger().log(message, e);
				return;
			}
			
			DecimalFormat df = new DecimalFormat("0.000");
			this.getNagiosLogger().log(ADIT_UK_CONNECTION_READ + " " + OK + " " + df.format(duration) + " " + SECONDS);
			
		} catch(Exception e) {
			LOG.error("Error while checking DVK database READ: ", e);
		}
	}
	
	/**
	 * Check DVK database read.
	 */
	public void checkDVKWrite(long messageDhlId) {
		LOG.info("ADIT monitor - Checking DVK WRITE.");
		
		try {
			
			double duration = 0;
			
			try {
				Date start = new Date();
				long startTime = start.getTime();
				
				PojoMessage document = this.getDvkDAO().getMessage(messageDhlId);
				
				if(document != null) {
					document.setTitle(Util.generateRandomID());
					this.getDvkDAO().updateDocument(document);
				}
				
				Date end = new Date();
				long endTime = end.getTime();
				duration = (endTime - startTime) / 1000.0;
				
				
			} catch (Exception e) {
				LOG.info("Error occurred while accessing DVK database WRITE function: ", e);
				String message = ADIT_UK_CONNECTION_WRITE + " " + FAIL + " ";
				this.getNagiosLogger().log(message, e);
				return;
			}
			
			DecimalFormat df = new DecimalFormat("0.000");
			this.getNagiosLogger().log(ADIT_UK_CONNECTION_WRITE + " " + OK + " " + df.format(duration) + " " + SECONDS);
			
		} catch(Exception e) {
			LOG.error("Error while checking DVK database READ: ", e);
		}
		
	}
	
	/**
	 * Tests "saveDocument" request.
	 * 
	 * @param documentID test document ID
	 * @return test result
	 */
	public MonitorResult saveDocumentCheck(Long documentID) {
		MonitorResult result = new MonitorResult();
		
		double duration = 0;
		Date start = new Date();
		long startTime = start.getTime();
		
		try {
			WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
			webServiceTemplate.setMarshaller(getMarshaller());
			webServiceTemplate.setUnmarshaller(getUnmarshaller());
			
			
			
			SaveDocumentRequest request = new SaveDocumentRequest();
			SaveDocumentRequestDocument document = new SaveDocumentRequestDocument();
			document.setHref("cid:document");
			request.setDocument(document);
			
			SaveDocumentRequestAttachment requestAttachment = new SaveDocumentRequestAttachment();
			requestAttachment.setDocumentType(DocumentService.DocType_Letter);
			requestAttachment.setId(documentID);		
			String newTitle = Util.dateToXMLDate(new Date());
			requestAttachment.setTitle(newTitle);
			
			// TODO: Add attachment
			SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
			SaajSoapMessage message = (SaajSoapMessage) messageFactory.createWebServiceMessage();
			
			// Write to temporary file
			String fileName = this.marshal(requestAttachment);
			LOG.debug("Request attachment marshalled to temporary file: '" + fileName + "'.");
			String base64zippedFile = Util.gzipAndBase64Encode(fileName, getConfiguration().getTempDir(), true);
			
			
			
			DataSource dataSource = new FileDataSource(base64zippedFile);
			DataHandler dataHandler = new DataHandler(dataSource);
			message.addAttachment("document", dataHandler);
			
			LOG.debug("Attachment added with id: 'document'");
			String uri = "http://localhost:7001/adit/service";
		
			//Object resultObject = webServiceTemplate.marshalSendAndReceive(uri, message);
			
			//LOG.debug("resultObject.class: " + resultObject.getClass());
			
			SimpleXTeeServiceConfiguration xTeeServiceConfiguration = new SimpleXTeeServiceConfiguration();
			xTeeServiceConfiguration.setDatabase("ametlikud-dokumendid");
			xTeeServiceConfiguration.setIdCode("EE00000000000");
			xTeeServiceConfiguration.setInstitution("10425769");
			xTeeServiceConfiguration.setMethod("saveDocument");
			xTeeServiceConfiguration.setVersion("v1");
			xTeeServiceConfiguration.setSecurityServer(uri);
			
			StandardXTeeConsumer standardXTeeConsumer = new StandardXTeeConsumer();
			standardXTeeConsumer.setWebServiceTemplate(webServiceTemplate);
			standardXTeeConsumer.setServiceConfiguration(xTeeServiceConfiguration);
			standardXTeeConsumer.setMsgCallbackFactory(new CustomMessageCallbackFactory());
			
			List<XTeeAttachment> attachments = new ArrayList<XTeeAttachment>();
			XTeeAttachment xTeeAttachment = new XTeeAttachment("document", "text/xml", Util.getBytesFromFile(new File(base64zippedFile)));
			attachments.add(xTeeAttachment);
			
			SaveDocumentResponse responseObject = (SaveDocumentResponse) standardXTeeConsumer.sendRequest(request, attachments);
			
			Date end = new Date();
			long endTime = end.getTime();
			duration = (endTime - startTime) / 1000.0;
			
			result.setDuration(duration);
			
		} catch(Exception e) {
			LOG.error("Error while testing 'saveDocument' request: ", e);
		}
		
		
		
		return result;
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

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
	
	/**
	 * Marshals the object to XML and stores the result in a temporary file.
	 * The location of the temporary file is specified by {@link Configuration}}
	 * 
	 * @param object the object to be marshalled.
	 * @return the absolute path to the temporary file created.
	 */
	public String marshal(Object object) {
		String result = null;
		FileOutputStream fos = null;
		try {
			// Create outputStream
			String tempFileName = Util.generateRandomFileName();
			String tempFileFullName = this.getConfiguration().getTempDir() + File.separator + tempFileName;
			fos = new FileOutputStream(tempFileFullName);
			StreamResult reponseObjectResult = new StreamResult(fos);
			
			// Marshal to output
			this.getMarshaller().marshal(object, reponseObjectResult);
			
			result = tempFileFullName;
		} catch (Exception e) {
			LOG.error("Error while marshalling object: " + object.getClass());
			LOG.error("Cause: ", e);
		} finally {
			Util.safeCloseStream(fos);
			fos = null;
		}
		
		return result;
	}

	public Marshaller getMarshaller() {
		return marshaller;
	}

	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}
}
