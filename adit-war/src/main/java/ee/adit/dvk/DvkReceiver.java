package ee.adit.dvk;

import ee.adit.dvk.api.ml.PojoMessage;

/**
 * @author Hendrik Pärna
 * @since 12.06.14
 */
public interface DvkReceiver {
    /**
     * Receive a message from DVK.
     * @param message {@link PojoMessage}
     * @return true if successfully received, otherwise false.
     */
    boolean receive(PojoMessage message);
}
