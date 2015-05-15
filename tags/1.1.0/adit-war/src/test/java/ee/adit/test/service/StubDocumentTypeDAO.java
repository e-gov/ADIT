package ee.adit.test.service;

import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.DocumentType;

/**
 * @author Hendrik Pärna
 * @since 5.05.14
 */
public class StubDocumentTypeDAO extends DocumentTypeDAO {

    @Override
    public DocumentType getDocumentType(String documentTypeShortName) {
        return new DocumentType("application", "Avaldus / Taotlus", null);
    }
}
