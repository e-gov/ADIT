package ee.adit.util;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.castor.core.util.Base64Encoder;

public class Util {

	private static Logger LOG = Logger.getLogger(Util.class);
	
	public static String base64encode(String string) throws UnsupportedEncodingException {
		return new String(Base64Encoder.encode(string.getBytes("UTF-8")));
	}
	
	public static void printHeader(CustomXTeeHeader header) {
		
		LOG.debug("-------- XTeeHeader --------");
		
		LOG.debug("Nimi: " + header.getNimi());
		LOG.debug("ID: " + header.getId());
		LOG.debug("Isikukood: " + header.getIsikukood());
		LOG.debug("Andmekogu: " + header.getAndmekogu());
		LOG.debug("Asutus: " + header.getAsutus());
		LOG.debug("Allasutus: " + header.getAllasutus());
		LOG.debug("Amet: " + header.getAmet());
		LOG.debug("Infosüsteem: " + header.getInfosysteem());
		
		LOG.debug("----------------------------");
	}
	
}
