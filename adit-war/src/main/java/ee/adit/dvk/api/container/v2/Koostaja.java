package ee.adit.dvk.api.container.v2;

public class Koostaja
{
	private String eesnimi;
	private String perenimi;
	private String ametinimetus;
	private String epost;
	private String telefon;

	public String getEesnimi() {
		return eesnimi;
	}

	public void setEesnimi(String eesnimi) {
		this.eesnimi = eesnimi;
	}

	public String getPerenimi() {
		return perenimi;
	}

	public void setPerenimi(String perenimi) {
		this.perenimi = perenimi;
	}

	public String getAmetinimetus() {
		return ametinimetus;
	}

	public void setAmetinimetus(String ametinimetus) {
		this.ametinimetus = ametinimetus;
	}

	public String getEpost() {
		return epost;
	}

	public void setEpost(String epost) {
		this.epost = epost;
	}

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

}
