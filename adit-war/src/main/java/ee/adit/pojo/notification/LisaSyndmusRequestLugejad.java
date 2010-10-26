package ee.adit.pojo.notification;

import java.util.ArrayList;

public class LisaSyndmusRequestLugejad {

	private ArrayList<LisaSyndmusRequestKasutaja> kasutajad;

	private String type;
	
	public ArrayList<LisaSyndmusRequestKasutaja> getKasutajad() {
		return kasutajad;
	}

	public void setKasutajad(ArrayList<LisaSyndmusRequestKasutaja> kasutajad) {
		this.kasutajad = kasutajad;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
