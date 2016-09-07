package ee.adit.dvk.converter;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import dvk.api.container.v2_1.RecordMetadata;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.Signature;
import ee.adit.test.service.StubDocumentTypeDAO;

/**
 * @author Hendrik Pärna
 * @since 5.05.14
 */
public class RecordMetadataTest {

    @Test
    public void testGeneralDataAndRecordDateRegistered_whenNoSignaturesPresent() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setDocumentTypeDAO(new StubDocumentTypeDAO());

        Document document = DocumentTestingUtil.createTestDocument();
        document.setId(1);
        document.setCreationDate(new Date());
        document.setTitle("My title");
        RecordMetadata recordMetadata = converter.createRecordMetadata(document);

        Assert.assertNotNull(recordMetadata);
        Assert.assertEquals("Avaldus / Taotlus", recordMetadata.getRecordType());
        Assert.assertEquals("1", recordMetadata.getRecordOriginalIdentifier());
        Assert.assertEquals(document.getCreationDate().getTime(), recordMetadata.getRecordDateRegistered().getTime());
        Assert.assertEquals(document.getTitle(), recordMetadata.getRecordTitle());
    }

    @Test
    public void testRecordDateRegisteredWhenSignaturesPresent() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setDocumentTypeDAO(new StubDocumentTypeDAO());

        Document document = DocumentTestingUtil.createTestDocument();
        document.setId(1);
        document.setCreationDate(new Date());
        document.setTitle("My title");

        Set<Signature> signatures = new HashSet<Signature>();
        Signature s1 = new Signature();
        s1.setSigningDate(getDateBeforeDays(3));
        Signature s2 = new Signature();
        s2.setSigningDate(getDateBeforeDays(1));
        signatures.add(s1);
        signatures.add(s2);
        document.setSignatures(signatures);

        RecordMetadata recordMetadata = converter.createRecordMetadata(document);
        Assert.assertEquals(s1.getSigningDate().getTime(), recordMetadata.getRecordDateRegistered().getTime());
    }

    private Date getDateBeforeDays(int before) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -before);
        return cal.getTime();
    }
}
