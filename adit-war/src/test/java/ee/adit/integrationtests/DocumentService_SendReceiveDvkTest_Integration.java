package ee.adit.integrationtests;

import dvk.api.container.Container;
import dvk.api.container.v1.ContainerVer1;
import dvk.api.container.v1.DataFile;
import dvk.api.container.v1.Saaja;
import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.DecRecipient;
import dvk.api.container.v2_1.Transport;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentFileDAO;
import ee.adit.dao.DocumentSharingDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.service.DocumentService;
import ee.adit.test.util.DAOCollections;
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
import java.util.UUID;

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
    private AditUserDAO aditUserDAO;
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
            System.out.println("!!!!!!!!!!!!!!!!!!!! Can't do operations after the test");
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
    // Test sendDocumentToDVKClient with container v 2.1
    //
    @Test
    public void sendDocumentToDVKClient_V2_Test() throws Exception {
        final String CONTAINER_V_2_1 = "containerVer2_1.xml";

        String containerFilePath;
        File containerFile;

        try {
            // Path to the container ver 2.1. Get the container
            containerFilePath = UtilsService.getContainerPath(CONTAINER_V_2_1, TO_DVK);
            containerFile = new File(containerFilePath);
        } catch (Exception ex) {
            System.out.println("There is a problem with the container"
                    + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }

        // Create a container ver 2.1, based on XML file
        ContainerVer2_1 containerInput = (ContainerVer2_1) UtilsService.getContainer(containerFile, Container.Version.Ver2_1);
        Assert.notNull(containerInput);

        // Create a document, based on the container and insert to ADIT DB
        Document document;
        // Gathering all necessary DAO objects to pass it
        DAOCollections daoCollections = new DAOCollections(documentDAO, aditUserDAO, documentSharingDAO, documentFileDAO);
        try {
            AditUser recipent = aditUserDAO.getUserByID(containerInput.getTransport().getDecRecipient().get(0).getOrganisationCode()); //find the user
            document = UtilsService.prepareAndSaveAditDocument(daoCollections, containerInput, recipent, documentService);
        } catch (Exception ex) {
            System.out.println("Can't save a document to ADIT DB");
            ex.printStackTrace();
            throw ex;
        }

        Assert.notNull(document);

        // Send the document from ADIT to DVK UK
        try {
            documentService.sendDocumentsToDVK();
        } catch (Exception ex) {
            System.out.println("Can't send document to DVK");
            ex.printStackTrace();
            throw ex;
        }

        // Get a sent document from ADIT DB, and get a received message from DVK UK DB
        Document sentAditDocument = documentDAO.getDocument(document.getId());
        PojoMessage receivedDVKMessage = dvkDAO.getMessage(sentAditDocument.getDvkId());

        Assert.notNull(sentAditDocument);
        Assert.notNull(receivedDVKMessage);

        Assert.notNull(sentAditDocument.getDocumentSharings());
        Assert.isTrue(sentAditDocument.getDocumentSharings().size() > 0);
        Assert.notNull(sentAditDocument.getDocumentFiles());
        Assert.notNull(receivedDVKMessage.getData());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(sentAditDocument.getGuid(), receivedDVKMessage.getDhlGuid()));
        Object documentSharings[] = sentAditDocument.getDocumentSharings().toArray();
        Assert.notNull(documentSharings);

        // Get a container ver 2.1 from the received DVK document
        ContainerVer2_1 containerOutput = ContainerVer2_1.parse(UtilsService.clobToString(receivedDVKMessage.getData()));

        // Do asserts with an input container and an output container
        Assert.notNull(containerInput);
        Assert.notNull(containerOutput);
        Transport transportInput = containerInput.getTransport();
        Transport transportOutput = containerOutput.getTransport();

        Assert.notNull(transportInput);
        Assert.notNull(transportOutput);
        Assert.notNull(transportInput.getDecSender());
        Assert.notNull(transportOutput.getDecSender());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(documentService.getConfiguration().getDvkOrgCode(),
                                                            transportOutput.getDecSender().getOrganisationCode()));
        Assert.isNull(transportOutput.getDecSender().getStructuralUnit());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(UtilsService.addPrefixIfNecessary(transportOutput.getDecSender().getPersonalIdCode()),
                                                            sentAditDocument.getCreatorCode()));
        Assert.notNull(transportOutput.getDecRecipient());
        Assert.notNull(transportInput.getDecRecipient());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(transportOutput.getDecRecipient().get(0).getOrganisationCode(),
                aditUserDAO.getUserByID(containerInput.getTransport().getDecRecipient().get(0).getOrganisationCode()).getDvkOrgCode()));
        Assert.isNull(transportOutput.getDecRecipient().get(0).getStructuralUnit());

        // Do asserts with DecMetaData
        Assert.isNull(containerOutput.getDecMetadata());

        // Do asserts with RecordCreator
        AditUser sender = aditUserDAO.getUserByID(containerInput.getTransport().getDecSender().getOrganisationCode());
        Assert.notNull(sender);
        Assert.notNull(containerOutput.getRecordCreator());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation().getName(),
                                                            sender.getFullName()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation().getOrganisationCode(),
                                                            sender.getDvkOrgCode()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecordCreator().getOrganisation().getResidency(),
                sentAditDocument.getCreatorCode().substring(0, Math.min(sentAditDocument.getCreatorCode().length(), 2))));
        Assert.isNull(containerOutput.getRecordCreator().getOrganisation().getStructuralUnit());
        Assert.isNull(containerOutput.getRecordCreator().getOrganisation().getPositionTitle());

        // Do asserts with RecordSenderToDec
        Assert.notNull(containerOutput.getRecordSenderToDec());
        Assert.notNull(containerOutput.getRecordSenderToDec().getOrganisation());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getName(),
                                                            sender.getFullName()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getOrganisationCode(),
                                                            sender.getDvkOrgCode()));
        Assert.isNull(containerOutput.getRecordSenderToDec().getOrganisation().getStructuralUnit());
        Assert.isNull(containerOutput.getRecordSenderToDec().getOrganisation().getPositionTitle());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecordSenderToDec().getOrganisation().getResidency(),
                sentAditDocument.getCreatorCode().substring(0, Math.min(sentAditDocument.getCreatorCode().length(), 2))));

        // Do asserts with Recipient
        Assert.notNull(containerOutput.getRecipient());
        Assert.isTrue(containerOutput.getRecipient().size() > 0);
        AditUser recipient = aditUserDAO.getUserByID(containerInput.getTransport().getDecRecipient().get(0).getOrganisationCode());
        Assert.notNull(recipient);
        Assert.notNull(UtilsService.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getName(),
                recipient.getFullName()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getMessageForRecipient(),
                                                           ((DocumentSharing) documentSharings[0]).getComment()));
        Assert.notNull(UtilsService.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getOrganisationCode(),
                recipient.getDvkOrgCode()));
        String documentSharingUserCode = ((DocumentSharing) documentSharings[0]).getUserCode();
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecipient().get(0).getOrganisation().getResidency(),
                documentSharingUserCode.substring(0, Math.min(documentSharingUserCode.length(), 2))));

        // Do asserts with RecordMetaData
        Assert.notNull(containerInput.getRecordMetadata());
        Assert.notNull(containerOutput.getRecordMetadata());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecordMetadata().getRecordGuid(),
                                                            sentAditDocument.getGuid()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerInput.getRecordMetadata().getRecordTitle(),
                                                            containerOutput.getRecordMetadata().getRecordTitle()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerInput.getRecordMetadata().getRecordType(),
                                                            containerOutput.getRecordMetadata().getRecordType()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getRecordMetadata().getRecordOriginalIdentifier(),
                Long.toString(sentAditDocument.getId())));
        Assert.isTrue(UtilsService.isToday(containerOutput.getRecordMetadata().getRecordDateRegistered()));
        // TODO: Document.content match with recordAbstract

        // Do asserts with Access
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerOutput.getAccess().getAccessConditionsCode(),
                                                            ACCESS_CONDITIONS_CODE));

        // Do asserts with an input file and output file
        Assert.notNull(containerInput.getFile().size() > 0);
        Assert.notNull(containerOutput.getFile().size() > 0);
        dvk.api.container.v2_1.File fileInput = containerInput.getFile().get(0);
        dvk.api.container.v2_1.File fileOutput = containerOutput.getFile().get(0);
        Assert.notNull(fileInput);
        Assert.notNull(fileOutput);
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(fileInput.getFileName(), fileOutput.getFileName()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(fileInput.getFileGuid(), fileOutput.getFileGuid()));
        Assert.isTrue(fileInput.getFileSize().equals(fileOutput.getFileSize()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(fileInput.getMimeType(), fileOutput.getMimeType()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(fileInput.getZipBase64Content(),
                                                            fileOutput.getZipBase64Content()));

        // Do container version assert
        Assert.notNull(containerOutput.getAccess());
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(containerInput.getInternalVersion().toString(),
                containerOutput.getInternalVersion().toString()));

        // Finally, clean the messages
        dvkMessages.add(receivedDVKMessage);
        aditDocuments.add(document);
    }


    //
    // Test receiveDocumentFromDVKClient with container v 1.0
    //
    @Test
    public void receiveDocumentFromDVKClient_V1_Test() throws Exception {
        final String DIGIDOC_CONF_FILE = "/jdigidoc.cfg";
        final String CONTAINER_V_1_0 = "containerVer1_0.xml";

        String digiDocConfFilePath = null;
        String containerFilePath;
        java.io.File containerFile = null;

        try {
            // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
            digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource(DIGIDOC_CONF_FILE).getPath();
            // Path to the container v 1.0
            containerFilePath = UtilsService.getContainerPath(CONTAINER_V_1_0, TO_ADIT);
            containerFile = new java.io.File(containerFilePath);

        } catch (Exception e) {
            //todo: inform about problems with files
            System.out.println("There is a problem with the container or/and  digidoc files." + e.getMessage());
            throw e;
        }


        System.out.println("" + DEFAULT_GUID);

        ContainerVer1 container = (ContainerVer1) UtilsService.getContainer(containerFile, Container.Version.Ver1);

        // Get recipients used space for further control
        Map<String, Long> usersFromContainer = new HashMap<String, Long>();
        for (Saaja saaja : container.getTransport().getSaajad()) {
            try {
                usersFromContainer.put(saaja.getIsikukood(), aditUserDAO.getUsedSpaceForUser(saaja.getIsikukood()));
            } catch (NullPointerException e) {
                System.out.println("There is a problem with getting recipients");
                throw e;
            }
        }


        // Create new PojoMessage with capsule v 1.0 and Save to DVK Client DB
        PojoMessage dvkMessage = null;
        try {
            dvkMessage = UtilsService.prepareAndSaveDvkMessage_V_1(dvkDAO, containerFile);
        } catch (Exception e) {
            System.out.println("Can't prepare and save the DVK message");
            throw e;
        }

        // Check message was inserted into DVK Client DB
        Assert.notNull(dvkDAO.getMessage(dvkMessage.getDhlMessageId()));

        // Call receiveDocumentsFromDVK for all incoming messages
        documentService.receiveDocumentsFromDVK(digiDocConfFilePath);

        List<Document> aditDocsWithDefaultDvkGuid = UtilsService.getDocumentsByDvkGuid(documentDAO, DEFAULT_GUID.toString());

        // Check there is only one document with DOCUMENT.DVK_GUID = DEFAULT_GUID in ADIT DB
        Assert.notNull(aditDocsWithDefaultDvkGuid);
        Assert.isTrue(aditDocsWithDefaultDvkGuid.size() == 1);

        Document aditDocument = aditDocsWithDefaultDvkGuid.get(0);
        try {
            aditDocument = UtilsService.getNonLazyInitializedDocument(documentDAO, aditDocument.getId());
        } catch (Exception e) {
            System.out.println("Can't get not lazy initialized document");
            throw e;
        }

        // Check document was inserted into ADIT DB
        Assert.notNull(aditDocument);

        // Check mapping

        // Table DOCUMENT
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(dvkMessage.getTitle(), aditDocument.getTitle()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(DocumentService.DOCTYPE_LETTER, aditDocument.getDocumentType()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(aditDocument.getCreatorCode(), "EE" + container.getTransport().getSaatjad().get(0).getRegNr()));
        // TODO: assert for CREATOR_NAME - understand how it should work
        // Assert.isTrue(UtilsService.compareStringsIgnoreCase(
        //        aditUserDAO.getUserByID("EE"+container.getTransport().getSaatjad().get(0).getIsikukood()).getFullName(),
        //        aditDocument.getCreatorName()));
        Assert.isTrue(UtilsService.isToday(aditDocument.getCreationDate()));
        Assert.isTrue(UtilsService.isToday(aditDocument.getLastModifiedDate()));
        Assert.isTrue(aditDocument.getDocumentDvkStatusId() == DVK_STATUS_SENT);
        Assert.isTrue(aditDocument.getDvkId() == DEFAULT_DHL_ID);
        // TODO: assert for PARENT_ID (Igor). Can't find PARENT_ID in aditDocument
        // Assert.isTrue();
        Assert.isTrue(aditDocument.getLocked());
        Assert.isTrue(UtilsService.isToday(aditDocument.getLockingDate()));
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
                    AditUser aditUser = aditUserDAO.getUserByID(recipient.getIsikukood());
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(aditUser.getUserCode(),
                            documentSharing.getUserCode()));
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(aditUser.getFullName(), documentSharing.getUserName()));
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(documentSharing.getDocumentSharingType(), DOCUMENT_SHARING_TYPE_SEND_TO_ADIT));
                    Assert.isTrue(UtilsService.isToday(documentSharing.getCreationDate()));
                    Assert.isNull(documentSharing.getDvkFolder());
                    // TODO assert for DVK_ID, ID, DOCUMENT_ID - understand ho it should work
                }
            }
        }

        // Table DOCUMENT_FILE
        // TODO: finish this mapping (stack - ???)
        Assert.notNull(aditDocument.getDocumentFiles());
        for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
            for (DataFile dataFile : container.getSignedDoc().getDataFiles()) {
                if (dataFile.getFileId().equals(documentFile.getDdocDataFileId())) {
                    // TODO: asserts for documentFile.getData (Blob) and String from dataFile - Do we need to do it?
                    // TODO: asserts for DOCUMENT_FILE_TYPE_ID  (if ddoc)
                    if (dataFile.getFileName().contains("ddoc")) {
                        // TODO: is it correct?
                        Assert.isTrue(documentFile.getDocumentFileTypeId() == DOCUMENT_FILE_TYPE_ID);
                    }
                    Assert.isTrue(!documentFile.getDeleted());
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(documentFile.getContentType(), dataFile.getFileMimeType()));
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(documentFile.getFileName(), dataFile.getFileName()));
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(documentFile.getFileSizeBytes().toString(), dataFile.getFileSize()));
                    Assert.notNull(UtilsService.isToday(documentFile.getLastModifiedDate()));
                    if (dataFile.getFileName().contains("ddoc")) {
                        // TODO: is it correct that just not null (low priority)
                        Assert.notNull(documentFile.getFileDataInDdoc());
                    }
                }
            }
        }



        // Table SIGNATURE
        // TODO: Do it later
