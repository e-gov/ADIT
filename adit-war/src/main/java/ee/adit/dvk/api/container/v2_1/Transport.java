package ee.adit.dvk.api.container.v2_1;

import java.util.List;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class Transport {
    private DecSender decSender;
    private List<DecRecipient> decRecipient;

    public DecSender getDecSender() {
        return decSender;
    }

    public void setDecSender(DecSender decSender) {
        this.decSender = decSender;
    }

    public List<DecRecipient> getDecRecipient() {
        return decRecipient;
    }

    public void setDecRecipient(List<DecRecipient> decRecipient) {
        this.decRecipient = decRecipient;
    }
}
