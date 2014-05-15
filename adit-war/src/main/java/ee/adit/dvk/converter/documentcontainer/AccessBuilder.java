package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.Access;
import ee.adit.dao.pojo.Document;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class AccessBuilder {

    private Document document;

    /**
     * Constructor.
     * @param document {@link Document}
     */
    public AccessBuilder(final Document document) {
         this.document = document;
    }

    /**
     * Builds a {@link Access}.
     * @return access
     */
    public Access build() {
        Access access = new Access();

        return access;
    }
}
