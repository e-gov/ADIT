package ee.adit.web.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MonitorController {

	private static Logger LOG = Logger.getLogger(MonitorController.class);
	
	@RequestMapping("/monitor")
	public ModelAndView aditMonitor() {
		LOG.info("ADIT monitoring servlet invoked.");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("index.jsp");
		return mav;
	}
	
}
