package ee.adit.dhx.api.container;

public class SaatjaDefineeritud {
	private String saatjaDefineeritud;
	private String valjanimi;

	public SaatjaDefineeritud() {
	}

    public SaatjaDefineeritud(String name, String value) {
        this.valjanimi = name;
        this.saatjaDefineeritud = value;
    }

	public String getSaatjaDefineeritud() {
		return saatjaDefineeritud;
	}

	public void setSaatjaDefineeritud(String saatjaDefineeritud) {
		this.saatjaDefineeritud = saatjaDefineeritud;
	}

	public String getValjanimi() {
		return valjanimi;
	}

	public void setValjanimi(String valjanimi) {
		this.valjanimi = valjanimi;
	}

}