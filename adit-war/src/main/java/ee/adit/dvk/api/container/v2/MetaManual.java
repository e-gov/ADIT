package ee.adit.dvk.api.container.v2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.adit.dvk.api.container.SaatjaDefineeritud;

public class MetaManual extends ee.adit.dvk.api.container.MetaManual
{
	private Boolean test;
	private String dokumentLiik;
	private String dokumentKeel;
	private String dokumentPealkiri;
	private String versioonNumber;
	private String dokumentGuid;
	private String dokumentViit;
	private Date kuupaevRegistreerimine;
	private Date kuupaevSaatmine;
	private Date tahtaeg;
	private SaatjaKontekst saatjaKontekst;
	private IPR ipr;
	private JuurdepaasPiirang juurdepaasPiirang;
	private List<Koostaja> koostajad;
	private SaatjaDefineeritud saatjaDefineeritud;

	public Boolean getTest() {
		return test;
	}

	public void setTest(Boolean test) {
		this.test = test;
	}

	public String getDokumentLiik() {
		return dokumentLiik;
	}

	public void setDokumentLiik(String dokumentLiik) {
		this.dokumentLiik = dokumentLiik;
	}

	public String getDokumentKeel() {
		return dokumentKeel;
	}

	public void setDokumentKeel(String dokumentKeel) {
		this.dokumentKeel = dokumentKeel;
	}

	public String getDokumentPealkiri() {
		return dokumentPealkiri;
	}

	public void setDokumentPealkiri(String dokumentPealkiri) {
		this.dokumentPealkiri = dokumentPealkiri;
	}

	public String getVersioonNumber() {
		return versioonNumber;
	}

	public void setVersioonNumber(String versioonNumber) {
		this.versioonNumber = versioonNumber;
	}

	public String getDokumentGuid() {
		return dokumentGuid;
	}

	public void setDokumentGuid(String dokumentGuid) {
		this.dokumentGuid = dokumentGuid;
	}

	public String getDokumentViit() {
		return dokumentViit;
	}

	public void setDokumentViit(String dokumentViit) {
		this.dokumentViit = dokumentViit;
	}

	public Date getKuupaevRegistreerimine() {
		return kuupaevRegistreerimine;
	}

	public void setKuupaevRegistreerimine(Date kuupaevRegistreerimine) {
		this.kuupaevRegistreerimine = kuupaevRegistreerimine;
	}

	public Date getKuupaevSaatmine() {
		return kuupaevSaatmine;
	}

	public void setKuupaevSaatmine(Date kuupaevSaatmine) {
		this.kuupaevSaatmine = kuupaevSaatmine;
	}

	public Date getTahtaeg() {
		return tahtaeg;
	}

	public void setTahtaeg(Date tahtaeg) {
		this.tahtaeg = tahtaeg;
	}

	public IPR getIpr() {
		return ipr;
	}

	public void setIpr(IPR ipr) {
		this.ipr = ipr;
	}

	public SaatjaKontekst getSaatjaKontekst() {
		return saatjaKontekst;
	}

	public void setSaatjaKontekst(SaatjaKontekst saatjaKontekst) {
		this.saatjaKontekst = saatjaKontekst;
	}

	public JuurdepaasPiirang getJuurdepaasPiirang() {
		return juurdepaasPiirang;
	}

	public void setJuurdepaasPiirang(JuurdepaasPiirang juurdepaasPiirang) {
		this.juurdepaasPiirang = juurdepaasPiirang;
	}

	public void createAllDescendants() {
		createDescendants(true, true, true, true, true);
	}

	public void createDescendants(boolean saatjaKontekst, boolean ipr, boolean juurdepaasPiirang, boolean koostajad,
		boolean saatjadDefineeritud) {
		if (saatjaKontekst) {
			if (this.saatjaKontekst == null) {
				this.saatjaKontekst = new SaatjaKontekst();
			}
		}

		if (ipr) {
			if (this.ipr == null) {
				this.ipr = new IPR();
			}
		}

		if (juurdepaasPiirang) {
			if (this.juurdepaasPiirang == null) {
				this.juurdepaasPiirang = new JuurdepaasPiirang();
			}
		}

		if (koostajad) {
			if (this.koostajad == null) {
				this.koostajad = new ArrayList<Koostaja>();
			}
		}

		if (saatjadDefineeritud) {
			if (this.saatjaDefineeritud == null) {
				this.saatjaDefineeritud = new SaatjaDefineeritud();
			}
		}
	}

	public List<Koostaja> getKoostajad() {
		return koostajad;
	}

	public void setKoostajad(List<Koostaja> koostajad) {
		this.koostajad = koostajad;
	}

	public SaatjaDefineeritud getSaatjaDefineeritud() {
		return saatjaDefineeritud;
	}

	public void setSaatjaDefineeritud(SaatjaDefineeritud saatjadDefineeritud) {
		this.saatjaDefineeritud = saatjadDefineeritud;
	}
}
