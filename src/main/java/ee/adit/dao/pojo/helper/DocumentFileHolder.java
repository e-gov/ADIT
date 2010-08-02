package ee.adit.dao.pojo.helper;

import ee.adit.dao.pojo.DocumentFile;

public class DocumentFileHolder extends DocumentFile {

	private String tempFile;

	public String getTempFile() {
		return tempFile;
	}

	public void setTempFile(String tempFile) {
		this.tempFile = tempFile;
	}
	
}
