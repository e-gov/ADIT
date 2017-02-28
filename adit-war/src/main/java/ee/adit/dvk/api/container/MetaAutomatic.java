package ee.adit.dvk.api.container;

import java.util.Date;

public abstract class MetaAutomatic
{
	private String dhlSaabumisviis;
	private Date dhlSaabumisaeg;
	private String dhlSaatmisviis;
	private Date dhlSaatmisaeg;
	private String dhlSaatjaAsutuseNr;
	private String dhlSaatjaAsutuseNimi;
	private String dhlSaatjaIsikukood;
	private String dhlSaajaAsutuseNr;
	private String dhlSaajaAsutuseNimi;
	private String dhlSaajaIsikukood;
	private String dhlSaatjaEpost;
	private String dhlSaajaEpost;
	private DhlEmailHeader dhlEmailHeader;
	private String dhlKaust;

	public String getDhlSaabumisviis() {
		return dhlSaabumisviis;
	}

	public void setDhlSaabumisviis(String dhlSaabumisviis) {
		this.dhlSaabumisviis = dhlSaabumisviis;
	}

	public Date getDhlSaabumisaeg() {
		return dhlSaabumisaeg;
	}

	public void setDhlSaabumisaeg(Date dhlSaabumisaeg) {
		this.dhlSaabumisaeg = dhlSaabumisaeg;
	}

	public String getDhlSaatmisviis() {
		return dhlSaatmisviis;
	}

	public void setDhlSaatmisviis(String dhlSaatmisviis) {
		this.dhlSaatmisviis = dhlSaatmisviis;
	}

	public Date getDhlSaatmisaeg() {
		return dhlSaatmisaeg;
	}

	public void setDhlSaatmisaeg(Date dhlSaatmisaeg) {
		this.dhlSaatmisaeg = dhlSaatmisaeg;
	}

	public String getDhlSaatjaAsutuseNr() {
		return dhlSaatjaAsutuseNr;
	}

	public void setDhlSaatjaAsutuseNr(String dhlSaatjaAsutuseNr) {
		this.dhlSaatjaAsutuseNr = dhlSaatjaAsutuseNr;
	}

	public String getDhlSaatjaAsutuseNimi() {
		return dhlSaatjaAsutuseNimi;
	}

	public void setDhlSaatjaAsutuseNimi(String dhlSaatjaAsutuseNimi) {
		this.dhlSaatjaAsutuseNimi = dhlSaatjaAsutuseNimi;
	}

	public String getDhlSaatjaIsikukood() {
		return dhlSaatjaIsikukood;
	}

	public void setDhlSaatjaIsikukood(String dhlSaatjaIsikukood) {
		this.dhlSaatjaIsikukood = dhlSaatjaIsikukood;
	}

	public String getDhlSaajaAsutuseNr() {
		return dhlSaajaAsutuseNr;
	}

	public void setDhlSaajaAsutuseNr(String dhlSaajaAsutuseNr) {
		this.dhlSaajaAsutuseNr = dhlSaajaAsutuseNr;
	}

	public String getDhlSaajaAsutuseNimi() {
		return dhlSaajaAsutuseNimi;
	}

	public void setDhlSaajaAsutuseNimi(String dhlSaajaAsutuseNimi) {
		this.dhlSaajaAsutuseNimi = dhlSaajaAsutuseNimi;
	}

	public String getDhlSaajaIsikukood() {
		return dhlSaajaIsikukood;
	}

	public void setDhlSaajaIsikukood(String dhlSaajaIsikukood) {
		this.dhlSaajaIsikukood = dhlSaajaIsikukood;
	}

	public String getDhlSaatjaEpost() {
		return dhlSaatjaEpost;
	}

	public void setDhlSaatjaEpost(String dhlSaatjaEpost) {
		this.dhlSaatjaEpost = dhlSaatjaEpost;
	}

	public String getDhlSaajaEpost() {
		return dhlSaajaEpost;
	}

	public void setDhlSaajaEpost(String dhlSaajaEpost) {
		this.dhlSaajaEpost = dhlSaajaEpost;
	}

	public DhlEmailHeader getDhlEmailHeader() {
		return dhlEmailHeader;
	}

	public void setDhlEmailHeader(DhlEmailHeader dhlEmailHeader) {
		this.dhlEmailHeader = dhlEmailHeader;
	}

	public String getDhlKaust() {
		return dhlKaust;
	}

	public void setDhlKaust(String dhlKaust) {
		this.dhlKaust = dhlKaust;
	}

}
