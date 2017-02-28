package ee.adit.dvk.api.container;


public class CompilatorInfo
{
	private Organisation organisation;
	private Compilator person;

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public Compilator getPerson() {
		return person;
	}

	public void setPerson(Compilator person) {
		this.person = person;
	}

	public boolean hasOrganisation() {
		return organisation != null;
	}

	public boolean hasPerson() {
		return person != null;
	}
}
