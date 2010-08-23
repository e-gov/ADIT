package ee.adit.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import ee.adit.exception.AditCodedException;
import ee.adit.pojo.Message;
import ee.adit.util.Configuration;

public class MessageService {

	private static Logger LOG = Logger.getLogger(MessageService.class);
	
	private Configuration configuration;
	
	private MessageSource messageSource;

	public List<Message> getMessages(String messageCode, Object[] parameters) {
		List<Message> result = new ArrayList<Message>();
		
		try {
			Iterator<String> configuredLanguages = this.getConfiguration().getLocales().iterator();
			Locale[] locales = Locale.getAvailableLocales();
			
			while(configuredLanguages.hasNext()) {
				String language = configuredLanguages.next();
				
				for(int i = 0; i < locales.length; i++) {
					Locale locale = locales[i];
					String langCountry = locale.getLanguage() + "_" + locale.getCountry();
					if(langCountry.equalsIgnoreCase(language)) {
						LOG.debug("Adding message for language: " + language);
						String message = this.getMessageSource().getMessage(messageCode, parameters, locale);
						result.add(new Message(locale.getLanguage(), message));
					}
				}				
			}
			
		} catch (Exception e) {
			LOG.error("Exception while getting messages for message: " + messageCode, e);
		}
		
		return result;
	}
	
	public String getMessage(String code, Object[] parameters, Locale locale) {
		return this.getMessageSource().getMessage(code, new Object[] { parameters }, locale);
	}
	
	public List<Message> getMessages(AditCodedException e) {
		return getMessages(e.getMessage(), e.getParameters());
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
}
