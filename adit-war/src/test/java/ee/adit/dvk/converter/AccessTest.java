package ee.adit.dvk.converter;

import org.junit.Test;

import ee.adit.dvk.api.container.v2_1.Access;
import ee.adit.dvk.converter.documentcontainer.AccessConditionsCode;
import ee.adit.test.service.StubDocumentTypeDAO;
import junit.framework.Assert;

/**
 * @author Hendrik Pärna
 * @since 19.05.14
 */
public class AccessTest {

    @Test
    public void testAccess() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setDocumentTypeDAO(new StubDocumentTypeDAO());
        Access access = converter.createAccess();
        Assert.assertNotNull(access);
        Assert.assertEquals(AccessConditionsCode.AK.getVal(), access.getAccessConditionsCode());
    }
}
