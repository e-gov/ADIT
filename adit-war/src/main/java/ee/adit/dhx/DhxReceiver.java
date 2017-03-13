package ee.adit.dhx;

/**
 * @author Hendrik Pärna
 * @since 12.06.14
 */
public interface DhxReceiver {
    /**
     * Receive a message from DHX.
     * @param message {@link PojoMessage}
     * @return id of the received document.
     */
    Long receive(final String containerFile, String consignmentId);
}
