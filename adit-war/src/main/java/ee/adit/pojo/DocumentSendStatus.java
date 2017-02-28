package ee.adit.pojo;

import java.util.List;

public class DocumentSendStatus {
	private Long dhlId;
	private String dhxReceiptId;

	private List<DocumentSharingRecipientStatus> recipients;
	
	public Long getDhlId() {
		return dhlId;
	}
	public void setDhlId(Long dhlId) {
		this.dhlId = dhlId;
	}
	public List<DocumentSharingRecipientStatus> getRecipients() {
		return recipients;
	}
	public void setRecipients(List<DocumentSharingRecipientStatus> recipients) {
		this.recipients = recipients;
	}
	
	/**
	 * @return the dhxReceiptId
	 */
	public String getDhxReceiptId() {
		return dhxReceiptId;
	}
	/**
	 * @param dhxReceiptId the dhxReceiptId to set
	 */
	public void setDhxReceiptId(String dhxReceiptId) {
		this.dhxReceiptId = dhxReceiptId;
	}

}
