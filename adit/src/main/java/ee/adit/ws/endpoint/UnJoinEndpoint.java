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
		
		// TODO: Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
		
		// TODO: Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid muuta (või üldse näha)
		
		// TODO: Kontrollime, kas päringu käivitanud kasutaja eksisteerib
		
		// TODO: Kontrollime, kas infosüsteem tohib antud kasutaja andmeid muuta
		
		// TODO: Märgime kasutaja lahkunuks
	}
}