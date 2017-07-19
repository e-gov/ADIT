package ee.adit.web.controller;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ee.adit.service.MonitorService;
import ee.adit.util.Configuration;
import ee.adit.util.MonitorResult;
import ee.adit.util.SecurityConfiguration;
import eu.europa.esig.dss.DSSRevocationUtils;

/**
 * Controller class for monitoring view.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class MonitorController extends AbstractController {

    /**
     * Log4J logger.
     */
    private static Logger logger = LogManager.getLogger(MonitorController.class);

    /**
     * Monitor service.
     */
    private MonitorService monitorService;

    /**
     * Configuration.
     */
    private Configuration configuration;

    /**
     * Default constructor.
     */
    public MonitorController() {
        logger.info("MONITORCONTROLLER created.");
        // testing whether BouncyCastle added or not
        logger.info("getting digest calculatore ");
        System.out.println("INITIATING SECURITY BC \n\n");
        SecurityConfiguration.init();
        DSSRevocationUtils.getSHA1DigestCalculator();
    }

    /**
     * Performs the actual monitoring and returns the response data.
     *
     * @param arg0 HTTP request
     * @param arg1 HTTP response
     * @return model and view
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {

        String requestURI = arg0.getRequestURI();
        String serverName = arg0.getServerName();
        int serverPort = arg0.getServerPort();
        logger.debug("requestURI: " + requestURI);
        logger.debug("serverName: " + serverName);
        logger.debug("serverPort: " + serverPort);

        // http://locahost:8080/adit/monitor

        String serviceURI = "http://" + serverName + ":" + serverPort + "/adit/service";

        logger.info("ADIT monitoring servlet invoked.");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("monitor.jsp");

        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator('.');
        formatSymbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("0.000", formatSymbols);

        List<MonitorResult> results = new ArrayList<MonitorResult>();
        boolean summaryStatusOk = true;

		
		
        try {
			logger.error("tmpshit" + getServletContext().getRealPath(arg0.getContextPath()));
            double duration = 0;
            Date start = new Date();
            long startTime = start.getTime();

            if (getMonitorService() == null) {
                logger.error("getMonitorService() == null");
            }
            if (getConfiguration() == null) {
                logger.error("getConfiguration() == null");
            }

            // 1. SAVE_DOCUMENT
            MonitorResult saveDocumentCheckResult = this.getMonitorService().saveDocumentCheck(serviceURI);
            saveDocumentCheckResult.setDurationString(df.format(saveDocumentCheckResult.getDuration()));
            if (saveDocumentCheckResult.isSuccess()) {
                saveDocumentCheckResult.setStatusString(MonitorService.OK);
            } else {
                saveDocumentCheckResult.setStatusString(MonitorService.FAIL);
                summaryStatusOk = false;
            }
            if (saveDocumentCheckResult.getExceptions() != null && saveDocumentCheckResult.getExceptions().size() > 0) {
                saveDocumentCheckResult.setExceptionString(saveDocumentCheckResult.getExceptions().get(0));
            }
            results.add(saveDocumentCheckResult);

            // 2. GET_DOCUMENT
            MonitorResult getDocumentCheckResult = this.getMonitorService().getDocumentCheck(serviceURI);
            getDocumentCheckResult.setDurationString(df.format(getDocumentCheckResult.getDuration()));
            if (getDocumentCheckResult.isSuccess()) {
                getDocumentCheckResult.setStatusString(MonitorService.OK);
            } else {
                getDocumentCheckResult.setStatusString(MonitorService.FAIL);
                summaryStatusOk = false;
            }
            if (getDocumentCheckResult.getExceptions() != null && getDocumentCheckResult.getExceptions().size() > 0) {
                getDocumentCheckResult.setExceptionString(getDocumentCheckResult.getExceptions().get(0));
            }
            results.add(getDocumentCheckResult);

            // 3. DHX_SEND
            MonitorResult dhxSendCheckResult = this.getMonitorService().checkDhxSend();
            dhxSendCheckResult.setDurationString(df.format(dhxSendCheckResult.getDuration()));
            if (dhxSendCheckResult.isSuccess()) {
            	dhxSendCheckResult.setStatusString(MonitorService.OK);
            } else {
            	dhxSendCheckResult.setStatusString(MonitorService.FAIL);
                summaryStatusOk = false;
            }
            if (dhxSendCheckResult.getExceptions() != null && dhxSendCheckResult.getExceptions().size() > 0) {
            	dhxSendCheckResult.setExceptionString(dhxSendCheckResult.getExceptions().get(0));
            }
            results.add(dhxSendCheckResult);

            // 4. GET_USER_INFO
            MonitorResult getUserInfoCheckResult = this.getMonitorService().getUserInfoCheck(serviceURI);
            getUserInfoCheckResult.setDurationString(df.format(getUserInfoCheckResult.getDuration()));
            if (getUserInfoCheckResult.isSuccess()) {
                getUserInfoCheckResult.setStatusString(MonitorService.OK);
            } else {
                getUserInfoCheckResult.setStatusString(MonitorService.FAIL);
                summaryStatusOk = false;
            }
            if (getUserInfoCheckResult.getExceptions() != null && getUserInfoCheckResult.getExceptions().size() > 0) {
                getUserInfoCheckResult.setExceptionString(getUserInfoCheckResult.getExceptions().get(0));
            }
            results.add(getUserInfoCheckResult);

            // 5. NOTIFICATIONS
            MonitorResult checkNotificationsResult = this.getMonitorService().checkNotifications();
            checkNotificationsResult.setDurationString(df.format(checkNotificationsResult.getDuration()));
            if (checkNotificationsResult.isSuccess()) {
                checkNotificationsResult.setStatusString(MonitorService.OK);
            } else {
                checkNotificationsResult.setStatusString(MonitorService.FAIL);
                summaryStatusOk = false;
            }
            if (checkNotificationsResult.getExceptions() != null && checkNotificationsResult.getExceptions().size() > 0) {
                checkNotificationsResult.setExceptionString(checkNotificationsResult.getExceptions().get(0));
            }
            results.add(checkNotificationsResult);

            // 6. ERROR_LOG
            MonitorResult checkErrorLogResult = this.getMonitorService().checkErrorLog();
            checkErrorLogResult.setDurationString(df.format(checkErrorLogResult.getDuration()));
            if (checkErrorLogResult.isSuccess()) {
                checkErrorLogResult.setStatusString(MonitorService.OK);
            } else {
                checkErrorLogResult.setStatusString(MonitorService.FAIL);
            }
            if (checkErrorLogResult.getExceptions() != null && checkErrorLogResult.getExceptions().size() > 0) {
                checkErrorLogResult.setExceptionString(checkErrorLogResult.getExceptions().get(0));
            }
            results.add(checkErrorLogResult);

            Date end = new Date();
            long endTime = end.getTime();
            duration = (endTime - startTime) / 1000.0;

            // 7. SUMMARY_STATUS
            MonitorResult summaryStatusResult = new MonitorResult();
            summaryStatusResult.setComponent("SUMMARY_STATUS");
            summaryStatusResult.setDurationString(df.format(duration));
            summaryStatusResult.setExceptionString(df.format(duration));
            summaryStatusResult.setSuccess(summaryStatusOk);

            if (summaryStatusOk) {
                summaryStatusResult.setStatusString(MonitorService.OK);
            } else {
                summaryStatusResult.setStatusString(MonitorService.FAIL);
            }

            results.add(summaryStatusResult);

        } catch (Exception e) {
            logger.error("Error while invoking monitoring controller: ", e);
        }

        mav.addObject("results", results);

        return mav;
    }

    /**
     * Get monitor service.
     * @return monitor service
     */
    public MonitorService getMonitorService() {
        return monitorService;
    }

    /**
     * Set monitor service.
     * @param monitorService monitor service
     */
    public void setMonitorService(MonitorService monitorService) {
        logger.info("Setting MONITORSERVICE on MONITORCONTROLLER");
        this.monitorService = monitorService;
    }

    /**
     * Get configuration.
     * @return configuration
     */
    public Configuration getConfiguration() {
        logger.info("Setting CONFIGURATION on MONITORCONTROLLER");
        return configuration;
    }

    /**
     * Set configuration.
     * @param configuration configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
