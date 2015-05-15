package ee.adit.util;

import java.util.List;

/**
 * Monitor result holder class.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 *
 */
public class MonitorResult {

    /**
     * Component.
     */
    private String component;

    /**
     * Test successful.
     */
    private boolean success;

    /**
     * Exception list.
     */
    private List<String> exceptions;

    /**
     * Operation duration.
     */
    private double duration;

    /**
     * Operation duration as a string.
     */
    private String durationString;

    /**
     * Exception as a string.
     */
    private String exceptionString;

    /**
     * Status string.
     */
    private String statusString;

    /**
     * Get component.
     * @return component
     */
    public String getComponent() {
        return component;
    }

    /**
     * Set component.
     * @param component component
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * Get exceptions.
     * @return exceptions
     */
    public List<String> getExceptions() {
        return exceptions;
    }

    /**
     * Set exceptions.
     * @param exceptions exceptions
     */
    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions;
    }

    /**
     * Get duration.
     * @return duration
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Set duration.
     * @param duration duration
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Is success.
     * @return success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Set success.
     * @param success success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Get duration string.
     * @return duration string
     */
    public String getDurationString() {
        return durationString;
    }

    /**
     * Set duration string.
     * @param durationString duration string
     */
    public void setDurationString(String durationString) {
        this.durationString = durationString;
    }

    /**
     * Get exception string.
     * @return exception string
     */
    public String getExceptionString() {
        return exceptionString;
    }

    /**
     * Set exception string.
     * @param exceptionString exception string
     */
    public void setExceptionString(String exceptionString) {
        this.exceptionString = exceptionString;
    }

    /**
     * Get status string.
     * @return status string
     */
    public String getStatusString() {
        return statusString;
    }

    /**
     * Set status string.
     * @param statusString status string
     */
    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

}
