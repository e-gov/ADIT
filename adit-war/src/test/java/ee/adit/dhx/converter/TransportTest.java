package ee.adit.dhx.converter;

import java.util.HashSet;

import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.DecRecipient;
import ee.adit.dhx.api.container.v2_1.DecSender;
import ee.adit.dhx.api.container.v2_1.Transport;
import ee.adit.dhx.converter.Converter;
import ee.adit.dhx.converter.DocumentToContainerVer2_1ConverterImpl;
import ee.adit.service.DocumentService;
import ee.adit.test.service.StubAditUserDAOForOrg;
import ee.adit.test.service.StubAditUserDAOForPerson;

/**
 * @author Hendrik Pärna
 * @since 22.04.14
 */
public class TransportTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConvertWithNull() {
        Converter<Document, ContainerVer2_1> converter = new DocumentToContainerVer2_1ConverterImpl();
        Document document = null;
        converter.convert(document);
    }

    @Test
    public void testDecSender_whenSenderIsPerson() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForPerson());
        converter.setConfiguration(DocumentTestingUtil.createConfiguration());
        Document document = DocumentTestingUtil.createTestDocument();
        Transport transport = converter.createTransport(document);
        Assert.assertNotNull(transport);
        Assert.assertNotNull(transport.getDecSender());
        DecSender sender = transport.getDecSender();
        Assert.assertEquals("36212240216", sender.getPersonalIdCode());
        Assert.assertEquals("12345678", sender.getOrganisationCode());
    }


    @Test
    @Ignore
    public void testDecSender_whenSenderIsOrg() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForOrg());
        converter.setConfiguration(DocumentTestingUtil.createConfiguration());
        Document document = DocumentTestingUtil.createTestDocument();
        Transport transport = converter.createTransport(document);
        Assert.assertNotNull(transport);
        Assert.assertNotNull(transport.getDecSender());
        DecSender sender = transport.getDecSender();
        Assert.assertEquals("36212240216", sender.getPersonalIdCode());
        Assert.assertEquals("12345678", sender.getOrganisationCode());
        Assert.assertEquals("MyComp OÜ", sender.getStructuralUnit());
    }

    @Test
    public void testDecRecipient_whenSentToPerson() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForPerson());
        converter.setConfiguration(DocumentTestingUtil.createConfiguration());
        Document document = DocumentTestingUtil.createTestDocument();
        document.setDocumentSharings(createDocumentSharings());
        Transport transport = converter.createTransport(document);
        Assert.assertNotNull(transport);
        Assert.assertNotNull(transport.getDecRecipient());
        Assert.assertNotNull(transport.getDecRecipient().get(0));
        DecRecipient recipient = transport.getDecRecipient().get(0);
        Assert.assertNull(recipient.getPersonalIdCode());
        Assert.assertEquals("12345678", recipient.getOrganisationCode());
        Assert.assertNull(recipient.getStructuralUnit());
    }

    @Test
    public void testDecRecipient_whenSentToOrg() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForOrg());
        converter.setConfiguration(DocumentTestingUtil.createConfiguration());
        Document document = DocumentTestingUtil.createTestDocument();
        document.setDocumentSharings(createDocumentSharings());
        Transport transport = converter.createTransport(document);
        Assert.assertNotNull(transport);
        Assert.assertNotNull(transport.getDecRecipient());
        Assert.assertNotNull(transport.getDecRecipient().get(0));
        DecRecipient recipient = transport.getDecRecipient().get(0);
        Assert.assertNull(recipient.getPersonalIdCode());
        Assert.assertEquals("12345678", recipient.getOrganisationCode());
        Assert.assertNull(recipient.getStructuralUnit());
    }



    private Set<DocumentSharing> createDocumentSharings() {
        Set<DocumentSharing> sharings = new HashSet<DocumentSharing>();
        DocumentSharing documentSharing = new DocumentSharing();
        documentSharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SEND_DHX);
        documentSharing.setDocumentDvkStatus(DocumentService.DHX_STATUS_WAITING);
        sharings.add(documentSharing);
        return sharings;
    }
}
