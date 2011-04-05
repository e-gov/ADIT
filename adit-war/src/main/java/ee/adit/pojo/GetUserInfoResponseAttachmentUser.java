package ee.adit.pojo;

public class GetUserInfoResponseAttachmentUser {

    private String userCode;
    private String name;
    private boolean hasJoined;
    private Long freeSpace;
    private Long usedSpace;
    private boolean canRead;
    private boolean canWrite;
    private boolean usesDVK;
    private ArrayOfMessage messages;

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isHasJoined() {
        return hasJoined;
    }

    public void setHasJoined(boolean hasJoined) {
        this.hasJoined = hasJoined;
    }

    public Long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(Long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public Long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(Long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean isUsesDVK() {
        return usesDVK;
    }

    public void setUsesDVK(boolean usesDVK) {
        this.usesDVK = usesDVK;
    }

	public ArrayOfMessage getMessages() {
		return messages;
	}

	public void setMessages(ArrayOfMessage messages) {
		this.messages = messages;
	}
}
