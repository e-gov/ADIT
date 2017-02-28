package ee.adit.dvk.converter;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ee.adit.dvk.api.container.v2_1.Recipient;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.service.DocumentService;
import ee.adit.test.service.StubAditUserDAOForPerson;
import ee.adit.test.service.StubDocumentTypeDAO;

/**
 * @author Hendrik Pärna
 * @since 19.05.14
 */
public class ReceipientsTest {


    @Test
    public void testRecipients() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setDocumentTypeDAO(new StubDocumentTypeDAO());
        converter.setAditUserDAO(new StubAditUserDAOForPerson());
        Document document = DocumentTestingUtil.createTestDocument();

        DocumentSharing documentSharing = new DocumentSharing();
        documentSharing.setUserCode("someCode");
        documentSharing.setDocumentSharingType(DocumentService.SHARINGTYPE_SEND_DVK);
        documentSharing.setDocumentDvkStatus(DocumentService.DVK_STATUS_MISSING);
        document.setId(1);
        document.setCreationDate(new Date());
        document.setTitle("My title");

        Set<DocumentSharing> documentSharings = new HashSet<DocumentSharing>();
        documentSharings.add(documentSharing);
        document.setDocumentSharings(documentSharings);

        List<Recipient> recipients = converter.createRecipients(document, documentSharings);

        Assert.assertNotNull(recipients);
        Assert.assertEquals(1, recipients.size());
        Recipient recipient = recipients.get(0);
        Assert.assertNotNull(recipient);
        Assert.assertEquals("Test User", recipient.getPerson().getName());
        Assert.assertEquals("Test", recipient.getPerson().getGivenName());
        Assert.assertEquals("User", recipient.getPerson().getSurname());
        Assert.assertEquals("36212240216", recipient.getPerson().getPersonalIdCode());
        Assert.assertEquals("EE", recipient.getPerson().getResidency());
    }
}
