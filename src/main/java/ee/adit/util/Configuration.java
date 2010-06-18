package ee.adit.util;

import java.math.BigInteger;

public class Configuration {

	private BigInteger getJoinedMaxResults;

	private String tempDir;
	
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
	
}
