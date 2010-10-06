package ee.adit.pojo;

import java.util.List;

public class OutputDocumentFilesList {
	private Integer totalFiles;
	private Long totalBytes;
	private List<OutputDocumentFile> files;
	
	public Integer getTotalFiles() {
		return totalFiles;
	}
	public void setTotalFiles(Integer totalFiles) {
		this.totalFiles = totalFiles;
	}
	
	public Long getTotalBytes() {
		return totalBytes;
	}
	public void setTotalBytes(Long totalBytes) {
		this.totalBytes = totalBytes;
	}
	
	public List<OutputDocumentFile> getFiles() {
		return files;
	}
	public void setFiles(List<OutputDocumentFile> files) {
		this.files = files;
	}
}
