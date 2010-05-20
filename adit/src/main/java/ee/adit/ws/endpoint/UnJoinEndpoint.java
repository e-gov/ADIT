package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "unJoin", version = "v1")
@Component
public class UnJoinEndpoint extends XteeCustomEndpoint {

	private static Logger LOG = Logger.getLogger(UnJoinEndpoint.class);

	@Override
	protected void invokeInternal(Document requestKeha, Element responseKeha, XTeeHeader xteeHeader) throws Exception {
		LOG.debug("UnJoinEndpoint invoked.");
		
		// TODO: Implement me!
	}
}