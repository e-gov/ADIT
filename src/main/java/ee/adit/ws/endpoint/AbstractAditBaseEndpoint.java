package ee.adit.ws.endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.util.Configuration;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.XRoadQueryName;
import ee.adit.util.Util;

/**
 * Base class for web-service endpoints. Wraps XML marshalling / unmarshalling. 
 * Provides methods for request logging and SOAP attacments.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public abstract class AbstractAditBaseEndpoint extends XteeCustomEndpoint {

	/**
	 * Log4J logger.
	 */
	private static Logger LOG = Logger.getLogger(AbstractAditBaseEndpoint.class);
	
	/**
	 * X-Tee header.
	 */
	private CustomXTeeHeader header;

	/**
	 * Marshaller - required to convert Java objects to XML.
	 */
	private Marshaller marshaller;

	/**
	 * Unmarshaller - required to convert XML to Java objects.
	 */
	private Unmarshaller unmarshaller;

	/**
	 * TODO: remove - all messages should come from one place - MessageService.
	 * Message source - provides locale based (error) messages.
	 */
	private MessageSource messageSource;

	/**
	 * Configuration.
	 */
	private Configuration configuration;
	
	/**
	 * Logging service.
	 */
	private LogService logService;
	
	/**
	 * MessageService.
	 */
	private MessageService messageService;

	/**
	 * Unmarshals the request element and calls the endpoint implementation class.
	 * The response object returned by the implementing class is marshalled back to XML.
	 * 
	 * @param requestKeha request body element 
	 * @param responseElement response body element
	 * @param xteeHeader X-Tee header
	 * @throws Exception
	 */
	@Override
	protected void invokeInternal(Document requestKeha, Element responseElement,
			CustomXTeeHeader xteeHeader) throws Exception {
		
		LOG.debug("AbstractAditBaseEndpoint invoked");
		Object responseObject = null;
		String requestName = null;
		int version = 1;
		
		if (requestKeha == null) {
			throw new Exception("Failed unmarshalling request because request body is null!");
		}
		
		try {
			// Set the header as a property
			this.setHeader(xteeHeader);
			
			
			if(!this.isMetaService()) {
				// Check request version
				if(xteeHeader.getNimi() == null) {
					throw new AditInternalException("X-Road header 'nimi' not defined: cannot check query version.");
				}
				XRoadQueryName queryName = Util.extractQueryName(xteeHeader.getNimi());
				version = queryName.getVersion();
				requestName = queryName.getName();
			}			
			
			// Unmarshall the request object
			Source requestObjectSource = new DOMSource(requestKeha);
			Object requestObject = null;
			
			try {
				requestObject = this.getUnmarshaller().unmarshal(requestObjectSource);
			} catch (Exception e) {
				LOG.error("Exception while unmarshalling request: ", e);
				throw new AditInternalException("Error in request SOAP envelope: check parameters and XML syntax.");
			}
			
			// Excecute business logic
			responseObject = invokeInternal(requestObject, version);
		} catch (Exception e) {
			LOG.error("Exception while marshalling response object: ", e);
			responseObject = getResultForGenericException(e);
			
			// Add request log entry
			this.getLogService().addRequestLogEntry(requestName + ".v" + version, null, new Date(), xteeHeader.getInfosysteem(), xteeHeader.getIsikukood(), xteeHeader.getAsutus(), "Exception while marshalling response object: " + e.getMessage());
			
		}
		
		if(responseObject != null) {
			// Marshall the response object
			DOMResult reponseObjectResult = new DOMResult(responseElement);
			this.getMarshaller().marshal(responseObject, reponseObjectResult);				
			
			// Add the reponse DOM tree as a child element to the responseKeha element				
			responseElement = (Element) reponseObjectResult.getNode();
		} else {
			LOG.error("Response object not initialized.");
		}
	}

	/**
	 * Adds a SOAP attachment to the response message.
	 * 
	 * @param fileName the temporary file to add as an attachment.
	 * @return attachment ID
	 * @throws Exception
	 */
	public String addAttachment(String fileName) throws Exception {	
		String result = null;
		try {
			LOG.debug("Adding SOAP attachment from file: " + fileName);
			SoapMessage responseMessage = this.getResponseMessage();
			InputStreamSource isr = new FileSystemResource(new File(fileName));			
			Attachment attachment = responseMessage.addAttachment(Util.generateRandomID(), isr, " text/xml");
			LOG.debug("Attachment added with ID: " + attachment.getContentId());
			result = attachment.getContentId();
		} catch (Exception e) {
			LOG.error("Exception while adding SOAP attachment to response message: ", e);
			throw e;
		}
		return result;
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
		} finally {
			Util.safeCloseStream(fos);
			fos = null;
		}
		
		return result;
	}
	
	/**
	 * Unmarshals the contents of the file.
	 * 
	 * @param fileName absolute path to the file that holds the XML content.
	 * @return object representing the XML content.
	 * @throws XmlMappingException
	 * @throws IOException
	 */
	public Object unMarshal(String fileName) throws XmlMappingException, IOException {
		FileInputStream fileInputStream = new FileInputStream(fileName);
		StreamSource streamSource = new StreamSource(fileInputStream);
		return this.getUnmarshaller().unmarshal(streamSource);
	}
	
	public String extractAttachmentXML(SoapMessage message, String attachmentID) throws IOException, AditInternalException, AditCodedException {
		LOG.info("Extracting attachment with ID: " + attachmentID);
		
		if(message == null) {
			LOG.error("SoapMessage is null.");
			throw new AditInternalException("SoapMessage is null. Could not extract attachment from it.");
		}
		
		if(attachmentID == null || attachmentID.trim().equals("")) {
			LOG.error("Attachment ID not specified or empty.");
			throw new AditCodedException("request.attachments.invalidID");
		}
		
		Attachment attachment = this.getRequestMessage().getAttachment(attachmentID);
		
		if(attachment == null) {
			throw new AditCodedException("request.attachments.invalidID");
		}
		
		return extractXML(attachment);
		
	}
	
	/**
	 * Extracts SOAP attachment to a temporary file.
	 * 
	 * @param attachment the attachment to be extracted.
	 * @return absolute path to the temporary file holding the attachment content.
	 * @throws IOException
	 */
	public String extractXML(Attachment attachment) throws IOException {
		String result = Util.createTemporaryFile(attachment.getInputStream(), this.getConfiguration().getTempDir());
		LOG.debug("Attachment extracted to temporary file: " + result);
		return result;
	}
	
	/**
	 * Logs the current request.
	 * 
	 * @param documentId ID of the document (if it is a document request)
	 * @param requestDate time of the request
	 * @param additionalInformation additional information about the request
	 */
	public void logCurrentRequest(Long documentId, Date requestDate, String additionalInformation) {
		try {
			if(this.header != null) {
				this.logService.addRequestLogEntry(
						this.header.getNimi(),
						documentId,
						requestDate,
						this.header.getInfosysteem(),
						(((this.header.getAllasutus() != null) && (this.header.getAllasutus().length() > 0)) ? this.header.getAllasutus() : this.header.getIsikukood()),
						this.header.getAsutus(),
						additionalInformation);
			} else {
				throw new NullPointerException("Request header not initialized.");
			}			
		} catch (Exception ex) {
			LOG.debug("Failed logging request!", ex);
		}
	}
	
	/**
	 * Log a downloading request - a request that downloads file data.
	 * 
	 * @param documentId the ID of the document associated with the file.
	 * @param fileId the ID of the file being downloaded.
	 * @param requestDate time of the request
	 */
	public void logDownloadRequest(Long documentId, Long fileId, Date requestDate) {
		try {
			if(this.header != null) {
				this.logService.addDownloadRequestLogEntry(
						documentId,
						fileId,
						requestDate,
						this.header.getInfosysteem(),
						(((this.header.getAllasutus() != null) && (this.header.getAllasutus().length() > 0)) ? this.header.getAllasutus() : this.header.getIsikukood()),
						this.header.getAsutus());
			} else {
				throw new NullPointerException("Request header not initialized.");
			}			
		} catch (Exception ex) {
			LOG.debug("Failed logging request!", ex);
		}
	}
	
	/**
	 * Log metadata request.
	 * 
	 * @param documentId the ID of the document queried
	 * @param requestDate time of the request
	 */
	public void logMetadataRequest(Long documentId, Date requestDate) {
		try {
			if(this.header != null) {
				this.logService.addMetadataRequestLogEntry(
						documentId,
						requestDate,
						this.header.getInfosysteem(),
						(((this.header.getAllasutus() != null) && (this.header.getAllasutus().length() > 0)) ? this.header.getAllasutus() : this.header.getIsikukood()),
						this.header.getAsutus());
			} else {
				throw new NullPointerException("Request header not initialized.");
			}			
		} catch (Exception ex) {
			LOG.debug("Failed logging request!", ex);
		}
	}
	
	/**
	 * Log an error.
	 * 
	 * @param documentId the document associated with the error.
	 * @param errorDate the time of the error
	 * @param level error severity (WARN, ERROR, FATAL)
	 * @param errorMessage error message
	 */
	public void logError(Long documentId, Date errorDate, String level, String errorMessage) {
		try {
			if(this.header != null) {
				this.logService.addErrorLogEntry(
						this.header.getNimi(),
						documentId,
						errorDate,
						this.header.getInfosysteem(),
						(((this.header.getAllasutus() != null) && (this.header.getAllasutus().length() > 0)) ? this.header.getAllasutus() : this.header.getIsikukood()),
						level,
						errorMessage);
			} else {
				throw new NullPointerException("Request header not initialized.");
			}			
		} catch (Exception ex) {
			LOG.debug("Failed logging request!", ex);
		}
	}
	
	/**
	 * Checks whether or not the request header contains all
	 * required fields. Throws a {@link AditCodedException} if
	 * any of required fields are missing or empty.
	 * 
	 * @param header				SOAP message header part as {@link CustomXTeeHeader}
	 * @throws AditCodedException	Exception describing which required field is missing or empty
	 */
	public void checkHeader(CustomXTeeHeader header) throws AditCodedException {
		if(header != null) {
			if ((header.getIsikukood() == null) || (header.getIsikukood().length() < 1)) {
				throw new AditCodedException("request.header.undefined.personalCode");
			} else if ((header.getInfosysteem() == null) || (header.getInfosysteem().length() < 1)) {
				throw new AditCodedException("request.header.undefined.systemName");
			} else if ((header.getAsutus() == null) || (header.getAsutus().length() < 1)) {
				throw new AditCodedException("request.header.undefined.institution");
			}
		}
	}
	
	/**
	 * Abstract method to be implemented by the subclasses. Invokes the endpoint logic.
	 * 
	 * @param requestObject request object
	 * @param version version of the service method
	 * @return response object
	 * @throws Exception
	 */
	protected abstract Object invokeInternal(Object requestObject, int version)
			throws Exception;
	
	/**
	 * Generates error messages, creates a response object and adds error messages to it.
	 * 
	 * @param ex the exception for which error messages have to be created
	 * @return response object
	 */
	protected abstract Object getResultForGenericException(Exception ex);
	
	/**
	 * Retreives the marshaller.
	 * @return	{@link Marshaller} object used for current request
	 */
	public Marshaller getMarshaller() {
		return marshaller;
	}

	/**
	 * Sets the marshaller.
	 * @param marshaller	{@link Marshaller} object to be used for current request
	 */
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	/**
	 * Retreives the unmarshaller.
	 * @return	{@link Unmarshaller} object used for current request
	 */
	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}
	
	/**
	 * Sets the unmarshaller.
	 * @param unmarshaller	{@link Unmarshaller} object to be used for current request
	 */
	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	/**
	 * Retreives the X-Tee header.
	 * @return		X-Tee header of current request
	 */
	public CustomXTeeHeader getHeader() {
		return header;
	}

	/**
	 * Sets the X-Tee header.
	 * @param header	X-Tee header of current request
	 */
	public void setHeader(CustomXTeeHeader header) {
		this.header = header;
	}

	/**
	 * Retreives the message source.
	 * @return		Currently used message source as {@link MessageSource} object
	 */
	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * Sets the message source.
	 * @param messageSource		{@link MessageSource} object that will be
	 * 							used for application success and error messages.
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * Retreives the configuration.
	 * @return	Application configuration as {@link Configuration} object.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Sets the configuration.
	 * @param configuration		Application configuration as {@link Configuration} object.
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Retreives the log service.
	 * @return		Log service
	 */
	public LogService getLogService() {
		return logService;
	}

	/**
	 * Sets the log service.
	 * @param logService	Log service
	 */
	public void setLogService(LogService logService) {
		this.logService = logService;
	}
	
	/**
	 * Retreives the message service.
	 * @return	Message service
	 */
	public MessageService getMessageService() {
		return messageService;
	}

	/**
	 * Sets the message service.
	 * @param messageService	Message service
	 */
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
}
