package ee.adit.exception;

/**
 * Business logic exception - returned with the web-service response.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 * 
 */
public class AditException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param message error message
     */
    public AditException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message error message
     * @param cause error cause
     */
    public AditException(String message, Throwable cause) {
        super(message, cause);
    }

}
