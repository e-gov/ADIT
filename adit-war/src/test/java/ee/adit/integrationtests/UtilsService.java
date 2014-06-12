package ee.adit.integrationtests;

import dvk.api.container.Container;
import dvk.api.container.v1.ContainerVer1;
import dvk.api.container.v1.Saaja;
import dvk.api.container.v1.Saatja;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.AditUserDAO;
import ee.adit.dao.DocumentDAO;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.service.DocumentService;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;

import java.io.*;
import java.sql.Clob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UtilsService {

    private static Logger logger = Logger.getLogger(UtilsService.class);

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
            dvkMessage.setTitle(AppSetupTest_Integration.DEFAULT_DOCUMENT_TITLE);

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
            dvkMessage.setDhlId(AppSetupTest_Integration.DEFAULT_DHL_ID);
            dvkMessage.setDhlGuid(AppSetupTest_Integration.DEFAULT_GUID.toString());

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

    public static PojoMessage prepareAndSaveDvkMessage_V_2_1(DvkDAO dvkDAO, String containerFilePath) throws Exception {
        PojoMessage dvkMessage = new PojoMessage();
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
            creator =  aditUserDAO.getUserByID(sender.getIsikukood());

            aditDoc.setCreatorCode(creator.getUserCode());
            aditDoc.setCreatorName(creator.getFullName());
            aditDoc.setCreatorUserCode(creatorUserPerson.getUserCode());
            aditDoc.setCreatorUserName(creatorUserPerson.getFullName());


        } catch (Exception e){
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
            throw  e;

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

    public static String getContainerPath(String fileName, String where){
        String containersPath = AppSetupTest_Integration.CONTAINERS_PATH + where;
        return UtilsService.class.getResource(containersPath + fileName).getPath();
    }

    public static boolean compareStringsIgnoreCase(String str1, String str2) {
        return !((str1 == null || str2 == null)) && str1.equalsIgnoreCase(str2);
    }

    public static boolean isToday(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return (sdf.format(date).equals(sdf.format(new Date())));
    }
}
