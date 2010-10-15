package ee.adit.monitor;

import java.util.List;

public class MonitorResult {

	private String component;
	
	private boolean success;
	
	private List<String> exceptions;
	
	private double duration;

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;	
	}

	public List<String> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<String> exceptions) {
		this.exceptions = exceptions;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
