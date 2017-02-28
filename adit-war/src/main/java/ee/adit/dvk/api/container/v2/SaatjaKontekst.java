package ee.adit.dvk.api.container.v2;

import java.util.Date;

public class SaatjaKontekst
{
	private String seosviit;
	private Date kuupaevSaatjaRegistreerimine;
	private String dokumentSaatjaGuid;

	public String getSeosviit() {
		return seosviit;
	}

	public void setSeosviit(String seosviit) {
		this.seosviit = seosviit;
	}

	public Date getKuupaevSaatjaRegistreerimine() {
		return kuupaevSaatjaRegistreerimine;
	}

	public void setKuupaevSaatjaRegistreerimine(Date kuupaevSaatjaRegistreerimine) {
		this.kuupaevSaatjaRegistreerimine = kuupaevSaatjaRegistreerimine;
	}

	public String getDokumentSaatjaGuid() {
		return dokumentSaatjaGuid;
	}

	public void setDokumentSaatjaGuid(String dokumentSaatjaGuid) {
		this.dokumentSaatjaGuid = dokumentSaatjaGuid;
	}

}
