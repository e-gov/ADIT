package ee.adit.dvk.api.container;

import java.util.ArrayList;
import java.util.List;

public class ArrayOfSignature {
    private List<Signature> signature;

    public List<Signature> getSignature() {
        return signature;
    }

    public void setSignature(List<Signature> signature) {
        this.signature = signature;
    }

    public ArrayOfSignature() {
        this.signature = new ArrayList<Signature>();
    }
}
