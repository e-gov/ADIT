package ee.adit.dao.pojo;

public class DocumentFileDeflateResult implements java.io.Serializable {
	private static final long serialVersionUID = -1723731077807470907L;
	private Boolean success;

	public DocumentFileDeflateResult() {
	}

	public DocumentFileDeflateResult(Boolean success) {
		this.success = success;
	}

	public Boolean getSuccess() {
		return this.success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

}
