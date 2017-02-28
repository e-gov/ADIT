package ee.adit.dvk.api.container.v1;

public class AddressType extends ee.adit.dvk.api.container.AddressType
{
	protected Long ametikohaKood;
	protected String allyksuseKood;

	public String getAllyksuseKood() {
		return allyksuseKood;
	}

	public void setAllyksuseKood(String allyksuseKood) {
		this.allyksuseKood = allyksuseKood;
	}

	public Long getAmetikohaKood() {
		return ametikohaKood;
	}

	public void setAmetikohaKood(Long ametikohaKood) {
		this.ametikohaKood = ametikohaKood;
	}
}
