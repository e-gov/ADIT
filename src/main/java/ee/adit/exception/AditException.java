package ee.adit.exception;

/**
 * Äriloogiline viga - tagastatakse ka veebiteenuse päringu vastuses.
 * 
 * @author markkur
 *
 */
public class AditException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AditException(String message) {
		super(message);
	}
	
	public AditException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
