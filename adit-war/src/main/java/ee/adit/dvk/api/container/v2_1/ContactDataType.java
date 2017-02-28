package ee.adit.dvk.api.container.v2_1;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class ContactDataType {
    private Boolean adit;
    private String phone;
    private String email;
    private String webPage;
    private String messagingAddress;
    private PostalAddressType postalAddress;

    public Boolean getAdit() {
        return adit;
    }

    public void setAdit(Boolean adit) {
        this.adit = adit;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebPage() {
        return webPage;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    public String getMessagingAddress() {
        return messagingAddress;
    }

    public void setMessagingAddress(String messagingAddress) {
        this.messagingAddress = messagingAddress;
    }

    public PostalAddressType getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(PostalAddressType postalAddress) {
        this.postalAddress = postalAddress;
    }
}