//        Assert.notNull(aditDocument.getSignatures());
//        for (Signature signature : aditDocument.getSignatures()){
//
//        }

        // Table DOCUMENT_HISTORY
        Assert.notNull(aditDocument.getDocumentHistories());
        // TODO: finish it later
//        for (DocumentHistory documentHistory : aditDocument.getDocumentHistories()) {
//            // TODO: DOCUMENT_HISTORY_TYPE, DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE (Liza TODO)
//            Assert.notNull(documentHistory.getEventDate());
//        }

        // Table ADIT_USER
        // TODO: understand why a quota doesn't decrease
        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            AditUser creatorUser = aditUserDAO.getUserByID(documentSharing.getUserCode());
            Assert.isTrue(creatorUser.getDiskQuotaUsed() >= usersFromContainer.get(creatorUser.getUserCode()));
        }
        //Table DHL_MESSAGES
        Assert.notNull(dvkDAO.getMessage(dvkMessage.getDhlMessageId()).getLocalItemId());

        dvkMessages.add(dvkMessage);
        aditDocuments.add(aditDocument);
    }


    //
    // Test receiveDocumentFromDVKClient with container v 2.1
    //
    @Test
    public void receiveDocumentFromDVKClient_V2_Test() throws Exception {
        final String DIGIDOC_CONF_FILE = "/jdigidoc.cfg";
        final String CONTAINER_V_2_1 = "containerVer2_1.xml";

        String digiDocConfFilePath = null;
        String containerFilePath;
        java.io.File containerFile = null;

        try {
            // Path to digiDoc configuration file, needed as parameter for receiveDocumentsFromDVK
            digiDocConfFilePath = DocumentService_SendReceiveDvkTest_Integration.class.getResource(DIGIDOC_CONF_FILE).getPath();
            // Path to the container v 1.0
            containerFilePath = UtilsService.getContainerPath(CONTAINER_V_2_1, TO_ADIT);
            containerFile = new java.io.File(containerFilePath);

        } catch (Exception e) {
            //todo: inform about problems with files
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!! There is a problem with the container or/and  digidoc files." + e.getMessage());
            throw e;
        }

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!" + DEFAULT_GUID);

        ContainerVer2_1 container = (ContainerVer2_1) UtilsService.getContainer(containerFile, Container.Version.Ver2_1);

        // Get recipients used space for further control
        Map<String, Long> usersFromContainer = new HashMap<String, Long>();
        for (DecRecipient saaja : container.getTransport().getDecRecipient()) {
            try {
                usersFromContainer.put(saaja.getPersonalIdCode(), aditUserDAO.getUsedSpaceForUser(saaja.getPersonalIdCode()));
            } catch (NullPointerException e) {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!! There is a problem with getting recipients");
                throw e;
            }
        }

        // Create new PojoMessage with capsule v 2.1 and Save to DVK Client DB
        PojoMessage dvkMessage = null;
        try {
            dvkMessage = UtilsService.prepareAndSaveDvkMessage_V_2_1(dvkDAO, containerFile);
        } catch (Exception e) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!! Can't prepare and save the DVK message");
            throw e;
        }

        // Check message was inserted into DVK Client DB
        Assert.notNull(dvkDAO.getMessage(dvkMessage.getDhlMessageId()));

        // Call receiveDocumentsFromDVK for all incoming messages
        documentService.receiveDocumentsFromDVK(digiDocConfFilePath);

        List<Document> aditDocsWithDefaultDvkGuid = UtilsService.getDocumentsByDvkGuid(documentDAO, DEFAULT_GUID.toString());

        // Check there is only one document with DOCUMENT.DVK_GUID = DEFAULT_GUID in ADIT DB
        Assert.notNull(aditDocsWithDefaultDvkGuid);
        Assert.isTrue(aditDocsWithDefaultDvkGuid.size() == 1);

        Document aditDocument = aditDocsWithDefaultDvkGuid.get(0);
        try {
            aditDocument = UtilsService.getNonLazyInitializedDocument(documentDAO, aditDocument.getId());
        } catch (Exception e) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!! Can't get not lazy initialized document");
            throw e;
        }

        // Check document was inserted into ADIT DB
        Assert.notNull(aditDocument);

        // Check mapping
        // Table DOCUMENT
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(dvkMessage.getTitle(), aditDocument.getTitle()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(DocumentService.DOCTYPE_LETTER, aditDocument.getDocumentType()));
        Assert.isTrue(UtilsService.compareStringsIgnoreCase(aditDocument.getCreatorCode(), UtilsService.addPrefixIfNecessary(container.getTransport().getDecSender().getOrganisationCode())));
        if (dvkMessage.getSenderOrgName() != null && !dvkMessage.getSenderOrgName().isEmpty()) {
            Assert.isTrue(UtilsService.compareStringsIgnoreCase(aditDocument.getCreatorName(), dvkMessage.getSenderOrgName()));
        }
        Assert.isTrue(UtilsService.isToday(aditDocument.getCreationDate()));
        Assert.isTrue(UtilsService.isToday(aditDocument.getLastModifiedDate()));
        Assert.isTrue(aditDocument.getDocumentDvkStatusId() == DVK_STATUS_SENT);
        Assert.isTrue(aditDocument.getDvkId() == DEFAULT_DHL_ID);
        // PARENT_ID will be not yet used in ADIT - JIRA ADIT-9
        Assert.isTrue(aditDocument.getLocked());
        Assert.isTrue(UtilsService.isToday(aditDocument.getLockingDate()));
        // If first added file happens to be a DigiDoc container then
        // extract files and signatures from container. Otherwise add
        // container as a regular file.
        Assert.isTrue(aditDocument.getSignable());
