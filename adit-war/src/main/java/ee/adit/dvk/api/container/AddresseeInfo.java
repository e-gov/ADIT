package ee.adit.dvk.api.container;

import ee.adit.dvk.api.container.v1.Addressee;

public class AddresseeInfo
{
	private Organisation organisation;
	private Addressee person;

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public Addressee getPerson() {
		return person;
	}

	public void setPerson(Addressee person) {
		this.person = person;
	}

	public boolean hasOrganisation() {
		return organisation != null;
	}

	public boolean hasPerson() {
		return person != null;
	}
}
