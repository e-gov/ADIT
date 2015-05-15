package ee.adit.exception;

import java.util.List;

import ee.adit.pojo.Message;

public class AditMultipleException extends AditException {

	private List<Message> messages;
	
	public AditMultipleException(String message) {
		super(message);
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	
	
}
