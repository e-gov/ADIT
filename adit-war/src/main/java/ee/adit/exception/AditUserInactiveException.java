package ee.adit.exception;

public class AditUserInactiveException extends AditCodedException {

    private static final String USER_INACTIVE_MESSAGE_CODE = "user.inactive";
    private String inactiveUserPersonalCode;

    public AditUserInactiveException(String userPersonalCode) {
        super(USER_INACTIVE_MESSAGE_CODE);
        setInactiveUserPersonalCode(userPersonalCode);
    }

    public String getInactiveUserPersonalCode() {
        return inactiveUserPersonalCode;
    }

    public void setInactiveUserPersonalCode(String inactiveUserPersonalCode) {
        setParameters(new Object[]{inactiveUserPersonalCode});
        this.inactiveUserPersonalCode = inactiveUserPersonalCode;
    }
}
