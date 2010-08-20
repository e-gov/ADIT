package ee.adit.util;

public class DVKUserSyncResult {

	private int added;
	
	private int modified;
	
	private int deactivated;

	public DVKUserSyncResult() {
		this.setAdded(0);
		this.setDeactivated(0);
		this.setModified(0);
	}
	
	public int getAdded() {
		return added;
	}

	public void setAdded(int added) {
		this.added = added;
	}

	public int getModified() {
		return modified;
	}

	public void setModified(int modified) {
		this.modified = modified;
	}

	public int getDeactivated() {
		return deactivated;
	}

	public void setDeactivated(int deactivated) {
		this.deactivated = deactivated;
	}
	
}
