package ee.adit.dvk.api.container;

import java.util.Date;

public class SignatureInfo {
	private Date signatureDate;
	private Date signatureTime;

	public Date getSignatureDate() {
		return signatureDate;
	}

	public void setSignatureDate(Date signatureDate) {
		this.signatureDate = signatureDate;
	}

	public Date getSignatureTime() {
		return signatureTime;
	}

	public void setSignatureTime(Date signatureTime) {
		this.signatureTime = signatureTime;
	}

}
