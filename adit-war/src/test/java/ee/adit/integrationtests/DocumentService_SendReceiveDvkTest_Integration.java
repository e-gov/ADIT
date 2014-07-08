package ee.adit.integrationtests;

import dvk.api.container.Container;
import dvk.api.container.v1.ContainerVer1;
import dvk.api.container.v1.Saaja;
import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.DecRecipient;
import dvk.api.container.v2_1.Recipient;
import dvk.api.container.v2_1.Transport;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dao.pojo.Signature;
import ee.adit.service.DocumentService;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Hendrik Pärna
 * @since 15.05.14
 */
@RunWith(JUnitParamsRunner.class)
@ContextConfiguration(locations = {"classpath:integration-tests.xml"})
public class DocumentService_SendReceiveDvkTest_Integration {
    final static String CONTAINERS_PATH = "/containers/";
    final static String TO_ADIT = "to_ADIT/";
    final static String TO_DVK = "to_DVK/";
    final static String DIGIDOC_CONF_FILE_NAME = "jdigidoc.cfg";
    final static long DEFAULT_DHL_ID = 1;
    final static String DEFAULT_DOCUMENT_TITLE = "Integration Tests TestDocument";
    final static UUID DEFAULT_GUID = UUID.randomUUID();
    final static long DVK_STATUS_SENT = 3;
    final static String RIA_ADIT_USER_CODE = "";
    final static String RIA_ADIT_PERSON_USER_CODE = "";
    final static String DOCUMENT_SHARING_TYPE_SEND_TO_ADIT = "send_adit";
    final static String DOCUMENT_SHARING_TYPE_SEND_TO_DVK = "send_dvk";
    final static int DOCUMENT_FILE_TYPE_ID = 1;
    final static String ACCESS_CONDITIONS_CODE = "AK";
    final static String DOCUMENT_SHARING_COMMENT = "Comment_example";
    final static String SIGNED_COUNTRY = "Estonia";
    final static String SIGNED_CITY = "Tallinn";
    final static String SIGNATURE_TYPE_COMPANY = "Digitempel";
    final static String CONTAINER_TYPE_NOT_DDOC = "Not_DDOC_Container";
    final static String CONTAINER_TYPE_DDOC = "DDOC_Container";

    private static Logger logger = Logger.getLogger(DocumentService_SendReceiveDvkTest_Integration.class);

    @Autowired
    private Utils utils;

    private List<PojoMessage> dvkMessages = new ArrayList<PojoMessage>();
    private List<Document> aditDocuments = new ArrayList<Document>();
    private TestContextManager testContextManager;

    @Before
    public void beforeTest() throws Exception{
        // Setup context
        this.testContextManager = new TestContextManager(getClass());
        this.testContextManager.prepareTestInstance(this);

        try {
            clearDvkDb(dvkMessages);
            clearAditDb(aditDocuments);
        } catch (Exception e) {
            logger.error("beforeTest(). " + e.getMessage());
            //throw new RuntimeException(e);
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
            logger.error("afterTest(). " + e.getMessage());
            //throw new RuntimeException(e);
        }
    }

    @Test
    public void justTestAppContextSetup() throws Exception {
        DocumentService documentService = utils.getDocumentService();
        Assert.notNull(documentService.getDvkDAO());
        Assert.notNull(documentService.getDvkDAO().getHibernateTemplate());
        Assert.notNull(documentService.getDvkDAO().getSessionFactory());

        Assert.notNull(documentService.getDocumentSharingDAO());

        Document document = documentService.getDocumentDAO().getDocument(10780);
        PojoMessage message = documentService.getDvkDAO().getMessage(412);

        Assert.notNull(document);
        Assert.notNull(message);
    }

