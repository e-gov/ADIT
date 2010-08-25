package ee.adit.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder class for the file splitting result. Files are splitted to remove the actual file data from certain request payloads
 * so only metadata (meaningful XML) remains for marshalling / umarshalling.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class FileSplitResult {
    
	/**
	 * The main file (from where the file data is removed)
	 */
	private String mainFile;
    
	/**
	 * The subfiles (file data is removed from the main file and put into a subfile)
	 */
    private List<String> subFiles;
    
    /**
     * Default constructor
     */
    public FileSplitResult() {
        mainFile = "";
        subFiles = new ArrayList<String>();
    }

    /**
     * Retrieves the main file
     * @return
     */
	public String getMainFile() {
		return mainFile;
	}

	/**
	 * Sets the main file
	 * @param mainFile
	 */
	public void setMainFile(String mainFile) {
		this.mainFile = mainFile;
	}

	/**
	 * Retrieves the subfiles list
	 * @return
	 */
	public List<String> getSubFiles() {
		return subFiles;
	}

	/**
	 * Sets the subfiles list
	 * @param subFiles
	 */
	public void setSubFiles(List<String> subFiles) {
		this.subFiles = subFiles;
	}
}