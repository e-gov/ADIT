package ee.adit.util;

import java.util.ArrayList;
import java.util.List;

public class FileSplitResult {
    private String mainFile;
    private List<String> subFiles;
    
    public FileSplitResult() {
        mainFile = "";
        subFiles = new ArrayList<String>();
    }

	public String getMainFile() {
		return mainFile;
	}

	public void setMainFile(String mainFile) {
		this.mainFile = mainFile;
	}

	public List<String> getSubFiles() {
		return subFiles;
	}

	public void setSubFiles(List<String> subFiles) {
		this.subFiles = subFiles;
	}
}