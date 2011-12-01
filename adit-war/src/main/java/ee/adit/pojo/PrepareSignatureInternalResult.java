package ee.adit.pojo;

public class PrepareSignatureInternalResult {
    private boolean success;
    private String signatureHash;
    private ArrayOfDataFileHash dataFileHashes;
    private String errorCode;

    public PrepareSignatureInternalResult() {
        this.success = false;
        this.signatureHash = "";
        this.dataFileHashes = new ArrayOfDataFileHash();
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

    public ArrayOfDataFileHash getDataFileHashes() {
		return dataFileHashes;
	}

	public void setDataFileHashes(ArrayOfDataFileHash dataFileHashes) {
		this.dataFileHashes = dataFileHashes;
	}

	public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