    //
    // Test sendDocumentToDVKClient with container v 2.1
    //
    @Test
    public void sendDocumentToDVKClient_V2_Test() throws Exception {
        final String CONTAINER_V_2_1 = "containerVer2_1.xml";
        final String CONTAINER_V_2_1_DDOC = "containerVer2_1-ddoc.xml";
        final String DIGIDOC_CONF_FILE = "/jdigidoc.cfg";

        String[] containersFileNames = {CONTAINER_V_2_1, CONTAINER_V_2_1_DDOC};

        // TODO: think about this comment
        // Iterate through all containers files
        for (String containerFileName : containersFileNames) {
            String containerFilePath;
            String digiDocConfFilePath;
            File containerFile;

            // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
            // TODO: take a digidoc file from Configuration
            digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource(DIGIDOC_CONF_FILE).getPath();
            // Path to the container ver 2.1. Get the container
            containerFilePath = Utils.getContainerPath(containerFileName, TO_DVK);
            containerFile = new File(containerFilePath);

            String containerType = CONTAINER_TYPE_NOT_DDOC;
            if (containerFilePath.contains("ddoc")) {
                containerType = CONTAINER_TYPE_DDOC;
            }

            DocumentService documentService = utils.getDocumentService();

            // Create a container ver 2.1, based on XML file
            ContainerVer2_1 containerInput = (ContainerVer2_1) Utils.getContainer(containerFile, Container.Version.Ver2_1);
            Assert.notNull(containerInput);

            // Create a document, based on the container and insert to ADIT DB
            Document document;
            logger.debug("DEFAULT_GUID " + DEFAULT_GUID);

            AditUser recipent = documentService.getAditUserDAO().getUserByID(containerInput.getTransport().
                    getDecRecipient().get(0).getOrganisationCode());
            document = utils.prepareAndSaveAditDocument(containerInput, recipent, digiDocConfFilePath, containerType);

            // Send the document from ADIT to DVK UK
            documentService.sendDocumentsToDVK();

            // Get a sent document from ADIT DB, and get a received message from DVK UK DB
            Document sentAditDocument = utils.getNonLazyInitializedDocument(document.getId());
            PojoMessage receivedDVKMessage = documentService.getDvkDAO().getMessage(sentAditDocument.getDvkId());

            Assert.notNull(sentAditDocument);
            Assert.notNull(receivedDVKMessage);

            Assert.notNull(sentAditDocument.getDocumentSharings());
            Assert.isTrue(sentAditDocument.getDocumentSharings().size() > 0);
            Assert.notNull(sentAditDocument.getDocumentFiles());
            Assert.isTrue(sentAditDocument.getDocumentFiles().size() > 0);
            Assert.notNull(receivedDVKMessage.getData());
            Assert.isTrue(Utils.compareStringsIgnoreCase(sentAditDocument.getGuid(), receivedDVKMessage.getDhlGuid()));
            Set<DocumentSharing> documentSharings = sentAditDocument.getDocumentSharings();
            Assert.notNull(documentSharings);

            // Get a container ver 2.1 from the received DVK document
            ContainerVer2_1 containerOutput = ContainerVer2_1.parse(documentService.readFromClob(receivedDVKMessage.getData()));

            // Do asserts with an input container and an output container
            Assert.notNull(containerInput);
            Assert.notNull(containerOutput);
            Transport transportInput = containerInput.getTransport();
            Transport transportOutput = containerOutput.getTransport();

            Assert.notNull(transportOutput);
            Assert.notNull(transportInput.getDecSender());
            Assert.notNull(transportOutput.getDecSender());
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentService.getConfiguration().getDvkOrgCode(),
                    transportOutput.getDecSender().getOrganisationCode()));
            Assert.isNull(transportOutput.getDecSender().getStructuralUnit());
            Assert.isTrue(Utils.compareStringsIgnoreCase(Utils.addPrefixIfNecessary(transportOutput.
                    getDecSender().getPersonalIdCode()),
                    sentAditDocument.getCreatorCode()));
            Assert.notNull(transportOutput.getDecRecipient());
            Assert.notNull(transportInput.getDecRecipient());
            Assert.isTrue(Utils.compareStringsIgnoreCase(transportOutput.getDecRecipient().get(0).getOrganisationCode(),
                    documentService.getAditUserDAO().getUserByID(containerInput.getTransport().getDecRecipient().get(0).
                            getOrganisationCode()).getDvkOrgCode()));
            Assert.isNull(transportOutput.getDecRecipient().get(0).getStructuralUnit());

            // Do asserts with DecMetaData
            Assert.isNull(containerOutput.getDecMetadata());

