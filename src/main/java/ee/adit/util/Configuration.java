package ee.adit.util;

import java.math.BigInteger;

public class Configuration {

	private BigInteger getJoinedMaxResults;

	private String tempDir;
	
	private String deleteTemporaryFiles;
	
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
}
