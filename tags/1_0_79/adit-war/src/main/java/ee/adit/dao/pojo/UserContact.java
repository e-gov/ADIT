package ee.adit.dao.pojo;



import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * UserContact 
 */
public class UserContact implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private long id;
    private AditUser user;
    private AditUser contact;
    private Date lastUsedDate;

    
    public UserContact() {
    }

    public UserContact(AditUser user, AditUser contact) {
        this.user = user;
        this.contact = contact;
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public AditUser getUser() {
		return user;
	}

	public void setUser(AditUser user) {
		this.user = user;
	}

	public AditUser getContact() {
		return contact;
	}

	public void setContact(AditUser contact) {
		this.contact = contact;
	}

	public Date getLastUsedDate() {
		return lastUsedDate;
	}

	public void setLastUsedDate(Date lastUsedDate) {
		this.lastUsedDate = lastUsedDate;
	}


}
