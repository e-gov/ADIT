package ee.adit.util;

import java.util.ArrayList;
import java.util.List;

import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.Signature;

/**
 * Class for storing data about files and signatures extracted from a
 * DigiDoc container.
 * 
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DigiDocExtractionResult {
	private boolean success;
	private List<DocumentFile> files;
	private List<Signature> signatures;
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public List<DocumentFile> getFiles() {
		return files;
	}
	public void setFiles(List<DocumentFile> files) {
		this.files = files;
	}
	public List<Signature> getSignatures() {
		return signatures;
	}
	public void setSignatures(List<Signature> signatures) {
		this.signatures = signatures;
	}
	
	/**
	 * Default constructor.
	 */
	public DigiDocExtractionResult() {
		this.success = false;
		this.files = new ArrayList<DocumentFile>();
		this.signatures = new ArrayList<Signature>();
	}
}
