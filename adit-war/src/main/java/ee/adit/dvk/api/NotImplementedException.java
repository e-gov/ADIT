package ee.adit.dvk.api;

/**
 * @author User
 *         Exception class notifying that some functionality which is to be
 *         implemented is absent.
 */
public class NotImplementedException extends RuntimeException {
    private static final long serialVersionUID = -4407708877036613695L;

    public NotImplementedException() {
        super("This logic is not implemented.");
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }
}
