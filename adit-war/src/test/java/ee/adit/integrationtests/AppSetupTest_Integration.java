package ee.adit.integrationtests;

import dvk.api.container.Container;
import dvk.api.container.v1.ContainerVer1;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentFileDAO;
import ee.adit.dao.DocumentSharingDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.service.DocumentService;
import ee.adit.util.Util;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Hendrik Pärna
 * @since 15.05.14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:integration-tests.xml"})
public class AppSetupTest_Integration {
    final static String CONTAINERS_PATH = "/containers/";
    final static long DEFAULT_DHL_ID = 1;
    final static String DEFAULT_DOCUMENT_TITLE = "Integration Tests TestDocument";
    final static UUID DEFAULT_GUID = UUID.randomUUID();

    //private static Logger logger = Logger.getLogger(UtilsService.class);

    @Autowired
    private DvkDAO dvkDAO;
    @Autowired
    private DocumentDAO documentDAO;
    @Autowired
    private DocumentSharingDAO documentSharingDAO;
    @Autowired
    private DocumentFileDAO documentFileDAO;
    @Autowired
    private DocumentService documentService;

    private List<PojoMessage> dvkMessages = new ArrayList<PojoMessage>();
    private List<Document> aditDocuments = new ArrayList<Document>();

    @Before
    public void beforeTest() {
        try {
            clearDvkDb(dvkMessages);
            clearAditDb(aditDocuments);
        } catch (Exception e) {
            //TODO: Smth to do with exceptions
            //logger.error(e.getMessage());
        }
    }

    @After
    public void afterTest() {
        try {
            clearDvkDb(dvkMessages);
            clearAditDb(aditDocuments);

            dvkMessages.clear();
            aditDocuments.clear();
        } catch (Exception e) {
            //TODO: Smth to do with exceptions
            //logger.error(e.getMessage());
        }
    }

    @Test
    public void justTestAppContextSetup() throws Exception {
        Assert.notNull(dvkDAO);
        Assert.notNull(dvkDAO.getHibernateTemplate());
        Assert.notNull(dvkDAO.getSessionFactory());

        Assert.notNull(documentSharingDAO);

        Document document = documentDAO.getDocument(10780);
        PojoMessage message = dvkDAO.getMessage(412);

        Assert.notNull(document);
        Assert.notNull(message);
    }

    //
    // Test receiveDocumentFromDVKClient with container v 1.0
    //
    @Test
    public void receiveDocumentFromDVKClient_V1_Test() throws Exception {
        final String DIGIDOC_CONF_FILE = "/jdigidoc.cfg";
        final String CONTAINER_V_1_0 = "ADIT_8_containerVer1_0_nok_dok_3.xml";

        String digiDocConfFilePath = null;
        String containerFilePath;
        File containerFile = null;

        try {
            // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
            digiDocConfFilePath = AppSetupTest_Integration.class.getResource(DIGIDOC_CONF_FILE).getPath();
            // Path to the container v 1.0
            containerFilePath = AppSetupTest_Integration.class.getResource(UtilsService.getContainerPath(CONTAINER_V_1_0)).getPath();
            containerFile = new File(containerFilePath);

        } catch (Exception e) {
            //todo: inform about problems with files
        }


        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!" + DEFAULT_GUID);

        ContainerVer1 container = (ContainerVer1) UtilsService.getContainer(containerFile, Container.Version.Ver1);

        // Create new PojoMessage with capsule v 1.0 and Save to DVK Client DB
        PojoMessage dvkMessage = UtilsService.prepareAndSaveDvkMessage_V_1(dvkDAO, containerFile);

        // Check message was inserted into DVK Client DB
        Assert.notNull(dvkDAO.getMessage(dvkMessage.getDhlMessageId()));

        // Call receiveDocumentsFromDVK for all incoming messages
        documentService.receiveDocumentsFromDVK(digiDocConfFilePath);

        List<Document> aditDocsWithDefaultDvkGuid = UtilsService.getDocumentsByDvkGuid(documentDAO, DEFAULT_GUID.toString());

        // Check there is only one document with DOCUMENT.DVK_GUID = DEFAULT_GUID in ADIT DB
        Assert.notNull(aditDocsWithDefaultDvkGuid);
        Assert.isTrue(aditDocsWithDefaultDvkGuid.size() == 1);

        Document aditDocument = aditDocsWithDefaultDvkGuid.get(0);
        aditDocument = UtilsService.getNonLazyInitializedDocument(documentDAO, aditDocument.getId());

        // Check document was inserted into ADIT DB
        Assert.notNull(aditDocument);

        //
        // Check mapping

        // Table DOCUMENT
        Assert.isTrue(dvkMessage.getTitle().equals(aditDocument.getTitle()));
        Assert.isTrue(container.getTransport().getSaatjad().get(0).getRegNr().equals(Util.removeCountryPrefix(aditDocument.getCreatorCode())));
        Assert.isTrue(DocumentService.DOCTYPE_LETTER.equals(aditDocument.getDocumentType()));
        Assert.notNull(aditDocument.getCreatorCode());

        // Table DOCUMENT_SHARING
        Assert.notNull(aditDocument.getDocumentSharings());

        // Table DOCUMENT_FILE
        Assert.notNull(aditDocument.getDocumentFiles());

        // Table SIGNATURE
        Assert.notNull(aditDocument.getSignatures());

        // Table DOCUMENT_HISTORY
        Assert.notNull(aditDocument.getDocumentHistories());

        // Table ADIT_USER


        dvkMessages.add(dvkMessage);
        aditDocuments.add(aditDocument);
    }

