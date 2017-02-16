package ee.adit.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.digidoc4j.Configuration;

public class AppContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		Configuration configuration = Configuration.getInstance();
		
		// Loading TSL list at the application start-up to speed-up signing process
		configuration.getTSL().refresh();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {}

}
