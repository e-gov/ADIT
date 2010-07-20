package ee.adit.schedule;

import java.math.BigInteger;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusResponseDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Tahtsus;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.SyndmuseTyyp;
import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;
import ee.webmedia.xtee.client.service.StandardXTeeConsumer;

public class ScheduleClient {
	private static Logger LOG = Logger.getLogger(ScheduleClient.class);
	private static int RESULT_OK = 0;
	
	public static boolean addEvent(
			final String eventText,
			final String eventType,
			final Calendar eventDate) {
		boolean success = false;
		
		try {
			LisaSyndmusDocument doc = LisaSyndmusDocument.Factory.newInstance();
			LisaSyndmus req = doc.addNewLisaSyndmus();
			LisaSyndmusDocument.LisaSyndmus.Keha keha = req.addNewKeha();
			
			// Event is not visible to ADIT system user in portal.
			keha.setNahtavOmanikule(false);
			
			keha.setKirjeldus(eventText);
			keha.setTahtsus(Tahtsus.KESKMINE);
			keha.setSyndmuseTyyp(SyndmuseTyyp.LIIGIS);
			keha.setLiik(eventType);
			
			// Start and end times
			keha.setAlgus(eventDate);
			keha.setLopp(eventDate);
			
			ClassPathXmlApplicationContext ctx = null;
			try {
				ctx = startContext();
				StandardXTeeConsumer xteeService = (StandardXTeeConsumer) ctx.getBean("xteeService");
				SimpleXTeeServiceConfiguration conf = (SimpleXTeeServiceConfiguration) xteeService.getServiceConfiguration();
				conf.setDatabase("teavituskalender");
				conf.setMethod("lisaSyndmus");
				conf.setVersion("v1");
				
				conf.setSecurityServer("http://192.168.80.30/cgi-bin/consumer_proxy");
				conf.setInstitution("12345");
				conf.setIdCode("1234");
				LisaSyndmusResponseDocument ret = (LisaSyndmusResponseDocument) xteeService.sendRequest(doc, conf);
				
				if (ret != null) {
					if (ret.getLisaSyndmusResponse() != null) {
						if (ret.getLisaSyndmusResponse().getKeha() != null) {
							if (ret.getLisaSyndmusResponse().getKeha().getTulemus() != null) {
								BigInteger resultCode = ret.getLisaSyndmusResponse().getKeha().getTulemus().getTulemuseKood();
								String resultMessage = ret.getLisaSyndmusResponse().getKeha().getTulemus().getTulemuseTekst();
								LOG.debug("LisaSyndmus result code: " + ((resultCode == null) ? "NULL" : resultCode.toString()));
								LOG.debug("LisaSyndmus result message: " + resultMessage);
								
								if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
									success = true;
								}
							} else {
								LOG.error("Error adding notification to 'teavituskalender' database. Response's 'tulemus' part is NULL.");
							}
						} else {
							LOG.error("Error adding notification to 'teavituskalender' database. Response's 'keha' part is NULL.");
						}
					} else {
						LOG.error("Error adding notification to 'teavituskalender' database. Response's 'LisaSyndmusResponse' part is NULL.");
					}
				} else {
					LOG.error("Error adding notification to 'teavituskalender' database. Response document is NULL.");
				}
			} finally {
				if (ctx != null) {
					ctx.close();
				}
			}
		} catch (Exception ex) {
			LOG.error("Error adding notification to 'teavituskalender' database.", ex);
		}
		
		return success;
	}
	
	public static ClassPathXmlApplicationContext startContext() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("xtee.xml");
		ctx.start();
		return ctx;
	}
}
