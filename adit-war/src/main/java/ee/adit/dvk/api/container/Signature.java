package ee.adit.dvk.api.container;


public class Signature
{
	private Person person;
	private SignatureInfo signatureInfo;

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public SignatureInfo getSignatureInfo() {
		return signatureInfo;
	}

	public void setSignatureInfo(SignatureInfo signatureInfo) {
		this.signatureInfo = signatureInfo;
	}

}
