package ee.adit;

import dvk.api.container.v2_1.ContainerVer2_1;
import dvk.api.ml.PojoMessage;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dvk.DvkSender;
import ee.adit.dvk.converter.DocumentToContainerVer2_1ConverterImpl;
import ee.adit.service.DocumentService;
import ee.adit.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author Hendrik Pärna
 * @since 13.06.14
 */
public class ContainerVer2_1Sender implements DvkSender {
    private static Logger logger = Logger.getLogger(ContainerVer2_1Sender.class);
    private DocumentService documentService;


    /**
     * Constructor.
     *
     * @param documentService {@link DocumentService}
     */
    public ContainerVer2_1Sender(final DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public Long send(final Document document) {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        converter.setAditUserDAO(documentService.getAditUserDAO());
        converter.setConfiguration(documentService.getConfiguration());
        converter.setDocumentTypeDAO(documentService.getDocumentTypeDAO());
        ContainerVer2_1 container = converter.convert(document);

        Long messageId = savePojoMessage(document);
        saveContainerToTempFile(container);
        updatePojoMessageWithData(messageId, container);

        return messageId;
    }

    private void updatePojoMessageWithData(final Long dvkMessageID, final ContainerVer2_1 containerVer2_1) {
        SessionFactory sessionFactory = documentService.getDvkDAO().getSessionFactory();
        Session dvkSession = sessionFactory.openSession();
        dvkSession.setFlushMode(FlushMode.COMMIT);
        Transaction dvkTransaction = dvkSession.beginTransaction();

        try {
            PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession.load(PojoMessage.class, dvkMessageID, LockOptions.UPGRADE);

            String temporaryFile = saveContainerToTempFile(containerVer2_1);

            // Write the temporary file to the database
            InputStream is = new FileInputStream(temporaryFile);
            Writer dataWriter = new StringWriter();
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
            	dataWriter.write(new String(buf, 0, len, "UTF-8"));
            }
            is.close();
            dataWriter.close();
            dvkMessageToUpdate.setData(dataWriter.toString());

            // Commit to DVK database
            dvkTransaction.commit();
        } catch (Exception e) {
            dvkTransaction.rollback();

            // Remove the document with empty clob from the database
            Session dvkSessionForDeletion = sessionFactory.openSession();
            dvkSessionForDeletion.setFlushMode(FlushMode.COMMIT);
            Transaction dvkTransactionForDeletion = dvkSessionForDeletion.beginTransaction();
            try {
                logger.debug("Starting to delete document from DVK Client database: " + dvkMessageID);
                PojoMessage dvkMessageToDelete = (PojoMessage) dvkSessionForDeletion
                        .load(PojoMessage.class, dvkMessageID);
                if (dvkMessageToDelete == null) {
                    logger.warn("DVK message to delete is not initialized.");
                }
                dvkSessionForDeletion.delete(dvkMessageToDelete);
                dvkTransactionForDeletion.commit();
                logger.info("Empty DVK document deleted from DVK Client database. ID: " + dvkMessageID);
            } catch (Exception dvkException) {
                dvkTransactionForDeletion.rollback();
                logger.error("Error deleting document from DVK database: ", dvkException);
            } finally {
                if (dvkSessionForDeletion != null) {
                    dvkSessionForDeletion.close();
                }
            }

            throw new DataRetrievalFailureException(
                    "Error while adding message to DVK Client database (CLOB update): ", e);
        } finally {
            if (dvkSession != null) {
                dvkSession.close();
            }
        }
    }

    private String saveContainerToTempFile(final ContainerVer2_1 container) {
        // Write the DVK Container to temporary file
        String temporaryFile = documentService.getConfiguration().getTempDir() + File.separator
                + Util.generateRandomFileName();
        try {
            container.save2File(temporaryFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return temporaryFile;
    }

    private PojoMessage createPojoMessage(final Document document, final Session dvkSession) {
        PojoMessage dvkMessage = new PojoMessage();
        dvkMessage.setIsIncoming(false);
        dvkMessage.setDhlFolderName(getFolderName(document));
        dvkMessage.setLocalItemId(document.getId());
        dvkMessage.setTitle(document.getTitle());
        dvkMessage.setDhlGuid(document.getGuid());
        dvkMessage.setSendingStatusId(DocumentService.DVK_STATUS_WAITING);
        dvkMessage.setSenderOrgCode(document.getCreatorCode());
        dvkMessage.setSenderPersonCode(document.getCreatorUserCode());
        dvkMessage.setSenderOrgName(document.getCreatorName());
        dvkMessage.setSenderName(document.getCreatorUserName());

        AditUser firstRecipient = getFirstRecipient(document);

        // Add first recipient data
        if (firstRecipient != null) {
            if (firstRecipient.isPerson()) {
                dvkMessage.setRecipientPersonCode(firstRecipient.getUserCode());
                dvkMessage.setRecipientName(firstRecipient.getFullName());
            } else {
                dvkMessage.setRecipientOrgCode(firstRecipient.getUserCode());
                dvkMessage.setRecipientOrgName(firstRecipient.getFullName());
            }
        }

        // Insert data as stream
        dvkMessage.setData(" ");
        return dvkMessage;
    }

    private Long savePojoMessage(final Document document) {
        SessionFactory sessionFactory = documentService.getDvkDAO().getSessionFactory();
        Session dvkSession = sessionFactory.openSession();
        dvkSession.setFlushMode(FlushMode.COMMIT);
        Transaction dvkTransaction = dvkSession.beginTransaction();

        PojoMessage dvkMessage = createPojoMessage(document, dvkSession);

        Long dvkMessageID = null;
        try {
            dvkMessageID = (Long) dvkSession.save(dvkMessage);

            if (dvkMessageID == null || dvkMessageID == 0) {
                logger.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
                throw new DataRetrievalFailureException(
                        "Error while saving outgoing message to DVK database - no ID returned by save method.");
            } else {
                logger.info("Outgoing message saved to DVK database. ID: " + dvkMessageID);
            }

            logger.debug("DVK Message saved to client database. GUID: " + dvkMessage.getDhlGuid());
            dvkTransaction.commit();
        } catch (Exception e) {
            dvkTransaction.rollback();
            throw new DataRetrievalFailureException("Error while adding message to DVK Client database: ", e);
        } finally {
            dvkSession.close();
        }

        return dvkMessageID;
    }

    private AditUser getFirstRecipient(final Document document) {
        Set<DocumentSharing> documentSharings = document.getDocumentSharings();

        if (documentSharings != null && documentSharings.size() > 0) {
            return documentService.getAditUserDAO().getUserByID(documentSharings.iterator().next().getUserCode());
        }

        throw new IllegalStateException("Document has no sharings!");
    }

    private String getFolderName(final Document document) {
        String folderName = "/";

        Set<DocumentSharing> documentSharings = document.getDocumentSharings();

        if (documentSharings != null && documentSharings.size() > 0) {
            folderName = documentSharings.iterator().next().getDvkFolder();
        }

        return folderName;
    }
}
