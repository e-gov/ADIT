package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.pojo.ArrayOfMessage;
import ee.adit.pojo.JoinRequest;
import ee.adit.pojo.JoinResponse;
import ee.adit.pojo.UnJoinRequest;
import ee.adit.pojo.UnJoinResponse;
import ee.adit.service.UserService;
import ee.adit.util.CustomXTeeHeader;
import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "unJoin", version = "v1")
@Component
public class UnJoinEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(UnJoinEndpoint.class);

	private UserService userService;
	
	@Override
	protected void invokeInternal(Document requestKeha, Element responseKeha, CustomXTeeHeader xteeHeader) throws Exception {
		
		
		
		
		
		
		
	}

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		
		UnJoinResponse response = new UnJoinResponse();
		ArrayOfMessage messages = new ArrayOfMessage();
		
		try {
			LOG.debug("UnJoinEndpoint.v1 invoked.");
			
			UnJoinRequest request = (UnJoinRequest) requestObject;
			CustomXTeeHeader header = this.getHeader();
			String applicationName = header.getInfosysteem();
			
			// TODO: Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
			
			
			
			// TODO: Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid muuta (või üldse näha)
			
			// TODO: Kontrollime, kas päringu käivitanud kasutaja eksisteerib
			
			// TODO: Kontrollime, kas infosüsteem tohib antud kasutaja andmeid muuta
			
			// TODO: Märgime kasutaja lahkunuks
			
		} catch (Exception e) {
			
		}
		
		return null;
	}
}