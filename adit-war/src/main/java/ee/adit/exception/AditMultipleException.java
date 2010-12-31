package ee.adit.exception;

import java.util.List;

import ee.adit.pojo.Message;

/**
 * Multiple language code error. The exception encapsulates error messages for multiple languages.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class AditMultipleException extends AditException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Message list.
     */
    private List<Message> messages;

    /**
     * Constructor.
     * 
     * @param message error message
     */
    public AditMultipleException(String message) {
        super(message);
    }

    /**
     * Get message list.
     * 
     * @return message list
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Set message list.
     * 
     * @param messages message list
     */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

}