/*
        if (container.getSignedDoc().getDataFiles().get(0).getFileName().contains("ddoc")) {
            Assert.isTrue(aditDocument.getSigned());
        }
*/
        // Table DOCUMENT_SHARING
        Assert.notNull(aditDocument.getDocumentSharings());
        Assert.isTrue(aditDocument.getDocumentSharings().size() == container.getTransport().getDecRecipient().size());
        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            for (DecRecipient recipient : container.getTransport().getDecRecipient()) {
                if (UtilsService.compareStringsIgnoreCase(recipient.getPersonalIdCode(), documentSharing.getUserCode())) {
                    AditUser aditUser = aditUserDAO.getUserByID(recipient.getPersonalIdCode());
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(aditUser.getFullName(), documentSharing.getUserName()));
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(documentSharing.getDocumentSharingType(), DOCUMENT_SHARING_TYPE_SEND_TO_ADIT));
                    Assert.isTrue(UtilsService.isToday(documentSharing.getCreationDate()));
                    Assert.isNull(documentSharing.getDvkFolder());
                    Assert.isTrue(documentSharing.getDvkId() == DEFAULT_DHL_ID);
                }
            }
        }

        // Table DOCUMENT_FILE
        Assert.notNull(aditDocument.getDocumentFiles());
        for (DocumentFile documentFile : aditDocument.getDocumentFiles()) {
            for (dvk.api.container.v2_1.File dataFile : container.getFile()) {
                if (dataFile.getFileGuid().equals(documentFile.getGuid())) {
                    // TODO: asserts for documentFile.getData (Blob) and String from dataFile - Do we need to do it?
                    // TODO: asserts for DOCUMENT_FILE_TYPE_ID  (if ddoc)
                    if (dataFile.getFileName().contains("ddoc")) {
                        // TODO: is it correct?
                        Assert.isTrue(documentFile.getDocumentFileTypeId() == DOCUMENT_FILE_TYPE_ID);
                    }
                    Assert.isTrue(!documentFile.getDeleted());
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(documentFile.getContentType(), dataFile.getMimeType()));
                    Assert.isTrue(UtilsService.compareStringsIgnoreCase(documentFile.getFileName(), dataFile.getFileName()));
                    Assert.isTrue(documentFile.getFileSizeBytes() == dataFile.getFileSize().intValue());
                    Assert.notNull(UtilsService.isToday(documentFile.getLastModifiedDate()));
                    if (dataFile.getFileName().contains("ddoc")) {
                        // TODO: is it correct that just not null (low priority)
                        Assert.notNull(documentFile.getFileDataInDdoc());
                    }
                }
            }
        }


        // Table SIGNATURE
        // TODO: Do it later
