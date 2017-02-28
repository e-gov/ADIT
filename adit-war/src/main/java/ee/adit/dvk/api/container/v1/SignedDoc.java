package ee.adit.dvk.api.container.v1;

import java.util.List;

import ee.adit.dvk.api.container.XmlBlock;

public class SignedDoc extends XmlBlock {
	private final static String ddocNamesapce = "http://www.sk.ee/DigiDoc/v1.3.0#";

	protected String version;
	protected String format;
	protected List<DataFile> dataFiles;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public List<DataFile> getDataFiles() {
		return dataFiles;
	}

	public void setDataFiles(List<DataFile> dataFiles) {
		this.dataFiles = dataFiles;
	}

	public String getDdocNamespace() {
		return ddocNamesapce;
	}

	public void setDdocNamespace(String xmlns) {
	}
}
