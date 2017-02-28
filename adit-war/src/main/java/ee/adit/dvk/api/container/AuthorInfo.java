package ee.adit.dvk.api.container;

public class AuthorInfo
{
	private Organisation organisation;
	private Person person;

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public boolean hasOrganisation() {
		return organisation != null;
	}

	public boolean hasPerson() {
		return person != null;
	}

	public void createDescendants(boolean organisation, boolean person) {
		if (organisation) {
			if (this.organisation == null) {
				this.organisation = new Organisation();
			}
		}

		if (person) {
			if (this.person == null) {
				this.person = new Person();
			}
		}
	}
}
