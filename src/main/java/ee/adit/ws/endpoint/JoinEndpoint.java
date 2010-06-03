package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ee.adit.pojo.JoinRequest;
import ee.adit.service.UserService;
import ee.adit.util.Util;
import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "join", version = "v1")
@Component
public class JoinEndpoint extends AbstractAditBaseEndpoint {

	private static Logger LOG = Logger.getLogger(JoinEndpoint.class);

	private UserService userService;
	
	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		
		try {
		
		LOG.debug("JoinEndpoint invoked.");
		JoinRequest request = (JoinRequest) requestObject;
		XTeeHeader header = this.getHeader();
		
		// Output the input parameters
		Util.printHeader(header);
		printRequest(request);
		
		// TODO: check header and request objects
		
		// TODO: Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
		// Selleks kontrollime tabelis REMOTE_APPLICATION olevaid kirjeid ja päringu parameetrit APPLICATION
		
		
		
		boolean applicationRegistered = this.getUserService().isApplicationRegistered(request.getApplication());
		
		// TODO: Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid muuta (või üldse näha)
		
		// TODO: Kontrollime, kas etteantud kasutajatüüp eksisteerib
		
		// TODO: Kontrollime, kas kasutaja juba eksisteerib
		// s.t. kas lisame uue kasutaja või muudame olemasolevat
		
		// TODO: Lisame kasutaja või muudame olemasolevat
		
		} catch (Exception e) {
			LOG.error("Exception: ", e);
		}
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

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}