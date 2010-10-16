package ee.adit.web.controller;

import java.text.DecimalFormat;

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
	
	private MonitorConfiguration monitorConfiguration;
	
	public MonitorController() {
		LOG.info("MONITORCONTROLLER created.");
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
		LOG.info("ADIT monitoring servlet invoked.");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("monitor.jsp");

		// 1. X-tee p�ringud
		//    - saveDocument()
		//    - getDocument()
		if(getMonitorService() == null)
			LOG.error("getMonitorService() == null");
		if(getConfiguration() == null)
			LOG.error("getConfiguration() == null");
		MonitorResult saveDocumentCheckResult = this.getMonitorService().saveDocumentCheck(getMonitorConfiguration().getTestDocumentId());
		
		
		
		if(saveDocumentCheckResult.isSuccess()) {
			DecimalFormat df = new DecimalFormat("0.000");
			mav.addObject("duration", df.format(saveDocumentCheckResult.getDuration()));
			mav.addObject("status", "OK");
		} else {
			mav.addObject("status", "FAIL");
			mav.addObject("exceptions", saveDocumentCheckResult.getExceptions());
		}
		
		
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

	public MonitorConfiguration getMonitorConfiguration() {
		return monitorConfiguration;
	}

	public void setMonitorConfiguration(MonitorConfiguration monitorConfiguration) {
		this.monitorConfiguration = monitorConfiguration;
	}
	
}