    @Ignore
    @Test
    public void receiveDocumentFromDVKClient_V2_Test() throws Exception {

        //Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
        String digiDocConfFile = AppSetupTest_Integration.class.getResource("/jdigidoc.cfg").getPath();

        // TODO: Path to the container v 2.1
        String containerFile = "";

        // Create new PojoMessage with capsule v 1.0
        PojoMessage newDvkMessage = UtilsService.prepareMessageBeforeInsert_V_2_1(dvkDAO, containerFile);
        // Save the message to DVK Client DB
        dvkDAO.updateDocument(newDvkMessage);
        // Check message was inserted into DVK Client DB
        Assert.notNull(dvkDAO.getMessage(newDvkMessage.getDhlMessageId()));

        // Call receiveDocumentsFromDVK for all incoming messages
        documentService.receiveDocumentsFromDVK(digiDocConfFile);

        Document aditDocument = documentDAO.getDocumentByDVKID(newDvkMessage.getDhlId());

        Assert.notNull(aditDocument);

        // ContainerVer1 container = ContainerVer1.parse(UtilsService.readSQLToString(containerFileV1));

        //Assert.notNull(container);

    }

    @Test
    public void sendDocumentToDVKClient() throws Exception {
        // TODO: 1) Prepare a message 2) Insert to ADIT DB 3) DocumentService.sendToDVK 4) Asserts
    }


    public void clearDvkDb(List<PojoMessage> dvkMsgs) {

        DetachedCriteria dcMessage = DetachedCriteria.forClass(PojoMessage.class, "dhlMessage");

        if (dvkMsgs == null || dvkMsgs.size() == 0) {
            dcMessage.add(Property.forName("dhlMessage.dhlId").eq(DEFAULT_DHL_ID));
            dvkMsgs = dvkDAO.getHibernateTemplate().findByCriteria(dcMessage);
        }

        for (PojoMessage msg : dvkMsgs) {
            dvkDAO.getHibernateTemplate().delete(msg);
        }
    }

    public void clearAditDb(List<Document> aditDocs) {

        DetachedCriteria dcDocument = DetachedCriteria.forClass(Document.class, "document");

        try {
            if (aditDocs == null || aditDocs.size() == 0) {
                dcDocument.add(Property.forName("document.dvkId").eq(DEFAULT_DHL_ID));
                aditDocs = documentDAO.getHibernateTemplate().findByCriteria(dcDocument);
            }

            for (Document doc : aditDocs) {
                doc = UtilsService.getNonLazyInitializedDocument(documentDAO, doc.getId());
                documentFileDAO.getHibernateTemplate().deleteAll(doc.getDocumentFiles());
                documentSharingDAO.getHibernateTemplate().deleteAll(doc.getDocumentSharings());
                documentDAO.getHibernateTemplate().delete(doc);
            }
        } catch (Exception e) {
            //TODO: Smth to do with exceptions
            //logger.error(e.getMessage());
        }


    }


}
