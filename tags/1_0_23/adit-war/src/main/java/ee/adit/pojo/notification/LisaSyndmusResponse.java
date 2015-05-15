package ee.adit.pojo.notification;


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
