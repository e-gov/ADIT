package ee.adit.service;

import java.util.List;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.util.Configuration;

/**
 * @author Hendrik Pärna
 * @since 21.04.14
 */
public class DvkServiceImpl implements DvkService {
    private static Logger logger = LogManager.getLogger(DvkServiceImpl.class);
    private DocumentDAO documentDAO;
    private Configuration configuration;

    /**
     * {@link ee.adit.service.DvkService#sendDocumentsToDVK()} impl.
     * @return nr of documents sent.
     */
    public int sendDocumentsToDVK() {
        int result = 0;

        List<Document> documents = documentDAO.findAllWaitingToBeSentToDVK();
        logger.info(documents.size() + " documents need to be sent to DVK.");

        for (Document document: documents) {
           //TODO finish me
        }

        return result;
    }

    public DocumentDAO getDocumentDAO() {
        return documentDAO;
    }

    public void setDocumentDAO(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }
}
