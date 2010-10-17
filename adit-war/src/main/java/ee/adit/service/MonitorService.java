package ee.adit.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.xml.sax.InputSource;

import dvk.api.ml.PojoMessage;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentSharingDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditInternalException;
import ee.adit.monitor.MonitorResult;
import ee.adit.pojo.ArrayOfMessageMonitor;
import ee.adit.pojo.GetDocumentRequest;
import ee.adit.pojo.GetDocumentResponse;
import ee.adit.pojo.GetDocumentResponseMonitor;
import ee.adit.pojo.OutputDocument;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.SaveDocumentRequest;
import ee.adit.pojo.SaveDocumentRequestAttachment;
import ee.adit.pojo.SaveDocumentRequestDocument;
import ee.adit.pojo.SaveDocumentResponseMonitor;
import ee.adit.util.Configuration;
import ee.adit.util.CustomClientInterceptor;
import ee.adit.util.CustomMessageCallbackFactory;
import ee.adit.util.CustomXTeeConsumer;
import ee.adit.util.CustomXTeeServiceConfiguration;
import ee.adit.util.MonitorConfiguration;
import ee.adit.util.NagiosLogger;
import ee.adit.util.Util;
import ee.webmedia.xtee.client.service.XTeeAttachment;

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

	public static final String OK = "OK";
	public static final String FAIL = "FAIL";
	public static final String MS = "ms";
	public static final String SECONDS = "seconds";
	
	private static final String ADIT_DB_CONNECTION = "ADIT_DB_CONNECTION";
	private static final String ADIT_DB_CONNECTION_READ = "ADIT_DB_CONNECTION_READ";
	private static final String ADIT_DB_CONNECTION_WRITE = "ADIT_DB_CONNECTION_WRITE";
	
	private static final String ADIT_UK_CONNECTION = "ADIT_UK_CONNECTION";
	private static final String ADIT_UK_CONNECTION_READ = "ADIT_UK_CONNECTION_READ";
	private static final String ADIT_UK_CONNECTION_WRITE = "ADIT_UK_CONNECTION_WRITE";
	
	private static final String ADIT_APP = "ADIT_APP";
	
	private static Logger LOG = Logger.getLogger(MonitorService.class);
	
	private DocumentDAO documentDAO; 
	
	private DocumentSharingDAO documentSharingDAO;
	
	private DvkDAO dvkDAO;
	
	private Configuration configuration;
	
	private NagiosLogger nagiosLogger;
	
	private DocumentService documentService;
	
	private MonitorConfiguration monitorConfiguration;
	
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
		checkDBRead(this.getMonitorConfiguration().getTestDocumentId());
		checkDBWrite(this.getMonitorConfiguration().getTestDocumentId());
		
		checkDVKconnection();
		checkDVKRead(this.getMonitorConfiguration().getDvkTestDocumentID());
		checkDVKWrite(this.getMonitorConfiguration().getDvkTestDocumentID());
		
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
			
			Long testDocumentID = this.getMonitorConfiguration().getTestDocumentId();
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
	 * @return test result
	 */
	public MonitorResult saveDocumentCheck() {
		MonitorResult result = new MonitorResult();
		result.setComponent("SAVE_DOCUMENT");
		
		LOG.info("Testing 'saveDocument' request...");
		
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
			requestAttachment.setId(this.getMonitorConfiguration().getTestDocumentId());		
			String newTitle = Util.dateToXMLDate(new Date());
			requestAttachment.setTitle(newTitle);
			
			List<OutputDocumentFile> files = new ArrayList<OutputDocumentFile>();
			
			String tmpFileName = Util.createTemporaryFile(new ByteArrayInputStream(newTitle.getBytes("UTF-8")), getConfiguration().getTempDir());
			
			File tmpFile = new File(tmpFileName);
			
			OutputDocumentFile file = new OutputDocumentFile();
			file.setId(this.getMonitorConfiguration().getTestDocumentFileId());
			file.setContentType("text/plain");
			file.setName("test.txt");
			file.setSizeBytes(tmpFile.length());
			file.setSysTempFile(tmpFileName);
			
			files.add(file);
			requestAttachment.setFiles(files);
			
			//SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
			//SaajSoapMessage message = (SaajSoapMessage) messageFactory.createWebServiceMessage();
			
			// Write to temporary file
			String fileName = this.marshal(requestAttachment);
			LOG.debug("Request attachment marshalled to temporary file: '" + fileName + "'.");
			String base64zippedFile = Util.gzipAndBase64Encode(fileName, getConfiguration().getTempDir(), true);			
			
			//DataSource dataSource = new FileDataSource(base64zippedFile);
			//DataHandler dataHandler = new DataHandler(dataSource);
			//message.addAttachment("document", dataHandler);
			
			LOG.debug("Attachment added with id: 'document'");
			
			CustomXTeeServiceConfiguration xTeeServiceConfiguration = new CustomXTeeServiceConfiguration();
			xTeeServiceConfiguration.setDatabase("ametlikud-dokumendid");
			xTeeServiceConfiguration.setIdCode(this.getMonitorConfiguration().getUserCode());
			xTeeServiceConfiguration.setInstitution(this.getMonitorConfiguration().getInstitutionCode());
			xTeeServiceConfiguration.setMethod("saveDocument");
			xTeeServiceConfiguration.setVersion("v1");
			xTeeServiceConfiguration.setSecurityServer(this.getMonitorConfiguration().getAditServiceUrl());
			xTeeServiceConfiguration.setInfosysteem(this.getMonitorConfiguration().getRemoteApplicationShortName());
			
			CustomXTeeConsumer customXTeeConsumer = new CustomXTeeConsumer();
			customXTeeConsumer.setWebServiceTemplate(webServiceTemplate);
			customXTeeConsumer.setServiceConfiguration(xTeeServiceConfiguration);
			customXTeeConsumer.setMsgCallbackFactory(new CustomMessageCallbackFactory());
			
			List<XTeeAttachment> attachments = new ArrayList<XTeeAttachment>();
			XTeeAttachment xTeeAttachment = new XTeeAttachment("document", "text/xml", Util.getBytesFromFile(new File(base64zippedFile)));
			attachments.add(xTeeAttachment);
			
			SaveDocumentResponseMonitor response = (SaveDocumentResponseMonitor) customXTeeConsumer.sendRequest(request, attachments);
			
			LOG.info("response.success: " + response.getKeha().getSuccess());
			LOG.info("response.documentId: " + response.getKeha().getDocumentId());
			
			Date end = new Date();
			long endTime = end.getTime();
			duration = (endTime - startTime) / 1000.0;
			
			// Populate result
			result.setDuration(duration);
			result.setSuccess(response.getKeha().getSuccess());
			
			if(!result.isSuccess()) {
				ArrayOfMessageMonitor messages = response.getKeha().getMessages();
				result.setExceptions(messages.getMessage());
			}
			
			
		} catch(Exception e) {
			LOG.error("Error while testing SAVE_DOCUMENT: ", e);
			
			result.setSuccess(false);
			List<String> exceptions = new ArrayList<String>();
			exceptions.add(e.getMessage());
			result.setExceptions(exceptions);
			
		}
		
		return result;
	}
	
	/**
	 * Tests "getDocument" request.
	 * 
	 * @return test result
	 */
	public MonitorResult getDocumentCheck() {
		MonitorResult result = new MonitorResult();
		result.setComponent("GET_DOCUMENT");
		
		LOG.info("Testing 'getDocument' request...");
		
		double duration = 0;
		boolean success = false;
		Date start = new Date();
		long startTime = start.getTime();
		
		try {
			WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
			webServiceTemplate.setMarshaller(getMarshaller());
			webServiceTemplate.setUnmarshaller(getUnmarshaller());
			CustomClientInterceptor interceptor = new CustomClientInterceptor();
			interceptor.setConfiguration(getConfiguration());
			webServiceTemplate.setInterceptors(new ClientInterceptor[] { interceptor });
			
			GetDocumentRequest request = new GetDocumentRequest();
			request.setDocumentId(this.getMonitorConfiguration().getTestDocumentId());
			request.setIncludeFileContents(true);
			
			SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
			SaajSoapMessage message = (SaajSoapMessage) messageFactory.createWebServiceMessage();

			CustomXTeeServiceConfiguration xTeeServiceConfiguration = new CustomXTeeServiceConfiguration();
			xTeeServiceConfiguration.setDatabase("ametlikud-dokumendid");
			xTeeServiceConfiguration.setIdCode(this.getMonitorConfiguration().getUserCode());
			xTeeServiceConfiguration.setInstitution(this.getMonitorConfiguration().getInstitutionCode());
			xTeeServiceConfiguration.setMethod("getDocument");
			xTeeServiceConfiguration.setVersion("v1");
			xTeeServiceConfiguration.setSecurityServer(this.getMonitorConfiguration().getAditServiceUrl());
			xTeeServiceConfiguration.setInfosysteem(this.getMonitorConfiguration().getRemoteApplicationShortName());
			
			CustomXTeeConsumer customXTeeConsumer = new CustomXTeeConsumer();
			customXTeeConsumer.setWebServiceTemplate(webServiceTemplate);
			customXTeeConsumer.setServiceConfiguration(xTeeServiceConfiguration);
			customXTeeConsumer.setMsgCallbackFactory(new CustomMessageCallbackFactory());
			
			GetDocumentResponseMonitor response = (GetDocumentResponseMonitor) customXTeeConsumer.sendRequest(request);
			
			if(response != null) {
				if(!response.isSuccess()) {
					String responseErrorMessage = null;
					if(response.getMessages() != null && response.getMessages().getMessage() != null && response.getMessages().getMessage().size() > 0) {
						responseErrorMessage = response.getMessages().getMessage().get(0);
					}
					throw new AditInternalException("The 'getDocument' request was not successful: " + responseErrorMessage);
				}
			} else {
				throw new AditInternalException("The 'getDocument' request was not successful: response could not be unmarshalled: unmarshalling returned null.");
			}
			
			OutputDocument document = null;
			
			if(interceptor.getTmpFile() != null) {
				LOG.info("Attachment saved to temporary file: " + interceptor.getTmpFile());
				Source unmarshalSource = new SAXSource(new InputSource(new FileInputStream(interceptor.getTmpFile())));
				document = (OutputDocument) getUnmarshaller().unmarshal(unmarshalSource);
			} else {
				throw new AditInternalException("Response message interceptor could not extract the attachment.");
			}
			
			if(document != null) {
				Date documentLastChangedDateTitle = Util.xmlDateToDate(document.getTitle());				
				File docTmpFile = new File(interceptor.getTmpFile());
				String tmpFileContents = Util.getFileContents(docTmpFile);
				
				int startIndex = tmpFileContents.indexOf("<data>");
				int endIndex = tmpFileContents.indexOf("</data>");
				
				String base64fileContent = tmpFileContents.substring(startIndex + "<data>".length(), endIndex);
				String fileContent = Util.base64decode(base64fileContent);
				Date documentLastChangedDateContents = Util.xmlDateToDate(fileContent);
				
				LOG.info("Document title date: " + documentLastChangedDateTitle);
				LOG.info("Document contents date: " + documentLastChangedDateContents);
				
				if(documentLastChangedDateTitle.equals(documentLastChangedDateContents)) {
					LOG.info("Document title and contents datetime match.");
					
					// Check if document was indeed changed during the last 'saveDocument' request
					Long docSaveInterval = getMonitorConfiguration().getDocumentSaveInterval();
					
					long lastChangedTitleMs = documentLastChangedDateTitle.getTime();
					long currentTimeMs = (new Date()).getTime() - docSaveInterval;
					
					if(lastChangedTitleMs > currentTimeMs) {
						LOG.info("The document was changed during the last 'saveDocument' request.");
						success = true;
					} else {
						throw new AditInternalException("The document was not changed during the last 'saveDocument' request.");
					}
					
				} else {
					throw new AditInternalException("Document title and document file content datetime do not match.");
				}
			} else {
				throw new AditInternalException("Response message document not initialized.");
			}
			
			Date end = new Date();
			long endTime = end.getTime();
			duration = (endTime - startTime) / 1000.0;
			
			// Populate result
			result.setDuration(duration);
			result.setSuccess(success);			
			
		} catch(Exception e) {
			LOG.error("Error while testing GET_DOCUMENT: ", e);
			result.setSuccess(false);
			List<String> exceptions = new ArrayList<String>();
			exceptions.add(e.getMessage());
			result.setExceptions(exceptions);
		}
		
		return result;
		
	}
	
	/**
	 * Tests if documents are sent to DVK client.
	 * 
	 * @return test result
	 */
	public MonitorResult checkDvkSend(){
		MonitorResult result = new MonitorResult();
		result.setComponent("ADIT_TO_DVK_CLIENT_SEND");
		
		LOG.info("Testing ADIT_TO_DVK_CLIENT_SEND...");
		
		double duration = 0;
		boolean success = false;
		Date start = new Date();
		long startTime = start.getTime();
		
		/*
		 * 1. Query ADIT database for all documents that are meant for sending to DVK client.
		 *    If there are messages that are not sent to DVK in the specified period, then the 
		 *    connection is broken.
		 */
		
		// 1. Query ADIT database
		try {	
			long comparisonDateMs = (new Date()).getTime();
			comparisonDateMs = comparisonDateMs - getMonitorConfiguration().getDocumentSendToDvkInterval();
			Date comparisonDate = new Date(comparisonDateMs);
			
			List<DocumentSharing> documentSharings = getDocumentSharingDAO().getDVKSharings(comparisonDate);
			
			if(documentSharings != null && documentSharings.size() > 0) {
				// DVK connection down - documents were found, that have not been sent to DVK in time				
				throw new AditInternalException("Number of documents not sent to DVK client in time: " + documentSharings.size());
			} else {
				LOG.debug("No document sharings found for DVK (with status 'missing' - 100)");
				result.setSuccess(true);
			}
			
			Date end = new Date();
			long endTime = end.getTime();
			duration = (endTime - startTime) / 1000.0;
			result.setDuration(duration);
			
		} catch(Exception e) {
			LOG.error("Error while testing ADIT_TO_DVK_CLIENT_SEND: ", e);
			result.setSuccess(false);
			List<String> exceptions = new ArrayList<String>();
			exceptions.add(e.getMessage());
			result.setExceptions(exceptions);
		}
		
		return result;
	}
	
	/**
	 * Checks if documents are being sent from DVK UK to DVK.
	 * 
	 * @return
	 */
	/*public MonitorResult checkDvkClientToDvkSend() {
		MonitorResult result = new MonitorResult();
		result.setComponent("DVK_CLIENT_TO_DVK_SEND");
		
		double duration = 0;
		Date start = new Date();
		long startTime = start.getTime();
		
		try {
			
			// Check if there are documents that are not being sent to DVK server
			// That can not be checked because DVK client does not record the time
			// when the message was created.
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			
			Calendar cal = new GregorianCalendar();
			Date currentDate = cal.getTime();
			int day = cal.get(Calendar.DATE);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);
			
			Date beginDate = sdf.parse((day - 1) + "." + month + "." + year + " 00:00:00");
			Date endDate = sdf.parse((day - 1) + "." + month + "." + year + " 23:59:59");
			
			// 1. Get the number of messages sent to DVK client from ADIT yesterday.
			long documentSharingCount = getDocumentSharingDAO().getDocumentsSentToDvk(beginDate, endDate);

			// 2. Get the number of messages sent to DVK server from DVK client yesterday.
			long pojoMessageCount = getDvkDAO().getSentDocuments(beginDate, endDate);
			
			if(documentSharingCount == pojoMessageCount) {
				LOG.info("Number of documents sent from ADIT -> DVK UK matches the number of documents sent from DVK UK -> DVK");
				result.setSuccess(true);
			} else {
				throw new AditInternalException("Number of documents sent from ADIT -> DVK UK does not match the number of documents sent from DVK UK -> DVK: " + documentSharingCount + "/" + pojoMessageCount + " (ADIT->DVKUK / DVKUK->DVK)");
			}
			
			Date end = new Date();
			long endTime = end.getTime();
			duration = (endTime - startTime) / 1000.0;
			result.setDuration(duration);
			
		} catch(Exception e) {
			LOG.error("Error while testing DVK_CLIENT_TO_DVK_SEND: ", e);
			result.setSuccess(false);
			List<String> exceptions = new ArrayList<String>();
			exceptions.add(e.getMessage());
			result.setExceptions(exceptions);
		}
		
		return result;
	}*/
	
	@Transactional
	public MonitorResult checkDvkReceive() {
		MonitorResult result = new MonitorResult();
		result.setComponent("DVK_RECEIVE");
		
		double duration = 0;
		int failCount = 0;
		Date start = new Date();
		long startTime = start.getTime();
		
		try {
			
			// Check if there are documents in the DVK client database that are
			// not read into ADIT database.
			long currentDateMs = (new Date()).getTime();
			Date comparisonDate = new Date(currentDateMs - getMonitorConfiguration().getDocumentSendToAditInterval());
			//  from PojoMessage where incoming = true and recipientStatusId = 105 and receivedDate <= :comparisonDate 
			List<PojoMessage> receivedMessages = getDvkDAO().getReceivedDocuments(comparisonDate);
			
			
			for(int i = 0; i < receivedMessages.size(); i++) {
				try {
					PojoMessage message = receivedMessages.get(i);
					
					Document document = getDocumentDAO().getDocumentByDVKID(message.getDhlId());
					
					if(document == null) {
						// Document has not reached ADIT
						failCount++;
					}
				} catch(Exception e) {
					LOG.error("Could not check document received from DVK: ", e);
				}
			}
			
			if(failCount > 0) {
				throw new AditInternalException("Some document have not been transfered to ADIT. Number of failed documents: " + failCount);
			}
				
				
			Date end = new Date();
			long endTime = end.getTime();
			duration = (endTime - startTime) / 1000.0;
			result.setDuration(duration);
			
		} catch(Exception e) {
			LOG.error("Error while testing DVK_RECEIVE: ", e);
			result.setSuccess(false);
			List<String> exceptions = new ArrayList<String>();
			exceptions.add(e.getMessage());
			result.setExceptions(exceptions);
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

	public MonitorConfiguration getMonitorConfiguration() {
		return monitorConfiguration;
	}

	public void setMonitorConfiguration(MonitorConfiguration monitorConfiguration) {
		this.monitorConfiguration = monitorConfiguration;
	}

	public DocumentSharingDAO getDocumentSharingDAO() {
		return documentSharingDAO;
	}

	public void setDocumentSharingDAO(DocumentSharingDAO documentSharingDAO) {
		this.documentSharingDAO = documentSharingDAO;
	}
}