//        Assert.notNull(aditDocument.getSignatures());
//        for (Signature signature : aditDocument.getSignatures()){
//
//        }

        // Table DOCUMENT_HISTORY
        Assert.notNull(aditDocument.getDocumentHistories());
        // TODO: finish it later
//        for (DocumentHistory documentHistory : aditDocument.getDocumentHistories()) {
//            // TODO: DOCUMENT_HISTORY_TYPE, DOCUMENT_HISTORY_DESCRIPTION_EXTRACT_FILE (Liza TODO)
//            Assert.notNull(documentHistory.getEventDate());
//        }

        // Table ADIT_USER
        // TODO: understand why a quota doesn't decrease
        for (DocumentSharing documentSharing : aditDocument.getDocumentSharings()) {
            AditUser creatorUser = aditUserDAO.getUserByID(documentSharing.getUserCode());
            Assert.isTrue(creatorUser.getDiskQuotaUsed() >= usersFromContainer.get(creatorUser.getUserCode()));
        }
        //Table DHL_MESSAGES
        Assert.notNull(dvkDAO.getMessage(dvkMessage.getDhlMessageId()).getLocalItemId());

        dvkMessages.add(dvkMessage);
        aditDocuments.add(aditDocument);
    }


    @Ignore
    @Test
    //ADIT-7
    public void testUpdateDocumentsFromDVK() throws Exception {
        // Update document statuses from DVK
        int updatedDocumentsCount = documentService.updateDocumentsFromDVK();
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
