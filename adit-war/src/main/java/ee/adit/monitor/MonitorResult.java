package ee.adit.monitor;

import java.util.List;

public class MonitorResult {

	private String component;
	
	private String status;
	
	private List<Exception> exceptions;
	
	private double duration;

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<Exception> exceptions) {
		this.exceptions = exceptions;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}
	
}
