package ee.adit.exception;

/**
 * Internal exception that is not meant for returning in the web-service response.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class AditInternalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param message error message
     */
    public AditInternalException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message error message
     * @param cause error cause
     */
    public AditInternalException(String message, Throwable cause) {
        super(message, cause);
    }

}
