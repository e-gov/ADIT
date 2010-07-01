package ee.adit.dao.pojo;

public class DocumentFileDeflateResult implements java.io.Serializable {
	private static final long serialVersionUID = -1723731077807470907L;
	private String resultCode;

	public DocumentFileDeflateResult() {
	}

	public DocumentFileDeflateResult(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultCode() {
		return this.resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
}
