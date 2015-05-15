package ee.adit.util;

public class StartEndOffsetPair {
	private long start;
	private long end;
	private byte[] dataMd5Hash;
	
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(long end) {
		this.end = end;
	}
	public byte[] getDataMd5Hash() {
		return dataMd5Hash;
	}
	public void setDataMd5Hash(byte[] dataMd5Hash) {
		this.dataMd5Hash = dataMd5Hash;
	}
}
