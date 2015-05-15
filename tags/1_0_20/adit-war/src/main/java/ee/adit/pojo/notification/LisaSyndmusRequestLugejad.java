package ee.adit.pojo.notification;

import java.util.ArrayList;

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
