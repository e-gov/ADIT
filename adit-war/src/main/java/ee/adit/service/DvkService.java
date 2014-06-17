package ee.adit.service;

/**
 * @author Hendrik Pärna
 * @since 21.04.14
 */
public interface DvkService {

    /**
     * Fetches all the documents that are to be sent to DVK. The DVK documents
     * are recognized by the following: <br />
     * 1. The document has at least one DocumentSharing associated with it
     * 2. That DocumentSharing must have the "documentSharingType" equal to
     * "send_dvk" <br />
     * 3. That DocumentSharing must have the "documentDvkStatus" not initialized
     * or set to "100"
     *
     * @return number of documents sent to DVK
     */
    int sendDocumentsToDVK();
}
