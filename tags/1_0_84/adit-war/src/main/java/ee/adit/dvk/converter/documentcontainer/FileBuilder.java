package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.File;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.service.DocumentService;
import ee.adit.util.Configuration;
import ee.adit.util.Util;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hendrik Pärna
 * @since 7.05.14
 */
public class FileBuilder {

    private static Logger logger = Logger.getLogger(FileBuilder.class);

    private Document document;
    private Configuration configuration;

    /**
     * Constructor.
     *
     * @param document {@link Document}
     */
    public FileBuilder(final Document document, final Configuration configuration) {
        this.document = document;
        this.configuration = configuration;
    }

    /**
     * Creates a list of {@link File}.
     *
     * @return list of files
     */
    public List<File> build() {
        List<File> files = new ArrayList<File>();

        if (document.getDocumentFiles() != null) {
            for (DocumentFile documentFile : document.getDocumentFiles()) {
                // Check should these files be sent
                // Take files that are not DDOC files, not deleted, and file type is FILETYPE_SIGNATURE_CONTAINER or FILETYPE_DOCUMENT_FILE
                if ((documentFile.getFileDataInDdoc() == null || !documentFile.getFileDataInDdoc()) &&
                        (documentFile.getDeleted() == null || !documentFile.getDeleted()) &&
                        ((documentFile.getDocumentFileTypeId() == DocumentService.FILETYPE_SIGNATURE_CONTAINER) ||
                        (documentFile.getDocumentFileTypeId() == DocumentService.FILETYPE_DOCUMENT_FILE))) {
                    File file = new File();
                    file.setFileGuid(documentFile.getGuid());
                    file.setFileName(documentFile.getFileName());
                    file.setFileSize(documentFile.getFileSizeBytes().intValue());
                    file.setMimeType(documentFile.getContentType());
                    file.setZipBase64Content(getGZippedBase64FileContent(documentFile));
                    files.add(file);
                }
            }
        }

        return files;
    }

    private String getGZippedBase64FileContent(DocumentFile documentFile) {
        String result = null;

        try {
            InputStream inputStream = documentFile.getFileData().getBinaryStream();
            String binaryContentsFile = Util.createTemporaryFile(inputStream, configuration.getTempDir());
            String gzAndBase64EncodedFile = Util.gzipAndBase64Encode(binaryContentsFile, configuration.getTempDir(), true);
            result = Util.getFileContents(new java.io.File(gzAndBase64EncodedFile));
        } catch (Exception e) {
            logger.error("Unable to create the gzipped and base64 encoded file contents: ", e);
        }

        return result;
    }
}
