package ee.adit.dvk.converter.documentcontainer;

import dvk.api.container.v2_1.RecordSenderToDec;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.Document;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class RecordSenderToDecBuilder extends ContactInfoBuilder {

    private Document document;
    private AditUserDAO aditUserDAO;

    /**
     * Constructor.
     *
     * @param document    {@link ee.adit.dao.pojo.Document}
     * @param aditUserDAO {@link ee.adit.dao.AditUserDAO}
     */
    public RecordSenderToDecBuilder(final Document document, final AditUserDAO aditUserDAO) {
        super(document, aditUserDAO);
    }


    /**
     * Builds a {@link RecordSenderToDec}.
     *
     * @return recordSenderToDec
     */
    @Override
    public RecordSenderToDec build() {
        return (RecordSenderToDec) super.build();
    }
}
