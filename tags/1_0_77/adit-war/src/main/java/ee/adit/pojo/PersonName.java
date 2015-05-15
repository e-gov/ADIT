package ee.adit.pojo;

public class PersonName {
	private String firstName;
	private String surname;

	public PersonName() {
		firstName = "";
		surname = "";
	}

	public PersonName(String firstName, String surname) {
		this.firstName = firstName;
		this.surname = surname;
	}

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
}
