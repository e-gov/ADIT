package ee.adit.integrationtests;

import dvk.api.container.Container;
import dvk.api.container.v1.ContainerVer1;
import dvk.api.container.v1.Saaja;
import dvk.api.container.v2_1.*;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dao.pojo.Signature;
import ee.adit.integrationtests.Parameters.ContainerFile;
import ee.adit.integrationtests.Parameters.ContainerSignature;
import ee.adit.integrationtests.Parameters.ReceiveFromDvkTestParameter;
import ee.adit.service.DocumentService;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
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
import org.springframework.test.context.TestContextManager;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    final static long DVK_STATUS_SENT = 3;
    final static String DOCUMENT_SHARING_TYPE_SEND_TO_ADIT = "send_adit";
    final static String DOCUMENT_SHARING_TYPE_SEND_TO_DVK = "send_dvk";
    final static String ACCESS_CONDITIONS_CODE = "AK";
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
    public void beforeTest() throws Exception {
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
    @Parameters({"containerVer2_1-ddoc.xml", "containerVer2_1.xml"})
    public void sendDocumentToDVKClient_V2_Test(String containerFileName) throws Exception {
        final String DIGIDOC_CONF_FILE = "/jdigidoc.cfg";

        String containerFilePath;
        String digiDocConfFilePath;
        File containerFile;

        // Path to digiDoc configuration file
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
        List<DecRecipient> decRecipients = containerInput.getTransport().getDecRecipient();
        ArrayList<AditUser> recipients = new ArrayList<AditUser>();
        for (DecRecipient decRecipient : decRecipients) {
            AditUser aditUser = documentService.getAditUserDAO().getUserByID(Utils.addPrefixIfNecessary(decRecipient.getOrganisationCode()));
            if (aditUser != null) recipients.add(aditUser);
        }
        document = utils.prepareAndSaveAditDocument(containerInput, recipients, digiDocConfFilePath, containerType);

        // Send the document from ADIT to DVK UK
        documentService.sendDocumentsToDVK();

        // Get a sent document from ADIT DB, and get a received message from DVK UK DB
        Document sentAditDocument = utils.getNonLazyInitializedDocument(document.getId());
        List<PojoMessage> receivedDVKMessages = utils.getDocumentFromDvkClientByGuid(sentAditDocument.getGuid());
        Assert.notNull(receivedDVKMessages);
        PojoMessage receivedDVKMessage = receivedDVKMessages.get(0);

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

        // Do asserts with DecSender
        Assert.notNull(transportInput.getDecSender());
        Assert.notNull(transportOutput.getDecSender());
        Assert.isTrue(Utils.compareStringsIgnoreCase(documentService.getConfiguration().getDvkOrgCode(),
                transportOutput.getDecSender().getOrganisationCode()));
        Assert.isNull(transportOutput.getDecSender().getStructuralUnit());
        Assert.isTrue(Utils.compareStringsIgnoreCase(Utils.addPrefixIfNecessary(transportOutput.
                getDecSender().getPersonalIdCode()),
                sentAditDocument.getCreatorCode()));

        // Do asserts with DecRecipients
        Assert.notNull(transportOutput.getDecRecipient());
        Assert.notNull(transportInput.getDecRecipient());

        // Gather all recipients from the input container
        Map<String, DecRecipient> decRecipientsFromInputContainer = new HashMap<String, DecRecipient>();
        for (DecRecipient inputDecRecipient: transportInput.getDecRecipient()) {
            decRecipientsFromInputContainer.put(inputDecRecipient.getOrganisationCode(), inputDecRecipient);
        }

        for (DecRecipient outputDecRecipient: transportOutput.getDecRecipient()) {
            String outputDecRecipientCode = Utils.addPrefixIfNecessary(outputDecRecipient.getOrganisationCode());
            Assert.isTrue(decRecipientsFromInputContainer.containsKey(outputDecRecipientCode));
            DecRecipient inputDecRecipient = decRecipientsFromInputContainer.get(outputDecRecipientCode);
            Assert.isTrue(Utils.compareStringsIgnoreCase(outputDecRecipient.getOrganisationCode(),
                    documentService.getAditUserDAO().getUserByID(inputDecRecipient.getOrganisationCode()).getDvkOrgCode()));
        }

        // Do asserts with DecMetaData blocks
        Assert.isNull(containerOutput.getDecMetadata());

        AditUser sender = documentService.getAditUserDAO().getUserByID(containerInput.getTransport().getDecSender().getOrganisationCode());

        // Do asserts with RecordCreator
        if (containerInput.getRecordCreator() != null) {
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
        }

        // Do asserts with RecordSenderToDec
        if (containerInput.getRecordSenderToDec() != null) {
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
        }

        // Do asserts with Recipients
        if (containerInput.getRecipient() != null) {
            Assert.isTrue(containerOutput.getRecipient().size() > 0);
            Map<String, Recipient> recipientsFromInputContainer = new HashMap<String, Recipient>();
            for (Recipient recipientFromInputContainer : containerInput.getRecipient()) {
                recipientsFromInputContainer.put(recipientFromInputContainer.getOrganisation().getOrganisationCode(),
                                                    recipientFromInputContainer);                
            }
            
            for (Recipient outputRecipient : containerOutput.getRecipient()) {
                String recipientFromOutputContainerOrgCode = Utils.addPrefixIfNecessary(outputRecipient.getOrganisation().getOrganisationCode());
                Assert.isTrue(recipientsFromInputContainer.containsKey(recipientFromOutputContainerOrgCode));
                Recipient inputRecipient = recipientsFromInputContainer.get(recipientFromOutputContainerOrgCode);
                Assert.isTrue(decRecipientsFromInputContainer.containsKey(Utils.addPrefixIfNecessary(inputRecipient.getOrganisation().getOrganisationCode())));
                AditUser recipient = documentService.getAditUserDAO().getUserByID(decRecipientsFromInputContainer.get(Utils.addPrefixIfNecessary
                                        (inputRecipient.getOrganisation().getOrganisationCode())).getOrganisationCode());
                Assert.notNull(recipient);
                DocumentSharing documentSharing = documentSharings.iterator().next();
                Assert.notNull(Utils.compareStringsIgnoreCase(outputRecipient.getOrganisation().getName(),
                        recipient.getFullName()));
                Assert.isTrue(Utils.compareStringsIgnoreCase(outputRecipient.getMessageForRecipient(),
                        documentSharing.getComment()));
                Assert.notNull(Utils.compareStringsIgnoreCase(outputRecipient.getOrganisation().getOrganisationCode(),
                        recipient.getDvkOrgCode()));
                String documentSharingUserCode = documentSharing.getUserCode();
                Assert.isTrue(Utils.compareStringsIgnoreCase(outputRecipient.getOrganisation().getResidency(),
                        documentSharingUserCode.substring(0, Math.min(documentSharingUserCode.length(), 2))));

            }
        }

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
        if (containerInput.getAccess() != null) {
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerOutput.getAccess().getAccessConditionsCode(),
                    ACCESS_CONDITIONS_CODE));
            Assert.notNull(containerOutput.getAccess());
            Assert.isTrue(Utils.compareStringsIgnoreCase(containerInput.getInternalVersion().toString(),
                    containerOutput.getInternalVersion().toString()));
        }

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

        // Do asserts with input and output files
        Assert.notNull(containerInput.getFile());
        Assert.notNull(containerOutput.getFile());
        Assert.notNull(containerInput.getFile().size() > 0);
        Assert.notNull(containerOutput.getFile().size() > 0);
        Map<String, dvk.api.container.v2_1.File> filesFromInputContainer = new HashMap<String, dvk.api.container.v2_1.File>();
        for (dvk.api.container.v2_1.File inputFile : containerInput.getFile()) {
            filesFromInputContainer.put(inputFile.getFileGuid(), inputFile);
        }

        for (dvk.api.container.v2_1.File outputFile : containerOutput.getFile()) {
            String outputFileCode = outputFile.getFileGuid();
            Assert.isTrue(filesFromInputContainer.containsKey(outputFileCode));
            dvk.api.container.v2_1.File inputFile = filesFromInputContainer.get(outputFileCode);
            Assert.isTrue(Utils.compareStringsIgnoreCase(inputFile.getFileName(), outputFile.getFileName()));
            Assert.isTrue(inputFile.getFileSize().equals(outputFile.getFileSize()));
            Assert.isTrue(Utils.compareStringsIgnoreCase(inputFile.getMimeType(), outputFile.getMimeType()));
            // Compare input and output files as byte arrays
            Path inputFilePath = Paths.get(Utils.unbaseAndUnpackData(inputFile.getZipBase64Content()));
            Path outputFilePath = Paths.get(Utils.unbaseAndUnpackData(outputFile.getZipBase64Content()));
            Assert.isTrue(Utils.compareByteArray(Files.readAllBytes(inputFilePath), Files.readAllBytes(outputFilePath)));
        }

        // Finally, clean the messages
        dvkMessages.add(receivedDVKMessage);
        aditDocuments.add(document);
    }

    private List<ReceiveFromDvkTestParameter> parametersForReceiveDocumentFromDVKClient_Container_1_0_Test() {
        List<ReceiveFromDvkTestParameter> parametersList = new ArrayList<ReceiveFromDvkTestParameter>();
        parametersList.add(
                new ReceiveFromDvkTestParameter(
                        "containerVer1_0_ddoc.xml",
                        Arrays.asList(
                                new ContainerFile(true, null, "TEST1.ddoc", 44569L)
                        ),
                        Arrays.asList(
                                new ContainerFile("TEST1.pdf", 7786L, "D0", 276L, 10824L),
                                new ContainerFile("testdokument 3.pdf", 19009L, "D1", 10995L, 36741L)
                        ),
                        Arrays.asList(
                                new ContainerSignature(null, "EE48208190319", "ALLERT, KÄRT", new GregorianCalendar(2014, 4, 12).getTime())
                        )
                ));
        parametersList.add(
                new ReceiveFromDvkTestParameter(
                        "containerVer1_0.xml",
                        Arrays.asList(
                                new ContainerFile(false, "966c2c3c-dff9-4bb7-a25c-034c602185c4", "Test_dokument.pdf", 117089L)
                        ),
                        null,//no files in ddoc
                        null //no signatures
                ));
        parametersList.add(
                new ReceiveFromDvkTestParameter(
                        "ADIT-14-12288.xml",
                        Arrays.asList(
                                new ContainerFile(false, null, "KiriEdastus-20140430-00050_template.rtf", 7443L)
                        ),
                        null,//no files in ddoc
                        null //no signatures
                ));
        return parametersList;
    }

    @Test
    @Parameters
    public void receiveDocumentFromDVKClient_Container_1_0_Test(ReceiveFromDvkTestParameter testParameters) throws Exception {

        // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
        String digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource("/" + DIGIDOC_CONF_FILE_NAME).getPath();

        // Container 1.0
        String containerFilePath = testParameters.getPathToXmlContainer();
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
        Assert.isTrue(aditDocsWithTestDvkGuid.size() == 1, "There are " + aditDocsWithTestDvkGuid.size() + " documents in ADIT DB with test dvkGUID: " + dvkMessage.getDhlGuid());

        Document aditDocument = utils.getNonLazyInitializedDocument(aditDocsWithTestDvkGuid.get(0).getId());
        Assert.notNull(aditDocument, "Document wasn't received from DVK_UK DB");

        // Assert mapping
        // Table DOCUMENT
        Assert.isTrue(Utils.compareStringsIgnoreCase(dvkMessage.getTitle(), aditDocument.getTitle()),
                "Document.Title expected:" + dvkMessage.getTitle() + ", actual:" + aditDocument.getTitle());
        Assert.isTrue(Utils.compareStringsIgnoreCase(DocumentService.DOCTYPE_LETTER, aditDocument.getDocumentType()),
                "Document.DocumentType expected:" + DocumentService.DOCTYPE_LETTER + ", actual:" + aditDocument.getDocumentType());
        Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocument.getCreatorCode(), Utils.addPrefixIfNecessary(container.getTransport().getSaatjad().get(0).getRegNr())),
                "Document.CreatorCode expected:" + Utils.addPrefixIfNecessary(container.getTransport().getSaatjad().get(0).getRegNr()) + ", actual:" + aditDocument.getCreatorCode());
        if (dvkMessage.getSenderOrgName() != null && !dvkMessage.getSenderOrgName().isEmpty()) {
            Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocument.getCreatorName(), dvkMessage.getSenderOrgName()),
                    "Document.CreatorName expected:" + dvkMessage.getSenderOrgName() + ", actual:" + aditDocument.getCreatorName() +
                            "Document.CreatorName expected:" + dvkMessage.getSenderOrgName().hashCode() + ", actual:" + aditDocument.getCreatorName().hashCode());
        }
        Assert.isTrue(Utils.isToday(aditDocument.getCreationDate()),
                "Document.CreationDate expected: current day, actual:" + aditDocument.getCreationDate());
        Assert.isTrue(Utils.isToday(aditDocument.getLastModifiedDate()),
                "Document.LastModifiedDate expected: current day, actual:" + aditDocument.getLastModifiedDate());
        Assert.isTrue(aditDocument.getDocumentDvkStatusId() == DVK_STATUS_SENT,
                "Document.DocumentDvkStatusId expected:" + DVK_STATUS_SENT + ", actual:" + aditDocument.getDocumentDvkStatusId());
        Assert.isTrue(aditDocument.getDvkId() == DEFAULT_DHL_ID,
                "Document.DvkId expected:" + DEFAULT_DHL_ID + ", actual:" + aditDocument.getDvkId());

        String originalIdentifier = Utils.getOriginalIdentifierFromContainer(container);
        if (originalIdentifier == null || originalIdentifier.length() == 0) {
            Assert.isNull(aditDocument.getDocument());
        } else {
            Assert.isTrue(Utils.compareStringsIgnoreCase(String.valueOf(aditDocument.getDocument().getId()), originalIdentifier),
                    "expected: " + originalIdentifier + ", actual:" + aditDocument.getDocument().getId());
        }

        Assert.isTrue(aditDocument.getLocked(),
                "Document.Locked expected: true, actual:" + aditDocument.getLocked());
        Assert.isTrue(Utils.isToday(aditDocument.getLockingDate()),
                "Document.LockingDate expected: current day, actual:" + aditDocument.getLockingDate());
        Assert.isTrue(aditDocument.getSignable(),
                "Document.Signable expected: true, actual:" + aditDocument.getSignable());
        if (container.getSignedDoc().getDataFiles().get(0).getFileName().contains("ddoc"))
            Assert.isTrue(aditDocument.getSigned(),
                    "Document.Signed expected: true, actual:" + aditDocument.getSigned());

        // Table DOCUMENT_SHARING
        Assert.notNull(aditDocument.getDocumentSharings(), "Received ADIT document doesn't have related DOCUMENT_SHARING records");
        ArrayList<Saaja> aditRecipients = new ArrayList<Saaja>();
        for (Saaja saaja : container.getTransport().getSaajad()) {
            if (Utils.compareStringsIgnoreCase(saaja.getRegNr(), "adit")) {
                aditRecipients.add(saaja);
            }
        }
        Assert.isTrue(aditDocument.getDocumentSharings().size() == aditRecipients.size(),
                "documentSharing.size expected:" + aditRecipients.size() + ", actual:" + aditDocument.getDocumentSharings().size());
        Map<String, DocumentSharing> receivedAditDocumentSharings = new HashMap<String, DocumentSharing>();
        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            receivedAditDocumentSharings.put(documentSharing.getUserCode(), documentSharing);
        }
        for (Saaja saaja : aditRecipients) {
            String recipientAditUserCode = Utils.addPrefixIfNecessary(saaja.getIsikukood());
            Assert.isTrue(receivedAditDocumentSharings.containsKey(recipientAditUserCode),
                    "Received ADIT document doesn't have related DOCUMENT_SHARING for recipient" + recipientAditUserCode);
            DocumentSharing documentSharing = receivedAditDocumentSharings.get(recipientAditUserCode);
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentSharing.getDocumentSharingType(), DOCUMENT_SHARING_TYPE_SEND_TO_ADIT),
                    "documentSharing.documentSharingType expected:" + DOCUMENT_SHARING_TYPE_SEND_TO_ADIT + ", actual:" + documentSharing.getDocumentSharingType());
            Assert.isTrue(Utils.isToday(documentSharing.getCreationDate()),
                    "documentSharing.creationDate expected: current day, actual:" + aditDocument.getCreationDate());
            Assert.isNull(documentSharing.getDvkFolder());// as designed
            Assert.isTrue(documentSharing.getDvkId() == DEFAULT_DHL_ID,
                    "documentSharing.dvkId expected:" + DEFAULT_DHL_ID + ", actual:" + documentSharing.getDvkId());
            String username = saaja.getNimi() == null ? documentService.getAditUserDAO().getUserByID(Utils.addPrefixIfNecessary(saaja.getIsikukood())).getFullName() : saaja.getNimi();
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentSharing.getUserName(), username),
                    "documentSharing.userName, expected:" + saaja.getNimi() + ", actual:" + username);
        }

        // Table DOCUMENT_FILE
        Assert.notNull(aditDocument.getDocumentFiles(), "Received ADIT document doesn't have related DOCUMENT_FILE records");
        if (testParameters.containerFiles != null && testParameters.filesInDdoc != null) {
            Assert.isTrue(aditDocument.getDocumentFiles().size() == testParameters.containerFiles.size() + testParameters.filesInDdoc.size(),
                    "Number of DOCUMENT_FILES records. expected:" + testParameters.containerFiles.size() + testParameters.filesInDdoc.size() + " ,actual:" + aditDocument.getDocumentFiles().size());
        }
        // Check DOCUMENT_FILE data with files in document container
        HashMap<String, DocumentFile> aditDocumentNotInDdocFilesByName = new HashMap<String, DocumentFile>();
        for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
            if (!documentFile.getFileDataInDdoc()) {
                aditDocumentNotInDdocFilesByName.put(documentFile.getFileName(), documentFile);
            }
        }
        Assert.isTrue(aditDocumentNotInDdocFilesByName.size() == container.getSignedDoc().getDataFiles().size(),
                "Number of DOCUMENT_FILES records with guid. expected:" + container.getSignedDoc().getDataFiles().size() + " ,actual:" + aditDocumentNotInDdocFilesByName.size());
        for (dvk.api.container.v1.DataFile dataFile : container.getSignedDoc().getDataFiles()) {
            String fileName = dataFile.getFileName();
            Assert.isTrue(aditDocumentNotInDdocFilesByName.containsKey(fileName),
                    "Received ADIT document doesn't have related DOCUMENT_FILE for file" + fileName);
            DocumentFile documentFileByName = aditDocumentNotInDdocFilesByName.get(fileName);
            Assert.isTrue(!documentFileByName.getDeleted(),
                    "DocumentFiles.deleted, expected: received ADIT document not deleted, actual: document deleted = " + documentFileByName.getDeleted());
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentFileByName.getContentType(), dataFile.getFileMimeType()),
                    "DocumentFiles.contentType, expected:" + dataFile.getFileMimeType() + ", actual:" + documentFileByName.getContentType());
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentFileByName.getFileName(), dataFile.getFileName()),
                    "DocumentFiles.fileName, expected:" + dataFile.getFileName() + ", actual:" + documentFileByName.getFileName());
            Assert.isTrue(Utils.isToday(documentFileByName.getLastModifiedDate()),
                    "DocumentFiles.lastModifiedDate, expected: current day, actual:" + documentFileByName.getLastModifiedDate());

            if (testParameters.containerFiles != null) {
                List<ContainerFile> filesFromTestParametersByName = testParameters.getFileByName(fileName);
                Assert.isTrue(filesFromTestParametersByName.size() == 1,
                        "There are " + filesFromTestParametersByName.size() + " files with name: " + fileName + " in container or in incorrect test parameters: " + testParameters.getXmlContainerFileName());
                Assert.isTrue(filesFromTestParametersByName.get(0).getSize().equals(documentFileByName.getFileSizeBytes()),
                        "DocumentFiles.FileSizeBytes, expected: " + filesFromTestParametersByName.get(0).getSize() + ", actual:" + documentFileByName.getFileSizeBytes());

                Assert.isTrue(filesFromTestParametersByName.get(0).getFileDataInDdoc().equals(documentFileByName.getFileDataInDdoc()),
                        "DocumentFiles.FileDataInDdoc, expected: " + filesFromTestParametersByName.get(0).getFileDataInDdoc() + ", actual:" + documentFileByName.getFileDataInDdoc());
                Assert.isTrue(Utils.compareStringsIgnoreCase(filesFromTestParametersByName.get(0).getDdocDataFileId(), documentFileByName.getDdocDataFileId()),
                        "DocumentFiles.DdocDataFileId, expected: " + filesFromTestParametersByName.get(0).getDdocDataFileId() + ", actual:" + documentFileByName.getDdocDataFileId());
                Assert.isTrue(Utils.compareObjects(filesFromTestParametersByName.get(0).getDdocDataFileStartOffset(), documentFileByName.getDdocDataFileStartOffset()),
                        "DocumentFiles.DdocDataFileStartOffset, expected: " + filesFromTestParametersByName.get(0).getDdocDataFileStartOffset() + ", actual:" + documentFileByName.getDdocDataFileStartOffset());
                Assert.isTrue(Utils.compareObjects(filesFromTestParametersByName.get(0).getDdocDataFileEndOffset(), documentFileByName.getDdocDataFileEndOffset()),
                        "DocumentFiles.DdocDataFileEndOffset, expected: " + filesFromTestParametersByName.get(0).getDdocDataFileEndOffset() + ", actual:" + documentFileByName.getDdocDataFileEndOffset());

                if (dataFile.getFileName().contains("ddoc")) {
                    Assert.isTrue(documentFileByName.getDocumentFileTypeId() == DocumentService.FILETYPE_SIGNATURE_CONTAINER,
                            "DocumentFiles.documentFileTypeId, expected: " + DocumentService.FILETYPE_SIGNATURE_CONTAINER + ", actual: " + documentFileByName.getDocumentFileTypeId());
                } else {
                    Assert.isTrue(documentFileByName.getDocumentFileTypeId() == DocumentService.FILETYPE_DOCUMENT_FILE,
                            "DocumentFiles.documentFileTypeId, expected: " + DocumentService.FILETYPE_DOCUMENT_FILE + ", actual: " + documentFileByName.getDocumentFileTypeId());
                }

                // Check BLOB
                Assert.isTrue(documentFileByName.getFileData().length() == filesFromTestParametersByName.get(0).getSize(),
                        "DocumentFiles.FileData.length expected: " + filesFromTestParametersByName.get(0).getSize() + ", actual: " + documentFileByName.getFileData().length());
                if (filesFromTestParametersByName.get(0).isResourceExists()) {
                    byte[] bdata = documentFileByName.getFileData().getBytes(1, (int) documentFileByName.getFileData().length());
                    if (filesFromTestParametersByName.get(0).getName().contains(".ddoc") || filesFromTestParametersByName.get(0).getName().contains(".txt")) {
                        String documentFileWithGuidBlobDataConvertedToString = new String(bdata, "UTF-8");
                        Assert.isTrue(Utils.compareStringsIgnoreCase(documentFileWithGuidBlobDataConvertedToString, filesFromTestParametersByName.get(0).getFileContent()),
                                "DocumentFiles.FileData doesn't match to container ZipBase64Content. File name:" + documentFileByName.getFileName());
                    } else {
                        Assert.isTrue(Utils.compareByteArray(bdata, filesFromTestParametersByName.get(0).getBinaryFileContent()),
                                "DocumentFiles.FileData doesn't match to container ZipBase64Content. File name:" + documentFileByName.getFileName());
                    }
                }
            }

        }
        // Check DOCUMENT_FILE data with files in test parameters
        if (testParameters.filesInDdoc != null) {
            HashMap<String, DocumentFile> aditDocumentFilesInDdoc = new HashMap<String, DocumentFile>();
            // We need to count number of files manually as more than one file with same name and size may be in ddoc
            int NumberOfAditDocumentFilesInDdoc = 0;
            for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
                if (documentFile.getFileDataInDdoc()) {
                    aditDocumentFilesInDdoc.put(documentFile.getFileName() + documentFile.getDdocDataFileId() + documentFile.getFileSizeBytes().toString(), documentFile);
                    NumberOfAditDocumentFilesInDdoc++;
                }
            }
            Assert.isTrue(NumberOfAditDocumentFilesInDdoc == testParameters.filesInDdoc.size(),
                    "Number of DOCUMENT_FILES records for files in ddoc container. expected:" + container.getSignedDoc().getDataFiles().size() + " or " + testParameters.containerFiles.size() +
                            " ,actual:" + NumberOfAditDocumentFilesInDdoc);
            for (ContainerFile file : testParameters.getFilesInDdoc()) {
                String fileInDdocKey = file.getName() + file.getDdocDataFileId() + file.getSize().toString();
                Assert.isTrue(aditDocumentFilesInDdoc.containsKey(fileInDdocKey),
                        "DocumentFiles doesn't contain file from test parameters, expected file: " + file.getName() + " " + file.getDdocDataFileId() + file.getSize().toString());
                DocumentFile documentFileFromDdoc = aditDocumentFilesInDdoc.get(fileInDdocKey);
                Assert.isTrue(!documentFileFromDdoc.getDeleted(),
                        "DocumentFiles.deleted, expected: received ADIT document not deleted, actual: document deleted = " + documentFileFromDdoc.getDeleted());
                Assert.isTrue(documentFileFromDdoc.getDocumentFileTypeId() == DocumentService.FILETYPE_DOCUMENT_FILE,
                        "DocumentFiles.documentFileTypeId, expected: " + DocumentService.FILETYPE_DOCUMENT_FILE + ", actual: " + documentFileFromDdoc.getDocumentFileTypeId());
                Assert.isTrue(file.getDdocDataFileStartOffset().equals(documentFileFromDdoc.getDdocDataFileStartOffset()),
                        "DocumentFiles.FileSizeBytes, expected: " + file.getDdocDataFileStartOffset() + ", actual:" + documentFileFromDdoc.getDdocDataFileStartOffset());
                Assert.isTrue(file.getDdocDataFileEndOffset().equals(documentFileFromDdoc.getDdocDataFileEndOffset()),
                        "DocumentFiles.FileSizeBytes, expected: " + file.getDdocDataFileEndOffset() + ", actual:" + documentFileFromDdoc.getDdocDataFileEndOffset());
                Assert.isTrue(Utils.isToday(documentFileFromDdoc.getLastModifiedDate()),
                        "DocumentFiles.lastModifiedDate, expected: current day, actual:" + documentFileFromDdoc.getLastModifiedDate());
                // Impossible to check blob content, as this files are just placeholders
            }
        }

        // Table SIGNATURE
        if (container.getSignedDoc().getDataFiles().get(0).getFileName().contains("ddoc") && testParameters.signaturesInDdoc != null) {
            Assert.isTrue(aditDocument.getSignatures() != null && aditDocument.getSignatures().size() != 0, "Received ADIT document doesn't have related SIGNATURE records");
            Signature signature = aditDocument.getSignatures().toArray(new Signature[]{})[0];
            ContainerSignature containerSignature = testParameters.getSignaturesInDdoc().get(0);
            Assert.isTrue(Utils.compareStringsIgnoreCase(signature.getUserCode(), containerSignature.getUserCode()),
                    "Signature.UserCode. expected:" + containerSignature.getUserCode() + ", actual:" + signature.getUserCode());
            Assert.isTrue(Utils.compareStringsIgnoreCase(signature.getSignerCode(), containerSignature.getSignerCode()),
                    "Signature.SignerCode. expected:" + containerSignature.getSignerCode() + ", actual:" + signature.getSignerCode());
            Assert.isTrue(Utils.compareStringsIgnoreCase(signature.getSignerName(), containerSignature.getSignerName()),
                    "Signature.SignerName. expected:" + containerSignature.getSignerName() + ", actual:" + signature.getSignerName());
            Assert.isTrue(Utils.compareDates(signature.getSigningDate(), containerSignature.getSigningDate()),
                    "Signature.SigningDate. expected:" + containerSignature.getSigningDate() + ", actual:" + signature.getSigningDate());
        }

        // Table DOCUMENT_HISTORY
        if (container.getSignedDoc().getDataFiles().get(0).getFileName().contains("ddoc")) {
            Assert.isTrue(aditDocument.getDocumentHistories() != null && aditDocument.getDocumentHistories().size() != 0, "Received ADIT document doesn't have related DOCUMENT_HISTORY records");
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
            Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocumentHistory.getRemoteApplicationName(), null),
                    "expected: null, actual:" + aditDocumentHistory.getRemoteApplicationName());
        }

        // Table ADIT_USER
        if (testParameters.containerFiles != null) {
            Long sumOfFilesSize = (long) 0;
            for (ContainerFile file : testParameters.containerFiles) {
                sumOfFilesSize = sumOfFilesSize + file.size;
            }
            Assert.isTrue((senderUsedSpace + sumOfFilesSize) == documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()),
                    "expected:" + (senderUsedSpace + sumOfFilesSize) + ", actual:" + documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()));
        } else {
            Assert.isTrue(senderUsedSpace < documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()),
                    "expected senderUsedSpace before test is less than senderUsedSpace after. SenderUsedSpace before test: " + senderUsedSpace + ", after:" + documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()));
        }

        //Table DHL_MESSAGES
        PojoMessage messageUpdated = documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId());
        Assert.isTrue(messageUpdated.getLocalItemId().equals(aditDocument.getId()),
                "expected:" + messageUpdated.getLocalItemId() + ", actual:" + aditDocument.getId());

        dvkMessages.add(dvkMessage);
        aditDocuments.add(aditDocument);

    }

    /**
     * Method set parameters for receiveDocumentFromDVKClient_Container_2_1_Test test
     *
     * @return test parameters
     */
    private List<ReceiveFromDvkTestParameter> parametersForReceiveDocumentFromDVKClient_Container_2_1_Test() {
        List<ReceiveFromDvkTestParameter> parametersList = new ArrayList<ReceiveFromDvkTestParameter>();
        parametersList.add(
                new ReceiveFromDvkTestParameter(
                        "containerVer2_1_ddoc.xml",
                        Arrays.asList(
                                new ContainerFile(true, "25892e17-80f6-415f-9c65-7395632f0223", "Ettepaneku edastus.ddoc", 19790L),
                                new ContainerFile(false, "25892e17-80f6-415f-9c65-7395632f0001", "Ettepanek.doc", 8674L),
                                new ContainerFile(false, "30891e17-66f4-468f-9c79-8315632f0314", "Vastus ettepanekule.ddoc", 19790L)
                        ),
                        Arrays.asList(
                                new ContainerFile("Ettepanek.doc", (long) 8674, "D0", (long) 289, (long) 12038),
                                new ContainerFile("Ettepanek.doc", (long) 8674, "D0", (long) 289, (long) 12038)
                        ),
                        Arrays.asList(
                                new ContainerSignature("EE38806190294", "EE38806190294", "TAMMEMÄE, LAURI", new GregorianCalendar(2013, 3, 4).getTime())
                        )
                ));
        parametersList.add(
                new ReceiveFromDvkTestParameter(
                        "containerVer2_1.xml",
                        Arrays.asList(
                                new ContainerFile(false, "25892e17-80f6-415f-9c65-7395632f0001", "Ettepanek.doc", 8674L)
                        ),
                        null,//no files in ddoc
                        null //no signatures
                ));
        // TODO: Sisemine ADIT-2
/*        parametersList.add(
                new ReceiveFromDvkTestParameter(
                        "containerVer2_1_ddoc_xsd_minimum.xml",
                        Arrays.asList(
                                new ContainerFile(true, "25892e17-80f6-415f-9c65-7395632f0223", "Ettepaneku edastus.ddoc", 19790L),
                                new ContainerFile(false, "25892e17-80f6-415f-9c65-7395632f0001", "Ettepanek.doc", 8674L),
                                new ContainerFile(false, "30891e17-66f4-468f-9c79-8315632f0314", "Vastus ettepanekule.ddoc", 19790L)
                        ),
                        Arrays.asList(
                                new ContainerFile("Ettepanek.doc", (long) 8674, "D0", (long) 289, (long) 12038),
                                new ContainerFile("Ettepanek.doc", (long) 8674, "D0", (long) 289, (long) 12038)
                        ),
                        Arrays.asList(
                                new ContainerSignature("EE38806190294", "EE38806190294", "TAMMEMÄE, LAURI", new GregorianCalendar(2013, 3, 4).getTime())
                        )
                ));*/
        parametersList.add(
                new ReceiveFromDvkTestParameter(
                        "ADIT-14-12289.xml",
                        Arrays.asList(
                                new ContainerFile(false, "B90CB69C-8BCA-4C0B-A8EC-1939A7AF8E10", "KiriEdastus-20140430-00050_template.rtf", (long) 33478)
                        ),
                        null,//no files in ddoc
                        null //no signatures
                ));

        return parametersList;
    }

    /**
     * Test for DocumentService.receiveDocumentsFromDVK() method with capsule 2.1
     *
     * @param testParameters Parameters of the test set with parametersForReceiveDocumentFromDVKClient_Container_2_1_Test() method
     * @throws Exception
     */
    @Test
    @Parameters
    public void receiveDocumentFromDVKClient_Container_2_1_Test(ReceiveFromDvkTestParameter testParameters) throws Exception {

        // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
        String digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource("/" + DIGIDOC_CONF_FILE_NAME).getPath();

        // Container 2.1
        String containerFilePath = testParameters.getPathToXmlContainer();
        java.io.File containerFile = new java.io.File(containerFilePath);
        ContainerVer2_1 container = (ContainerVer2_1) Utils.getContainer(containerFile, Container.Version.Ver2_1);

        DocumentService documentService = utils.getDocumentService();

        // Get sender used space for further control
        AditUser messageSender = documentService.getAditUserDAO().getUserByID(Utils.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode()));
        Long senderUsedSpace = documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode());

        // Insert message into DVK_UK DB
        PojoMessage dvkMessage = utils.prepareAndSaveDvkMessage_Container_2_1(containerFile);
        dvkMessage = documentService.getDvkDAO().getMessage(dvkMessage.getDhlMessageId());

        Assert.notNull(dvkMessage, "Message wasn't inserted into DVK_UK.DHL_MESSAGE table");
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
                "Document.Title expected:" + dvkMessage.getTitle() + ", actual:" + aditDocument.getTitle());
        Assert.isTrue(Utils.compareStringsIgnoreCase(DocumentService.DOCTYPE_LETTER, aditDocument.getDocumentType()),
                "Document.DocumentType expected:" + DocumentService.DOCTYPE_LETTER + ", actual:" + aditDocument.getDocumentType());
        Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocument.getCreatorCode(), Utils.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode())),
                "Document.CreatorCode expected:" + Utils.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode()) + ", actual:" + aditDocument.getCreatorCode());
        if (dvkMessage.getSenderOrgName() != null && !dvkMessage.getSenderOrgName().isEmpty()) {
            Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocument.getCreatorName(), dvkMessage.getSenderOrgName()),
                    "Document.CreatorName expected:" + dvkMessage.getSenderOrgName() + ", actual:" + aditDocument.getCreatorName() +
                            "Document.CreatorName expected:" + dvkMessage.getSenderOrgName().hashCode() + ", actual:" + aditDocument.getCreatorName().hashCode());
        }
        Assert.isTrue(Utils.isToday(aditDocument.getCreationDate()),
                "Document.CreationDate expected: current day, actual:" + aditDocument.getCreationDate());
        Assert.isTrue(Utils.isToday(aditDocument.getLastModifiedDate()),
                "Document.LastModifiedDate expected: current day, actual:" + aditDocument.getLastModifiedDate());
        Assert.isTrue(aditDocument.getDocumentDvkStatusId() == DVK_STATUS_SENT,
                "Document.DocumentDvkStatusId expected:" + DVK_STATUS_SENT + ", actual:" + aditDocument.getDocumentDvkStatusId());
        Assert.isTrue(aditDocument.getDvkId() == DEFAULT_DHL_ID,
                "Document.DvkId expected:" + DEFAULT_DHL_ID + ", actual:" + aditDocument.getDvkId());

        // JIRA ADIT-9
        ArrayList<Long> recipientsRecordOriginalIdentifiers = utils.initRecipientsRecordOriginalIdentifiers(container);
        if (recipientsRecordOriginalIdentifiers != null && recipientsRecordOriginalIdentifiers.size() != 0) {
            Assert.isTrue(recipientsRecordOriginalIdentifiers.contains(aditDocument.getDocument().getId()),
                    "expected one of:" + recipientsRecordOriginalIdentifiers + ", actual:" + aditDocument.getDocument().getId());
        } else {
            Assert.isNull(aditDocument.getDocument());
        }

        Assert.isTrue(aditDocument.getLocked(),
                "Document.Locked expected: true, actual:" + aditDocument.getLocked());
        Assert.isTrue(Utils.isToday(aditDocument.getLockingDate()),
                "Document.LockingDate expected: current day, actual:" + aditDocument.getLockingDate());
        Assert.isTrue(aditDocument.getSignable(),
                "Document.Signable expected: true, actual:" + aditDocument.getSignable());
        if (container.getSignatureMetadata() != null) Assert.isTrue(aditDocument.getSigned(),
                "Document.Signed expected: true, actual:" + aditDocument.getSigned());

        // Table DOCUMENT_SHARING
        Assert.notNull(aditDocument.getDocumentSharings(), "Received ADIT document doesn't have related DOCUMENT_SHARING records");
        ArrayList<DecRecipient> aditRecipients = new ArrayList<DecRecipient>();
        for (DecRecipient decRecipient : container.getTransport().getDecRecipient()) {
            if (Utils.compareStringsIgnoreCase(decRecipient.getOrganisationCode(), "adit")) {
                aditRecipients.add(decRecipient);
            }
        }
        Assert.isTrue(aditDocument.getDocumentSharings().size() == aditRecipients.size(),
                "documentSharing.size expected:" + aditRecipients.size() + ", actual:" + aditDocument.getDocumentSharings().size());
        Map<String, DocumentSharing> receivedAditDocumentSharings = new HashMap<String, DocumentSharing>();
        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            receivedAditDocumentSharings.put(documentSharing.getUserCode(), documentSharing);
        }
        for (DecRecipient recipient : aditRecipients) {
            String recipientAditUserCode = Utils.addPrefixIfNecessary(recipient.getPersonalIdCode());
            Assert.isTrue(receivedAditDocumentSharings.containsKey(recipientAditUserCode),
                    "Received ADIT document doesn't have related DOCUMENT_SHARING for recipient" + recipientAditUserCode);
            DocumentSharing documentSharing = receivedAditDocumentSharings.get(recipientAditUserCode);
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentSharing.getDocumentSharingType(), DOCUMENT_SHARING_TYPE_SEND_TO_ADIT),
                    "documentSharing.documentSharingType expected:" + DOCUMENT_SHARING_TYPE_SEND_TO_ADIT + ", actual:" + documentSharing.getDocumentSharingType());
            Assert.isTrue(Utils.isToday(documentSharing.getCreationDate()),
                    "documentSharing.creationDate expected: current day, actual:" + aditDocument.getCreationDate());
            Assert.isNull(documentSharing.getDvkFolder());// as was designed for capsule 1.0
            Assert.isTrue(documentSharing.getDvkId() == DEFAULT_DHL_ID,
                    "documentSharing.dvkId expected:" + DEFAULT_DHL_ID + ", actual:" + documentSharing.getDvkId());

            Recipient recipientInfo = utils.getRecipient_By_OrganisationCode_And_PersonCode(container.getRecipient(), recipient.getOrganisationCode(), recipient.getPersonalIdCode());
            if (recipientInfo != null) {
                Assert.isTrue(Utils.compareStringsIgnoreCase(documentSharing.getUserName(), recipientInfo.getPerson().getName()),
                        "documentSharing.userName, expected:" + recipientInfo.getPerson().getName() + ", actual:" + documentSharing.getUserName());
                Assert.isTrue(Utils.compareStringsIgnoreCase(documentSharing.getComment(), recipientInfo.getMessageForRecipient()),
                        "documentSharing.comment, expected:" + recipientInfo.getMessageForRecipient() + ", actual:" + documentSharing.getComment());
            }
        }

        // Table DOCUMENT_FILE
        Assert.notNull(aditDocument.getDocumentFiles(), "Received ADIT document doesn't have related DOCUMENT_FILE records");
        if (testParameters.containerFiles != null && testParameters.filesInDdoc != null) {
            Assert.isTrue(aditDocument.getDocumentFiles().size() == testParameters.containerFiles.size() + testParameters.filesInDdoc.size(),
                    "Number of DOCUMENT_FILES records. expected:" + (testParameters.containerFiles.size() + testParameters.filesInDdoc.size()) + " ,actual:" + aditDocument.getDocumentFiles().size());
        }

        // Check DOCUMENT_FILE data with files in document container
        HashMap<String, DocumentFile> aditDocumentFilesWithGuid = new HashMap<String, DocumentFile>();
        for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
            if (documentFile.getGuid() != null) aditDocumentFilesWithGuid.put(documentFile.getGuid(), documentFile);
        }
        Assert.isTrue(aditDocumentFilesWithGuid.size() == container.getFile().size(),
                "Number of DOCUMENT_FILES records with guid. expected:" + container.getFile().size() + " ,actual:" + aditDocumentFilesWithGuid.size());
        for (dvk.api.container.v2_1.File dataFile : container.getFile()) {
            String fileGuid = dataFile.getFileGuid();
            Assert.isTrue(aditDocumentFilesWithGuid.containsKey(fileGuid),
                    "Received ADIT document doesn't have related DOCUMENT_FILE for file" + fileGuid);
            DocumentFile documentFileWithGuid = aditDocumentFilesWithGuid.get(fileGuid);
            Assert.isTrue(!documentFileWithGuid.getDeleted(),
                    "DocumentFiles.deleted, expected: received ADIT document not deleted, actual: document deleted = " + documentFileWithGuid.getDeleted());
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentFileWithGuid.getContentType(), dataFile.getMimeType()),
                    "DocumentFiles.contentType, expected:" + dataFile.getMimeType() + ", actual:" + documentFileWithGuid.getContentType());
            Assert.isTrue(Utils.compareStringsIgnoreCase(documentFileWithGuid.getFileName(), dataFile.getFileName()),
                    "DocumentFiles.fileName, expected:" + dataFile.getFileName() + ", actual:" + documentFileWithGuid.getFileName());
            Assert.isTrue(Utils.isToday(documentFileWithGuid.getLastModifiedDate()),
                    "DocumentFiles.lastModifiedDate, expected: current day, actual:" + documentFileWithGuid.getLastModifiedDate());

            if (testParameters.containerFiles != null) {
                List<ContainerFile> filesFromTestParametersByGuid = testParameters.getFileByGuid(fileGuid);
                Assert.isTrue(filesFromTestParametersByGuid.size() == 1,
                        "There are more than one file with guid: " + fileGuid + " in container or in incorrect test parameters: " + testParameters.getXmlContainerFileName());
                ContainerFile fileFromTestParametersByGuid = filesFromTestParametersByGuid.get(0);
                Assert.isTrue(fileFromTestParametersByGuid.getSize().equals(documentFileWithGuid.getFileSizeBytes()),
                        "DocumentFiles.FileSizeBytes, expected: " + fileFromTestParametersByGuid.getSize() + ", actual:" + documentFileWithGuid.getFileSizeBytes());
                Assert.isTrue(fileFromTestParametersByGuid.getFileDataInDdoc().equals(documentFileWithGuid.getFileDataInDdoc()),
                        "DocumentFiles.FileSizeBytes, expected: " + fileFromTestParametersByGuid.getFileDataInDdoc() + ", actual:" + documentFileWithGuid.getFileDataInDdoc());
                Assert.isTrue(Utils.compareStringsIgnoreCase(fileFromTestParametersByGuid.getDdocDataFileId(), documentFileWithGuid.getDdocDataFileId()),
                        "DocumentFiles.FileSizeBytes, expected: " + fileFromTestParametersByGuid.getDdocDataFileId() + ", actual:" + documentFileWithGuid.getDdocDataFileId());
                Assert.isTrue(Utils.compareObjects(fileFromTestParametersByGuid.getDdocDataFileStartOffset(), documentFileWithGuid.getDdocDataFileStartOffset()),
                        "DocumentFiles.FileSizeBytes, expected: " + fileFromTestParametersByGuid.getDdocDataFileStartOffset() + ", actual:" + documentFileWithGuid.getDdocDataFileStartOffset());
                Assert.isTrue(Utils.compareObjects(fileFromTestParametersByGuid.getDdocDataFileEndOffset(), documentFileWithGuid.getDdocDataFileEndOffset()),
                        "DocumentFiles.FileSizeBytes, expected: " + fileFromTestParametersByGuid.getDdocDataFileEndOffset() + ", actual:" + documentFileWithGuid.getDdocDataFileEndOffset());

                if (dataFile.getFileName().contains("ddoc")) {
                    Assert.isTrue(documentFileWithGuid.getDocumentFileTypeId() == DocumentService.FILETYPE_SIGNATURE_CONTAINER,
                            "DocumentFiles.documentFileTypeId, expected: " + DocumentService.FILETYPE_SIGNATURE_CONTAINER + ", actual: " + documentFileWithGuid.getDocumentFileTypeId());
                } else {
                    Assert.isTrue(documentFileWithGuid.getDocumentFileTypeId() == DocumentService.FILETYPE_DOCUMENT_FILE,
                            "DocumentFiles.documentFileTypeId, expected: " + DocumentService.FILETYPE_DOCUMENT_FILE + ", actual: " + documentFileWithGuid.getDocumentFileTypeId());
                }

                // Check BLOB
                Assert.isTrue(documentFileWithGuid.getFileData().length() == fileFromTestParametersByGuid.getSize(),
                        "DocumentFiles.FileData.length expected: " + fileFromTestParametersByGuid.getSize() + ", actual: " + documentFileWithGuid.getFileData().length());
                if (fileFromTestParametersByGuid.isResourceExists()) {
                    byte[] bdata = documentFileWithGuid.getFileData().getBytes(1, (int) documentFileWithGuid.getFileData().length());
                    if (fileFromTestParametersByGuid.getName().contains(".ddoc") || fileFromTestParametersByGuid.getName().contains(".txt")) {
                        String documentFileWithGuidBlobDataConvertedToString = new String(bdata, "UTF-8");
                        Assert.isTrue(Utils.compareStringsIgnoreCase(documentFileWithGuidBlobDataConvertedToString, fileFromTestParametersByGuid.getFileContent()),
                                "DocumentFiles.FileData doesn't match to container ZipBase64Content. File name:" + documentFileWithGuid.getFileName());
                    } else {
                        Assert.isTrue(Utils.compareByteArray(bdata, fileFromTestParametersByGuid.getBinaryFileContent()),
                                "DocumentFiles.FileData doesn't match to container ZipBase64Content. File name:" + documentFileWithGuid.getFileName());
                    }
                }
            }
        }
        // Check DOCUMENT_FILE data with files in ddoc container (test parameters)
        if (testParameters.filesInDdoc != null) {
            HashMap<String, DocumentFile> aditDocumentFilesInDdoc = new HashMap<String, DocumentFile>();
            // We need to count number of files manually as more than one file with same name and size may be in ddoc
            int NumberOfAditDocumentFilesInDdoc = 0;
            for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
                if (documentFile.getFileDataInDdoc()) {
                    aditDocumentFilesInDdoc.put(documentFile.getFileName() + documentFile.getDdocDataFileId() + documentFile.getFileSizeBytes().toString(), documentFile);
                    NumberOfAditDocumentFilesInDdoc++;
                }
            }
            Assert.isTrue(NumberOfAditDocumentFilesInDdoc == testParameters.filesInDdoc.size(),
                    "Number of DOCUMENT_FILES records for files in ddoc container. expected:" + container.getFile().size() + " or " + testParameters.containerFiles.size() +
                            " ,actual:" + NumberOfAditDocumentFilesInDdoc);
            for (ContainerFile file : testParameters.getFilesInDdoc()) {
                String fileInDdocKey = file.getName() + file.getDdocDataFileId() + file.getSize().toString();
                Assert.isTrue(aditDocumentFilesInDdoc.containsKey(fileInDdocKey),
                        "DocumentFiles doesn't contain file from test parameters, expected file: " + file.getName() + " " + file.getDdocDataFileId() + file.getSize().toString());
                DocumentFile documentFileFromDdoc = aditDocumentFilesInDdoc.get(fileInDdocKey);
                Assert.isTrue(!documentFileFromDdoc.getDeleted(),
                        "DocumentFiles.deleted, expected: received ADIT document not deleted, actual: document deleted = " + documentFileFromDdoc.getDeleted());
                Assert.isTrue(documentFileFromDdoc.getDocumentFileTypeId() == DocumentService.FILETYPE_DOCUMENT_FILE,
                        "DocumentFiles.documentFileTypeId, expected: " + DocumentService.FILETYPE_DOCUMENT_FILE + ", actual: " + documentFileFromDdoc.getDocumentFileTypeId());
                Assert.isTrue(file.getDdocDataFileStartOffset().equals(documentFileFromDdoc.getDdocDataFileStartOffset()),
                        "DocumentFiles.FileSizeBytes, expected: " + file.getDdocDataFileStartOffset() + ", actual:" + documentFileFromDdoc.getDdocDataFileStartOffset());
                Assert.isTrue(file.getDdocDataFileEndOffset().equals(documentFileFromDdoc.getDdocDataFileEndOffset()),
                        "DocumentFiles.FileSizeBytes, expected: " + file.getDdocDataFileEndOffset() + ", actual:" + documentFileFromDdoc.getDdocDataFileEndOffset());
                Assert.isTrue(Utils.isToday(documentFileFromDdoc.getLastModifiedDate()),
                        "DocumentFiles.lastModifiedDate, expected: current day, actual:" + documentFileFromDdoc.getLastModifiedDate());
                // Impossible to check blob content, as this files are just placeholders
            }
        }

        // Table SIGNATURE
        if (container.getFile().get(0).getFileName().contains("ddoc") && testParameters.signaturesInDdoc != null) {
            // todo add verification of all signatures
            Assert.isTrue(aditDocument.getSignatures() != null && aditDocument.getSignatures().size() != 0, "Received ADIT document doesn't have related SIGNATURE records");
            Signature signature = aditDocument.getSignatures().toArray(new Signature[]{})[0];
            ContainerSignature containerSignature = testParameters.getSignaturesInDdoc().get(0);
            Assert.isTrue(Utils.compareStringsIgnoreCase(signature.getUserCode(), containerSignature.getUserCode()),
                    "Signature.UserCode. expected:" + containerSignature.getUserCode() + ", actual:" + signature.getUserCode());
            Assert.isTrue(Utils.compareStringsIgnoreCase(signature.getSignerCode(), containerSignature.getSignerCode()),
                    "Signature.SignerCode. expected:" + containerSignature.getSignerCode() + ", actual:" + signature.getSignerCode());
            Assert.isTrue(Utils.compareStringsIgnoreCase(signature.getSignerName(), containerSignature.getSignerName()),
                    "Signature.SignerName. expected:" + containerSignature.getSignerName() + ", actual:" + signature.getSignerName());
            Assert.isTrue(Utils.compareDates(signature.getSigningDate(), containerSignature.getSigningDate()),
                    "Signature.SigningDate. expected:" + containerSignature.getSigningDate() + ", actual:" + signature.getSigningDate());
        }

        // Table DOCUMENT_HISTORY
        if (container.getFile().get(0).getFileName().contains("ddoc") && testParameters.signaturesInDdoc != null) {
            Assert.isTrue(aditDocument.getDocumentHistories() != null && aditDocument.getDocumentHistories().size() != 0, "Received ADIT document doesn't have related DOCUMENT_HISTORY records");
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
            Assert.isTrue(Utils.compareStringsIgnoreCase(aditDocumentHistory.getRemoteApplicationName(), null),
                    "expected: null, actual:" + aditDocumentHistory.getRemoteApplicationName());
        }

        // Table ADIT_USER

        // Currently 1.0 ja 2.1 take for used space into account only files in container. Size of files extracted from ddoc not accounted.
        if (testParameters.containerFiles != null) {
            Long sumOfFilesSize = (long) 0;
            for (ContainerFile file : testParameters.containerFiles) {
                sumOfFilesSize = sumOfFilesSize + file.size;
            }
            Assert.isTrue((senderUsedSpace + sumOfFilesSize) == documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()),
                    "expected:" + (senderUsedSpace + sumOfFilesSize) + ", actual:" + documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()));
        } else {
            Assert.isTrue(senderUsedSpace < documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()),
                    "expected senderUsedSpace before test is less than senderUsedSpace after. SenderUsedSpace before test: " + senderUsedSpace + ", after:" + documentService.getAditUserDAO().getUsedSpaceForUser(messageSender.getUserCode()));
        }

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
