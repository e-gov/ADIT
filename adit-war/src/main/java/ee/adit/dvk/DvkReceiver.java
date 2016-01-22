package ee.adit.dvk;

import java.util.List;

import dvk.api.ml.PojoMessage;

/**
 * @author Hendrik Pärna
 * @since 12.06.14
 */
public interface DvkReceiver {
	
    /**
     * Receive a message from DVK.
     * @param message {@link PojoMessage}
     * @return a list of wrapper objects containing data related to this operation
     */
    List<DispatchReport> receive(PojoMessage message);
    
}
