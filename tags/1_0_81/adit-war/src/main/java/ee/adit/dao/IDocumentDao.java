package ee.adit.dao;

import ee.adit.dao.pojo.Document;

import java.util.List;

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
