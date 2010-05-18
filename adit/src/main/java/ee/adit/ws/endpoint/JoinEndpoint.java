package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "join", version = "v1")
@Component
public class JoinEndpoint extends XteeCustomEndpoint {

	private static Logger LOG = Logger.getLogger(JoinEndpoint.class);

	@Override
	protected void invokeInternal(Document requestKeha, Element responseKeha, XTeeHeader xteeHeader) throws Exception {
		LOG.debug("JoinEndpoint invoked.");
		
		// TODO: Implement me!
	}
}