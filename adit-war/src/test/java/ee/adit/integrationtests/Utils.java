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
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dao.pojo.Signature;
import ee.adit.dvk.converter.ContainerVer2_1ToDocumentConverterImpl;
import ee.adit.service.DocumentService;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Blob;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Utils {

    private static Logger logger = Logger.getLogger(Utils.class);

    private DocumentService documentService;

    public Document prepareAndSaveAditDocument(ContainerVer2_1 container, AditUser recipient,
                                               String digiDocConfFilePath, String containerType) throws Exception {
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
            pojoMessage.setTitle(container.getRecordMetadata().getRecordTitle());
            pojoMessage.setDhlGuid(DocumentService_SendReceiveDvkTest_Integration.DEFAULT_GUID.toString());

            // Create a document, is based on the container
            ContainerVer2_1ToDocumentConverterImpl containerVer2_1ToDocumentConverter =
                    new ContainerVer2_1ToDocumentConverterImpl(pojoMessage);
            containerVer2_1ToDocumentConverter.setAditUserDAO(documentService.getAditUserDAO());
            containerVer2_1ToDocumentConverter.setDocumentService(documentService);
            containerVer2_1ToDocumentConverter.setJdigidocCfgTmpFile(digiDocConfFilePath);
            document = containerVer2_1ToDocumentConverter.convert(container);
            document.setDocumentDvkStatusId(DocumentService.DVK_STATUS_SENDING);

            // Create a document signature, related with this document (if it's DDOC container)
            if (Utils.compareStringsIgnoreCase(containerType,
                    DocumentService_SendReceiveDvkTest_Integration.CONTAINER_TYPE_DDOC)) {
                Signature signature = new Signature();
                signature.setDocument(document);
                signature.setCity(DocumentService_SendReceiveDvkTest_Integration.SIGNED_CITY);
                signature.setCountry(DocumentService_SendReceiveDvkTest_Integration.SIGNED_COUNTRY);
                signature.setSignerName(container.getSignatureMetadata().get(0).getSigner());
                signature.setSigningDate(container.getSignatureMetadata().get(0).getSignatureVerificationDate());
                signature.setSignerCode("EE23456783");
                Set<Signature> signatures = new HashSet<Signature>();
                signatures.add(signature);
                document.setSignatures(signatures);
            }

            // Save this document to the ADIT DB (with signatures as well)
            aditDBSession = documentService.getDocumentDAO().getSessionFactory().openSession();
            aditDBSession.setFlushMode(FlushMode.COMMIT);
            transaction = aditDBSession.beginTransaction();
            aditDBSession.save(document);
            transaction.commit();

            // Create a document sharing, related with this document
            documentSharing = new DocumentSharing();
            documentSharing.setDocumentId(document.getId());
            documentSharing.setUserCode(recipient.getUserCode());
            documentService.sendDocument(document, recipient, null, null,
                    container.getRecipient().get(0).getMessageForRecipient());

            // Create a document file, related with this document
            documentFile = new DocumentFile();
            documentFile.setDocument(document);
            documentFile.setFileName(container.getFile().get(0).getFileName());
            documentFile.setGuid(container.getFile().get(0).getFileGuid());
            documentFile.setContentType(container.getFile().get(0).getMimeType());
            documentFile.setFileSizeBytes((long) container.getFile().get(0).getFileSize());

            // Save this document file to the ADIT DB
            documentFileSession = documentService.getDocumentFileDAO().getSessionFactory().openSession();
            documentFileSession.setFlushMode(FlushMode.COMMIT);
            transaction = documentFileSession.beginTransaction();
            // Create a Blob
            Blob fileData = Hibernate.createBlob(container.getFile().get(0).getZipBase64Content().getBytes(),
                    documentFileSession);
            documentFile.setFileData(fileData);
            documentFileSession.save(documentFile);
            transaction.commit();

        } catch (Exception ex) {
            logger.error("prepareAndSaveAditDocument() - exception: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            if (aditDBSession != null) aditDBSession.close();
            if (documentSharingSession != null) documentSharingSession.close();
            if (documentFileSession != null) documentFileSession.close();
        }

        return document;
    }

    public PojoMessage prepareAndSaveDvkMessage_Container_1_0(File containerFile) throws Exception {
        DvkDAO dvkDAO = documentService.getDvkDAO();
        UUID dhlGuid = UUID.randomUUID();

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
            logger.info("prepareAndSaveDvkMessage_Container_1_0  - test dhlGuid" + dhlGuid);
            dvkMessage.setDhlGuid(dhlGuid.toString());

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
                //dvkSession.flush();
                dvkSession.close();
            }
        }
        return dvkMessage;
    }

    public PojoMessage prepareAndSaveDvkMessage_Container_2_1(File containerFile) throws Exception {
        DvkDAO dvkDAO = documentService.getDvkDAO();
        UUID dhlGuid = UUID.randomUUID();
        PojoMessage dvkMessage = new PojoMessage();
        BufferedReader in = null;
        Session dvkSession = null;

        try {
            // Get container v 2.1
            ContainerVer2_1 container = (ContainerVer2_1) Utils.getContainer(containerFile, Container.Version.Ver2_1);

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

            System.out.println("!!!!!!!!!!!!!!!!!prepareAndSaveDvkMessage_Container_2_1.senderOrganisationInfo.getName()" + senderOrganisationInfo.getName());

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
            dvkMessage.setRecipientOrgName(recipientOrganisationInfo == null ? "" : recipientOrganisationInfo.getName());
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
            logger.info("prepareAndSaveDvkMessage_Container_2_1  - test dhlGuid" + dhlGuid);
            dvkMessage.setDhlGuid(dhlGuid.toString());

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
                //dvkSession.flush();
                dvkSession.close();
            }
        }
        return dvkMessage;
    }

    public static Container getContainer(File containerFile, Container.Version version) throws Exception {
        BufferedReader in = null;
        Container container = null;

        try {
            in = new BufferedReader(new FileReader(containerFile));
            container = Container.marshal(in, version);

        } catch (Exception e) {
            throw e;

        } finally {
            IOUtils.closeQuietly(in);

        }

        return container;
    }

    public List<Document> getDocumentsByDvkGuid(String documentGuid) {
        List<Document> result;
        DetachedCriteria dt = DetachedCriteria.forClass(Document.class, "document");
        dt.add(Property.forName("document.guid").eq(documentGuid));
        result = documentService.getDocumentDAO().getHibernateTemplate().findByCriteria(dt);

        System.out.println("!!!!!!!!!getDocumentsByDvkGuid!!!!!!!!!!" + result.get(0).getCreatorName());


        logger.info("There are " + result.size() + " Documents with dvk_guid = " + documentGuid + "found in ADIT DB");
        return (result.isEmpty() ? null : result);
    }

    public Document getDocumentsByDvkGuid_1(String documentGuid) {
        Document result;
        String sql = "from Document where guid = " + documentGuid;
        Session session = null;

        try {
            session = documentService.getDocumentDAO().getSessionFactory().openSession();
            session.beginTransaction();
            result = (Document) session.createQuery(sql).uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }
;
        return result;
    }

    public Document getNonLazyInitializedDocument(Long docId) throws Exception {
        Document result = null;
        Session session = null;

        try {
            session = documentService.getDocumentDAO().getSessionFactory().openSession();
            result = (Document) session.get(Document.class, docId);
            result.getDocumentFiles().toString();
            result.getDocumentSharings().toString();
            result.getSignatures().toString();
            result.getDocumentHistories().toString();

            if (result.getDocumentFiles() == null || result.getDocumentFiles().size() == 0) {
                logger.error("DocumentFiles - " + result.getDocumentFiles());
                //throw new Exception("DocumentFiles wasn't retrieved");
            }
            if (result.getDocumentSharings() == null || result.getDocumentSharings().size() == 0) {
                logger.error("DocumentSharings - " + result.getDocumentSharings());
                //throw new Exception("DocumentSharings wasn't retrieved");
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;

        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    public static PersonType getPersonByCode(List<ContactInfo> usersInfo, String personCode) {
        PersonType person = null;
        for (ContactInfo contact : usersInfo) {
            if (person == null && contact != null && contact.getPerson() != null && compareStringsIgnoreCase(contact.getPerson().getPersonalIdCode(), personCode)) {
                person = contact.getPerson();
            }
        }
        return person;
    }

    public static OrganisationType getOrganisationByCode(List<ContactInfo> usersInfo, String organizationCode) {
        OrganisationType organisation = null;
        for (ContactInfo contact : usersInfo) {
            if (organisation == null && contact != null && contact.getOrganisation() != null && compareStringsIgnoreCase(contact.getOrganisation().getOrganisationCode(), organizationCode)) {
                organisation = contact.getOrganisation();
            }
        }
        return organisation;
    }

    public static Recipient getRecipient_By_OrganisationCode_And_PersonCode(List<Recipient> recipients, String organizationCode, String personCode){
        for (Recipient recipient : recipients) {
            if (recipient.getOrganisation() == null ){
                OrganisationType aditOrganization = new OrganisationType();
                aditOrganization.setOrganisationCode("adit");
                recipient.setOrganisation(aditOrganization);
            }
            if (compareStringsIgnoreCase(recipient.getOrganisation().getOrganisationCode(), organizationCode)
                    && compareStringsIgnoreCase(recipient.getPerson().getPersonalIdCode(), personCode)) {
                return recipient;
            }
        }
        return null;
    }

    public static String getContainerPath(String fileName, String where) {
        String containersPath = DocumentService_SendReceiveDvkTest_Integration.CONTAINERS_PATH + where;
        return Utils.class.getResource(containersPath + fileName).getPath();
    }

    public static boolean compareStringsIgnoreCase(String str1, String str2) {
        return str1 == null && str2 == null || !(str1 == null || str2 == null) && str1.equalsIgnoreCase(str2);
    }

    public static boolean compareObjects(Object obj1, Object obj2) {
        return obj1 == null && obj2 == null || !(obj1 == null || obj2 == null) && obj1.equals(obj2);
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

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
}
