package ee.adit.util;

/**
 * A holder class for the DVK to ADIT users synchronization result.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DVKUserSyncResult {

	/**
	 * Number of new users added
	 */
	private int added;
	
	/**
	 * Number of users modified
	 */
	private int modified;
	
	/**
	 * Number of users deleted / deactivated
	 */
	private int deactivated;

	/**
	 * Default constructor
	 */
	public DVKUserSyncResult() {
		this.setAdded(0);
		this.setDeactivated(0);
		this.setModified(0);
	}
	
	/**
	 * Retrieves the number of users added
	 * @return			Number of users added
	 */
	public int getAdded() {
		return added;
	}

	/**
	 * Sets the number of users added
	 * @param added		Number of users added
	 */
	public void setAdded(int added) {
		this.added = added;
	}

	/**
	 * Retrieves the number of users modified
	 * @return			Number of users modified
	 */
	public int getModified() {
		return modified;
	}

	/**
	 * Sets the number of users modified
	 * @param modified		Number of users modified
	 */
	public void setModified(int modified) {
		this.modified = modified;
	}
	
	/**
	 * Retrieves the number of users deactivated
	 * @return			Number of users deleted/deactivated
	 */
	public int getDeactivated() {
		return deactivated;
	}

	/**
	 * Sets the number of users deleted / deactivated
	 * @param deactivated	Number of users deleted/deactivated
	 */
	public void setDeactivated(int deactivated) {
		this.deactivated = deactivated;
	}
	
}
