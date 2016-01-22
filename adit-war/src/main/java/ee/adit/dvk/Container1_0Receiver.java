package ee.adit.dvk;

import dvk.api.ml.PojoMessage;
import ee.adit.service.DocumentService;

/**
 * @author Hendrik Pärna
 * @since 12.06.14
 */
public class Container1_0Receiver implements DvkReceiver {

    private DocumentService documentService;
    private String jdigidocCfgTmpFile;

    /**
     * Constructor.
     * @param documentService {@link DocumentService}
     * @param jdigidocCfgTmpFile tmp file
     */
    public Container1_0Receiver(final DocumentService documentService, final String jdigidocCfgTmpFile) {
        this.documentService = documentService;
        this.jdigidocCfgTmpFile = jdigidocCfgTmpFile;
    }

    @Override
    public boolean receive(final PojoMessage message) {
        return documentService.receiveSingleDocumentFromDVK(message, jdigidocCfgTmpFile);
    }
}
