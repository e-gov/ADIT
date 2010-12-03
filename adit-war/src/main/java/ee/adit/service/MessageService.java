package ee.adit.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import ee.adit.exception.AditCodedException;
import ee.adit.pojo.Message;
import ee.adit.util.Configuration;

/**
 * Provides methods for retrieving messages from Spring {@code MessageSource}.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class MessageService {

    /**
     * Log4J logger
     */
    private static Logger logger = Logger.getLogger(MessageService.class);

    /**
     * Configuration
     */
    private Configuration configuration;

    /**
     * Message source
     */
    private MessageSource messageSource;

    /**
     * Retrieves messages for the specified code.
     * 
     * @param messageCode
     *            message code
     * @param parameters
     *            parameters for creating the messages
     * @return List of {@link Message} objects for given message code. Contains
     *         same message in different UI languages.
     */
    public List<Message> getMessages(String messageCode, Object[] parameters) {
        List<Message> result = new ArrayList<Message>();

        try {
            Iterator<String> configuredLanguages = this.getConfiguration().getLocales().iterator();
            Locale[] locales = Locale.getAvailableLocales();

            while (configuredLanguages.hasNext()) {
                String language = configuredLanguages.next();

                for (int i = 0; i < locales.length; i++) {
                    Locale locale = locales[i];
                    String langCountry = locale.getLanguage() + "_" + locale.getCountry();
                    if (langCountry.equalsIgnoreCase(language)) {
                        logger.debug("Adding message for language: " + language);
                        String message = this.getMessageSource().getMessage(messageCode, parameters, locale);
                        result.add(new Message(locale.getLanguage(), message));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Exception while getting messages for message: " + messageCode, e);
        }

        return result;
    }

    /**
     * Retrieves messages for the specified locale.
     * 
     * @param code
     *            message code
     * @param parameters
     *            message parameters
     * @param locale
     *            message locale
     * @return Message as text
     */
    public String getMessage(String code, Object[] parameters, Locale locale) {
        return this.getMessageSource().getMessage(code, parameters, locale);
    }

    /**
     * Retrieves messages for the specified {@code AditCodedException}.
     * 
     * @param e
     *            exception
     * @return List of {@link Message} objects for given exception. Contains
     *         same message in different UI languages.
     */
    public List<Message> getMessages(AditCodedException e) {
        return getMessages(e.getMessage(), e.getParameters());
    }

    /**
     * Retrieves the configuration
     * 
     * @return Application configuration as {@link Configuration} object
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration
     * 
     * @param configuration
     *            Application configuration as {@link Configuration} object
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Retrieves the message source
     * 
     * @return Message source
     */
    public MessageSource getMessageSource() {
        return messageSource;
    }

    /**
     * Sets the message source
     * 
     * @param messageSource
     *            Message source
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

}
