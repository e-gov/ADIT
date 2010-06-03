package ee.adit.util;

import org.apache.log4j.Logger;

import ee.webmedia.xtee.XTeeHeader;

public class Util {

	private static Logger LOG = Logger.getLogger(Util.class);
	
	public static void printHeader(XTeeHeader header) {
		
		LOG.debug("-------- XTeeHeader --------");
		
		LOG.debug("Nimi: " + header.getNimi());
		LOG.debug("ID: " + header.getId());
		LOG.debug("Isikukood: " + header.getIsikukood());
		LOG.debug("Andmekogu: " + header.getAndmekogu());
		LOG.debug("Asutus: " + header.getAsutus());
		LOG.debug("Allasutus: " + header.getAllasutus());
		LOG.debug("Amet: " + header.getAmet());
		
		LOG.debug("----------------------------");
	}
	
}
