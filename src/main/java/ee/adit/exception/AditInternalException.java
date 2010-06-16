package ee.adit.exception;

public class AditInternalException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AditInternalException(String message) {
		super(message);
	}
	
	public AditInternalException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
