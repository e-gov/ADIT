package ee.adit.integrationtests;

import dvk.api.container.v1.ContainerVer1;
import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.service.DocumentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * @author Hendrik Pärna
 * @since 15.05.14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:integration-tests.xml"})
public class AppSetupTest {
    @Autowired
    private DvkDAO dvkDAO;
    @Autowired
    private DocumentDAO documentDAO;
    @Autowired
    private DocumentService documentService;


    @Test
    public void justTestAppContextSetup() throws Exception {
        Assert.notNull(dvkDAO);
        Assert.notNull(dvkDAO.getHibernateTemplate());
        Assert.notNull(dvkDAO.getSessionFactory());

        Document document = documentDAO.getDocument(3493);

        PojoMessage message = dvkDAO.getMessage(81);

        Assert.notNull(document);
    }

    @Test
    public void receiveDocumentFromDVKClient() throws Exception {
        // Path to the container, and parse XML to a container
        String containerFile = AppSetupTest.
                class.getResource("/testOldContainer.xml").getPath();
        String digiDocConfFile = AppSetupTest.
                class.getResource("/jdigidoc.cfg").getPath();
//        ContainerVer2_1 inputContainer = new ContainerVer2_1();
        // TODO: fix this string
        // inputContainer = ContainerVer2_1.parseFile(containerFile);
        // Prepare a new message
        PojoMessage newMessage = UtilsService.prepareMessageBeforeInsert(dvkDAO, containerFile);
        // Save the message to DVK Client DB
        dvkDAO.updateDocument(newMessage);
        documentService.receiveDocumentsFromDVK(digiDocConfFile);
        ContainerVer1 container = ContainerVer1.parse(UtilsService.readSQLToString(containerFile));

        Assert.notNull(container);
//
    }

    @Test
    public void sendDocumentToDVKClient() throws Exception {
        // TODO: 1) Prepare a message 2) Insert to ADIT DB 3) DocumentService.sendToDVK 4) Asserts
    }
}
