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

import ee.adit.exception.AditInternalException;
import ee.adit.service.LogService;
import ee.adit.service.MessageService;
import ee.adit.util.Configuration;
import ee.adit.util.CustomXTeeHeader;
import ee.adit.util.XRoadQueryName;
import ee.adit.util.Util;

public abstract class AbstractAditBaseEndpoint extends XteeCustomEndpoint {

	private static Logger LOG = Logger.getLogger(AbstractAditBaseEndpoint.class);
	
	private CustomXTeeHeader header;

	private Marshaller marshaller;

	private Unmarshaller unmarshaller;

	private MessageSource messageSource;

	private Configuration configuration;
	
	private LogService logService;
	
	private MessageService messageService;

	protected void invokeInternal(Document requestKeha, Element responseElement,
			CustomXTeeHeader xteeHeader) throws Exception {
		
		LOG.debug("AbstractAditBaseEndpoint invoked");
		Object responseObject = null;
		
		if (requestKeha == null) {
			throw new Exception("Failed unmarshalling request because request body is null!");
		}
		
		try {
			// Set the header as a property
			this.setHeader(xteeHeader);
			
			// Check request version
			if(xteeHeader.getNimi() == null) {
				throw new AditInternalException("X-Road header 'nimi' not defined: cannot check query version.");
			}
			
			XRoadQueryName queryName = Util.extractQueryName(xteeHeader.getNimi());
			
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
			responseObject = invokeInternal(requestObject, queryName.getVersion());
		} catch (Exception e) {
			LOG.error("Exception while marshalling response object: ", e);
			responseObject = getResultForGenericException(e);
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
	
	public Object unMarshal(String fileName) throws XmlMappingException, IOException {
		FileInputStream fileInputStream = new FileInputStream(fileName);
		StreamSource streamSource = new StreamSource(fileInputStream);
		return this.getUnmarshaller().unmarshal(streamSource);
	}
	
	public String extractXML(Attachment attachment) throws IOException {
		String result = Util.createTemporaryFile(attachment.getInputStream(), this.getConfiguration().getTempDir());
		LOG.debug("Attachment extracted to temporary file: " + result);
		return result;
	}
	
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
	
	// Abstract method for implementing by subclasses
	protected abstract Object invokeInternal(Object requestObject, int version)
			throws Exception;
	
	protected abstract Object getResultForGenericException(Exception ex);
	
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

	public CustomXTeeHeader getHeader() {
		return header;
	}

	public void setHeader(CustomXTeeHeader header) {
		this.header = header;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public LogService getLogService() {
		return logService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}
	
	public MessageService getMessageService() {
		return messageService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
}
