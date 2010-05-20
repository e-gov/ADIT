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
		
		// TODO: Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
		
		// TODO: Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid muuta (või üldse näha)
		
		// TODO: Kontrollime, kas etteantud kasutajatüüp eksisteerib
		
		// TODO: Kontrollime, kas kasutaja juba eksisteerib
		// s.t. kas lisame uue kasutaja või muudame olemasolevat
		
		// TODO: Lisame kasutaja või muudame olemasolevat
	}
}