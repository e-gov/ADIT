package ee.adit.schedule;

import java.math.BigInteger;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ee.adit.dao.pojo.Notification;
import ee.adit.service.UserService;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusResponseDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Tahtsus;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.SyndmuseTyyp;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad.Kasutaja;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad.Kasutaja.KasutajaTyyp;
import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;
import ee.webmedia.xtee.client.service.StandardXTeeConsumer;

public class ScheduleClient {
	private static Logger LOG = Logger.getLogger(ScheduleClient.class);
	private static int RESULT_OK = 0;

	public static String NotificationType_Send = "send";
	public static String NotificationType_Share = "share";
	public static String NotificationType_View = "view";
	public static String NotificationType_Modify = "modify";
	public static String NotificationType_Sign = "sign";
	
	public static long addEvent(Notification notification, final String eventType, final UserService userService) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(notification.getEventDate());
		return addEvent(
				notification.getId(),
				notification.getUserCode(),
				notification.getNotificationText(),
				eventType,
				cal,
				notification.getNotificationType(),
				notification.getDocumentId(),
				userService);
	}

	public static long addEvent(
		final String eventOwnerCode,
		final String eventText,
		final String eventType,
		final Calendar eventDate,
		final String notificationType,
		final long relatedDocumentId,
		final UserService userService) {
		
		return addEvent(0, eventOwnerCode, eventText, eventType, eventDate, notificationType, relatedDocumentId, userService);
	}
	
	public static long addEvent(
			final long notificationId,
			final String eventOwnerCode,
			final String eventText,
			final String eventType,
			final Calendar eventDate,
			final String notificationType,
			final long relatedDocumentId,
			final UserService userService) {
		long eventId = 0;
		
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
			
			Lugejad recipients = keha.addNewLugejad();
			Kasutaja recipient = recipients.addNewKasutaja();
			recipient.setKood(eventOwnerCode);
			
			// Start and end times
			keha.setAlgus(eventDate);
			keha.setLopp(eventDate);
			
			ClassPathXmlApplicationContext ctx = null;
			try {
				ctx = startContext();
				StandardXTeeConsumer xteeService = (StandardXTeeConsumer) ctx.getBean("xteeConsumer");
				SimpleXTeeServiceConfiguration conf = (SimpleXTeeServiceConfiguration) xteeService.getServiceConfiguration();
				conf.setDatabase("teavituskalender");
				conf.setMethod("lisaSyndmus");
				conf.setVersion("v1");
				LisaSyndmusResponseDocument ret = (LisaSyndmusResponseDocument) xteeService.sendRequest(doc, conf);
				
				if (ret != null) {
					if (ret.getLisaSyndmusResponse() != null) {
						if (ret.getLisaSyndmusResponse().getKeha() != null) {
							BigInteger resultEventId = ret.getLisaSyndmusResponse().getKeha().getSyndmusId();
							LOG.debug("LisaSyndmus result event ID: " + ((resultEventId == null) ? "NULL" : resultEventId.toString()));

							if (ret.getLisaSyndmusResponse().getKeha().getTulemus() != null) {
								BigInteger resultCode = ret.getLisaSyndmusResponse().getKeha().getTulemus().getTulemuseKood();
								String resultMessage = ret.getLisaSyndmusResponse().getKeha().getTulemus().getTulemuseTekst();
								LOG.debug("LisaSyndmus result code: " + ((resultCode == null) ? "NULL" : resultCode.toString()));
								LOG.debug("LisaSyndmus result message: " + resultMessage);
								
								if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
									eventId = resultEventId.longValue();
									LOG.debug("Successfully added notification to 'teavituskalender' database. Related document ID: " + String.valueOf(relatedDocumentId));
								}
							} else {
								LOG.error("Error adding notification to 'teavituskalender' database. Response's 'tulemus' part is NULL. Related document ID: " + String.valueOf(relatedDocumentId));
							}
						} else {
							LOG.error("Error adding notification to 'teavituskalender' database. Response's 'keha' part is NULL. Related document ID: " + String.valueOf(relatedDocumentId));
						}
					} else {
						LOG.error("Error adding notification to 'teavituskalender' database. Response's 'LisaSyndmusResponse' part is NULL. Related document ID: " + String.valueOf(relatedDocumentId));
					}
				} else {
					LOG.error("Error adding notification to 'teavituskalender' database. Response document is NULL. Related document ID: " + String.valueOf(relatedDocumentId));
				}
			} finally {
				if (ctx != null) {
					ctx.close();
				}
			}
		} catch (Exception ex) {
			LOG.error("Error adding notification to 'teavituskalender' database. Related document ID: " + String.valueOf(relatedDocumentId), ex);
		}
		
		// Save notification to database
		if (eventId > 0) {
			try {
				userService.addNotification(
					notificationId,
					relatedDocumentId,
					notificationType,
					eventOwnerCode,
					eventDate.getTime(),
					eventText,
					eventId,
					Calendar.getInstance().getTime());
			} catch (Exception ex) {
				LOG.error("Failed saving successfully sent notification.", ex);
			}
		} else {
			try {
				userService.addNotification(
					notificationId,
					relatedDocumentId,
					notificationType,
					eventOwnerCode,
					eventDate.getTime(),
					eventText,
					null,
					null);
			} catch (Exception ex) {
				LOG.error("Failed saving unsent notification.", ex);
			}
		}
		
		return eventId;
	}
	
	public static ClassPathXmlApplicationContext startContext() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("xtee.xml");
		ctx.start();
		return ctx;
	}
}
