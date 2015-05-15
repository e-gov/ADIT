package ee.adit.pojo.notification;

/**
 * Request class for notification calendar (teavituskalender) requests.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class LisaSyndmusRequest {

    private Boolean nahtavOmanikule;

    private String kirjeldus;

    private String tahtsus;

    private String syndmuseTyyp;

    private String liik;

    private LisaSyndmusRequestLugejad lugejad;

    private String algus;

    private String lopp;

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

    public String getAlgus() {
        return algus;
    }

    public void setAlgus(String algus) {
        this.algus = algus;
    }

    public String getLopp() {
        return lopp;
    }

    public void setLopp(String lopp) {
        this.lopp = lopp;
    }

    public String getXsiType() {
        return xsiType;
    }

    public void setXsiType(String xsiType) {
        this.xsiType = xsiType;
    }

}
