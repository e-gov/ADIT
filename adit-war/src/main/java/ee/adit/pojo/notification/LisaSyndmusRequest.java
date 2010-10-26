package ee.adit.pojo.notification;

import java.util.ArrayList;
import java.util.Date;

public class LisaSyndmusRequest {

	private Boolean nahtavOmanikule;
	
	private String kirjeldus;
	
	private String tahtsus;
	
	private String syndmuseTyyp;
	
	private String liik;
	
	private LisaSyndmusRequestLugejad lugejad;
	
	private Date algus;
	
	private Date lopp;
	
	private String xsiType;
	
	public Boolean getNahtavOmanikule() {
		return nahtavOmanikule;
	}
	public void setNahtavOmanikule(Boolean nahtavOmanikule) {
		this.nahtavOmanikule = nahtavOmanikule;
	}
	public String getKirjeldus() {
		return kirjeldus;
	}
	public void setKirjeldus(String kirjeldus) {
		this.kirjeldus = kirjeldus;
	}
	public String getTahtsus() {
		return tahtsus;
	}
	public void setTahtsus(String tahtsus) {
		this.tahtsus = tahtsus;
	}
	public String getSyndmuseTyyp() {
		return syndmuseTyyp;
	}
	public void setSyndmuseTyyp(String syndmuseTyyp) {
		this.syndmuseTyyp = syndmuseTyyp;
	}
	public String getLiik() {
		return liik;
	}
	public void setLiik(String liik) {
		this.liik = liik;
	}
	public LisaSyndmusRequestLugejad getLugejad() {
		return lugejad;
	}
	public void setLugejad(LisaSyndmusRequestLugejad lugejad) {
		this.lugejad = lugejad;
	}
	public Date getAlgus() {
		return algus;
	}
	public void setAlgus(Date algus) {
		this.algus = algus;
	}
	public Date getLopp() {
		return lopp;
	}
	public void setLopp(Date lopp) {
		this.lopp = lopp;
	}
	public String getXsiType() {
		return xsiType;
	}
	public void setXsiType(String xsiType) {
		this.xsiType = xsiType;
	}
	
	
}
