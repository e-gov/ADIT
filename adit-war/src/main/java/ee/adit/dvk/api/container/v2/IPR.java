package ee.adit.dvk.api.container.v2;

import java.util.Date;

public class IPR
{
	private Date iprTahtaeg;
	private String iprOmanik;
	private boolean reprodutseerimineKeelatud;

	public Date getIprTahtaeg() {
		return iprTahtaeg;
	}

	public void setIprTahtaeg(Date iprTahtaeg) {
		this.iprTahtaeg = iprTahtaeg;
	}

	public String getIprOmanik() {
		return iprOmanik;
	}

	public void setIprOmanik(String iprOmanik) {
		this.iprOmanik = iprOmanik;
	}

	public boolean isReprodutseerimineKeelatud() {
		return reprodutseerimineKeelatud;
	}

	public void setReprodutseerimineKeelatud(boolean reprodutseerimineKeelatud) {
		this.reprodutseerimineKeelatud = reprodutseerimineKeelatud;
	}

}
