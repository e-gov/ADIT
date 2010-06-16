package ee.adit.util;

import org.apache.log4j.Logger;

public class Util {

	private static Logger LOG = Logger.getLogger(Util.class);
	
	public static String base64encode(String string) {
		String result = null;
		
		// TODO: base64 encode
		
		return result;
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
