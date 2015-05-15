package ee.adit.exception;

/**
 * ADIT rakenduse sisemine viga, mis ei ole mõeldud veebiteenuse päringu vastuses tagastamiseks.
 * 
 * @author markkur
 *
 */
public class AditInternalException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AditInternalException(String message) {
		super(message);
	}
	
	public AditInternalException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
