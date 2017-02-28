package ee.adit.dvk.api.container.v2;

public class AddressType extends ee.adit.dvk.api.container.AddressType
{
	private String ametikohaLyhinimetus;
	private String allyksuseLyhinimetus;

	public String getAmetikohaLyhinimetus() {
		return ametikohaLyhinimetus;
	}

	public void setAmetikohaLyhinimetus(String ametikohaLyhinimetus) {
		this.ametikohaLyhinimetus = ametikohaLyhinimetus;
	}

	public String getAllyksuseLyhinimetus() {
		return allyksuseLyhinimetus;
	}

	public void setAllyksuseLyhinimetus(String allyksuseLyhinimetus) {
		this.allyksuseLyhinimetus = allyksuseLyhinimetus;
	}

}
