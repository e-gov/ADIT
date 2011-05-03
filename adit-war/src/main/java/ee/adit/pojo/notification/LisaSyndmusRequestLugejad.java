package ee.adit.pojo.notification;

import java.util.ArrayList;

/**
 * User list wrapper class for notification calendar (teavituskalender) requests.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class LisaSyndmusRequestLugejad {

    private ArrayList<LisaSyndmusRequestKasutaja> kasutajad;

    private String type;

    private String xsiType;

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

    public String getXsiType() {
        return xsiType;
    }

    public void setXsiType(String xsiType) {
        this.xsiType = xsiType;
    }

}
