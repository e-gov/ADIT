package ee.adit.dao.pojo;

/**
 * Data access class for MAINTENANCE_JOB table.
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class MaintenanceJob implements java.io.Serializable {

	private static final long serialVersionUID = -8144353401044679861L;
	private long id;
	private String name;
	private boolean running;

	public long getId() {
		return id;
	}
	public void setId(long value) {
		this.id = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean value) {
		this.running = value;
	}

	public MaintenanceJob() {
	}

	public MaintenanceJob(long id, boolean running) {
		this.id = id;
		this.running = running;
	}

	public MaintenanceJob(long id, String name, boolean running) {
		this.id = id;
		this.name = name;
		this.running = running;
	}
}
