package ee.adit.pojo;

public class PrepareSignatureInternalResult {
	private boolean success;
	private String signatureHash;
	private String errorCode;
	
	public PrepareSignatureInternalResult() {
		this.success = false;
		this.signatureHash = "";
		this.errorCode = "";
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public String getSignatureHash() {
		return signatureHash;
	}

	public void setSignatureHash(String signatureHash) {
		this.signatureHash = signatureHash;
	}

	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