            // Do asserts with RecordCreator
            AditUser sender = documentService.getAditUserDAO().getUserByID(containerInput.getTransport().getDecSender().getOrganisationCode());
            Assert.notNull(sender);
            Assert.notNull(containerOutput.getRecordCreator());
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation().getName(),
                    sender.getFullName()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation()
                    .getOrganisationCode(),
                    sender.getDvkOrgCode()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation().getResidency(),
                    sentAditDocument.getCreatorCode().substring(0, Math.min(sentAditDocument.getCreatorCode().length(), 2))));
            Assert.isNull(containerOutput.getRecordCreator().getOrganisation().getStructuralUnit());
            Assert.isNull(containerOutput.getRecordCreator().getOrganisation().getPositionTitle());

            // Do asserts with RecordSenderToDec
            Assert.notNull(containerOutput.getRecordSenderToDec());
            Assert.notNull(containerOutput.getRecordSenderToDec().getOrganisation());
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getName(),
                    sender.getFullName()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getOrganisationCode(),
                    sender.getDvkOrgCode()));
            Assert.isNull(containerOutput.getRecordSenderToDec().getOrganisation().getStructuralUnit());
            Assert.isNull(containerOutput.getRecordSenderToDec().getOrganisation().getPositionTitle());
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getResidency(),
                    sentAditDocument.getCreatorCode().substring(0, Math.min(sentAditDocument.getCreatorCode().length(), 2))));

            // Do asserts with Recipient
            Assert.notNull(containerOutput.getRecipient());
            Assert.isTrue(containerOutput.getRecipient().size() > 0);
            DocumentSharing documentSharing = documentSharings.iterator().next();
            List<Recipient> recipientsFromContainer = containerOutput.getRecipient();

            AditUser recipient = documentService.getAditUserDAO().getUserByID(containerInput.getTransport().getDecRecipient().get(0).getOrganisationCode());
            Assert.notNull(recipient);
            Assert.notNull(Utils.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getName(),
                    recipient.getFullName()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getMessageForRecipient(),
                    documentSharing.getComment()));
            Assert.notNull(Utils.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getOrganisationCode(),
                    recipient.getDvkOrgCode()));
            String documentSharingUserCode = documentSharing.getUserCode();
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getResidency(),
                    documentSharingUserCode.substring(0, Math.min(documentSharingUserCode.length(), 2))));

            // Do asserts with RecordMetaData
            Assert.notNull(containerOutput.getRecordMetadata());
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecordMetadata().getRecordGuid(),
                    sentAditDocument.getGuid()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerInput.getRecordMetadata().getRecordTitle(),
                    containerOutput.getRecordMetadata().getRecordTitle()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerInput.getRecordMetadata().getRecordType(),
                    containerOutput.getRecordMetadata().getRecordType()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getRecordMetadata().getRecordOriginalIdentifier(),
                    Long.toString(sentAditDocument.getId())));

            if (Utils.compareStringsIgnoreCase(containerType, CONTAINER_TYPE_NOT_DDOC)) {
                Assert.isTrue(Utils.isToday(containerOutput.getRecordMetadata().getRecordDateRegistered()));
            } else if (Utils.compareStringsIgnoreCase(containerType, CONTAINER_TYPE_DDOC)) {
                int expectedResultOfCompare = 0;
                Assert.isTrue(expectedResultOfCompare == (containerOutput.getRecordMetadata().getRecordDateRegistered().compareTo
                        (containerInput.getSignatureMetadata().get(0).getSignatureVerificationDate())));
            }

            // Do asserts with Access
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getAccess().getAccessConditionsCode(),
                    ACCESS_CONDITIONS_CODE));

            // Do asserts with SignatureMetaData (if it's DDOC Container)
            if (Utils.compareStringsIgnoreCase(containerType, CONTAINER_TYPE_DDOC)) {
                Assert.notNull(containerOutput.getSignatureMetadata());
                Assert.isTrue(containerOutput.getSignatureMetadata().size() > 0);
                Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getSignatureMetadata().get(0).getSignatureType(),
                        SIGNATURE_TYPE_COMPANY));
                Assert.isTrue(containerInput.getSignatureMetadata().get(0).getSignatureVerificationDate().equals
                        (containerOutput.getSignatureMetadata().get(0).getSignatureVerificationDate()));
                Assert.isTrue(Utils.compareStringsIgnoreCase(containerInput.getSignatureMetadata().get(0).getSigner(),
                        containerOutput.getSignatureMetadata().get(0).getSigner()));
            }

            // Do asserts with an input file and output file
            Assert.notNull(containerInput.getFile().size() > 0);
            Assert.notNull(containerOutput.getFile().size() > 0);
            dvk.api.container.v2_1.File fileInput = containerInput.getFile().get(0);
            dvk.api.container.v2_1.File fileOutput = containerOutput.getFile().get(0);
            Assert.notNull(fileInput);
            Assert.notNull(fileOutput);
            Assert.isTrue(Utils.compareStringsIgnoreCase(fileInput.getFileName(), fileOutput.getFileName()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(fileInput.getFileGuid(), fileOutput.getFileGuid()));
            Assert.isTrue(fileInput.getFileSize().equals(fileOutput.getFileSize()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(fileInput.getMimeType(), fileOutput.getMimeType()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(fileInput.getZipBase64Content(),
                    fileOutput.getZipBase64Content()));

            // Do container version assert
            Assert.notNull(containerOutput.getAccess());
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerInput.getInternalVersion().toString(),
                    containerOutput.getInternalVersion().toString()));

            // Finally, clean the messages
            dvkMessages.add(receivedDVKMessage);
            aditDocuments.add(document);
        }
    }

    /**
     * Test receiveDocumentFromDVKClient with container v 1.0
     *
     * @param containerFileName
     * @throws Exception
     */
    @Test
    @Parameters({
            "containerVer1_0_ddoc.xml"})

    public void receiveDocumentFromDVKClient_Container_1_0_Test(String containerFileName) throws Exception {

        // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
        String digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource("/" + DIGIDOC_CONF_FILE_NAME).getPath();

        // Container 1.0
        String containerFilePath = Utils.getContainerPath(containerFileName, TO_ADIT);
        java.io.File containerFile = new java.io.File(containerFilePath);
        ContainerVer1 container = (ContainerVer1) Utils.getContainer(containerFile, Container.Version.Ver1);

        DocumentService documentService = utils.getDocumentService();

        // Get sender used space for further control
        AditUser messageSender = documentService.getAditUserDAO().getUserByID(Utils.addPrefixIfNecessary(container.getTransport().getSaatjad().get(0).getRegNr()));
        Long senderUsedSpace = documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode());

        // Insert message into DVK_UK DB
        PojoMessage dvkMessage = utils.prepareAndSaveDvkMessage_Container_1_0(containerFile);
        Assert.notNull(documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId()), "Message wasn't inserted into DVK_UK.DHL_MESSAGE table");
        logger.debug("DhlGuid for inserted message  - " + dvkMessage.getDhlGuid());

        documentService.receiveDocumentsFromDVK(digiDocConfFilePath);

        List<Document> aditDocsWithTestDvkGuid = utils.getDocumentsByDvkGuid(dvkMessage.getDhlGuid());
        Assert.notNull(aditDocsWithTestDvkGuid, "There is no documents in ADIT DB with test dvkGUID: " + dvkMessage.getDhlGuid());
        Assert.isTrue(aditDocsWithTestDvkGuid.size() == 1, "There are more than one document in ADIT DB with test dvkGUID: " + dvkMessage.getDhlGuid());

        Document aditDocument = utils.getNonLazyInitializedDocument(aditDocsWithTestDvkGuid.get(0).getId());
        Assert.notNull(aditDocument, "Document wasn't received from DVK_UK DB");

        // Assert mapping
        // Table DOCUMENT
        Assert.isTrue(Utils.compareStringsIgnoreCase(dvkMessage.getTitle(), aditDocument.getTitle()));
        Assert.isTrue(Utils.compareStringsIgnoreCase(DocumentService.DOCTYPE_LETTER, aditDocument.getDocumentType()));
        Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocument.getCreatorCode(), "EE" + container.getTransport().getSaatjad().get(0).getRegNr()));
        // TODO: assert for CREATOR_NAME - understand how it should work
        // Assert.isTrue(Utils.compareStringsIgnoreCase(
        //        aditUserDAO.getUserByID("EE"+container.getTransport().getSaatjad().get(0).getIsikukood()).getFullName(),
        //        aditDocument.getCreatorName()));
        Assert.isTrue(Utils.isToday(aditDocument.getCreationDate()));
        Assert.isTrue(Utils.isToday(aditDocument.getLastModifiedDate()));
        Assert.isTrue(aditDocument.getDocumentDvkStatusId() == DVK_STATUS_SENT);
        Assert.isTrue(aditDocument.getDvkId() == DEFAULT_DHL_ID);
        // TODO: assert for PARENT_ID (Igor). Can't find PARENT_ID in aditDocument
        // Assert.isTrue();
        Assert.isTrue(aditDocument.getLocked());
        Assert.isTrue(Utils.isToday(aditDocument.getLockingDate()));
        // If first added file happens to be a DigiDoc container then
        // extract files and signatures from container. Otherwise add
        // container as a regular file.
        Assert.isTrue(aditDocument.getSignable());
        if (container.getSignedDoc().getDataFiles().get(0).getFileName().contains("ddoc")) {
            Assert.isTrue(aditDocument.getSigned());
        }

        // Table DOCUMENT_SHARING
        Assert.notNull(aditDocument.getDocumentSharings());
        ArrayList<Saaja> aditRecipients = new ArrayList<Saaja>();
        for (Saaja saaja : container.getTransport().getSaajad()) {
            if (Utils.compareStringsIgnoreCase(saaja.getRegNr().toString(), "adit")) {
                aditRecipients.add(saaja);
            }
        }

        Assert.isTrue(aditDocument.getDocumentSharings().size() == aditRecipients.size());
        // TODO: finish this mapping (stack - ???)
        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            int count = 0;
            for (Saaja recipient : container.getTransport().getSaajad()) {
                if (recipient.getIsikukood().equalsIgnoreCase(documentSharing.getUserCode())) {
                    count++;
                    if (count > 1) {
                        throw new Exception();
                    }
                    AditUser aditUser = documentService.getAditUserDAO().getUserByID(recipient.getIsikukood());
                    Assert.isTrue(Utils.compareStringsIgnoreCase(aditUser.getUserCode(),
                            documentSharing.getUserCode()));
                    Assert.isTrue(Utils.compareStringsIgnoreCase(aditUser.getFullName(), documentSharing.getUserName()));
                    Assert.isTrue(Utils.compareStringsIgnoreCase(documentSharing.getDocumentSharingType(), DOCUMENT_SHARING_TYPE_SEND_TO_ADIT));
                    Assert.isTrue(Utils.isToday(documentSharing.getCreationDate()));
                    Assert.isNull(documentSharing.getDvkFolder());
                    // TODO assert for DVK_ID, ID, DOCUMENT_ID - understand ho it should work
                }
            }
        }

        // Table DOCUMENT_FILE
        // TODO: finish this mapping (stack - ???)
        Assert.notNull(aditDocument.getDocumentFiles());
