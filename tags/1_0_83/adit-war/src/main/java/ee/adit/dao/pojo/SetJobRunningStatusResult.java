package ee.adit.dao.pojo;

/**
 * Class for serializing result message of SET_JOB_RUNNING_STATUS
 * stored procedure (in database).
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class SetJobRunningStatusResult implements java.io.Serializable {
	private static final long serialVersionUID = 7782346620347891090L;
	private String resultCode;

    public SetJobRunningStatusResult() {
    }

    public SetJobRunningStatusResult(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
