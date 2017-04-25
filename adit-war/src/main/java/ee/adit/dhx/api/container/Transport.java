package ee.adit.dhx.api.container;

import java.util.ArrayList;
import java.util.List;

public abstract class Transport<Saaja, Saatja>
{
	private List<Saaja> saajad;
	private List<Saatja> saatjad;

	public List<Saaja> getSaajad() {
		return saajad;
	}

	public void setSaajad(List<Saaja> saajad) {
		this.saajad = saajad;
	}

	public List<Saatja> getSaatjad() {
		return saatjad;
	}

	public void setSaatjad(List<Saatja> satajad) {
		this.saatjad = satajad;
	}

	public boolean hasSaajad() {
		return saajad != null && saajad.size() > 0;
	}

	public boolean hasSaatjad() {
		return saatjad != null && saatjad.size() > 0;
	}

	public void printSaatjad() {
		if (!hasSaatjad()) {
			return;
		}

		for (Saatja s : saatjad) {
			System.out.println("\tSaatja: " + s);
		}
	}

	public void printSaajad() {
		if (!hasSaajad()) {
			return;
		}

		for (Saaja s : saajad) {
			System.out.println("\tSaaja: " + s);
		}
	}

	public void createLists(boolean saajad, boolean saatjad) {
		if (saajad) {
			if (getSaajad() == null) {
				setSaajad(new ArrayList<Saaja>());
			}
		} else if (saatjad) {
			if (getSaatjad() == null) {
				setSaatjad(new ArrayList<Saatja>());
			}
		}
	}
}