/*        for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
            for (DataFile dataFile : container.getSignedDoc().getDataFiles()) {
                if (dataFile.getFileId().equals(documentFile.getDdocDataFileId())) {
                    // TODO: asserts for documentFile.getData (Blob) and String from dataFile - Do we need to do it?
                    // TODO: asserts for DOCUMENT_FILE_TYPE_ID  (if ddoc)
                    if (dataFile.getFileName().contains("ddoc")) {
                        // TODO: is it correct?
                        Assert.isTrue(documentFile.getDocumentFileTypeId() == DOCUMENT_FILE_TYPE_ID);
                    }
                    Assert.isTrue(!documentFile.getDeleted());
                    Assert.isTrue(Utils.compareStringsIgnoreCase(documentFile.getContentType(), dataFile.getFileMimeType()));
                    Assert.isTrue(Utils.compareStringsIgnoreCase(documentFile.getFileName(), dataFile.getFileName()));
                    Assert.isTrue(Utils.compareStringsIgnoreCase(documentFile.getFileSizeBytes().toString(), dataFile.getFileSize()));
                    Assert.notNull(Utils.isToday(documentFile.getLastModifiedDate()));
                    if (dataFile.getFileName().contains("ddoc")) {
                        // TODO: is it correct that just not null (low priority)
                        Assert.notNull(documentFile.getFileDataInDdoc());
                    }
                }
            }
        }*/

        // Table SIGNATURE
        // TODO: Do it later
