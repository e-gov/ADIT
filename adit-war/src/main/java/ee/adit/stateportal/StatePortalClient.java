package ee.adit.stateportal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ee.adit.pojo.EmailAddress;
import ee.adit.util.Util;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusDocument;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusDocument.TellimusteStaatus;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusResponseDocument;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusResponseDocument.TellimusteStaatusResponse.Keha.Suunamised.Suunamine;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusResponseDocument.TellimusteStaatusResponse.Keha.TkalTeenused.TkalTeenus;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusResponseDocument.TellimusteStaatusResponse.Keha.TkalTeenused.TkalTeenus.EpostStaatus;
import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;
import ee.webmedia.xtee.client.service.StandardXTeeConsumer;

/**
 * Web service client class for State Portal (Riigiportaal) X-Road database.
 * Enables execution of state portal web service requests.
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public final class StatePortalClient {

    private static Logger logger = LogManager.getLogger(StatePortalClient.class);

    /**
     * Result code OK.
     */
    private static final int RESULT_OK = 0;

    /**
     * Default constructors.
     */
    private StatePortalClient() { }

    /**
     * Executes X-Road request "riigiportaal.tellimusteStaatus.v1" and returns
     * data about given persons ordering status of specified notification. <br>
     * <br>
     * This method throws no exceptions even if failing. This is necessary to
     * avoid breaking current applications main functionality, even if
     * interfaces with other systems are temporarily unavailable.
     *
     * @param userCode
     *            Personal ID code of person, whose notification ordering status
     *            will be checked.
     * @param eventTypeName
     *            Full name of notification type. Only this type's ordering
     *            status will be checked.
     * @return Data about notification status as {@link NotificationStatus}
     *         object.
     */
    public static NotificationStatus getNotificationStatus(String userCode, String eventTypeName) {
        NotificationStatus result = null;

        String databaseName = "riigiportaal";
        String queryName = "tellimusteStaatus";
        String queryVersion = "v1";
        String orgCodeForLog = "adit";

        try {
            TellimusteStaatusDocument doc = TellimusteStaatusDocument.Factory.newInstance();
            TellimusteStaatus req = doc.addNewTellimusteStaatus();
            TellimusteStaatusDocument.TellimusteStaatus.Keha keha = req.addNewKeha();

            // Remove country prefix because "riigiportaal" database
            // does not support ID codes beginning with country prefix
            keha.setIsikukood(Util.getPersonalIdCodeWithoutCountryPrefix(userCode));

            ClassPathXmlApplicationContext ctx = null;
            try {
                ctx = startContext();
                StandardXTeeConsumer xteeService = (StandardXTeeConsumer) ctx.getBean("xteeConsumer");
                SimpleXTeeServiceConfiguration conf = (SimpleXTeeServiceConfiguration) xteeService.getServiceConfiguration();
                conf.setDatabase(databaseName);
                conf.setMethod(queryName);
                conf.setVersion(queryVersion);
                orgCodeForLog = conf.getInstitution();
                TellimusteStaatusResponseDocument ret = (TellimusteStaatusResponseDocument) xteeService.sendRequest(doc, conf);

                if (ret != null) {
                    if (ret.getTellimusteStaatusResponse() != null) {
                        if (ret.getTellimusteStaatusResponse().getKeha() != null) {
                            if (ret.getTellimusteStaatusResponse().getKeha().getResult() != null) {
                                BigInteger resultCode = ret.getTellimusteStaatusResponse().getKeha().getResult().getResultCode();
                                String resultMessage = ret.getTellimusteStaatusResponse().getKeha().getResult().getResultText();
                                logger.debug("TellimusteStaatus result code: "
                                        + ((resultCode == null) ? "NULL" : resultCode.toString()));
                                logger.debug("TellimusteStaatus result message: " + resultMessage);

                                if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
                                    result = new NotificationStatus();
                                    result.setNotificationTypeName(eventTypeName);
                                    if (ret.getTellimusteStaatusResponse().getKeha().getTkalTeenused() != null) {
                                        List<TkalTeenus> notifications = ret.getTellimusteStaatusResponse().getKeha()
                                                .getTkalTeenused().getTkalTeenusList();
                                        for (TkalTeenus item : notifications) {
                                            if (eventTypeName.equalsIgnoreCase(item.getNimetus())) {
                                                result.setNotificationEmailStatus(item.getEpostStaatus() != EpostStaatus.NO);
                                                break;
                                            }
                                        }
                                    } else {
                                        logger.debug("Notifications list is NULL!");
                                    }

                                    if (ret.getTellimusteStaatusResponse().getKeha().getSuunamised() != null) {
                                        result.setEmailList(new ArrayList<EmailAddress>());
                                        List<Suunamine> emails = ret.getTellimusteStaatusResponse().getKeha()
                                                .getSuunamised().getSuunamineList();
                                        for (Suunamine item : emails) {
                                            EmailAddress address = new EmailAddress();
                                            address.setAddress(item.getEpost());
                                            address.setRedirectedTo(item.getAadress());
                                            result.getEmailList().add(address);
                                        }
                                    }

                                    logger.debug("Successfully retreived notification status from 'riigiportaal' database. Related user: "
                                                    + userCode);
                                }
                            } else {
                                logger.error("Error getting notification status from 'riigiportaal' database. Response's 'tulemus' part is NULL. Related user: "
                                                + userCode);
                            }
                        } else {
                            logger.error("Error getting notification status from 'riigiportaal' database. Response's 'keha' part is NULL. Related user: "
                                            + userCode);
                        }
                    } else {
                        logger.error("Error getting notification status from 'riigiportaal' database. Response's 'LisaSyndmusResponse' part is NULL. Related user: "
                                        + userCode);
                    }
                } else {
                    logger.error("Error getting notification status from 'riigiportaal' database. Response document is NULL. Related user: "
                                    + userCode);
                }
            } finally {
                if (ctx != null) {
                    ctx.close();
                }
            }
        } catch (Exception ex) {
        	String errorMessage = String.format("Error getting notification status from '%s' database. Related user: %s."
        		+ " Please verify that organization '%s' is allowed to run '%s' query in '%s' database.",
        		databaseName, userCode, orgCodeForLog, queryName, databaseName);
            logger.error(errorMessage, ex);
        }
        return result;
    }

    /**
     * Helper method to start application context.
     *
     * @return Application context.
     */
    private static ClassPathXmlApplicationContext startContext() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("xtee.xml");
        ctx.start();
        return ctx;
    }
}
