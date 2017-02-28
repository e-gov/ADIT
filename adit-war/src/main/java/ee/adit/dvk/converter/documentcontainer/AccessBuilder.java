package ee.adit.dvk.converter.documentcontainer;

import ee.adit.dvk.api.container.v2_1.Access;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class AccessBuilder {

    /**
     * Constructor.
     */
    public AccessBuilder() {
    }

    /**
     * Builds a {@link ee.adit.dvk.converter.documentcontainer.AccessConditionsCode}.
     * @return access
     */
    public Access build() {
        Access access = new Access();
        access.setAccessConditionsCode(AccessConditionsCode.AK.getVal());
        return access;
    }

}
