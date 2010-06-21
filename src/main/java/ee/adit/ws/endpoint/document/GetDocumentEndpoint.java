package ee.adit.ws.endpoint.document;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.util.CustomXTeeHeader;
import ee.adit.ws.endpoint.XteeCustomEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getDocument", version = "v1")
@Component
public class GetDocumentEndpoint extends XteeCustomEndpoint {

	private static Logger LOG = Logger.getLogger(GetDocumentEndpoint.class);

	@Override
	protected void invokeInternal(Document requestKeha, Element responseKeha,
			CustomXTeeHeader xteeHeader) throws Exception {

		

	}
}
