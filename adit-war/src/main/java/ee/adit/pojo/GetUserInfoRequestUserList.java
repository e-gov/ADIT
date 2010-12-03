package ee.adit.pojo;

public class GetUserInfoRequestUserList {

    protected String href;

    public GetUserInfoRequestUserList() {
    }

    public GetUserInfoRequestUserList(String href) {
        this.href = href;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setHref(String value) {
        this.href = value;
    }

}
