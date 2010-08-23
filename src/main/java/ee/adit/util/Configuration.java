package ee.adit.util;

import java.math.BigInteger;
import java.util.List;

/**
 * Application configuration holder class. The configuring takes place during startup and the parameters are
 * specified in servlet configuration.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class Configuration {
	
	private BigInteger getJoinedMaxResults;

	private String tempDir;
	
	private String deleteTemporaryFiles;
	
	private String schedulerEventTypeName;

	private String dvkResponseMessageStylesheet;
	
	private Long globalDiskQuota;
	
	private List<String> locales;
	
	public List<String> getLocales() {
		return locales;
	}

	public void setLocales(List<String> locales) {
		this.locales = locales;
	}

	public BigInteger getGetJoinedMaxResults() {
		return getJoinedMaxResults;
	}

	public void setGetJoinedMaxResults(BigInteger getJoinedMaxResults) {
		this.getJoinedMaxResults = getJoinedMaxResults;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	public String getDeleteTemporaryFiles() {
		return deleteTemporaryFiles;
	}

	public void setDeleteTemporaryFiles(String deleteTemporaryFiles) {
		this.deleteTemporaryFiles = deleteTemporaryFiles;
	}
	
	public boolean getDeleteTemporaryFilesAsBoolean() {
		boolean result = false;
		if(this.getDeleteTemporaryFiles() != null) {
			result = (new Boolean(this.getDeleteTemporaryFiles())).booleanValue();
		} else {
			result = false;
		}
		return result;
	}

	public String getSchedulerEventTypeName() {
		return schedulerEventTypeName;
	}

	public void setSchedulerEventTypeName(String schedulerEventTypeName) {
		this.schedulerEventTypeName = schedulerEventTypeName;
	}

	public String getDvkResponseMessageStylesheet() {
		return dvkResponseMessageStylesheet;
	}

	public void setDvkResponseMessageStylesheet(String dvkResponseMessageStylesheet) {
		this.dvkResponseMessageStylesheet = dvkResponseMessageStylesheet;
	}

	public Long getGlobalDiskQuota() {
		return globalDiskQuota;
	}

	public void setGlobalDiskQuota(Long globalDiskQuota) {
		this.globalDiskQuota = globalDiskQuota;
	}
}
