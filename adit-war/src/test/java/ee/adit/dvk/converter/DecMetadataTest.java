package ee.adit.dvk.converter;

import dvk.api.container.v2_1.DecMetadata;
import ee.adit.dao.pojo.Document;
import ee.adit.test.service.StubAditUserDAOForOrg;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Hendrik Pärna
 * @since 23.04.14
 */
public class DecMetadataTest {

    @Test
    public void testDecMetadata() throws Exception {
        DocumentContainerVer2_1ConverterImpl converter = new DocumentContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForOrg());
        converter.setConfiguration(DocumentTestingUtil.createConfiguration());
        Document document = DocumentTestingUtil.createTestDocument();
        DecMetadata decMetadata = converter.createDecMetaData(document);
        Assert.assertNotNull(decMetadata);
        Assert.assertNotNull(decMetadata.getDecId());
        Assert.assertEquals("1", decMetadata.getDecId());
    }
}
