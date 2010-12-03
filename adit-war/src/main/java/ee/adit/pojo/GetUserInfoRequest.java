package ee.adit.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetUserInfoRequest", propOrder = {"userList" })
public class GetUserInfoRequest {

    @XmlElement(required = true)
    protected GetUserInfoRequestUserList userList;

    /**
     * Gets the value of the userList property.
     * 
     * @return possible object is byte[]
     */
    public GetUserInfoRequestUserList getUserList() {
        return userList;
    }

    /**
     * Sets the value of the userList property.
     * 
     * @param value
     *            allowed object is byte[]
     */
    public void setUserList(GetUserInfoRequestUserList value) {
        this.userList = (value);
    }

}
