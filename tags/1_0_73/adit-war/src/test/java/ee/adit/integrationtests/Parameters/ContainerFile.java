package ee.adit.integrationtests.Parameters;

/**
 * Created with IntelliJ IDEA.
 * User: Liza Leo
 * Date: 7.07.14
 * Time: 12:26
 */

public class ContainerFile {
    public Boolean isDdoc;
    public String guid;
    public String name;
    public Long size;
    public Boolean fileDataInDdoc;
    public String ddocDataFileId;
    public Long ddocDataFileStartOffset;
    public Long ddocDataFileEndOffset;

    /**
     * Construstor for files in document container
     *
     * @param isDdocFile
     * @param fileGuid
     * @param fileName
     * @param fileSize
     */
    public ContainerFile(Boolean isDdocFile, String fileGuid, String fileName, Long fileSize){
        isDdoc = isDdocFile;
        guid = (fileGuid != null)? fileGuid : "";
        name = fileName;
        size = fileSize;
        fileDataInDdoc = false;
    }

    /**
     * Constructor for files in DDOC
     *
     * @param fileName
     * @param fileSize
     */
    public ContainerFile(String fileName, Long fileSize, String ddocFileId, Long startOffset, Long endOffset){
        isDdoc = false;
        guid = null;
        name = fileName;
        size = fileSize;
        fileDataInDdoc = true;
        ddocDataFileId = ddocFileId;
        ddocDataFileStartOffset = startOffset;
        ddocDataFileEndOffset = endOffset;
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
