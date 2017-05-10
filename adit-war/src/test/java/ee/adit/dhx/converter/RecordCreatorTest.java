package ee.adit.dhx.converter;

import org.junit.Assert;

import org.junit.Test;

import ee.adit.dao.pojo.Document;
import ee.adit.dhx.api.container.v2_1.RecordCreator;
import ee.adit.dhx.converter.DocumentToContainerVer2_1ConverterImpl;
import ee.adit.test.service.StubAditUserDAOForOrg;
import ee.adit.test.service.StubAditUserDAOForPerson;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class RecordCreatorTest {

    @Test
    public void testOrganization() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForOrg());

        Document document = DocumentTestingUtil.createTestDocument();
        RecordCreator documentCreator = converter.createRecordCreator(document);

        Assert.assertNotNull(documentCreator);
        Assert.assertNotNull(documentCreator.getOrganisation());
        Assert.assertEquals("MyComp OÜ", documentCreator.getOrganisation().getName());
        Assert.assertEquals("12345678", documentCreator.getOrganisation().getOrganisationCode());
        Assert.assertEquals("EE", documentCreator.getOrganisation().getResidency());
    }

    @Test
    public void testPerson() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForPerson());

        Document document = DocumentTestingUtil.createTestDocument();
        RecordCreator documentCreator = converter.createRecordCreator(document);

        Assert.assertNotNull(documentCreator);
        Assert.assertNotNull(documentCreator.getPerson());
        Assert.assertEquals("Test User", documentCreator.getPerson().getName());
        Assert.assertEquals("Test", documentCreator.getPerson().getGivenName());
        Assert.assertEquals("User", documentCreator.getPerson().getSurname());
        Assert.assertEquals("36212240216", documentCreator.getPerson().getPersonalIdCode());
        Assert.assertEquals("EE", documentCreator.getPerson().getResidency());
    }

    @Test
    public void testPersonForOrg() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForPerson());

        Document document = DocumentTestingUtil.createTestDocument();
        RecordCreator documentCreator = converter.createRecordCreator(document);

        Assert.assertNotNull(documentCreator);
        Assert.assertNotNull(documentCreator.getPerson());
        Assert.assertEquals("Test User", documentCreator.getPerson().getName());
        Assert.assertEquals("Test", documentCreator.getPerson().getGivenName());
        Assert.assertEquals("User", documentCreator.getPerson().getSurname());
        Assert.assertEquals("36212240216", documentCreator.getPerson().getPersonalIdCode());
        Assert.assertEquals("EE", documentCreator.getPerson().getResidency());
    }
}