//        Assert.notNull(aditDocument.getSignatures());
//        for (Signature signature : aditDocument.getSignatures()){
//
//        }

        // Table DOCUMENT_HISTORY
        aditDocument = documentService.getDocumentDAO().getDocument(aditDocument.getId());
        Set<DocumentHistory> documentHistories = aditDocument.getDocumentHistories();
        Assert.notNull(documentHistories);


        // TODO: finish it later
//        for (DocumentHistory documentHistory : aditDocument.getDocumentHistories()) {
//            // TODO: DOCUMENT_HISTORY_TYPE, DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE (Liza TODO)
//            Assert.notNull(documentHistory.getEventDate());
//        }

        /*for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            AditUser recipientUser = documentService.getAditUserDAO().getUserByID(documentSharing.getUserCode());
            logger.debug("Recipient " + documentSharing.getUserCode() + " recipientUserQuota " + usersFromContainer.get(recipientUser.getUserCode()));
            logger.debug("Recipient " + documentSharing.getUserCode() + " recipientUserQuota after receive from DVK " + recipientUser.getDiskQuotaUsed());

            Assert.isTrue(recipientUser.getDiskQuotaUsed() >= usersFromContainer.get(recipientUser.getUserCode()));
        }*/

/*        logger.debug("senderUserOrganisationQuota " + senderUserOrganisationQuota);
        logger.debug("senderOrganisationQuota after receive from DVK " + senderUserOrganisationQuota);
        logger.debug("senderUserPersonQuota " + senderUserPersonQuota);
        logger.debug("senderUserPersonQuota  after receive from DVK " + senderUserPersonQuota);
        // Assert.isTrue(usersFromContainer1.get(0) < aditUserDAO.getUserByID(aditDocument.getCreatorCode()).getDiskQuotaUsed());*/

        //Table DHL_MESSAGES
        Assert.notNull(documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId()).getLocalItemId());

        dvkMessages.add(dvkMessage);
        aditDocuments.add(aditDocument);
    }

    /**
     * Test receiveDocumentFromDVKClient with container v 2.1
     *
     * @param containerFileName
     * @throws Exception
     */
    @Test
    @Parameters({
            "containerVer2_1_ddoc.xml"})
    public void receiveDocumentFromDVKClient_Container_2_1_Test(String containerFileName) throws Exception {

        // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
        String digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource("/" + DIGIDOC_CONF_FILE_NAME).getPath();

        // Container 2.1
        String containerFilePath = Utils.getContainerPath(containerFileName, TO_ADIT);
        java.io.File containerFile = new java.io.File(containerFilePath);
        ContainerVer2_1 container = (ContainerVer2_1) Utils.getContainer(containerFile, Container.Version.Ver2_1);

        DocumentService documentService = utils.getDocumentService();

        // Get sender used space for further control
        AditUser messageSender = documentService.getAditUserDAO().getUserByID(Utils.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode()));
        Long senderUsedSpace = documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode());

        // Insert message into DVK_UK DB
        PojoMessage dvkMessage = utils.prepareAndSaveDvkMessage_Container_2_1(containerFile);
        Assert.notNull(documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId()), "Message wasn't inserted into DVK_UK.DHL_MESSAGE table");
        logger.debug("DhlGuid for inserted message  - " + dvkMessage.getDhlGuid());

        documentService.receiveDocumentsFromDVK(digiDocConfFilePath);

        List<Document> aditDocsWithTestDvkGuid = utils.getDocumentsByDvkGuid(dvkMessage.getDhlGuid());
        Assert.notNull(aditDocsWithTestDvkGuid, "There is no documents in ADIT DB with test dvkGUID: " + dvkMessage.getDhlGuid());
        Assert.isTrue(aditDocsWithTestDvkGuid.size() == 1, "There are " + aditDocsWithTestDvkGuid.size() + " document in ADIT DB with test dvkGUID: " + dvkMessage.getDhlGuid());

        Document aditDocument = utils.getNonLazyInitializedDocument(aditDocsWithTestDvkGuid.get(0).getId());
        Assert.notNull(aditDocument, "Document wasn't received from DVK_UK DB");

        // Assert mapping
        // Table DOCUMENT
        Assert.isTrue(Utils.compareStringsIgnoreCase(dvkMessage.getTitle(), aditDocument.getTitle()), 
                "expected:" + dvkMessage.getTitle() + ", actual:" + aditDocument.getTitle());
        Assert.isTrue(Utils.compareStringsIgnoreCase(DocumentService.DOCTYPE_LETTER, aditDocument.getDocumentType()), 
                "expected:" + DocumentService.DOCTYPE_LETTER + ", actual:" + aditDocument.getDocumentType());
        Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocument.getCreatorCode(), Utils.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode())), 
                "expected:" + Utils.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode()) + ", actual:" + aditDocument.getCreatorCode());
        if (dvkMessage.getSenderOrgName() != null && !dvkMessage.getSenderOrgName().isEmpty()) {
        logger.error("expected:" + dvkMessage.getSenderOrgName() + ", actual:" + aditDocument.getCreatorName());
            // todo : problem with utf-8
            //Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocument.getCreatorName(), dvkMessage.getSenderOrgName()),
            //        "expected:" + dvkMessage.getSenderOrgName() + ", actual:" + aditDocument.getCreatorName());
        }
        Assert.isTrue(Utils.isToday(aditDocument.getCreationDate()), 
                "expected: current day, actual:" + aditDocument.getCreationDate());
        Assert.isTrue(Utils.isToday(aditDocument.getLastModifiedDate()),
                "expected: current day, actual:" + aditDocument.getLastModifiedDate());
        Assert.isTrue(aditDocument.getDocumentDvkStatusId() == DVK_STATUS_SENT,
                "expected:" + DVK_STATUS_SENT + ", actual:" + aditDocument.getDocumentDvkStatusId());
        Assert.isTrue(aditDocument.getDvkId() == DEFAULT_DHL_ID,
                "expected:" + DEFAULT_DHL_ID + ", actual:" + aditDocument.getDvkId());
        // PARENT_ID will be not yet used in ADIT - JIRA ADIT-9
        Assert.isTrue(aditDocument.getLocked(),
                "expected: true, actual:" + aditDocument.getLocked());
        Assert.isTrue(Utils.isToday(aditDocument.getLockingDate()),
                "expected: current day, actual:" + aditDocument.getLockingDate());
        Assert.isTrue(aditDocument.getSignable(),
                "expected: true, actual:" + aditDocument.getSignable());
        if (container.getSignatureMetadata() != null) Assert.isTrue(aditDocument.getSigned(),
                "expected: true, actual:" + aditDocument.getSigned());

        // Table DOCUMENT_SHARING
        Assert.notNull(aditDocument.getDocumentSharings(), "Received ADIT document doesn't have related DOCUMENT_SHARING records");
        Assert.isTrue(aditDocument.getDocumentSharings().size() == container.getTransport().getDecRecipient().size(),
                "expected:" + container.getTransport().getDecRecipient().size() + ", actual:" + aditDocument.getDocumentSharings().size());
        Map<String, DocumentSharing> receivedAditDocumentSharings = new HashMap<String, DocumentSharing>();
        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            receivedAditDocumentSharings.put(documentSharing.getUserCode(), documentSharing);
        }
        for (DecRecipient recipient : container.getTransport().getDecRecipient()) {
            String recipientAditUserCode = Utils.addPrefixIfNecessary(recipient.getPersonalIdCode());
            Assert.isTrue(receivedAditDocumentSharings.containsKey(recipientAditUserCode),
                    "Received ADIT document doesn't have related DOCUMENT_SHARING for recipient" + recipientAditUserCode);
            DocumentSharing documentSharing = receivedAditDocumentSharings.get(recipientAditUserCode);
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentSharing.getDocumentSharingType(), DOCUMENT_SHARING_TYPE_SEND_TO_ADIT),
                    "expected:" + DOCUMENT_SHARING_TYPE_SEND_TO_ADIT + ", actual:" + documentSharing.getDocumentSharingType());
            Assert.isTrue(Utils.isToday(documentSharing.getCreationDate()),
                    "expected: current day, actual:" + aditDocument.getCreationDate());
            Assert.isNull(documentSharing.getDvkFolder());//todo
            Assert.isTrue(documentSharing.getDvkId() == DEFAULT_DHL_ID,
                    "expected:" + DEFAULT_DHL_ID + ", actual:" + documentSharing.getDvkId());
            // todo match decrecipient ro appropriate recipient
            if (container.getRecipient().get(0).getMessageForRecipient() != null) {
                Assert.isTrue(Utils.compareStringsIgnoreCase(documentSharing.getComment(), container.getRecipient().get(0).getMessageForRecipient()),
                        "expected:" + container.getRecipient().get(0).getMessageForRecipient() + ", actual:" + documentSharing.getComment());
            }
        }

        // Table DOCUMENT_FILE
        Assert.notNull(aditDocument.getDocumentFiles(), "Received ADIT document doesn't have related DOCUMENT_FILE records");
        HashMap<String, DocumentFile> getDocumentFiles = new HashMap<String, DocumentFile>();
        for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
            getDocumentFiles.put(documentFile.getGuid(), documentFile);
        }
        for (dvk.api.container.v2_1.File dataFile : container.getFile()) {
            String fileGuid = dataFile.getFileGuid();
            Assert.isTrue(getDocumentFiles.containsKey(fileGuid),
                    "Received ADIT document doesn't have related DOCUMENT_FILE for file" + fileGuid);
            DocumentFile documentFile = getDocumentFiles.get(fileGuid);

            //TODO: check how getDocumentFileTypeId done for capsule 1.0
            if (dataFile.getFileName().contains("ddoc")) {
                Assert.isTrue(documentFile.getDocumentFileTypeId() == DocumentService.FILETYPE_SIGNATURE_CONTAINER);
            }
            Assert.isTrue(!documentFile.getDeleted(),  "expected: received ADIT document not deleted, actual: document deleted = " + documentFile.getDeleted());
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentFile.getContentType(), dataFile.getMimeType()),
                    "expected:" + dataFile.getMimeType() + ", actual:" + documentFile.getContentType());
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentFile.getFileName(), dataFile.getFileName()),
                    "expected:" + dataFile.getFileName() + ", actual:" + documentFile.getFileName());
