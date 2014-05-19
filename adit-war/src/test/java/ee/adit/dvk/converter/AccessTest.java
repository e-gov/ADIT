package ee.adit.dvk.converter;

import dvk.api.container.v2_1.Access;
import ee.adit.dao.pojo.Document;
import ee.adit.dvk.converter.documentcontainer.AccessConditionsCode;
import ee.adit.test.service.StubDocumentTypeDAO;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Hendrik Pärna
 * @since 19.05.14
 */
public class AccessTest {

    @Test
    public void testAccess() throws Exception {
        DocumentContainerVer2_1ConverterImpl converter = new DocumentContainerVer2_1ConverterImpl();
        converter.setDocumentTypeDAO(new StubDocumentTypeDAO());
        Access access = converter.createAccess();
        Assert.assertNotNull(access);
        Assert.assertEquals(AccessConditionsCode.AK.getVal(), access.getAccessConditionsCode());
    }
}
