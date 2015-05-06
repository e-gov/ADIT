package ee.adit.dvk.converter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import dvk.api.container.v2_1.File;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.test.service.StubAditUserDAOForOrg;
import ee.adit.util.Configuration;

/**
 * @author Hendrik Pärna
 * @since 20.05.14
 */
public class FileTest {

    @Test
    public void testFileCreation() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(new StubAditUserDAOForOrg());
        Configuration configuration = new Configuration();
        configuration.setTempDir(System.getProperty("java.io.tmpdir"));
        converter.setConfiguration(configuration);

        Document document = DocumentTestingUtil.createTestDocument();
        DocumentFile documentFile = new DocumentFile();
        documentFile.setContentType("application/xml");
        documentFile.setFileSizeBytes(12288L);
        documentFile.setFileName("testFile.xml");
        documentFile.setGuid(UUID.randomUUID().toString());

        byte[] bytes = IOUtils.toByteArray(FileTest.class.getResourceAsStream("testFile.xml"));
        documentFile.setFileData(bytes);

        Set<DocumentFile> documentFiles = new HashSet<DocumentFile>();
        documentFiles.add(documentFile);
        document.setDocumentFiles(documentFiles);

        List<File> files = converter.createFiles(document);
        Assert.assertNotNull(files);
        Assert.assertNotNull(files.get(0));
        File file = files.get(0);
        Assert.assertEquals(documentFile.getContentType(), file.getMimeType());
        Assert.assertTrue(documentFile.getFileSizeBytes().intValue() == file.getFileSize());
        Assert.assertEquals(documentFile.getFileName(), file.getFileName());
        Assert.assertEquals(documentFile.getGuid(), file.getFileGuid());
        //TODO: add more sophisticated checks
        Assert.assertTrue(!file.getZipBase64Content().isEmpty());
    }


}
