package ee.adit.integrationtests;

import dvk.api.container.Container;
import dvk.api.container.v1.ContainerVer1;
import dvk.api.container.v1.Saaja;
import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.DecRecipient;
import dvk.api.container.v2_1.Recipient;
import dvk.api.container.v2_1.Transport;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentFileDAO;
import ee.adit.dao.DocumentHistoryDAO;
import ee.adit.dao.DocumentSharingDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dao.pojo.Signature;
import ee.adit.service.DocumentService;
import ee.adit.test.util.DAOCollections;
import org.apache.log4j.Logger;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.fail;

/**
 * @author Hendrik Pärna
 * @since 15.05.14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:integration-tests.xml"})
public class DocumentService_SendReceiveDvkTest_Integration {
    final static String CONTAINERS_PATH = "/containers/";
    final static String TO_ADIT = "to_ADIT/";
    final static String TO_DVK = "to_DVK/";
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
    private TestUtils testUtils;

    private List<PojoMessage> dvkMessages = new ArrayList<PojoMessage>();
    private List<Document> aditDocuments = new ArrayList<Document>();

    @Before
    public void beforeTest() {
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
        DocumentService documentService = testUtils.getDocumentService();
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
        final String CONTAINER_V_2_1_DDOG = "containerVer2_1-ddoc.xml";
        final String DIGIDOC_CONF_FILE = "/jdigidoc.cfg";

        String[] containersFileNames = {CONTAINER_V_2_1, CONTAINER_V_2_1_DDOG};

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
            containerFilePath = TestUtils.getContainerPath(containerFileName, TO_DVK);
            containerFile = new File(containerFilePath);

            String containerType = CONTAINER_TYPE_NOT_DDOC;
            if (containerFilePath.contains("ddoc")) {
                containerType = CONTAINER_TYPE_DDOC;
            }

            DocumentService documentService = testUtils.getDocumentService();

            // Create a container ver 2.1, based on XML file
            ContainerVer2_1 containerInput = (ContainerVer2_1) TestUtils.getContainer(containerFile, Container.Version.Ver2_1);
            Assert.notNull(containerInput);

            // Create a document, based on the container and insert to ADIT DB
            Document document;

            AditUser recipent = documentService.getAditUserDAO().getUserByID(containerInput.getTransport().
                    getDecRecipient().get(0).getOrganisationCode());
            document = testUtils.prepareAndSaveAditDocument(containerInput, recipent, digiDocConfFilePath, containerType);

            // Send the document from ADIT to DVK UK
            documentService.sendDocumentsToDVK();

            // Get a sent document from ADIT DB, and get a received message from DVK UK DB
            Document sentAditDocument = testUtils.getNonLazyInitializedDocument(document.getId());
            PojoMessage receivedDVKMessage = documentService.getDvkDAO().getMessage(sentAditDocument.getDvkId());

            Assert.notNull(sentAditDocument);
            Assert.notNull(receivedDVKMessage);

            Assert.notNull(sentAditDocument.getDocumentSharings());
            Assert.isTrue(sentAditDocument.getDocumentSharings().size() > 0);
            Assert.notNull(sentAditDocument.getDocumentFiles());
            Assert.isTrue(sentAditDocument.getDocumentFiles().size() > 0);
            Assert.notNull(receivedDVKMessage.getData());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(sentAditDocument.getGuid(), receivedDVKMessage.getDhlGuid()));
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
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentService.getConfiguration().getDvkOrgCode(),
                    transportOutput.getDecSender().getOrganisationCode()));
            Assert.isNull(transportOutput.getDecSender().getStructuralUnit());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(TestUtils.addPrefixIfNecessary(transportOutput.
                    getDecSender().getPersonalIdCode()),
                    sentAditDocument.getCreatorCode()));
            Assert.notNull(transportOutput.getDecRecipient());
            Assert.notNull(transportInput.getDecRecipient());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(transportOutput.getDecRecipient().get(0).getOrganisationCode(),
                    documentService.getAditUserDAO().getUserByID(containerInput.getTransport().getDecRecipient().get(0).
                            getOrganisationCode()).getDvkOrgCode()));
            Assert.isNull(transportOutput.getDecRecipient().get(0).getStructuralUnit());

            // Do asserts with DecMetaData
            Assert.isNull(containerOutput.getDecMetadata());

            // Do asserts with RecordCreator
            AditUser sender = documentService.getAditUserDAO().getUserByID(containerInput.getTransport().getDecSender().getOrganisationCode());
            Assert.notNull(sender);
            Assert.notNull(containerOutput.getRecordCreator());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation().getName(),
                    sender.getFullName()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation()
                    .getOrganisationCode(),
                    sender.getDvkOrgCode()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation().getResidency(),
                    sentAditDocument.getCreatorCode().substring(0, Math.min(sentAditDocument.getCreatorCode().length(), 2))));
            Assert.isNull(containerOutput.getRecordCreator().getOrganisation().getStructuralUnit());
            Assert.isNull(containerOutput.getRecordCreator().getOrganisation().getPositionTitle());

            // Do asserts with RecordSenderToDec
            Assert.notNull(containerOutput.getRecordSenderToDec());
            Assert.notNull(containerOutput.getRecordSenderToDec().getOrganisation());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getName(),
                    sender.getFullName()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getOrganisationCode(),
                    sender.getDvkOrgCode()));
            Assert.isNull(containerOutput.getRecordSenderToDec().getOrganisation().getStructuralUnit());
            Assert.isNull(containerOutput.getRecordSenderToDec().getOrganisation().getPositionTitle());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getResidency(),
                    sentAditDocument.getCreatorCode().substring(0, Math.min(sentAditDocument.getCreatorCode().length(), 2))));

            // Do asserts with Recipient
            Assert.notNull(containerOutput.getRecipient());
            Assert.isTrue(containerOutput.getRecipient().size() > 0);
            DocumentSharing documentSharing = documentSharings.iterator().next();
            List<Recipient> recipientsFromContainer = containerOutput.getRecipient();

            AditUser recipient = documentService.getAditUserDAO().getUserByID(containerInput.getTransport().getDecRecipient().get(0).getOrganisationCode());
            Assert.notNull(recipient);
            Assert.notNull(TestUtils.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getName(),
                    recipient.getFullName()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getMessageForRecipient(),
                    documentSharing.getComment()));
            Assert.notNull(TestUtils.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getOrganisationCode(),
                    recipient.getDvkOrgCode()));
            String documentSharingUserCode = documentSharing.getUserCode();
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getResidency(),
                    documentSharingUserCode.substring(0, Math.min(documentSharingUserCode.length(), 2))));

            // Do asserts with RecordMetaData
            Assert.notNull(containerOutput.getRecordMetadata());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecordMetadata().getRecordGuid(),
                    sentAditDocument.getGuid()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerInput.getRecordMetadata().getRecordTitle(),
                    containerOutput.getRecordMetadata().getRecordTitle()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerInput.getRecordMetadata().getRecordType(),
                    containerOutput.getRecordMetadata().getRecordType()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getRecordMetadata().getRecordOriginalIdentifier(),
                    Long.toString(sentAditDocument.getId())));

            if (TestUtils.compareStringsIgnoreCase(containerType, CONTAINER_TYPE_NOT_DDOC)) {
                Assert.isTrue(TestUtils.isToday(containerOutput.getRecordMetadata().getRecordDateRegistered()));
            } else if (TestUtils.compareStringsIgnoreCase(containerType, CONTAINER_TYPE_DDOC)) {
                int expectedResultOfCompare = 0;
                Assert.isTrue(expectedResultOfCompare == (containerOutput.getRecordMetadata().getRecordDateRegistered().compareTo
                        (containerInput.getSignatureMetadata().get(0).getSignatureVerificationDate())));
            }

            // Do asserts with Access
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getAccess().getAccessConditionsCode(),
                    ACCESS_CONDITIONS_CODE));

            // Do asserts with SignatureMetaData (if it's DDOC Container)
            if (TestUtils.compareStringsIgnoreCase(containerType, CONTAINER_TYPE_DDOC)) {
                Assert.notNull(containerOutput.getSignatureMetadata());
                Assert.isTrue(containerOutput.getSignatureMetadata().size() > 0);
                Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerOutput.getSignatureMetadata().get(0).getSignatureType(),
                        SIGNATURE_TYPE_COMPANY));
                Assert.isTrue(containerInput.getSignatureMetadata().get(0).getSignatureVerificationDate().equals
                        (containerOutput.getSignatureMetadata().get(0).getSignatureVerificationDate()));
                Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerInput.getSignatureMetadata().get(0).getSigner(),
                        containerOutput.getSignatureMetadata().get(0).getSigner()));
            }

            // Do asserts with an input file and output file
            Assert.notNull(containerInput.getFile().size() > 0);
            Assert.notNull(containerOutput.getFile().size() > 0);
            dvk.api.container.v2_1.File fileInput = containerInput.getFile().get(0);
            dvk.api.container.v2_1.File fileOutput = containerOutput.getFile().get(0);
            Assert.notNull(fileInput);
            Assert.notNull(fileOutput);
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(fileInput.getFileName(), fileOutput.getFileName()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(fileInput.getFileGuid(), fileOutput.getFileGuid()));
            Assert.isTrue(fileInput.getFileSize().equals(fileOutput.getFileSize()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(fileInput.getMimeType(), fileOutput.getMimeType()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(fileInput.getZipBase64Content(),
                    fileOutput.getZipBase64Content()));

            // Do container version assert
            Assert.notNull(containerOutput.getAccess());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(containerInput.getInternalVersion().toString(),
                    containerOutput.getInternalVersion().toString()));

            // Finally, clean the messages
            dvkMessages.add(receivedDVKMessage);
            aditDocuments.add(document);
        }
    }


    //
    // Test receiveDocumentFromDVKClient with container v 1.0
    //
    @Test
    public void receiveDocumentFromDVKClient_V1_Test() throws Exception {
        final String DIGIDOC_CONF_FILE = "/jdigidoc.cfg";
        final String CONTAINER_V_1_0 = "containerVer1_0_ddoc.xml";

        String digiDocConfFilePath = null;
        String containerFilePath;
        java.io.File containerFile = null;

        try {
            // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
            digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource(DIGIDOC_CONF_FILE).getPath();
            // Path to the container v 1.0
            containerFilePath = TestUtils.getContainerPath(CONTAINER_V_1_0, TO_ADIT);
            containerFile = new java.io.File(containerFilePath);

        } catch (Exception e) {
            throw new RuntimeException("There is a problem with the container or/and  digidoc files.", e);
        }

        logger.debug("DEFAULT_GUID - " + DEFAULT_GUID);
        ContainerVer1 container = (ContainerVer1) TestUtils.getContainer(containerFile, Container.Version.Ver1);

        DocumentService documentService = testUtils.getDocumentService();

        // Get recipients used space for further control
        Map<String, Long> usersFromContainer = new HashMap<String, Long>();
        for (Saaja saaja : container.getTransport().getSaajad()) {
            try {
                usersFromContainer.put(saaja.getIsikukood(), documentService.getAditUserDAO().getUsedSpaceForUser(saaja.getIsikukood()));
            } catch (NullPointerException e) {
                throw new RuntimeException("There is a problem with getting recipients.", e);
            }
        }

        Long senderUserPersonQuota = documentService.getAditUserDAO().getUsedSpaceForUser(TestUtils.addPrefixIfNecessary(container.getTransport().getSaatjad().get(0).getIsikukood()));
        Long senderUserOrganisationQuota = documentService.getAditUserDAO().getUsedSpaceForUser(TestUtils.addPrefixIfNecessary(container.getTransport().getSaatjad().get(0).getRegNr()));

        // Create new PojoMessage with capsule v 1.0 and Save to DVK Client DB
        PojoMessage dvkMessage = null;
        try {
            dvkMessage = testUtils.prepareAndSaveDvkMessage_V_1(containerFile);
        } catch (Exception e) {
            logger.error("Can't prepare and save the DVK message." + e.getMessage());
            throw new RuntimeException(e);
        }

        // Check message was inserted into DVK Client DB
        Assert.notNull(documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId()));

        // Call receiveDocumentsFromDVK for all incoming messages
        documentService.receiveDocumentsFromDVK(digiDocConfFilePath);

        List<Document> aditDocsWithDefaultDvkGuid = testUtils.getDocumentsByDvkGuid(DEFAULT_GUID.toString());

        // Check there is only one document with DOCUMENT.DVK_GUID = DEFAULT_GUID in ADIT DB
        Assert.notNull(aditDocsWithDefaultDvkGuid);
        Assert.isTrue(aditDocsWithDefaultDvkGuid.size() == 1);

        Document aditDocument = aditDocsWithDefaultDvkGuid.get(0);
        try {
            aditDocument = testUtils.getNonLazyInitializedDocument(aditDocument.getId());
        } catch (Exception e) {
            logger.error("Can't get not lazy initialized document." + e.getMessage());
            throw new RuntimeException(e);
        }

        // Check document was inserted into ADIT DB
        Assert.notNull(aditDocument);

        // Check mapping

        // Table DOCUMENT
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(dvkMessage.getTitle(), aditDocument.getTitle()));
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(DocumentService.DOCTYPE_LETTER, aditDocument.getDocumentType()));
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditDocument.getCreatorCode(), "EE" + container.getTransport().getSaatjad().get(0).getRegNr()));
        // TODO: assert for CREATOR_NAME - understand how it should work
        // Assert.isTrue(TestUtils.compareStringsIgnoreCase(
        //        aditUserDAO.getUserByID("EE"+container.getTransport().getSaatjad().get(0).getIsikukood()).getFullName(),
        //        aditDocument.getCreatorName()));
        Assert.isTrue(TestUtils.isToday(aditDocument.getCreationDate()));
        Assert.isTrue(TestUtils.isToday(aditDocument.getLastModifiedDate()));
        Assert.isTrue(aditDocument.getDocumentDvkStatusId() == DVK_STATUS_SENT);
        Assert.isTrue(aditDocument.getDvkId() == DEFAULT_DHL_ID);
        // TODO: assert for PARENT_ID (Igor). Can't find PARENT_ID in aditDocument
        // Assert.isTrue();
        Assert.isTrue(aditDocument.getLocked());
        Assert.isTrue(TestUtils.isToday(aditDocument.getLockingDate()));
        // If first added file happens to be a DigiDoc container then
        // extract files and signatures from container. Otherwise add
        // container as a regular file.
        Assert.isTrue(aditDocument.getSignable());
        if (container.getSignedDoc().getDataFiles().get(0).getFileName().contains("ddoc")) {
            Assert.isTrue(aditDocument.getSigned());
        }

        // Table DOCUMENT_SHARING
        Assert.notNull(aditDocument.getDocumentSharings());
        Assert.isTrue(aditDocument.getDocumentSharings().size() == container.getTransport().getSaajad().size());
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
                    Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditUser.getUserCode(),
                            documentSharing.getUserCode()));
                    Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditUser.getFullName(), documentSharing.getUserName()));
                    Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentSharing.getDocumentSharingType(), DOCUMENT_SHARING_TYPE_SEND_TO_ADIT));
                    Assert.isTrue(TestUtils.isToday(documentSharing.getCreationDate()));
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
                    Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentFile.getContentType(), dataFile.getFileMimeType()));
                    Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentFile.getFileName(), dataFile.getFileName()));
                    Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentFile.getFileSizeBytes().toString(), dataFile.getFileSize()));
                    Assert.notNull(TestUtils.isToday(documentFile.getLastModifiedDate()));
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

        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            AditUser recipientUser = documentService.getAditUserDAO().getUserByID(documentSharing.getUserCode());
            logger.debug("Recipient " + documentSharing.getUserCode() + " recipientUserQuota " + usersFromContainer.get(recipientUser.getUserCode()));
            logger.debug("Recipient " + documentSharing.getUserCode() + " recipientUserQuota after receive from DVK " + recipientUser.getDiskQuotaUsed());

            Assert.isTrue(recipientUser.getDiskQuotaUsed() >= usersFromContainer.get(recipientUser.getUserCode()));
        }

        logger.debug("senderUserOrganisationQuota " + senderUserOrganisationQuota);
        logger.debug("senderOrganisationQuota after receive from DVK " + senderUserOrganisationQuota);
        logger.debug("senderUserPersonQuota " + senderUserPersonQuota);
        logger.debug("senderUserPersonQuota  after receive from DVK " + senderUserPersonQuota);
        // Assert.isTrue(usersFromContainer1.get(0) < aditUserDAO.getUserByID(aditDocument.getCreatorCode()).getDiskQuotaUsed());

        //Table DHL_MESSAGES
        Assert.notNull(documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId()).getLocalItemId());

        dvkMessages.add(dvkMessage);
        aditDocuments.add(aditDocument);
    }


    //
    // Test receiveDocumentFromDVKClient with container v 2.1
    //
    @Test
    public void receiveDocumentFromDVKClient_V2_Test() throws Exception {
        final String DIGIDOC_CONF_FILE = "/jdigidoc.cfg";
        final String CONTAINER_V_2_1 = "containerVer2_1_ddoc.xml";

        String digiDocConfFilePath = null;
        String containerFilePath;
        java.io.File containerFile = null;

        try {
            // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
            digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource(DIGIDOC_CONF_FILE).getPath();
            // Path to the 2.1 container file
            containerFilePath = TestUtils.getContainerPath(CONTAINER_V_2_1, TO_ADIT);
            containerFile = new java.io.File(containerFilePath);

        } catch (Exception e) {
            logger.error("There is a problem with the container or/and digidoc files. " + e.getMessage());
            throw new RuntimeException(e);
        }

        logger.debug("DEFAULT_GUID " + DEFAULT_GUID);
        ContainerVer2_1 container = (ContainerVer2_1) TestUtils.getContainer(containerFile, Container.Version.Ver2_1);

        DocumentService documentService = testUtils.getDocumentService();

        // Get recipients used space for further control
        Map<String, Long> usersFromContainer = new HashMap<String, Long>();
        for (DecRecipient saaja : container.getTransport().getDecRecipient()) {
            try {
                usersFromContainer.put(saaja.getPersonalIdCode(), documentService.getAditUserDAO().getUsedSpaceForUser(saaja.getPersonalIdCode()));
            } catch (Exception e) {
                logger.error("There is a problem with getting recipients. " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        AditUser messageSender = documentService.getAditUserDAO().getUserByID(TestUtils.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode()));
        Long senderUsedSpace = documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode());

        // Create new PojoMessage with capsule v 2.1 and Save to DVK Client DB
        PojoMessage dvkMessage = null;
        try {
            dvkMessage = testUtils.prepareAndSaveDvkMessage_V_2_1(containerFile);
        } catch (Exception e) {
            logger.error("Can't prepare and save the DVK message. " + e.getMessage());
            throw new RuntimeException(e);
        }

        // Check message was inserted into DVK Client DB
        Assert.notNull(documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId()));

        // Call receiveDocumentsFromDVK for all incoming messages
        documentService.receiveDocumentsFromDVK(digiDocConfFilePath);

        List<Document> aditDocsWithDefaultDvkGuid = testUtils.getDocumentsByDvkGuid(DEFAULT_GUID.toString());

        // Check there is only one document with DOCUMENT.DVK_GUID = DEFAULT_GUID in ADIT DB
        Assert.notNull(aditDocsWithDefaultDvkGuid);
        Assert.isTrue(aditDocsWithDefaultDvkGuid.size() == 1);

        Document aditDocument = aditDocsWithDefaultDvkGuid.get(0);
        try {
            aditDocument = testUtils.getNonLazyInitializedDocument(aditDocument.getId());
        } catch (Exception e) {
            logger.error("Can't get not lazy initialized document. " + e.getMessage());
            throw new RuntimeException(e);
        }

        // Check document was inserted into ADIT DB
        Assert.notNull(aditDocument);

        // Check mapping
        // Table DOCUMENT
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(dvkMessage.getTitle(), aditDocument.getTitle()));
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(DocumentService.DOCTYPE_LETTER, aditDocument.getDocumentType()));
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditDocument.getCreatorCode(), TestUtils.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode())));
        if (dvkMessage.getSenderOrgName() != null && !dvkMessage.getSenderOrgName().isEmpty()) {
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditDocument.getCreatorName(), dvkMessage.getSenderOrgName()));
        }
        Assert.isTrue(TestUtils.isToday(aditDocument.getCreationDate()));
        Assert.isTrue(TestUtils.isToday(aditDocument.getLastModifiedDate()));
        Assert.isTrue(aditDocument.getDocumentDvkStatusId() == DVK_STATUS_SENT);
        Assert.isTrue(aditDocument.getDvkId() == DEFAULT_DHL_ID);
        // PARENT_ID will be not yet used in ADIT - JIRA ADIT-9
        Assert.isTrue(aditDocument.getLocked());
        Assert.isTrue(TestUtils.isToday(aditDocument.getLockingDate()));
        Assert.isTrue(aditDocument.getSignable());
        if (container.getSignatureMetadata() != null) Assert.isTrue(aditDocument.getSigned());

        // Table DOCUMENT_SHARING
        Assert.notNull(aditDocument.getDocumentSharings());
        Assert.isTrue(aditDocument.getDocumentSharings().size() == container.getTransport().getDecRecipient().size());

        Map<String, DocumentSharing> aditDocSharings = new HashMap<String, DocumentSharing>();
        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            aditDocSharings.put(documentSharing.getUserCode(), documentSharing);
        }
        for (DecRecipient recipient : container.getTransport().getDecRecipient()) {
            String recipientAditUserCode = TestUtils.addPrefixIfNecessary(recipient.getPersonalIdCode());
            Assert.isTrue(aditDocSharings.containsKey(recipientAditUserCode));
            DocumentSharing documentSharing = aditDocSharings.get(recipientAditUserCode);

            Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentSharing.getDocumentSharingType(), DOCUMENT_SHARING_TYPE_SEND_TO_ADIT));
            Assert.isTrue(TestUtils.isToday(documentSharing.getCreationDate()));
            Assert.isNull(documentSharing.getDvkFolder());//todo
            Assert.isTrue(documentSharing.getDvkId() == DEFAULT_DHL_ID);

            // For number of recipients
            if(container.getRecipient().get(0).getMessageForRecipient() != null) {
                Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentSharing.getComment(), container.getRecipient().get(0).getMessageForRecipient()));
            }
        }

        // Table DOCUMENT_FILE
        Assert.notNull(aditDocument.getDocumentFiles());
        HashMap<String, DocumentFile> aditDocFiles = new HashMap<String, DocumentFile>();
        for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
            aditDocFiles.put(documentFile.getGuid(), documentFile);
        }
        for (dvk.api.container.v2_1.File dataFile : container.getFile()) {
            String fileGuid = dataFile.getFileGuid();
            Assert.isTrue(aditDocFiles.containsKey(fileGuid));
            DocumentFile documentFile = aditDocFiles.get(fileGuid);

 /*           if (dataFile.getFileName().contains("ddoc")) {
                Assert.isTrue(documentFile.getDocumentFileTypeId() == DocumentService.FILETYPE_SIGNATURE_CONTAINER);
            }
            Assert.isTrue(!documentFile.getDeleted());
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentFile.getContentType(), dataFile.getMimeType()));
            Assert.isTrue(TestUtils.compareStringsIgnoreCase(documentFile.getFileName(), dataFile.getFileName()));
            Assert.isTrue(documentFile.getFileSizeBytes() == dataFile.getFileSize().intValue());
            Assert.notNull(TestUtils.isToday(documentFile.getLastModifiedDate()));
            if (dataFile.getFileName().contains("ddoc")) {
                // TODO: is it correct that just not null (low priority)
                Assert.notNull(documentFile.getFileDataInDdoc());
            }
*/
            // TODO: asserts for documentFile.getData (Blob) and String from dataFile
       /*     Assert.isTrue(TestUtils.compareStringsIgnoreCase(dataFile.getZipBase64Content(),
                    documentFile.getFileData().toString().getZipBase64Content()));*/
        }

        // Table SIGNATURE
        if (container.getSignatureMetadata() != null) {
            Assert.notNull(aditDocument.getSignatures());
            Assert.isTrue(container.getSignatureMetadata().size() == aditDocument.getSignatures().size());
            for (Signature signature : aditDocument.getSignatures()) {
// TODO: Do it later

            }

        }

        // Table DOCUMENT_HISTORY
        Assert.notNull(aditDocument.getDocumentHistories());
        DocumentHistory aditDocumentHistory = aditDocument.getDocumentHistories().toArray(new DocumentHistory[]{})[0];
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditDocumentHistory.getDocumentHistoryType(), DocumentService.HISTORY_TYPE_EXTRACT_FILE));
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditDocumentHistory.getDescription(), DocumentService.DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE));
        Assert.isTrue(TestUtils.isToday(aditDocumentHistory.getEventDate()));
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditDocumentHistory.getUserCode(), aditDocument.getCreatorCode()));
        Assert.isTrue(TestUtils.compareStringsIgnoreCase(aditDocumentHistory.getXteeUserCode(), aditDocument.getCreatorCode()));


        // Table ADIT_USER
        // TODO: update < with file size summa
        Assert.isTrue(senderUsedSpace < documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()));


        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            AditUser recipientUser = documentService.getAditUserDAO().getUserByID(documentSharing.getUserCode());
            logger.debug("Recipient " + documentSharing.getUserCode() + " recipientUserQuota " + usersFromContainer.get(recipientUser.getUserCode()));
            logger.debug("Recipient " + documentSharing.getUserCode() + " recipientUserQuota after receive from DVK " + recipientUser.getDiskQuotaUsed());

            Assert.isTrue(recipientUser.getDiskQuotaUsed() >= usersFromContainer.get(recipientUser.getUserCode()));
        }

        //Table DHL_MESSAGES
        PojoMessage messageUpdated = documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId());
        Assert.isTrue(messageUpdated.getLocalItemId().equals(aditDocument.getId()));

        dvkMessages.add(dvkMessage);
        aditDocuments.add(aditDocument);
    }


    @Ignore
    @Test
    //ADIT-7
    public void testUpdateDocumentsFromDVK() throws Exception {
        // Update document statuses from DVK
        int updatedDocumentsCount = testUtils.getDocumentService().updateDocumentsFromDVK();
    }

    public void clearDvkDb(List<PojoMessage> dvkMsgs) {
        DetachedCriteria dcMessage = DetachedCriteria.forClass(PojoMessage.class, "dhlMessage");
        DvkDAO dvkDAO = testUtils.getDocumentService().getDvkDAO();

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
        DocumentService documentService = testUtils.getDocumentService();

        if (aditDocs == null || aditDocs.size() == 0) {
            dcDocument.add(Property.forName("document.dvkId").eq(DEFAULT_DHL_ID));
            aditDocs = documentService.getDocumentDAO().getHibernateTemplate().findByCriteria(dcDocument);
        }

        for (Document doc : aditDocs) {
            doc = testUtils.getNonLazyInitializedDocument(doc.getId());
            documentService.getDocumentHistoryDAO().getHibernateTemplate().deleteAll(doc.getDocumentHistories());
            documentService.getDocumentFileDAO().getHibernateTemplate().deleteAll(doc.getDocumentFiles());
            documentService.getDocumentSharingDAO().getHibernateTemplate().deleteAll(doc.getDocumentSharings());
            documentService.getDocumentDAO().getHibernateTemplate().deleteAll(doc.getSignatures());
            documentService.getDocumentDAO().getHibernateTemplate().delete(doc);
        }
    }
}
