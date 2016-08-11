package ee.adit.dvk.converter;

import ee.adit.dao.pojo.Document;
import ee.adit.util.Configuration;

/**
 * @author Hendrik Pärna
 * @since 23.04.14
 */
public class DocumentTestingUtil {

    public static Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setDvkOrgCode("12345678");
        return configuration;
    }

    public static Document createTestDocument() {
        Document document = new Document();
        document.setCreatorCode("36212240216");
        document.setCreatorUserName("Test User");
        document.setDvkId(1L);
        return document;
    }
}
