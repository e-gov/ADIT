package ee.adit.dvk.api.container.v2;

import java.util.ArrayList;
import java.util.List;

public class FailideKonteiner
{
	private short kokku;
	private List<Fail> failid;

	public short getKokku() {
		return kokku;
	}

	public void setKokku(short kokku) {
		this.kokku = kokku;
	}

	public List<Fail> getFailid() {
		return failid;
	}

	public void setFailid(List<Fail> files) {
		this.failid = files;
	}

	public List<Fail> createList() {
		if (failid == null) {
			failid = new ArrayList<Fail>();
		}

		return failid;
	}

	public boolean hasFailid() {
		return failid != null && failid.size() > 0;
	}
}
