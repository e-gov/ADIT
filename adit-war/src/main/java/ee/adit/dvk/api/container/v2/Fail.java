package ee.adit.dvk.api.container.v2;

import java.io.File;


import ee.adit.dvk.api.ml.Util;

public class Fail
{
	private short jrkNr = 0;
	private String failPealkiri;
	private long failSuurus = 0;
	private String failTyyp;
	private String failNimi;
	private String zipBase64Sisu;
	private boolean krypteering;
	private boolean pohiDokument;
	private String pohiDokumentKonteineris;

	public short getJrkNr() {
		return jrkNr;
	}

	public void setJrkNr(short jrkNr) {
		this.jrkNr = jrkNr;
	}

	public String getFailPealkiri() {
		return failPealkiri;
	}

	public void setFailPealkiri(String failPealkiri) {
		this.failPealkiri = failPealkiri;
	}

	public long getFailSuurus() {
		return failSuurus;
	}

	public void setFailSuurus(long failSuurus) {
		this.failSuurus = failSuurus;
	}

	public String getFailTyyp() {
		return failTyyp;
	}

	public void setFailTyyp(String failTyyp) {
		this.failTyyp = failTyyp;
	}

	public String getFailNimi() {
		return failNimi;
	}

	public void setFailNimi(String failNimi) {
		this.failNimi = failNimi;
	}

	public String getZipBase64Sisu() {
		return zipBase64Sisu;
	}

	public void setZipBase64Sisu(String zipBase64Sisu) {
		this.zipBase64Sisu = zipBase64Sisu;
	}

	public boolean isKrypteering() {
		return krypteering;
	}

	public void setKrypteering(boolean krypteering) {
		this.krypteering = krypteering;
	}

	public boolean isPohiDokument() {
		return pohiDokument;
	}

	public void setPohiDokument(boolean pohiDokument) {
		this.pohiDokument = pohiDokument;
	}

	public String getPohiDokumentKonteineris() {
		return pohiDokumentKonteineris;
	}

	public void setPohiDokumentKonteineris(String pohiDokumentKonteineris) {
		this.pohiDokumentKonteineris = pohiDokumentKonteineris;
	}

	public void setFile(File file) {
		if (file == null || !file.exists()) {
			return;
		}

		failNimi = file.getName();

		int indx = failNimi.lastIndexOf('.');

		if (indx > -1 && (indx + 1 < failNimi.length())) {
			// set extension
			failTyyp = failNimi.substring(indx + 1);
		}

		failSuurus = file.length();

		zipBase64Sisu = Util.zipAndEncode(file.getPath());
	}

	public String getDecodedContent() {
		if (Util.isEmpty(zipBase64Sisu)) {
			return null;
		}

		return Util.decodeAndUnzip(zipBase64Sisu);
	}
}
