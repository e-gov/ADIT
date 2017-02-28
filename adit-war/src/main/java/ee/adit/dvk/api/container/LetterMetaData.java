package ee.adit.dvk.api.container;

import java.util.Date;


public class LetterMetaData
{
	private Date signDate;
	private String senderIdentifier;
	private String originalIdentifier;
	private String type;
	private String languange;
	private String version;
	private String title;
	private Date deadline;
	private Enclosure enclosure;
	private AccessRights accessRights;
	private IntellectualPropertyRights intellectualPropertyRights;
	private String noCopies;
	private String senderSeriesTitle;
	private String senderFolderTitle;
	private Boolean senderVitalRecordIndicator;

	public Date getSignDate() {
		return signDate;
	}

	public void setSignDate(Date signDate) {
		this.signDate = signDate;
	}

	public String getSenderIdentifier() {
		return senderIdentifier;
	}

	public void setSenderIdentifier(String senderIdentifier) {
		this.senderIdentifier = senderIdentifier;
	}

	public String getOriginalIdentifier() {
		return originalIdentifier;
	}

	public void setOriginalIdentifier(String originalIdentifier) {
		this.originalIdentifier = originalIdentifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLanguange() {
		return languange;
	}

	public void setLanguange(String languange) {
		this.languange = languange;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public Enclosure getEnclosure() {
		return enclosure;
	}

	public void setEnclosure(Enclosure enclosures) {
		this.enclosure = enclosures;
	}

	public AccessRights getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(AccessRights accessRights) {
		this.accessRights = accessRights;
	}

	public IntellectualPropertyRights getIntellectualPropertyRights() {
		return intellectualPropertyRights;
	}

	public void setIntellectualPropertyRights(IntellectualPropertyRights intellectualPropertyRights) {
		this.intellectualPropertyRights = intellectualPropertyRights;
	}

	public String getNoCopies() {
		return noCopies;
	}

	public void setNoCopies(String noCopies) {
		this.noCopies = noCopies;
	}

	public String getSenderSeriesTitle() {
		return senderSeriesTitle;
	}

	public void setSenderSeriesTitle(String senderSeriesTitle) {
		this.senderSeriesTitle = senderSeriesTitle;
	}

	public String getSenderFolderTitle() {
		return senderFolderTitle;
	}

	public void setSenderFolderTitle(String senderFolderTitle) {
		this.senderFolderTitle = senderFolderTitle;
	}

	public Boolean getSenderVitalRecordIndicator() {
		return senderVitalRecordIndicator;
	}

	public void setSenderVitalRecordIndicator(Boolean senderVitalRecordIndicator) {
		this.senderVitalRecordIndicator = senderVitalRecordIndicator;
	}

}
