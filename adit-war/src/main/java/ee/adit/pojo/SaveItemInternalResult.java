package ee.adit.pojo;

import java.util.ArrayList;
import java.util.List;

public class SaveItemInternalResult {
	private boolean success;
	private long itemId;
	private List<Message> messages;
	
	public SaveItemInternalResult() {
		this.success = false;
		this.itemId = 0;
		this.messages = new ArrayList<Message>();
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public long getItemId() {
		return itemId;
	}
	public void setItemId(long itemId) {
		this.itemId = itemId;
	}
	
	public List<Message> getMessages() {
		return messages;
	}
	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
}
