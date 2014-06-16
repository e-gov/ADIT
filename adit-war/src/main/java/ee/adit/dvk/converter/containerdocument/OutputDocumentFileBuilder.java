package ee.adit.dvk.converter.containerdocument;

import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.File;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.util.Configuration;
import ee.adit.util.Util;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hendrik Pärna
 * @since 10.06.14
 */
public class OutputDocumentFileBuilder {
    private static Logger logger = Logger.getLogger(OutputDocumentFileBuilder.class);
    private Configuration configuration;
    private ContainerVer2_1 container;

    /**
     * Constructor.
     * @param configuration - configuration
     * @param container - 2.1 version of the container.
     */
    public OutputDocumentFileBuilder(final Configuration configuration, final ContainerVer2_1 container) {
        this.container = container;
        this.configuration = configuration;
    }

    /**
     * Builds a list of DocumentFiles from ContainerVer2_1.
     * @return list of DocumentFiles.
     */
    public List<OutputDocumentFile> build() {
        return getDocumentOutputFiles();
    }

    /**
     * Extracts files from DVK container.
     *
     * @return list of files extracted
     */
    private List<OutputDocumentFile> getDocumentOutputFiles() {
        List<OutputDocumentFile> result = new ArrayList<OutputDocumentFile>();

        try {
            if (container.getFile() != null) {
                logger.debug("Total number of files in DVK Container: " + container.getFile().size());

                for (File dvkFile: container.getFile()) {
                    logger.debug("Processing file with GUID: " + dvkFile.getFileGuid());

                    String fileContents = dvkFile.getZipBase64Content();
                    InputStream inputStream = new StringBufferInputStream(fileContents);
                    String tempFile = Util.createTemporaryFile(inputStream, configuration.getTempDir());
                    String decodedTempFile = Util.base64DecodeAndUnzip(tempFile, configuration.getTempDir(), true);

                    OutputDocumentFile tempDocument = new OutputDocumentFile();
                    tempDocument.setSysTempFile(decodedTempFile);
                    tempDocument.setContentType(dvkFile.getMimeType());
                    tempDocument.setName(dvkFile.getFileName());
                    tempDocument.setSizeBytes(dvkFile.getFileSize().longValue());
                    tempDocument.setGuid(dvkFile.getFileGuid());
                    // Add the temporary file to the list
                    result.add(tempDocument);
                }
            }
        } catch (Exception e) {
            if (result.size() > 0) {
                // Delete temporary files
                for (OutputDocumentFile documentToDelete: result) {
                    try {
                        if (documentToDelete.getSysTempFile() != null
                                && !documentToDelete.getSysTempFile().trim().equalsIgnoreCase("")) {
                            java.io.File f = new java.io.File(documentToDelete.getSysTempFile());
                            if (f.exists()) {
                                f.delete();
                            } else {
                                throw new FileNotFoundException("Could not find temporary file (to delete): "
                                        + documentToDelete.getSysTempFile());
                            }
                        }
                    } catch (Exception exc) {
                        logger.debug("Error while deleting temporary files: ", exc);
                    }
                }
                logger.info("Temporary files deleted.");
            }
            throw new AditInternalException("Error while saving files: ", e);
        }

        return result;
    }
}
