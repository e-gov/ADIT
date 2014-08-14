package ee.adit.integrationtests.Parameters;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;

/**
 * Created with IntelliJ IDEA.
 * User: Liza Leo
 * Date: 7.07.14
 * Time: 12:26
 */

public class ContainerFile {
    public final static String RECEIVE_FROM_DVK_PARAMETER_FILES_FOLDER = ConfigurationConstants.CONTAINERS_PATH + ConfigurationConstants.TO_ADIT;
    public Boolean isDdoc;
    public String guid;
    public String name;
    public Long size;
    public Boolean fileDataInDdoc;
    public String ddocDataFileId;
    public Long ddocDataFileStartOffset;
    public Long ddocDataFileEndOffset;

    private static Logger logger = org.apache.log4j.Logger.getLogger(ContainerFile.class);

    /**
     * Construstor for files in document container
     *
     * @param isDdocFile
     * @param fileGuid
     * @param fileName
     * @param fileSize
     */
    public ContainerFile(Boolean isDdocFile, String fileGuid, String fileName, Long fileSize) {
        isDdoc = isDdocFile;
        guid = (fileGuid != null) ? fileGuid : "";
        name = fileName;
        size = fileSize;
        fileDataInDdoc = false;
    }

    /**
     * Constructor for files in DDOC
     *
     * @param fileName
     * @param fileSize
     * @param ddocFileId
     * @param startOffset
     * @param endOffset
     */
    public ContainerFile(String fileName, Long fileSize, String ddocFileId, Long startOffset, Long endOffset) {
        isDdoc = false;
        guid = null;
        name = fileName;
        size = fileSize;
        fileDataInDdoc = true;
        ddocDataFileId = ddocFileId;
        ddocDataFileStartOffset = startOffset;
        ddocDataFileEndOffset = endOffset;
    }

    public boolean isResourceExists(){
        try {
            File file = new File(URLDecoder.decode(ContainerFile.class.getResource(RECEIVE_FROM_DVK_PARAMETER_FILES_FOLDER + this.name).getPath(), "UTF-8"));
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public String getFileContent() {
        String fileContent = "";
        try {
            File file = new File(URLDecoder.decode(ContainerFile.class.getResource(RECEIVE_FROM_DVK_PARAMETER_FILES_FOLDER + this.name).getPath(), "UTF-8"));
            fileContent = FileUtils.readFileToString(file, "UTF-8");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return fileContent;
    }

    public byte[] getBinaryFileContent() {
        byte[] fileContent = null;
        try {
            File file = new File(URLDecoder.decode(ContainerFile.class.getResource(RECEIVE_FROM_DVK_PARAMETER_FILES_FOLDER + this.name).getPath(), "UTF-8"));
            fileContent = FileUtils.readFileToByteArray(file);

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return fileContent;
    }

    public Boolean getDdoc() {
        return isDdoc;
    }

    public String getGuid() {
        return guid;
    }

    public Long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public Boolean getFileDataInDdoc() {
        return fileDataInDdoc;
    }

    public String getDdocDataFileId() {
        return ddocDataFileId;
    }

    public Long getDdocDataFileStartOffset() {
        return ddocDataFileStartOffset;
    }

    public Long getDdocDataFileEndOffset() {
        return ddocDataFileEndOffset;
    }
}
