package ee.adit.pojo.notification;

/**
 * Response class for notification calendar (teavituskalender) requests.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class LisaSyndmusResponse {

    private LisaSyndmusResponseTulemus tulemus;

    private Integer syndmusId;

    public LisaSyndmusResponseTulemus getTulemus() {
        return tulemus;
    }

    public void setTulemus(LisaSyndmusResponseTulemus tulemus) {
        this.tulemus = tulemus;
    }

    public Integer getSyndmusId() {
        return syndmusId;
    }

    public void setSyndmusId(Integer syndmusId) {
        this.syndmusId = syndmusId;
    }

}
