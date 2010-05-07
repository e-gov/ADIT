package ee.adit.ws.endpoint;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getDocument", version = "v1")
@Component
public class GetDocumentEndpoint extends XteeCustomEndpoint {

	private static Logger LOG = Logger.getLogger(GetDocumentEndpoint.class);

	@Override
	protected void invokeInternal(Document requestKeha, Element responseKeha,
			XTeeHeader xteeHeader) throws Exception {

		MessageContext context = this.getMessageContext();

		SaajSoapMessage requestMessage = (SaajSoapMessage) context.getRequest();

		SaajSoapMessage responseMessage = (SaajSoapMessage) context.getResponse();
		File attachmentFile = new File("C:\\Avaldus Marko Kurm.pdf");
		Attachment att = responseMessage.addAttachment("1", attachmentFile);
		LOG.debug("Added attachment size: " + att.getSize());
		
		LOG.debug("GetDocumentEndpoint invoked.");

	}
}
