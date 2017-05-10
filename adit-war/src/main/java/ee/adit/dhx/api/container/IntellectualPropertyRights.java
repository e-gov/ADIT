package ee.adit.dhx.api.container;

import java.util.Date;

public class IntellectualPropertyRights
{
	private Date copyrightEndDate;
	private String iprOwner;

	public Date getCopyrightEndDate() {
		return copyrightEndDate;
	}

	public void setCopyrightEndDate(Date copyrightEndDate) {
		this.copyrightEndDate = copyrightEndDate;
	}

	public String getIprOwner() {
		return iprOwner;
	}

	public void setIprOwner(String iprOwner) {
		this.iprOwner = iprOwner;
	}

}
