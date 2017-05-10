package ee.adit.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ee.adit.service.DocumentService;
import ee.adit.util.Configuration;


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
    private static Logger logger = LogManager.getLogger(VerifyController.class);

	/**
	 * Document service.
	 */
	private DocumentService documentService;

	private Boolean checkSignatures;

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
	 * @param arg0
	 *            HTTP request
	 * @param arg1
	 *            HTTP response
	 * @return model and view
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
		logger.info("ADIT verify servlet invoked.");

		ModelAndView mav = new ModelAndView();
		if (getCheckSignatures()) {
			getDocumentService().verifySignedDocuments();

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

