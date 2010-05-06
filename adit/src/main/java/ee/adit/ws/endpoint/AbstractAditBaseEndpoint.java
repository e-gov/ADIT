package ee.adit.ws.endpoint;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.endpoint.AbstractXTeeBaseEndpoint;

//AbstractXTeeBaseEndpoint

/**
 * Peaks tegema nii, et klassil on AbstractXTeeBaseEndpoint omadused ning
 * implementeeriks samas ka AbstractMarshallingPayloadEndpoint nimelist abstract
 * klassi.
 * 
 */

public abstract class AbstractAditBaseEndpoint extends AbstractXTeeBaseEndpoint {

	private XTeeHeader header;

	private Marshaller marshaller;

	private Unmarshaller unmarshaller;

	protected void invokeInternal(Document requestKeha, Element responseKeha,
			XTeeHeader xteeHeader) throws Exception {

		try {
			// Unmarshall the request object
			Source requestObjectSource = new DOMSource(requestKeha);
			Object requestObject = this.getUnmarshaller().unmarshal(requestObjectSource);
			
			// Excecute business logic
			Object responseObject = invokeInternal(requestObject);
			
			// Marshall the response object
			DOMResult reponseObjectResult = new DOMResult();
			this.getMarshaller().marshal(responseObject, reponseObjectResult);
			
			// Add the reponse DOM tree as a child element to the responseKeha element
			responseKeha.appendChild(reponseObjectResult.getNode());
		} catch (XmlMappingException e) {
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

	public XTeeHeader getHeader() {
		return header;
	}

	public void setHeader(XTeeHeader header) {
		this.header = header;
	}

	// Abstract method for implementing by subclasses
	protected abstract Object invokeInternal(Object requestObject)
			throws Exception;
	
}
