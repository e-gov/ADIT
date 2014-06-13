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

    private enum SignatureType {
        DIGITAL_SIGNATURE("Digitaalallkiri"), DIGITAL_STAMP("Digitempel");

        private String value;

        SignatureType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

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
                signatureMetadata.setSignatureType(getSignatureType(signature.getSignerCode()));
                result.add(signatureMetadata);
            }
        }

        return result;
    }

    private String getSignatureType(final String signerCode) {
        String signatureType = null;

        if (signerCode != null) {
            if (signerCode.startsWith("3")
                    || signerCode.startsWith("4")
                    || signerCode.startsWith("5")
                    || signerCode.startsWith("6")) {
                signatureType = SignatureType.DIGITAL_SIGNATURE.value;
            } else {
                signatureType = SignatureType.DIGITAL_STAMP.value;
            }
        }

        return signatureType;
    }
}
