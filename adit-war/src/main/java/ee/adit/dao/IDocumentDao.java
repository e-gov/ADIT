package ee.adit.dao;

import java.util.List;

import ee.adit.dao.pojo.Document;

/**
 * @author Hendrik Pärna
 * @since 21.04.14
 */
public interface IDocumentDao {
    /**
     * Find all {@link ee.adit.dao.pojo.Document} objects waiting to be sent to DVK.
     * @return list of {@link ee.adit.dao.pojo.Document}
     */
    List<Document> findAllWaitingToBeSentToDVK();
}
