package ee.adit.dvk.api.container;


public class AddressType extends XmlBlock
{
	private String isikukood;
	private String ametikohaNimetus;
	private String epost;
	private String nimi;
	private String osakonnaKood;
	private String osakonnaNimi;
	private String regNr;
	private String asutuseNimi;
	private String allyksuseNimetus;

	public String getRegNr() {
		return regNr;
	}

	public void setRegNr(String regNr) {
		this.regNr = regNr;
	}

	public String getAsutuseNimi() {
		return asutuseNimi;
	}

	public void setAsutuseNimi(String asutuseNimi) {
		this.asutuseNimi = asutuseNimi;
	}

	public String getAllyksuseNimetus() {
		return allyksuseNimetus;
	}

	public void setAllyksuseNimetus(String allyksuseNimetus) {
		this.allyksuseNimetus = allyksuseNimetus;
	}

	public String getIsikukood() {
		return isikukood;
	}

	public void setIsikukood(String isikukood) {
		this.isikukood = isikukood;
	}

	public String getAmetikohaNimetus() {
		return ametikohaNimetus;
	}

	public void setAmetikohaNimetus(String ametikohaNimetus) {
		this.ametikohaNimetus = ametikohaNimetus;
	}

	public String getEpost() {
		return epost;
	}

	public void setEpost(String epost) {
		this.epost = epost;
	}

	public String getNimi() {
		return nimi;
	}

	public void setNimi(String nimi) {
		this.nimi = nimi;
	}

	public String getOsakonnaKood() {
		return osakonnaKood;
	}

	public void setOsakonnaKood(String osakonnaKood) {
		this.osakonnaKood = osakonnaKood;
	}

	public String getOsakonnaNimi() {
		return osakonnaNimi;
	}

	public void setOsakonnaNimi(String osakonnaNimi) {
		this.osakonnaNimi = osakonnaNimi;
	}
}
