package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.File;
import ee.adit.dao.pojo.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hendrik Pärna
 * @since 7.05.14
 */
public class FileBuilder {

    private Document document;

    /**
     * Constructor.
     * @param document {@link Document}
     */
    public FileBuilder(final Document document) {
        this.document = document;
    }

    /**
     * Creates a list of {@link File}.
     * @return list of files
     */
    public List<File> build() {
        List<File> files = new ArrayList<File>();

        return files;
    }
}
