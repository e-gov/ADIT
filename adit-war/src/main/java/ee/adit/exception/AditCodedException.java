package ee.adit.exception;

/**
 * Document sharing data access class. Provides methods for retrieving and manipulating
 * document sharing data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class AditCodedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Parameter array.
     */
    private Object[] parameters;

    /**
     * Constructor.
     * 
     * @param message error message
     */
    public AditCodedException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message error message
     * @param cause cause exception
     */
    public AditCodedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get parameter array.
     * 
     * @return parameter array
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Set parameter array.
     * 
     * @param parameters parameters
     */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

}
