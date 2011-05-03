package ee.adit.pojo.notification;

/**
 * Result class for notification calendar (teavituskalender) response.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class LisaSyndmusResponseTulemus {

    private Integer tulemuseKood;

    private String tulemuseTekst;

    private String tulemuseKoodType;

    private String tulemuseTekstType;

    public Integer getTulemuseKood() {
        return tulemuseKood;
    }

    public void setTulemuseKood(Integer tulemuseKood) {
        this.tulemuseKood = tulemuseKood;
    }

    public String getTulemuseTekst() {
        return tulemuseTekst;
    }

    public void setTulemuseTekst(String tulemuseTekst) {
        this.tulemuseTekst = tulemuseTekst;
    }

    public String getTulemuseKoodType() {
        return tulemuseKoodType;
    }

    public void setTulemuseKoodType(String tulemuseKoodType) {
        this.tulemuseKoodType = tulemuseKoodType;
    }

    public String getTulemuseTekstType() {
        return tulemuseTekstType;
    }

    public void setTulemuseTekstType(String tulemuseTekstType) {
        this.tulemuseTekstType = tulemuseTekstType;
    }

}
