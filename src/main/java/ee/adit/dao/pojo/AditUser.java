package ee.adit.dao.pojo;

// Generated 16.06.2010 14:33:21 by Hibernate Tools 3.2.4.GA

import java.util.HashSet;
import java.util.Set;

/**
 * AditUser generated by hbm2java
 */
public class AditUser implements java.io.Serializable {

	private String userCode;
	private Usertype usertype;
	private String fullName;
	private Boolean active;
	private String dvkOrgCode;
	private String dvkSubdivisionShortName;
	private String dvkOccupationShortName;
	private Long diskQuota;
	private Set signatures = new HashSet(0);
	private Set accessRestrictions = new HashSet(0);

	public AditUser() {
	}

	public AditUser(String userCode, Usertype usertype) {
		this.userCode = userCode;
		this.usertype = usertype;
	}

	public AditUser(String userCode, Usertype usertype, String fullName,
			Boolean active, String dvkOrgCode, String dvkSubdivisionShortName,
			String dvkOccupationShortName, Long diskQuota, Set signatures,
			Set accessRestrictions) {
		this.userCode = userCode;
		this.usertype = usertype;
		this.fullName = fullName;
		this.active = active;
		this.dvkOrgCode = dvkOrgCode;
		this.dvkSubdivisionShortName = dvkSubdivisionShortName;
		this.dvkOccupationShortName = dvkOccupationShortName;
		this.diskQuota = diskQuota;
		this.signatures = signatures;
		this.accessRestrictions = accessRestrictions;
	}

	public String getUserCode() {
		return this.userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public Usertype getUsertype() {
		return this.usertype;
	}

	public void setUsertype(Usertype usertype) {
		this.usertype = usertype;
	}

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getActive() {
		return this.active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getDvkOrgCode() {
		return this.dvkOrgCode;
	}

	public void setDvkOrgCode(String dvkOrgCode) {
		this.dvkOrgCode = dvkOrgCode;
	}

	public String getDvkSubdivisionShortName() {
		return this.dvkSubdivisionShortName;
	}

	public void setDvkSubdivisionShortName(String dvkSubdivisionShortName) {
		this.dvkSubdivisionShortName = dvkSubdivisionShortName;
	}

	public String getDvkOccupationShortName() {
		return this.dvkOccupationShortName;
	}

	public void setDvkOccupationShortName(String dvkOccupationShortName) {
		this.dvkOccupationShortName = dvkOccupationShortName;
	}

	public Long getDiskQuota() {
		return this.diskQuota;
	}

	public void setDiskQuota(Long diskQuota) {
		this.diskQuota = diskQuota;
	}

	public Set getSignatures() {
		return this.signatures;
	}

	public void setSignatures(Set signatures) {
		this.signatures = signatures;
	}

	public Set getAccessRestrictions() {
		return this.accessRestrictions;
	}

	public void setAccessRestrictions(Set accessRestrictions) {
		this.accessRestrictions = accessRestrictions;
	}

}