//            Assert.isTrue(documentFile.getFileSizeBytes() == dataFile.getFileSize().intValue());
            Assert.isTrue(Utils.isToday(documentFile.getLastModifiedDate()),
                    "expected: current day, actual:" + aditDocument.getLastModifiedDate());
            if (dataFile.getFileName().contains("ddoc")) {
                // TODO: check how getDocumentFileTypeId done for capsule 1.0
                //              Assert.notNull(documentFile.getFileDataInDdoc());
            }
            // TODO: asserts for documentFile.getData (Blob) and String from dataFile
       /*     Assert.isTrue(Utils.compareStringsIgnoreCase(dataFile.getZipBase64Content(),
                    documentFile.getFileData().toString().getZipBase64Content()));*/
/*             InputStream inputStream = documentFile.getFileData().getBinaryStream();
            String binaryContentsFile = Util.createTemporaryFile(inputStream, configuration.getTempDir());
           File file1 = new File();

            Util.base64DecodeAndUnzip(dataFile.getZipBase64Content());
                 Assert.isTrue(UtilsService.compareStringsIgnoreCase(dataFile.getZipBase64Content(),
                         Util.getFileContents(new java.io.File(binaryContentsFile))));*/

        }

        // Table SIGNATURE
        if (container.getSignatureMetadata() != null) {
            Assert.notNull(aditDocument.getSignatures(), "Received ADIT document doesn't have related SIGNATURE records");
            Assert.isTrue(container.getSignatureMetadata().size() == aditDocument.getSignatures().size(),
                    "expected:" + container.getSignatureMetadata().size() + ", actual:" + aditDocument.getSignatures().size());
            for (Signature signature : aditDocument.getSignatures()) {
// TODO: Do it later

            }

        }

        // Table DOCUMENT_HISTORY
        Assert.notNull(aditDocument.getDocumentHistories(), "Received ADIT document doesn't have related DOCUMENT_HISTORY records");
        DocumentHistory aditDocumentHistory = aditDocument.getDocumentHistories().toArray(new DocumentHistory[]{})[0];
        Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocumentHistory.getDocumentHistoryType(), DocumentService.HISTORY_TYPE_EXTRACT_FILE),
                "expected:" + DocumentService.HISTORY_TYPE_EXTRACT_FILE + ", actual:" + aditDocumentHistory.getDocumentHistoryType());
        Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocumentHistory.getDescription(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE),
                "expected:" + DocumentService.DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE + ", actual:" + aditDocumentHistory.getDescription());
        Assert.isTrue(Utils.isToday(aditDocumentHistory.getEventDate()),
                "expected: current day, actual:" + aditDocumentHistory.getEventDate());
        Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocumentHistory.getUserCode(), aditDocument.getCreatorCode()),
                "expected:" + aditDocument.getCreatorCode() + ", actual:" + aditDocumentHistory.getUserCode());
        Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocumentHistory.getXteeUserCode(), aditDocument.getCreatorCode()),
                "expected:" + aditDocument.getCreatorCode() + ", actual:" + aditDocumentHistory.getXteeUserCode());

        // Table ADIT_USER
        // TODO: update < with file size summa
        Assert.isTrue(senderUsedSpace < documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()),
                "expected:" + senderUsedSpace + ", actual:" + documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()));

        //Table DHL_MESSAGES
        PojoMessage messageUpdated = documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId());
        Assert.isTrue(messageUpdated.getLocalItemId().equals(aditDocument.getId()),
                "expected:" + messageUpdated.getLocalItemId() + ", actual:" + aditDocument.getId());

        dvkMessages.add(dvkMessage);
        aditDocuments.add(aditDocument);
    }

    @Ignore
    @Test
    //ADIT-7
    public void testUpdateDocumentsFromDVK() throws Exception {
        // Update document statuses from DVK
        int updatedDocumentsCount = utils.getDocumentService().updateDocumentsFromDVK();
    }

    public void clearDvkDb(List<PojoMessage> dvkMsgs) {
        DetachedCriteria dcMessage = DetachedCriteria.forClass(PojoMessage.class, "dhlMessage");
        DvkDAO dvkDAO = utils.getDocumentService().getDvkDAO();

        if (dvkMsgs == null || dvkMsgs.size() == 0) {
            dcMessage.add(Property.forName("dhlMessage.dhlId").eq(DEFAULT_DHL_ID));
            dvkMsgs = dvkDAO.getHibernateTemplate().findByCriteria(dcMessage);
        }

        for (PojoMessage msg : dvkMsgs) {
            dvkDAO.getHibernateTemplate().delete(msg);
        }
    }

    public void clearAditDb(List<Document> aditDocs) throws Exception {
        DetachedCriteria dcDocument = DetachedCriteria.forClass(Document.class, "document");
        DocumentService documentService = utils.getDocumentService();

        if (aditDocs == null || aditDocs.size() == 0) {
            dcDocument.add(Property.forName("document.dvkId").eq(DEFAULT_DHL_ID));
            aditDocs = documentService.getDocumentDAO().getHibernateTemplate().findByCriteria(dcDocument);
        }

        for (Document doc : aditDocs) {
            doc = utils.getNonLazyInitializedDocument(doc.getId());
            documentService.getDocumentHistoryDAO().getHibernateTemplate().deleteAll(doc.getDocumentHistories());
            documentService.getDocumentFileDAO().getHibernateTemplate().deleteAll(doc.getDocumentFiles());
            documentService.getDocumentSharingDAO().getHibernateTemplate().deleteAll(doc.getDocumentSharings());
            documentService.getDocumentDAO().getHibernateTemplate().deleteAll(doc.getSignatures());
            documentService.getDocumentDAO().getHibernateTemplate().delete(doc);
        }
    }
}
