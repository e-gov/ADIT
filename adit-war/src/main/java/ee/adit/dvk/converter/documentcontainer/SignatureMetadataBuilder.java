package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.SignatureMetadata;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.Signature;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class SignatureMetadataBuilder {

    private Document document;

    /**
     * Constructor.
     * @param document {@link Document}
     */
    public SignatureMetadataBuilder(final Document document) {
        this.document = document;
    }

    /**
     * Builds a list of {@link SignatureMetadata}.
     * @return signatureMetadata list
     */
    public List<SignatureMetadata> build() {
        List<SignatureMetadata> result = new ArrayList<SignatureMetadata>();

        if (document.getSignatures() != null) {
            for (Signature signature: document.getSignatures()) {
                SignatureMetadata signatureMetadata = new SignatureMetadata();
                signatureMetadata.setSignatureVerificationDate(signature.getSigningDate());
                result.add(signatureMetadata);
            }
        }

        return result;
    }
}
