package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentFileDeflateResult;

public class DocumentFileDAO extends HibernateDaoSupport {
    private static Logger logger = Logger.getLogger(DocumentFileDAO.class);

    public String deflateDocumentFile(final long documentId, final long fileId, final boolean markDeleted) {
        logger.debug("deflateDocumentFile starting...");
        DocumentFileDeflateResult result = (DocumentFileDeflateResult) getHibernateTemplate().execute(
                new HibernateCallback() {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {
                        Query q = session.getNamedQuery("DEFLATE_FILE");
                        q.setLong("documentId", documentId);
                        q.setLong("fileId", fileId);
                        q.setBoolean("markDeleted", markDeleted);

                        logger.debug("Executing stored procedure DEFLATE_FILE");
                        return q.uniqueResult();
                    }
                });

        logger.debug("File deflation result code is: " + result.getResultCode());

        return result.getResultCode();
    }
}
