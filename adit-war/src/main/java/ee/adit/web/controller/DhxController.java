package ee.adit.web.controller;


import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import ee.adit.service.DocumentService;
import ee.adit.util.Configuration;
import ee.adit.util.Util;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.ws.context.AppContext;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;

/**
 * Controller class for monitoring view.
 *
 * @author Aleksei Kokarev
 */
public class DhxController extends AbstractController {

    /**
     * Log4J logger.
     */
    private static Logger logger = LogManager.getLogger(DhxController.class);

    @Autowired
    private AddressService addressService;
    
    
    /**
     * Default constructor.
     */
    public DhxController() {
        logger.info("DhxController created.");
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
        logger.info("ADIT DHx servlet invoked.");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("dhx.jsp");
        try {
        	getAddressService().renewAddressList();
	        mav.addObject("results", "DHX addressregistry is renewed");

        } catch(DhxException ex) {
        	mav.addObject("results", "Error occured while renewing DHX addressregistry. " + ex.getMessage());
        }
        return mav;
    }


	/**
	 * @return the addressService
	 */
	public AddressService getAddressService() {
		return addressService;
	}


	/**
	 * @param addressService the addressService to set
	 */
	public void setAddressService(AddressService addressService) {
		this.addressService = addressService;
	}



}