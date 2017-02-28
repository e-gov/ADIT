package ee.adit.dvk.api.container.v2;

import java.util.Date;

public class JuurdepaasPiirang
{
	private String piirang;
	private Date piirangAlgus;
	private Date piirangLopp;
	private String piirangAlus;

	public String getPiirang() {
		return piirang;
	}

	public void setPiirang(String piirang) {
		this.piirang = piirang;
	}

	public Date getPiirangAlgus() {
		return piirangAlgus;
	}

	public void setPiirangAlgus(Date piirangAlgus) {
		this.piirangAlgus = piirangAlgus;
	}

	public Date getPiirangLopp() {
		return piirangLopp;
	}

	public void setPiirangLopp(Date piirangLopp) {
		this.piirangLopp = piirangLopp;
	}

	public String getPiirangAlus() {
		return piirangAlus;
	}

	public void setPiirangAlus(String piirangAlus) {
		this.piirangAlus = piirangAlus;
	}

}
