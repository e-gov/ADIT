package ee.adit.pojo;

public class GetUserInfoResponseAttachmentUser {

    String userCode;

    boolean hasJoined;

    Long freeSpace;

    Long usedSpace;

    boolean canRead;

    boolean canWrite;

    boolean usesDVK;

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
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

}
