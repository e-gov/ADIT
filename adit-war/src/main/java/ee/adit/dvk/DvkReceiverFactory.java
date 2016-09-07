package ee.adit.dvk;

import java.util.regex.Pattern;

import dvk.api.ml.PojoMessage;
import ee.adit.service.DocumentService;

/**
 * @author Hendrik Pärna
 * @since 12.06.14
 */
public class DvkReceiverFactory {

    private DocumentService documentService;
    private String jdigidocCfgTmpFile;

    /**
     * Constructor.
     * @param documentService {@link DocumentService}
     * @param jdigidocCfgTmpFile conf file
     */
    public DvkReceiverFactory(final DocumentService documentService, final String jdigidocCfgTmpFile) {
        this.documentService = documentService;
        this.jdigidocCfgTmpFile = jdigidocCfgTmpFile;
    }

    /**
     * Decides which implementation of the {@link DvkReceiver} should be used.
     * @param message {@link PojoMessage}
     * @return correct implementation
     */
    public DvkReceiver getReceiver(final PojoMessage message) {
        //TODO: Loading whole clob into String could cause problem in case of big document
        String xml = message.getData();

        DvkReceiver dvkReceiver;

        Pattern decContainerPattern = Pattern.compile(Pattern.quote("<DecContainer"), Pattern.CASE_INSENSITIVE);
        Pattern dhlPattern = Pattern.compile(Pattern.quote("<dhl:dokument"), Pattern.CASE_INSENSITIVE);

        if (decContainerPattern.matcher(xml).find()) {
           dvkReceiver = new Container2_1Receiver(documentService, jdigidocCfgTmpFile);
        } else if (dhlPattern.matcher(xml).find()) {
           dvkReceiver = new Container1_0Receiver(documentService, jdigidocCfgTmpFile);
        } else {
            throw new IllegalStateException("Unable to decide which container should be parsed!");
        }

        return dvkReceiver;
    }
}
