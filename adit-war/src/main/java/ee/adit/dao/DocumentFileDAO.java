package ee.adit.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentFileDeflateResult;

/**
 * Document file data access class. Provides methods for retrieving and manipulating
 * document file data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentFileDAO extends HibernateDaoSupport {
    
    private static Logger logger = Logger.getLogger(DocumentFileDAO.class);

    /**
     * Deflate document file.
     * 
     * @param documentId document ID
     * @param fileId document file ID
     * @param markDeleted mark document deleted
     * @param failIfSignature
     *     Should the method return an error message if someone attempts to
     *     deflate a signature container.
     * @return deflation result code
     */
    public String deflateDocumentFile(
    		final long documentId,
    		final long fileId,
    		final boolean markDeleted,
    		final boolean failIfSignature) {
    	
        logger.debug("deflateDocumentFile starting...");
        DocumentFileDeflateResult result = (DocumentFileDeflateResult) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        Query q = session.getNamedQuery("DEFLATE_FILE");
                        session.connection().setAutoCommit(false);
                        q.setLong("documentId", documentId);
                        q.setLong("fileId", fileId);
                        q.setLong("markDeleted", markDeleted ? 1 : 0);
                        q.setLong("failIfSignature", failIfSignature ? 1 : 0);
                        logger.debug("Executing stored procedure DEFLATE_FILE");
                        
                        Object uniqueResult = q.uniqueResult();
                        session.connection().setAutoCommit(true);
                        return uniqueResult;
                    }
                });

        logger.debug("File deflation result code is: " + result.getResultCode());

        return result.getResultCode();
    }

    /**
     * Replaces file contents in database with MD5 hash. Also saves offset
     * markers of file contents in signature container.
     * 
     * @param documentId
     *     ID of affected document
     * @param fileId
     *     ID of affected file
     * @param dataStartOffset
     *     Start offset of data file contents in signature container
     * @param dataEndOffset
     *     End offset of data file contents in signature container
     * @return
     *     Result code
     */
    public String removeSignedFileContents(
    	final long documentId,
    	final long fileId,
    	final long dataStartOffset,
    	final long dataEndOffset) {
        
    	logger.debug("removeSignedFileContents starting...");
        DocumentFileDeflateResult result = (DocumentFileDeflateResult) getHibernateTemplate().execute(
            new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    Query q = session.getNamedQuery("REMOVE_SIGNED_FILE_CONTENTS");
                    session.connection().setAutoCommit(false);
                    q.setLong("documentId", documentId);
                    q.setLong("fileId", fileId);
                    q.setLong("ddocStartOffset", dataStartOffset);
                    q.setLong("ddocEndOffset", dataEndOffset);

                    logger.debug("Executing stored procedure REMOVE_SIGNED_FILE_CONTENTS");
                    Object uniqueResult = q.uniqueResult();
                    session.connection().setAutoCommit(true);
                    return uniqueResult;
                }
            });

        logger.debug("REMOVE_SIGNED_FILE_CONTENTS result code is: " + result.getResultCode());

        return result.getResultCode();
    }
    
    @SuppressWarnings("unchecked")
    public DocumentFile getDocumentFileIdByGuid(Document doc, String documentFileGuid) {
    	logger.debug("Attempting to load document file id from database. Document ID: " + String.valueOf(doc.getId()) + " Document File GUID: " + String.valueOf(documentFileGuid));
        
        List<DocumentFile> result;
        DetachedCriteria dt = DetachedCriteria.forClass(DocumentFile.class, "document_file");
//        dt.add(Property.forName("document_file.document.id").eq(documentId));
        dt.add(Restrictions.or(Restrictions.eq("document", doc), Restrictions.eq("guid", documentFileGuid)));
        result = this.getHibernateTemplate().findByCriteria(dt);
        
        return (result.isEmpty() ? null : result.get(0));
    }

}
