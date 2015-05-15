package ee.adit.pojo.notification;

/**
 * User data class for notification calendar (teavituskalender) requests.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class LisaSyndmusRequestKasutaja {

    private String kood;

    private String kasutajaTyyp;

    public String getKood() {
        return kood;
    }

    public void setKood(String kood) {
        this.kood = kood;
    }

    public String getKasutajaTyyp() {
        return kasutajaTyyp;
    }

    public void setKasutajaTyyp(String kasutajaTyyp) {
        this.kasutajaTyyp = kasutajaTyyp;
    }

}
