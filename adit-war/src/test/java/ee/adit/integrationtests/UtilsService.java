package ee.adit.integrationtests;

import dvk.api.container.Container;
import dvk.api.container.v1.ContainerVer1;
import dvk.api.container.v1.Saaja;
import dvk.api.container.v1.Saatja;
import dvk.api.container.v2_1.ContactInfo;
import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.container.v2_1.DecRecipient;
import dvk.api.container.v2_1.DecSender;
import dvk.api.container.v2_1.OrganisationType;
import dvk.api.container.v2_1.PersonType;
import dvk.api.container.v2_1.Recipient;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dvk.converter.ContainerVer2_1ToDocumentConverterImpl;
import ee.adit.dvk.converter.containerdocument.RecipientsBuilder;
import ee.adit.service.DocumentService;
import ee.adit.test.util.DAOCollections;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UtilsService {

    private static Logger logger = Logger.getLogger(UtilsService.class);

    public static Document prepareAndSaveAditDocument(DAOCollections daoCollections,
                                                      ContainerVer2_1 container,
                                                      DocumentService documentService) throws Exception {
        Document document = null;
        DocumentSharing documentSharing;
        DocumentFile documentFile;

        Session aditDBSession = null;
        Session documentSharingSession = null;
        Session documentFileSession = null;
        Transaction transaction;

        try {
            // Crate a PojoMessage just to use convert method. Put some information to this message
            PojoMessage pojoMessage = new PojoMessage();
            pojoMessage.setDhlId(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_DHL_ID);
            pojoMessage.setTitle(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_DOCUMENT_TITLE);
            pojoMessage.setDhlGuid(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_GUID.toString());

            // Create a document, is based on the container
            ContainerVer2_1ToDocumentConverterImpl containerVer2_1ToDocumentConverter =
                    new ContainerVer2_1ToDocumentConverterImpl(pojoMessage);
            containerVer2_1ToDocumentConverter.setAditUserDAO(daoCollections.getAditUserDAO());
            containerVer2_1ToDocumentConverter.setDocumentService(documentService);
            document = containerVer2_1ToDocumentConverter.convert(container);

            // Save this document to the ADIT DB
            aditDBSession = daoCollections.getDocumentDAO().getSessionFactory().openSession();
            aditDBSession.setFlushMode(FlushMode.COMMIT);
            transaction = aditDBSession.beginTransaction();
            aditDBSession.save(document);
            transaction.commit();

            // Create a document sharing, related with this document
//            // TODO: probably, add some other information (fields). At this moment we have only mandatory fields,
//            // TODO: that, just to save succesfully to DHL_SHARING table
            documentSharing = new DocumentSharing();
            documentSharing.setDocumentId(document.getId());
            documentSharing.setUserCode(container.getTransport().getDecSender().getPersonalIdCode());
            documentSharing.setDocumentSharingType(DocumentService_SendReceiveDvkTest_Integration.DOCUMENT_SHARING_TYPE_SEND_TO_DVK);
//            RecipientsBuilder recipientsBuilder = new RecipientsBuilder(container);
//            recipientsBuilder.setConfiguration(documentService.getConfiguration());
//            recipientsBuilder.setAditUserDAO(documentService.getAditUserDAO());
//            List<Pair<AditUser, String>> pair = recipientsBuilder.build();
//            documentService.sendDocument(document, pair.get(0).getLeft(), null, null, pair.get(0).
//                    getRight());
            // Save this document sharing to the ADIT DB
            documentSharingSession = daoCollections.getDocumentSharingDAO().getSessionFactory().openSession();
            documentSharingSession.setFlushMode(FlushMode.COMMIT);
            transaction = documentSharingSession.beginTransaction();
            documentSharingSession.save(documentSharing);
            transaction.commit();

            // Create a document file, related with this document
            documentFile = new DocumentFile();
            documentFile.setDocument(document);
            documentFile.setFileName(container.getFile().get(0).getFileName());
            documentFile.setGuid(container.getFile().get(0).getFileGuid());
            documentFile.setContentType(container.getFile().get(0).getMimeType());
            documentFile.setFileSizeBytes((long) container.getFile().get(0).getFileSize());

            // Save this document file to the ADIT DB
            documentFileSession = daoCollections.getDocumentFileDAO().getSessionFactory().openSession();
            documentFileSession.setFlushMode(FlushMode.COMMIT);
            transaction = documentFileSession.beginTransaction();
            // Create a Blob
            Blob fileData = Hibernate.createBlob(container.getFile().get(0).getZipBase64Content().getBytes(),
                    documentFileSession);
            documentFile.setFileData(fileData);
            documentFileSession.save(documentFile);
            transaction.commit();

        } catch (Exception ex) {
            System.out.println("prepareAndSaveAditDocument() - exception: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        } finally {
            if (aditDBSession != null) aditDBSession.close();
            if (documentSharingSession != null) documentSharingSession.close();
            if (documentFileSession != null) documentFileSession.close();
        }

        return document;
    }

    public static PojoMessage prepareAndSaveDvkMessage_V_1(DvkDAO dvkDAO, File containerFile) throws Exception {

        PojoMessage dvkMessage = new PojoMessage();
        BufferedReader in = null;
        Session dvkSession = null;

        try {
            // Get container v 1.0
            ContainerVer1 container = (ContainerVer1) getContainer(containerFile, Container.Version.Ver1);

            //
            // Set PojoMessage data using container data
            //
            dvkMessage.setIsIncoming(true);
            dvkMessage.setTitle(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_DOCUMENT_TITLE);

            Saatja sender = container.getTransport().getSaatjad().get(0);
            dvkMessage.setSenderOrgCode(sender.getRegNr());
            dvkMessage.setSenderOrgName(sender.getAsutuseNimi());
            dvkMessage.setSenderPersonCode(sender.getIsikukood());
            dvkMessage.setSenderName(sender.getNimi());

            Saaja firstRecipient = container.getTransport().getSaajad().get(0);
            dvkMessage.setRecipientOrgCode(firstRecipient.getRegNr());
            dvkMessage.setRecipientOrgName(firstRecipient.getAsutuseNimi());
            dvkMessage.setRecipientPersonCode(firstRecipient.getIsikukood());
            dvkMessage.setRecipientName(firstRecipient.getNimi());

            Date date = new Date();
            dvkMessage.setSendingDate(date);
            dvkMessage.setReceivedDate(date);
            dvkMessage.setSendingStatusId(DocumentService.DVK_STATUS_WAITING);
            dvkMessage.setUnitId(0);
            dvkMessage.setLocalItemId((long) 0);
            dvkMessage.setStatusUpdateNeeded((long) 0);
            dvkMessage.setDhlFolderName("/");
            dvkMessage.setDhlId(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_DHL_ID);
            dvkMessage.setDhlGuid(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_GUID.toString());

            // We use BufferedReader for containerFile instead of container.getContent(),
            // because may be errors in big files handling
            dvkSession = dvkDAO.getSessionFactory().openSession();
            in = new BufferedReader(new FileReader(containerFile));
            Clob clob = Hibernate.createClob(in, containerFile.length(), dvkSession);
            dvkMessage.setData(clob);

            // Save message in DVK UK DB
            dvkDAO.updateDocument(dvkMessage);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;

        } finally {

            IOUtils.closeQuietly(in);

            if (dvkSession != null) {
                dvkSession.close();
            }
        }
        return dvkMessage;
    }

    public static PojoMessage prepareAndSaveDvkMessage_V_2_1(DvkDAO dvkDAO, File containerFile) throws Exception {

        PojoMessage dvkMessage = new PojoMessage();
        BufferedReader in = null;
        Session dvkSession = null;

        try {
            // Get container v 2.1
            ContainerVer2_1 container = (ContainerVer2_1) UtilsService.getContainer(containerFile, Container.Version.Ver2_1);

            //
            // Set PojoMessage data using container data
            //
            dvkMessage.setIsIncoming(true);
            dvkMessage.setTitle(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_DOCUMENT_TITLE);

            DecSender sender = container.getTransport().getDecSender();
            dvkMessage.setSenderOrgCode(sender.getOrganisationCode());
            dvkMessage.setSenderPersonCode(sender.getPersonalIdCode());

            List<ContactInfo> recordSenderInfo = Arrays.asList((ContactInfo) container.getRecordCreator(), container.getRecordSenderToDec());
            OrganisationType senderOrganisationInfo = getOrganisationByCode(recordSenderInfo, sender.getOrganisationCode());
            dvkMessage.setSenderOrgName(senderOrganisationInfo == null ? "" : senderOrganisationInfo.getName());
            PersonType senderPersonInfo = getPersonByCode(recordSenderInfo, sender.getPersonalIdCode());
            dvkMessage.setSenderName(senderPersonInfo == null ? "" : senderPersonInfo.getName());

            DecRecipient firstRecipient = container.getTransport().getDecRecipient().get(0);
            dvkMessage.setRecipientOrgCode(firstRecipient.getOrganisationCode());
            dvkMessage.setRecipientPersonCode(firstRecipient.getPersonalIdCode());

            List<ContactInfo> recordRecipientsInfo = new ArrayList<ContactInfo>();
            for (Recipient recipient : container.getRecipient()) {
                recordRecipientsInfo.add((ContactInfo) recipient);
            }
            OrganisationType recipientOrganisationInfo = getOrganisationByCode(recordRecipientsInfo, firstRecipient.getOrganisationCode());
            dvkMessage.setRecipientOrgName(recipientOrganisationInfo == null ? "" : senderOrganisationInfo.getName());
            PersonType recipientPersonInfo = getPersonByCode(recordSenderInfo, firstRecipient.getPersonalIdCode());
            dvkMessage.setRecipientName(recipientPersonInfo == null ? "" : senderPersonInfo.getName());

            Date date = new Date();
            dvkMessage.setSendingDate(date);
            dvkMessage.setReceivedDate(date);
            dvkMessage.setSendingStatusId(DocumentService.DVK_STATUS_WAITING);
            dvkMessage.setUnitId(0);
            dvkMessage.setLocalItemId((long) 0);
            dvkMessage.setStatusUpdateNeeded((long) 0);
            dvkMessage.setDhlFolderName("/");
            dvkMessage.setDhlId(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_DHL_ID);
            dvkMessage.setDhlGuid(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_GUID.toString());

            // We use BufferedReader for containerFile instead of container.getContent(),
            // because may be errors in big files handling
            dvkSession = dvkDAO.getSessionFactory().openSession();
            in = new BufferedReader(new FileReader(containerFile));
            Clob clob = Hibernate.createClob(in, containerFile.length(), dvkSession);
            dvkMessage.setData(clob);

            // Save message in DVK UK DB
            dvkDAO.updateDocument(dvkMessage);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;

        } finally {

            IOUtils.closeQuietly(in);

            if (dvkSession != null) {
                dvkSession.close();
            }
        }
        return dvkMessage;
    }

    public static Document prepareAndSaveAditDocument_V_1_0(DocumentDAO documentDAO, AditUserDAO aditUserDAO,
                                                            AditUser creatorUserPerson, String recipient, File containerFile) throws Exception {
        Document aditDoc = null;
        DocumentSharing aditDocSharing = null;
        AditUser creator;

        try {
            // Get container v 1.0
            ContainerVer1 container = (ContainerVer1) getContainer(containerFile, Container.Version.Ver1);

            Saatja sender = container.getTransport().getSaatjad().get(0);
            creator = aditUserDAO.getUserByID(sender.getIsikukood());

            aditDoc.setCreatorCode(creator.getUserCode());
            aditDoc.setCreatorName(creator.getFullName());
            aditDoc.setCreatorUserCode(creatorUserPerson.getUserCode());
            aditDoc.setCreatorUserName(creatorUserPerson.getFullName());


        } catch (Exception e) {
            logger.error(e.getMessage());

        } finally {

        }

        return aditDoc;
    }


    public static Container getContainer(File containerFile, Container.Version version) {
        BufferedReader in = null;
        Container container = null;

        try {
            in = new BufferedReader(new FileReader(containerFile));
            container = Container.marshal(in, version);

        } catch (Exception e) {
            //todo

        } finally {
            IOUtils.closeQuietly(in);

        }

        return container;
    }

    public static List<Document> getDocumentsByDvkId(DocumentDAO documentDAO, Long documentDvkId) {
        List<Document> result;
        DetachedCriteria dt = DetachedCriteria.forClass(Document.class, "document");
        dt.add(Property.forName("document.dvkId").eq(documentDvkId));
        result = documentDAO.getHibernateTemplate().findByCriteria(dt);

        logger.info("There are " + result.size() + " Documents with dvk_id = " + documentDvkId + "found in ADIT DB");
        return (result.isEmpty() ? null : result);
    }

    public static List<Document> getDocumentsByDvkGuid(DocumentDAO documentDAO, String documentGuid) {
        List<Document> result;
        DetachedCriteria dt = DetachedCriteria.forClass(Document.class, "document");
        dt.add(Property.forName("document.guid").eq(documentGuid));
        result = documentDAO.getHibernateTemplate().findByCriteria(dt);

        logger.info("There are " + result.size() + " Documents with dvk_guid = " + documentGuid + "found in ADIT DB");
        return (result.isEmpty() ? null : result);
    }

    public static Document getNonLazyInitializedDocument(DocumentDAO documentDAO, Long docId) throws Exception {
        Document result = null;
        Session session = null;

        try {
            session = documentDAO.getSessionFactory().openSession();
            result = (Document) session.get(Document.class, docId);
            result.getDocumentFiles();
            result.getDocumentSharings();
            result.getSignatures();
            result.getDocumentHistories();

            if (result.getDocumentFiles() == null || result.getDocumentFiles().size() == 0) {
                logger.error("DocumentFiles - " + result.getDocumentFiles());
            }
            if (result.getDocumentSharings() == null || result.getDocumentSharings().size() == 0) {
                logger.error("DocumentFiles - " + result.getDocumentSharings());
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;

        } finally {
            if (session != null) {
                session.close();
            }
        }

        if (result.getDocumentFiles() == null || result.getDocumentFiles().size() == 0) {
            logger.error("DocumentFiles - " + result.getDocumentFiles());
            throw new Exception("DocumentFiles wasn't retrieved");
        }
        if (result.getDocumentSharings() == null || result.getDocumentSharings().size() == 0) {
            logger.error("DocumentSharings - " + result.getDocumentSharings());
            throw new Exception("DocumentSharings wasn't retrieved");
        }

        return result;
    }

    public static PersonType getPersonByCode(List<ContactInfo> usersInfo, String personCode) {
        PersonType person = null;
        for (ContactInfo contact : usersInfo) {
            if (contact.getPerson() != null && compareStringsIgnoreCase(contact.getPerson().getPersonalIdCode(), personCode)) {
                person = contact.getPerson();
            }
        }
        return person;
    }

    public static OrganisationType getOrganisationByCode(List<ContactInfo> usersInfo, String organizationCode) {
        OrganisationType organisation = null;
        for (ContactInfo contact : usersInfo) {
            if (contact.getOrganisation() != null && compareStringsIgnoreCase(contact.getOrganisation().getOrganisationCode(), organizationCode)) {
                organisation = contact.getOrganisation();
            }
        }
        return organisation;
    }

    public static String readSQLToString(String filePath) throws Exception {
        StringBuilder fileData = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), "UTF-8"));
        char[] buf = new char[1024];
        int numRead;

        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();

        return fileData.toString();
    }

    public static String getContainerPath(String fileName, String where) {
        String containersPath = DocumentService_SendReceiveDvkTest_Integration.CONTAINERS_PATH + where;
        return UtilsService.class.getResource(containersPath + fileName).getPath();
    }

    public static boolean compareStringsIgnoreCase(String str1, String str2) {
        return !((str1 == null || str2 == null)) && str1.equalsIgnoreCase(str2);
    }

    public static boolean isToday(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return (sdf.format(date).equals(sdf.format(new Date())));
    }

    public static String addPrefixIfNecessary(String code) {
        if (code != null && !code.toUpperCase().startsWith("EE")) {
            return "EE" + code;
        }
        return code;
    }

    public static String clobToString(Clob clobData) {
        if (clobData == null) {
            return "";
        }

        StringBuffer stringBuffer = new StringBuffer();
        String str = "";

        try {
            BufferedReader bufferRead = new BufferedReader(clobData.getCharacterStream());
            while ((str = bufferRead.readLine()) != null) {
                stringBuffer.append(str);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

        return stringBuffer.toString();
    }
}
