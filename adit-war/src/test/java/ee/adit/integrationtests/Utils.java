package ee.adit.integrationtests;

import java.io.BufferedReader;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dao.pojo.Signature;
import ee.adit.dhx.api.container.Container;
import ee.adit.dhx.api.container.v2_1.ContactInfo;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.DecRecipient;
import ee.adit.dhx.api.container.v2_1.DecSender;
import ee.adit.dhx.api.container.v2_1.OrganisationType;
import ee.adit.dhx.api.container.v2_1.PersonType;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.service.DocumentService;

public class Utils {

    private static Logger logger = LogManager.getLogger(Utils.class);

    private DocumentService documentService;

  
    public static Container getContainer(File containerFile, Container.Version version) throws Exception {
        BufferedReader in = null;
        Container container = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(containerFile), "UTF8"));
            container = Container.marshal(in, version);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return container;
    }

    public List<Document> getDocumentsByDvkGuid(String documentGuid) {
        List<Document> result;
        DetachedCriteria dt = DetachedCriteria.forClass(Document.class, "document");
        dt.add(Property.forName("document.guid").eq(documentGuid));
        result = (List<Document>) documentService.getDocumentDAO().getHibernateTemplate().findByCriteria(dt);
        logger.info("There are " + result.size() + " Documents with dvk_guid = " + documentGuid + "found in ADIT DB");
        return (result.isEmpty() ? null : result);
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

    public Recipient getRecipient_By_OrganisationCode_And_PersonCode(List<Recipient> recipients, String organizationCode, String personCode) {
        for (Recipient recipient : recipients) {
            if (recipient.getOrganisation() == null) {
                OrganisationType aditOrganization = new OrganisationType();
                aditOrganization.setOrganisationCode(documentService.getConfiguration().getDvkOrgCode());
                recipient.setOrganisation(aditOrganization);
            }
            if (compareStringsIgnoreCase(recipient.getOrganisation().getOrganisationCode(), organizationCode)
                    && compareStringsIgnoreCase(recipient.getPerson().getPersonalIdCode(), personCode)) {
                return recipient;
            }
        }
        return null;
    }


    public static boolean compareStringsIgnoreCase(String str1, String str2) {
        return str1 == null && str2 == null || !(str1 == null || str2 == null) && str1.equalsIgnoreCase(str2);
    }

    public static boolean compareObjects(Object obj1, Object obj2) {
        return obj1 == null && obj2 == null || !(obj1 == null || obj2 == null) && obj1.equals(obj2);
    }

    public static boolean compareByteArray(byte[] arr1, byte[] arr2) {
        if (arr1 == null && arr2 == null) {
            return true;
        } else {
            return Arrays.equals(arr1, arr2);
        }
    }

    public static boolean compareDates(Date date1, Date date2) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (date1 == null && date2 == null) {
            return true;
        } else if (date1 == null || date2 == null) {
            return false;
        } else {
            date1 = formatter.parse(formatter.format(date1));
            date2 = formatter.parse(formatter.format(date2));
            logger.debug("date1: " + date1 + ", date2:" + date2);
            return date1.equals(date2);
        }
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

    public ArrayList<Long> initRecipientsRecordOriginalIdentifiers(ContainerVer2_1 container) {
        ArrayList<Long> result = new ArrayList<Long>();
        for (DecRecipient decRecipient : container.getTransport().getDecRecipient()) {
            Recipient recipient = null;
            if (Utils.compareStringsIgnoreCase(decRecipient.getOrganisationCode(), documentService.getConfiguration().getDvkOrgCode())) {
                recipient = getRecipient_By_OrganisationCode_And_PersonCode(container.getRecipient(), decRecipient.getOrganisationCode(), decRecipient.getPersonalIdCode());
            }
            if (recipient != null && recipient.getRecipientRecordOriginalIdentifier() != null && recipient.getRecipientRecordOriginalIdentifier().length() != 0) {
                try {
                    Long recipientRecordOriginalIdentifier = Long.valueOf(recipient.getRecipientRecordOriginalIdentifier());
                    if (documentService.getDocumentDAO().getDocument(recipientRecordOriginalIdentifier) != null) {
                        result.add(recipientRecordOriginalIdentifier);
                    }
                } catch (NumberFormatException e) {
                    logger.info("Incorrect RecipientRecordOriginalIdentifier:" + recipient.getRecipientRecordOriginalIdentifier() +
                            " for recipient person code:" + recipient.getPerson().getPersonalIdCode());
                }
            }
        }
        return result;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
    
    public static String unbaseAndUnpackData(String content) throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String unzippedDataFileName = tmpDir + "fromBase" + generateRandomFileName();
        logger.debug("Unpack and unzip file name: " + unzippedDataFileName);

        ByteArrayInputStream zipBase64ContentInputStream = new ByteArrayInputStream(
                content.getBytes("UTF-8"));
        Base64InputStream base64InputStream = new Base64InputStream(zipBase64ContentInputStream, false, 76, "\n".getBytes());


        FileOutputStream unzippedDataFileOutputStream = new FileOutputStream(
                unzippedDataFileName);
        GZIPInputStream gzipInputStream = new GZIPInputStream(base64InputStream);
        IOUtils.copy(gzipInputStream, unzippedDataFileOutputStream);
        base64InputStream.close();
        gzipInputStream.close();
        zipBase64ContentInputStream.close();
        unzippedDataFileOutputStream.close();

        return unzippedDataFileName;
    }

    public static String generateRandomFileName() {
        StringBuffer result = new StringBuffer();
        Random r = new Random();
        for (int i = 0; i < 30; i++) {
            result.append(r.nextInt(10));
        }
        result.append(".dat");
        return result.toString();
    }
}
