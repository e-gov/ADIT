package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.adit.pojo.JoinRequest;
import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "join", version = "v1")
@Component
public class JoinEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(JoinEndpoint.class);

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		
		LOG.debug("JoinEndpoint invoked using object.");
		
		JoinRequest request = (JoinRequest) requestObject;
		
		LOG.debug("JoinEndpoint request object cast successful.");
		
		// TODO: Kontrollime, kas p�ringu k�ivitanud infos�steem on ADITis registreeritud
		
		// TODO: Kontrollime, kas p�ringu k�ivitanud infos�steem tohib andmeid muuta (v�i �ldse n�ha)
		
		// TODO: Kontrollime, kas etteantud kasutajat��p eksisteerib
		
		// TODO: Kontrollime, kas kasutaja juba eksisteerib
		// s.t. kas lisame uue kasutaja v�i muudame olemasolevat
		
		// TODO: Lisame kasutaja v�i muudame olemasolevat
		
		return null;
	}
}