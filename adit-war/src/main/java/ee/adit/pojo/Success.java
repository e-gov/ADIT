package ee.adit.pojo;

public class Success {

    private boolean success;

    // Do not remove default constructor!
    // It is required for XML mapping.
    public Success() {}
    
    public Success(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
