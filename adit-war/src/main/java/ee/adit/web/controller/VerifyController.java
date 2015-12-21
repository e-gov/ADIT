package ee.adit.web.controller;


import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ee.adit.service.DocumentService;
import ee.adit.util.Configuration;
import ee.adit.util.Util;

/**
 * Controller class for monitoring view.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class VerifyController extends AbstractController {

    /**
     * Log4J logger.
     */
    private static Logger logger = Logger.getLogger(VerifyController.class);

    /**
     * Document service.
     */
    private DocumentService documentService;
    
    private Boolean checkSignatures;
    
    private String digidocConfigurationFile; 
    /**
     * Configuration.
     */
    private Configuration configuration;
    /**
     * Default constructor.
     */
    public VerifyController() {
        logger.info("VerifyController created.");
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
        logger.info("ADIT verify servlet invoked.");
        ModelAndView mav = new ModelAndView();
        if (getCheckSignatures()) {
        	InputStream input = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(getDigidocConfigurationFile());
			String jdigidocCfgTmpFile = Util.createTemporaryFile(input,
					getConfiguration().getTempDir());
			logger.debug("JDigidoc.cfg file created as a temporary file: '"
					+ jdigidocCfgTmpFile + "'");
	        getDocumentService().verifySignedDocuments(jdigidocCfgTmpFile);
	        mav.setViewName("verify.jsp");
	        mav.addObject("results", "Documents are verified. Results are in the log file");
        } else {
        	 mav.addObject("results", "Documents verification is turned off in configuration");
        }
        return mav;
    }

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	public String getDigidocConfigurationFile() {
		return digidocConfigurationFile;
	}

	public void setDigidocConfigurationFile(String digidocConfigurationFile) {
		this.digidocConfigurationFile = digidocConfigurationFile;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public Boolean getCheckSignatures() {
		return checkSignatures;
	}

	public void setCheckSignatures(Boolean checkSignatures) {
		this.checkSignatures = checkSignatures;
	}

}
