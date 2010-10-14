package ee.adit.web.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ee.adit.monitor.MonitorResult;
import ee.adit.service.MonitorService;
import ee.adit.util.Configuration;

@Controller
public class MonitorController {

	private static Logger LOG = Logger.getLogger(MonitorController.class);
	
	private MonitorService monitorService;
	
	private Configuration configuration;
	
	@RequestMapping("/monitor")
	public ModelAndView aditMonitor() {
		LOG.info("ADIT monitoring servlet invoked.");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("monitor.jsp");

		// 1. X-tee päringud
		//    - saveDocument()
		//    - getDocument()
		if(getMonitorService() == null)
			LOG.error("getMonitorService() == null");
		if(getConfiguration() == null)
			LOG.error("getConfiguration() == null");
		MonitorResult saveDocumentCheckResult = this.getMonitorService().saveDocumentCheck(getConfiguration().getTestDocumentID());
		
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
	
}
