package ee.adit.schedule;

import java.math.BigInteger;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDateBuilder;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Notification;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;
import ee.adit.util.SchedulerSoapArrayInterceptor;
import ee.adit.util.Util;
import ee.adit.util.xroad.CustomWSConsumptionLoggingInterceptor;
import ee.adit.util.xroad.XRoadResponseSanitizerInterceptor;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad.Kasutaja;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad.Kasutaja.KasutajaTyyp;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.SyndmuseTyyp;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Tahtsus;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusResponseDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadDocument.OtsiKasutajad;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadDocument.OtsiKasutajad.Keha.Kasutajad;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadResponseDocument;
import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;
import ee.webmedia.xtee.client.service.StandardXTeeConsumer;

/**
 * Web service client class for notification calendar (teavituskalender) X-Road
 * database. Enables execution of notification calendar web service requests.
 * 
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class ScheduleClient {
    
    private static Logger logger = Logger.getLogger(ScheduleClient.class);
    
    private static final int RESULT_OK = 0;

    /**
     * Notification type SEND.
     */
    public static final String NOTIFICATION_TYPE_SEND = "send";
    
    /**
     * Notification type SHARE.
     */
    public static final String NOTIFICATION_TYPE_SHARE = "share";
    
    /**
     * Notification type VIEW.
     */
    public static final String NOTIFICATION_TYPE_VIEW = "view";
    
    /**
     * Notification type MODIFY. 
     */
    public static final String NOTIFICATION_TYPE_MODIFY = "modify";
    
    /**
     * Notification type SIGN.
     */
    public static final String NOTIFICATION_TYPE_SIGN = "sign";

    /**
     * Notification priority LOW. 
     */
    public static final String NOTIFICATION_PRIORITY_LOW = "madal";
    
    /**
     * Notification priority MEDIUM. 
     */
    public static final String NOTIFICATION_PRIORITY_MEDIUM = "keskmine";
    
    /**
     * Notification priority HIGH.
     */
    public static final String NOTIFICATION_PRIORITY_HIGH = "korge";

    /**
     * Notification type type.
     */
    public static final String NOTIFICATION_TYPE_TEAVITUSKALENDER_LIIGIS = "liigis";
    
    /**
     * Notification type OBLIGATORY.
     */
    public static final String NOTIFICATION_TYPE_TEAVITUSKALENDER_KOHUSTUSLIK = "kohustuslik";

    /**
     * Notification user type PERSON.
     */
    public static final String NOTIFICATION_USER_PERSON = "isik";
    
    /**
     * Notification user type OFFICIAL.
     */
    public static final String NOTIFICATION_USER_OFFICIAL = "ametnik";
    
    /**
     * Notification user type INSTITUTION.
     */
    public static final String NOTIFICATION_USER_INSTITUTION = "asutus";

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private Configuration configuration;

    /**
     * Adds a notification to "teavituskalender" X-Road database. <br>
     * <br>
     * This method intentionally throws no exception when failing. Periodic
     * attempts will be made to send notifications notifications that could not
     * be sent in previous attempts.
     * 
     * @param notification
     *            Local {@link Notification} object that needs to be sent to
     *            "teavituskalender" database.
     * @param eventType
     *            Name of notifications type in "teavituskalender" database
     * @param userService
     *            Current {@link UserService} instance
     * @return Notification ID in "teavituskalender" database
     */
    public long addEvent(Notification notification, final String eventType, final UserService userService) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(notification.getEventDate());
        AditUser eventOwner = userService.getUserByID(notification.getUserCode());
        return addEvent(notification.getId(), eventOwner, notification.getNotificationText(), eventType, cal,
                notification.getNotificationType(), notification.getDocumentId(), userService);
    }

    /**
     * Adds a notification to "teavituskalender" X-Road database. <br>
     * <br>
     * This method intentionally throws no exception when failing. Periodic
     * attempts will be made to send notifications notifications that could not
     * be sent in previous attempts.
     * 
     * @param eventOwner
     *            {@link AditUser} to whom this notification will be sent
     * @param eventText
     *            Notification text
     * @param eventType
     *            Name of notifications type in "teavituskalender" database
     * @param eventDate
     *            Date and time describing when the notified event occurred (for
     *            example, when a document was signed)
     * @param notificationType
     *            Code of notification type in local database. This type is more
     *            fine grained than the type in "teavituskalender" database
     * @param relatedDocumentId
     *            ID of the document this notification is about
     * @param userService
     *            Current {@link UserService} instance
     * @return Notification ID in "teavituskalender" database
     */
    public long addEvent(final AditUser eventOwner, final String eventText, final String eventType,
            final Calendar eventDate, final String notificationType, final long relatedDocumentId,
            final UserService userService) {

        return addEvent(0, eventOwner, eventText, eventType, eventDate, notificationType, relatedDocumentId,
                userService);
    }

    /**
     * Adds a notification to "teavituskalender" X-Road database. <br>
     * <br>
     * This method intentionally throws no exception when failing. Periodic
     * attempts will be made to send notifications notifications that could not
     * be sent in previous attempts.
     * 
     * @param notificationId
     *            Notification ID in local database
     * @param eventOwner
     *            {@link AditUser} to whom this notification will be sent
     * @param eventText
     *            Notification text
     * @param eventType
     *            Name of notifications type in "teavituskalender" database
     * @param eventDate
     *            Date and time describing when the notified event occurred (for
     *            example, when a document was signed)
     * @param notificationType
     *            Code of notification type in local database. This type is more
     *            fine grained than the type in "teavituskalender" database
     * @param relatedDocumentId
     *            ID of the document this notification is about
     * @param userService
     *            Current {@link UserService} instance
     * @return Notification ID in "teavituskalender" database
     */
    public static long addEvent(final long notificationId, final AditUser eventOwner, final String eventText,
            final String eventType, final Calendar eventDate, final String notificationType,
            final long relatedDocumentId, final UserService userService) {
        long eventId = 0;

        logger.debug("AddEvent with XteeBeans invoked...");

        try {

            logger.debug("Setting messageFactory for addEvent call.");
            System.setProperty("javax.xml.soap.MessageFactory", "com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl");

            LisaSyndmusDocument doc = LisaSyndmusDocument.Factory.newInstance();
            LisaSyndmus req = doc.addNewLisaSyndmus();
            LisaSyndmusDocument.LisaSyndmus.Keha keha = req.addNewKeha();

            // Event is visible to ADIT system user in portal.
            keha.setNahtavOmanikule(true);

            keha.setKirjeldus(eventText);
            keha.setTahtsus(Tahtsus.KESKMINE);
            keha.setSyndmuseTyyp(SyndmuseTyyp.LIIGIS);
            keha.setLiik(eventType);

            Lugejad recipients = keha.addNewLugejad();
            Kasutaja recipient = recipients.addNewKasutaja();
            // Remove country prefix because teavituskalender does not support
            // ID codes beginning with country prefix
            String userCode = eventOwner.getUserCode();
            recipient.setKood(Util.getPersonalIdCodeWithoutCountryPrefix(userCode));
            if (UserService.USERTYPE_PERSON.equalsIgnoreCase(eventOwner.getUsertype().getShortName())) {
                recipient.setKasutajaTyyp(KasutajaTyyp.ISIK);
            } else {
                recipient.setKasutajaTyyp(KasutajaTyyp.ASUTUS);
            }

            // Start and end times
            keha.setAlgus(eventDate);
            keha.setLopp(eventDate);
            
            // Start and end times of notification displaying
            Calendar displayStartDate = Calendar.getInstance();
            displayStartDate.set(eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH));
            GDateBuilder displayStartDateWithoutTimeZone = new GDateBuilder(displayStartDate);
            displayStartDateWithoutTimeZone.clearTimeZone();
            
            Calendar displayEndDate = Calendar.getInstance();
            displayEndDate.set(eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH), eventDate.get(Calendar.DAY_OF_MONTH));
            displayEndDate.add(Calendar.DAY_OF_MONTH, 7);
            GDateBuilder displayEndDateWithoutTimeZone = new GDateBuilder(displayEndDate);
            displayEndDateWithoutTimeZone.clearTimeZone();
            
            keha.setTeavitusAlgus(displayStartDateWithoutTimeZone.getCalendar());
            keha.setTeavitusLopp(displayEndDateWithoutTimeZone.getCalendar());

            ClassPathXmlApplicationContext ctx = null;
            try {
                ctx = startContext();
                StandardXTeeConsumer xteeService = (StandardXTeeConsumer) ctx.getBean("xteeConsumer");
                SimpleXTeeServiceConfiguration conf = (SimpleXTeeServiceConfiguration) xteeService
                        .getServiceConfiguration();
                conf.setDatabase("teavituskalender");
                conf.setMethod("lisaSyndmus");
                conf.setVersion("v1");

                SchedulerSoapArrayInterceptor ai = new SchedulerSoapArrayInterceptor();
                XRoadResponseSanitizerInterceptor ci = new XRoadResponseSanitizerInterceptor();
                CustomWSConsumptionLoggingInterceptor li = new CustomWSConsumptionLoggingInterceptor();

                xteeService.getWebServiceTemplate().setInterceptors(new ClientInterceptor[] {ai, ci, li });
                LisaSyndmusResponseDocument ret = (LisaSyndmusResponseDocument) xteeService.sendRequest(doc, conf);

                if (ret != null) {
                    if (ret.getLisaSyndmusResponse() != null) {
                        if (ret.getLisaSyndmusResponse().getKeha() != null) {
                            BigInteger resultEventId = ret.getLisaSyndmusResponse().getKeha().getSyndmusId();
                            logger.debug("LisaSyndmus result event ID: "
                                    + ((resultEventId == null) ? "NULL" : resultEventId.toString()));

                            if (ret.getLisaSyndmusResponse().getKeha().getTulemus() != null) {
                                BigInteger resultCode = ret.getLisaSyndmusResponse().getKeha().getTulemus().getTulemuseKood();
                                String resultMessage = ret.getLisaSyndmusResponse().getKeha().getTulemus().getTulemuseTekst();
                                logger.debug("LisaSyndmus result code: "
                                        + ((resultCode == null) ? "NULL" : resultCode.toString()));
                                logger.debug("LisaSyndmus result message: " + resultMessage);

                                if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
                                    eventId = resultEventId.longValue();
                                    logger.debug("Successfully added notification to 'teavituskalender' database. Related document ID: "
                                    	+ String.valueOf(relatedDocumentId));
                                } else {
                                	logger.error("Error adding notification to 'teavituskalender' database. Result code: "
                                		+ String.valueOf(resultCode) + ", result message: " + resultMessage + ", event ID: " + String.valueOf(resultEventId));
                                	logger.error(doc.xmlText());
                                }
                            } else {
                                logger.error("Error adding notification to 'teavituskalender' database. Response's 'tulemus' part is NULL. Related document ID: "
                                	+ String.valueOf(relatedDocumentId));
                            }
                        } else {
                            logger.error("Error adding notification to 'teavituskalender' database. Response's 'keha' part is NULL. Related document ID: "
                            	+ String.valueOf(relatedDocumentId));
                        }
                    } else {
                        logger.error("Error adding notification to 'teavituskalender' database. Response's 'LisaSyndmusResponse' part is NULL. Related document ID: "
                                        + String.valueOf(relatedDocumentId));
                    }
                } else {
                    logger.error("Error adding notification to 'teavituskalender' database. Response document is NULL. Related document ID: "
                                    + String.valueOf(relatedDocumentId));
                }
            } finally {
                if (ctx != null) {
                    ctx.close();
                }
            }
        } catch (Exception ex) {
            logger.error("Error adding notification to 'teavituskalender' database. Related document ID: "
                    + String.valueOf(relatedDocumentId), ex);
        }

        // Save notification to database
        if (eventId > 0) {
            try {
                userService.addNotification(notificationId, relatedDocumentId, notificationType, eventOwner
                        .getUserCode(), eventDate.getTime(), eventText, eventId, Calendar.getInstance().getTime());
            } catch (Exception ex) {
                logger.error("Failed saving successfully sent notification.", ex);
            }
        } else {
            try {
                userService.addNotification(notificationId, relatedDocumentId, notificationType, eventOwner
                        .getUserCode(), eventDate.getTime(), eventText, null, null);
            } catch (Exception ex) {
                logger.error("Failed saving unsent notification.", ex);
            }
        }

        return eventId;
    }

    /**
     * Queries "teavituskalender" database to find out whether or not it
     * contains specified user in its user database.
     * 
     * @param userCode
     *            User (person or organization) code.
     * @return Whether or not 'teavituskalender' database contains a user
     *         matching the given code.
     */
    public static boolean userExists(String userCode) {
        boolean result = false;
        try {
            OtsiKasutajadDocument doc = OtsiKasutajadDocument.Factory.newInstance();
            OtsiKasutajad req = doc.addNewOtsiKasutajad();
            OtsiKasutajadDocument.OtsiKasutajad.Keha keha = req.addNewKeha();

            Kasutajad users = keha.addNewKasutajad();
            ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadDocument.OtsiKasutajad.Keha.Kasutajad.Kasutaja user = users
                    .addNewKasutaja();
            user.setKood(userCode);

            ClassPathXmlApplicationContext ctx = null;
            try {
                ctx = startContext();
                StandardXTeeConsumer xteeService = (StandardXTeeConsumer) ctx.getBean("xteeConsumer");
                SimpleXTeeServiceConfiguration conf = (SimpleXTeeServiceConfiguration) xteeService
                        .getServiceConfiguration();
                conf.setDatabase("teavituskalender");
                conf.setMethod("otsiKasutajad");
                conf.setVersion("v1");
                OtsiKasutajadResponseDocument ret = (OtsiKasutajadResponseDocument) xteeService.sendRequest(doc, conf);

                if (ret != null) {
                    if (ret.getOtsiKasutajadResponse() != null) {
                        if (ret.getOtsiKasutajadResponse().getKeha() != null) {
                            BigInteger foundUserCount = ret.getOtsiKasutajadResponse().getKeha().getLeitudArv();
                            logger.debug("OtsiKasutajad request found "
                                    + ((foundUserCount == null) ? "NULL" : foundUserCount.toString())
                                    + " mathing users.");

                            if (ret.getOtsiKasutajadResponse().getKeha().getTulemus() != null) {
                                BigInteger resultCode = ret.getOtsiKasutajadResponse().getKeha().getTulemus()
                                        .getTulemuseKood();
                                String resultMessage = ret.getOtsiKasutajadResponse().getKeha().getTulemus()
                                        .getTulemuseTekst();
                                logger.debug("OtsiKasutajad result code: "
                                        + ((resultCode == null) ? "NULL" : resultCode.toString()));
                                logger.debug("OtsiKasutajad result message: " + resultMessage);

                                if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
                                    result = (foundUserCount.intValue() > 0);
                                    logger.debug("Successfully found out whether or not user " + userCode
                                            + " exists in 'teavituskalender' database "
                                            + (result ? "(exists)." : "(does not exist)."));
                                }
                            } else {
                                logger
                                        .error("Error finding user in 'teavituskalender' database. Response's 'tulemus' part is NULL. Related user code: "
                                                + userCode);
                            }
                        } else {
                            logger
                                    .error("Error finding user in 'teavituskalender' database. Response's 'keha' part is NULL. Related user code: "
                                            + userCode);
                        }
                    } else {
                        logger
                                .error("Error finding user in 'teavituskalender' database. Response's 'LisaSyndmusResponse' part is NULL. Related user code: "
                                        + userCode);
                    }
                } else {
                    logger
                            .error("Error finding user in 'teavituskalender' database. Response document is NULL. Related user code: "
                                    + userCode);
                }
            } finally {
                if (ctx != null) {
                    ctx.close();
                }
            }
        } catch (Exception ex) {
            logger.error("Error finding user in 'teavituskalender' database. Related user code: " + userCode, ex);
        }
        return result;
    }

    /**
     * Helper method to start application context.
     * 
     * @return Application context as {@link ClassPathXmlApplicationContext}
     *         object
     */
    public static ClassPathXmlApplicationContext startContext() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("xtee.xml");
        ctx.start();
        return ctx;
    }

    /**
     * Get marshaller.
     * 
     * @return marshaller
     */
    public Marshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Set marshaller.
     * 
     * @param marshaller marshaller
     */
    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Get unmarshaller.
     * 
     * @return unmarshaller
     */
    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Set unmarshaller.
     * 
     * @param unmarshaller unmarshaller
     */
    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * Get configuration.
     * 
     * @return configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Set configuration.
     * 
     * @param configuration configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
