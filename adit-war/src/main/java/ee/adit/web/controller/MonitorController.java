package ee.adit.web.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ee.adit.monitor.MonitorResult;
import ee.adit.service.MonitorService;
import ee.adit.util.Configuration;
import ee.adit.util.MonitorConfiguration;

public class MonitorController extends AbstractController {

	private static Logger LOG = Logger.getLogger(MonitorController.class);
	
	private MonitorService monitorService;
	
	private Configuration configuration;
	
	public MonitorController() {
		LOG.info("MONITORCONTROLLER created.");
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
		LOG.info("ADIT monitoring servlet invoked.");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("monitor.jsp");
		DecimalFormat df = new DecimalFormat("0.000");
		
		List<MonitorResult> results = new ArrayList<MonitorResult>();
		
		try {
			
			if(getMonitorService() == null)
				LOG.error("getMonitorService() == null");
			if(getConfiguration() == null)
				LOG.error("getConfiguration() == null");
			
			// 1. SAVE_DOCUMENT
			MonitorResult saveDocumentCheckResult = this.getMonitorService().saveDocumentCheck();
			saveDocumentCheckResult.setDurationString(df.format(saveDocumentCheckResult.getDuration()));
			if(saveDocumentCheckResult.isSuccess()) {
				saveDocumentCheckResult.setStatusString(MonitorService.OK);
			} else {
				saveDocumentCheckResult.setStatusString(MonitorService.FAIL);
			}
			if(saveDocumentCheckResult.getExceptions() != null && saveDocumentCheckResult.getExceptions().size() > 0) {
				saveDocumentCheckResult.setExceptionString(saveDocumentCheckResult.getExceptions().get(0));
			}
			results.add(saveDocumentCheckResult);
			
			// 2. GET_DOCUMENT
			MonitorResult getDocumentCheckResult = this.getMonitorService().getDocumentCheck();
			getDocumentCheckResult.setDurationString(df.format(getDocumentCheckResult.getDuration()));
			if(getDocumentCheckResult.isSuccess()) {
				getDocumentCheckResult.setStatusString(MonitorService.OK);
			} else {
				getDocumentCheckResult.setStatusString(MonitorService.FAIL);
			}
			if(getDocumentCheckResult.getExceptions() != null && getDocumentCheckResult.getExceptions().size() > 0) {
				getDocumentCheckResult.setExceptionString(getDocumentCheckResult.getExceptions().get(0));
			}
			results.add(getDocumentCheckResult);
			
			// 3. DVK_SEND
			MonitorResult dvkSendCheckResult = this.getMonitorService().checkDvkSend();
			dvkSendCheckResult.setDurationString(df.format(dvkSendCheckResult.getDuration()));
			if(dvkSendCheckResult.isSuccess()) {
				dvkSendCheckResult.setStatusString(MonitorService.OK);
			} else {
				dvkSendCheckResult.setStatusString(MonitorService.FAIL);
			}
			if(dvkSendCheckResult.getExceptions() != null && dvkSendCheckResult.getExceptions().size() > 0) {
				dvkSendCheckResult.setExceptionString(dvkSendCheckResult.getExceptions().get(0));
			}
			results.add(dvkSendCheckResult);
			
			// 4. DVK_CLIENT_TO_DVK_SEND
			MonitorResult checkDvkClientToDvkSendResult = this.getMonitorService().checkDvkClientToDvkSend();
			checkDvkClientToDvkSendResult.setDurationString(df.format(checkDvkClientToDvkSendResult.getDuration()));
			if(checkDvkClientToDvkSendResult.isSuccess()) {
				checkDvkClientToDvkSendResult.setStatusString(MonitorService.OK);
			} else {
				checkDvkClientToDvkSendResult.setStatusString(MonitorService.FAIL);
			}
			if(checkDvkClientToDvkSendResult.getExceptions() != null && checkDvkClientToDvkSendResult.getExceptions().size() > 0) {
				checkDvkClientToDvkSendResult.setExceptionString(checkDvkClientToDvkSendResult.getExceptions().get(0));
			}
			results.add(checkDvkClientToDvkSendResult);
			
			// 2. ADIT -> DVK
			//    - ADIT -> DVK UK
			//    - DVK UK -> DVK

			// 3. DVK -> ADIT   
			//    - DVK -> DVK UK
			//    - DVK UK -> ADIT
			
			// 4. Kasutajad
			//    - testkasutaja päring ADIT-ist üle X-tee
			
			// 5. Teavituskalendri liides
			//    - kas on teavitusi, mille saatmine on ebaõnnestunud
			
			// 6. Rakenduse vead (tabelist ERROR_LOG, kus level = FATAL/ERROR)	
		
		} catch (Exception e) {
			LOG.error("Error while invoking monitoring controller: ", e);
		}
		
		mav.addObject("results", results);
		
		return mav;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		LOG.info("Setting MONITORSERVICE on MONITORCONTROLLER");
		this.monitorService = monitorService;
	}

	public Configuration getConfiguration() {
		LOG.info("Setting CONFIGURATION on MONITORCONTROLLER");
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
}
