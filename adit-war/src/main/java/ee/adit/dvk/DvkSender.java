package ee.adit.dvk;

import ee.adit.dao.pojo.Document;

/**
 * @author Hendrik Pärna
 * @since 13.06.14
 */
public interface DvkSender {

    /**
     * Send document to dvk client.
     * @param document {@link Document}
     * @return id of the {@link ee.adit.dvk.api.ml.PojoMessage}
     */
    Long send(Document document);
}
