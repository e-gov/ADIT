package ee.adit.util;

/**
 * Class for storing offsets and hash of portions of data from other data files.
 * 
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class StartEndOffsetPair {
	private long start;
	private long end;
	private byte[] dataMd5Hash;
	private Boolean bdocOrigin = false;
	
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
	public Boolean getBdocOrigin() {
		return bdocOrigin;
	}
	public void setBdocOrigin(Boolean bdocOrigin) {
		this.bdocOrigin = bdocOrigin;
	}
}
