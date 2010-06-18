package ee.adit.ws.endpoint;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.log4j.Logger;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.util.CustomXTeeHeader;

public abstract class AbstractAditBaseEndpoint extends XteeCustomEndpoint {

	private static Logger LOG = Logger.getLogger(AbstractAditBaseEndpoint.class);
	
	private CustomXTeeHeader header;

	private Marshaller marshaller;

	private Unmarshaller unmarshaller;

	protected void invokeInternal(Document requestKeha, Element responseElement,
			CustomXTeeHeader xteeHeader) throws Exception {

		LOG.debug("AbstractAditBaseEndpoint invoked");
		
		try {
			
			// Set the header as a property
			this.setHeader(xteeHeader);
			
			// Unmarshall the request object
			Source requestObjectSource = new DOMSource(requestKeha);
			Object requestObject = this.getUnmarshaller().unmarshal(requestObjectSource);
			
			// Excecute business logic
			Object responseObject = invokeInternal(requestObject);
			
			if(responseObject != null) {
				// Marshall the response object
				DOMResult reponseObjectResult = new DOMResult(responseElement);
				this.getMarshaller().marshal(responseObject, reponseObjectResult);				
				
				// Add the reponse DOM tree as a child element to the responseKeha element				
				responseElement = (Element) reponseObjectResult.getNode();
			} else {
				LOG.error("Response object not initialized.");
			}
			
			
		} catch (Exception e) {
			LOG.error("Exception while marshalling response object: ", e);
		}
		
	}

	public void addAttachment(String fileName) throws Exception {		
		try {
			LOG.debug("Adding SOAP attachment from file: " + fileName);
			SOAPMessage responseMessage = this.getResponseMessage();
			FileDataSource fileDataSource = new FileDataSource(fileName);
			MimetypesFileTypeMap typeMap = new MimetypesFileTypeMap();
			typeMap.addMimeTypes("base64");
			fileDataSource.setFileTypeMap(typeMap);
			DataHandler dataHandler = new DataHandler(fileDataSource);
			AttachmentPart attachmentPart = responseMessage.createAttachmentPart(dataHandler);
			responseMessage.addAttachmentPart(attachmentPart);
			LOG.debug("Attachment added.");
		} catch (Exception e) {
			LOG.error("Exception while adding SOAP attachment to response message: ", e);
			throw e;
		}		
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

	public CustomXTeeHeader getHeader() {
		return header;
	}

	public void setHeader(CustomXTeeHeader header) {
		this.header = header;
	}

	// Abstract method for implementing by subclasses
	protected abstract Object invokeInternal(Object requestObject)
			throws Exception;
	
}
