package ee.adit.exception;

/**
 * 
 * @author markkur
 * 
 */
public class AditCodedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Object[] parameters;

    public AditCodedException(String message) {
        super(message);
    }

    public AditCodedException(String message, Throwable cause) {
        super(message, cause);
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

}
