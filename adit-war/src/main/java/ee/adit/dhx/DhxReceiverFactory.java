package ee.adit.dhx;

import java.util.regex.Pattern;

import ee.adit.service.DocumentService;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

/**
 * @author Hendrik Pärna
 * @since 12.06.14
 */
public class DhxReceiverFactory {

    private DocumentService documentService;
    private String jdigidocCfgTmpFile;

    /**
     * Constructor.
     * @param documentService {@link DocumentService}
     * @param jdigidocCfgTmpFile conf file
     */
    public DhxReceiverFactory(final DocumentService documentService, final String jdigidocCfgTmpFile) {
        this.documentService = documentService;
        this.jdigidocCfgTmpFile = jdigidocCfgTmpFile;
    }
   
    
    /**
     * Decides which implementation of the {@link DhxReceiver} should be used.
     * @param message {@link PojoMessage}
     * @return correct implementation
     */
    public DhxReceiver getReceiver(final Object container) {
    	DhxReceiver dhxReceiver = null;
        if (container instanceof DecContainer) {
           dhxReceiver = new Container2_1Receiver(documentService, jdigidocCfgTmpFile);
        } else {
            throw new IllegalStateException("Unable to decide which container should be parsed!");
        }

        return dhxReceiver;
    }
}
