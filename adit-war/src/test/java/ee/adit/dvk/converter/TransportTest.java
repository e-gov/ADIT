package ee.adit.dvk.converter;

import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.DecRecipient;
import dvk.api.container.v2_1.DecSender;
import dvk.api.container.v2_1.Transport;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.service.DocumentService;
import ee.adit.test.service.StubAditUserDAOForOrg;
import ee.adit.test.service.StubAditUserDAOForPerson;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Hendrik Pärna
 * @since 22.04.14
 */
public class TransportTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConvertWithNull() {
        Converter<Document, ContainerVer2_1> converter = new DocumentContainerVer2_1ConverterImpl();
        Document document = null;
        converter.convert(document);
    }

    @Test
    public void testDecSender_whenSenderIsPerson() throws Exception {
        DocumentContainerVer2_1ConverterImpl converter = new DocumentContainerVer2_1ConverterImpl();
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
        DocumentContainerVer2_1ConverterImpl converter = new DocumentContainerVer2_1ConverterImpl();
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
        DocumentContainerVer2_1ConverterImpl converter = new DocumentContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForPerson());
        converter.setConfiguration(DocumentTestingUtil.createConfiguration());
        Document document = DocumentTestingUtil.createTestDocument();
        document.setDocumentSharings(createDocumentSharings());
        Transport transport = converter.createTransport(document);
        Assert.assertNotNull(transport);
        Assert.assertNotNull(transport.getDecRecipient());
        Assert.assertNotNull(transport.getDecRecipient().get(0));
        DecRecipient recipient = transport.getDecRecipient().get(0);
        Assert.assertEquals("36212240216", recipient.getPersonalIdCode());
        Assert.assertEquals("12345678", recipient.getOrganisationCode());
        Assert.assertNull(recipient.getStructuralUnit());
    }

    @Test
    public void testDecRecipient_whenSentToOrg() throws Exception {
        DocumentContainerVer2_1ConverterImpl converter = new DocumentContainerVer2_1ConverterImpl();
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
        Assert.assertEquals("MyComp OÜ", recipient.getStructuralUnit());
    }



    private Set<DocumentSharing> createDocumentSharings() {
        Set<DocumentSharing> sharings = new HashSet<DocumentSharing>();
        DocumentSharing documentSharing = new DocumentSharing();
        documentSharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SEND_DVK);
        documentSharing.setDocumentDvkStatus(DocumentService.DVK_STATUS_WAITING);
        sharings.add(documentSharing);
        return sharings;
    }
}
