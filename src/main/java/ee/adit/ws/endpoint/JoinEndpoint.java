package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.pojo.JoinRequest;
import ee.adit.util.Util;
import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "join", version = "v1")
@Component
public class JoinEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(JoinEndpoint.class);

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		
		LOG.debug("JoinEndpoint invoked.");
		JoinRequest request = (JoinRequest) requestObject;
		XTeeHeader header = this.getHeader();
		
		// Output the input parameters
		Util.printHeader(header);
		printRequest(request);
		
		
		
		
		// TODO: Kontrollime, kas p�ringu k�ivitanud infos�steem on ADITis registreeritud
		
		// TODO: Kontrollime, kas p�ringu k�ivitanud infos�steem tohib andmeid muuta (v�i �ldse n�ha)
		
		// TODO: Kontrollime, kas etteantud kasutajat��p eksisteerib
		
		// TODO: Kontrollime, kas kasutaja juba eksisteerib
		// s.t. kas lisame uue kasutaja v�i muudame olemasolevat
		
		// TODO: Lisame kasutaja v�i muudame olemasolevat
		
		return null;
	}
	
	private static void printRequest(JoinRequest request) {
		
		LOG.debug("-------- JoinRequest -------");
		
		LOG.debug("Application: " + request.getApplication());
		LOG.debug("InstitutionCode: " + request.getInstitutionCode());
		LOG.debug("UserName: " + request.getUserName());
		LOG.debug("UserType: " + request.getUserType());
		
		LOG.debug("----------------------------");
	
	}
}