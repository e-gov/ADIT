package ee.adit.integrationtests.Parameters;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Liza Leo
 * Date: 11.07.14
 * Time: 14:30
 */

public class ContainerSignature {
    public String userCode;
    public String signerCode;
    public String signerName;
    public Date signingDate;

    public ContainerSignature(String uc, String sc, String sn, Date sd){
        userCode = uc;
        signerCode = sc;
        signerName = sn;
        signingDate = sd;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getSignerCode() {
        return signerCode;
    }

    public String getSignerName() {
        return signerName;
    }

    public Date getSigningDate() {
        return signingDate;
    }

}
