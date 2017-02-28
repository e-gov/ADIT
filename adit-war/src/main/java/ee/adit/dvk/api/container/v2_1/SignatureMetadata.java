package ee.adit.dvk.api.container.v2_1;

import java.util.Date;

/**
 * @author Hendrik Pärna
 * @since 29.01.14
 */
public class SignatureMetadata {
    private String signatureType;
    private String signer;
    private String verified;
    private Date signatureVerificationDate;

    public Date getSignatureVerificationDate() {
        return signatureVerificationDate;
    }

    public void setSignatureVerificationDate(Date signatureVerificationDate) {
        this.signatureVerificationDate = signatureVerificationDate;
    }

    public String getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(String signatureType) {
        this.signatureType = signatureType;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }
}
