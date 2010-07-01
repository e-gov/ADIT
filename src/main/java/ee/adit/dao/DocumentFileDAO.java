package ee.adit.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ee.adit.dao.pojo.DocumentFileDeflateResult;

public class DocumentFileDAO extends AbstractAditDAO {
	private static Logger LOG = Logger.getLogger(DocumentFileDAO.class);
	
	public String deflateDocumentFile(final long documentId, final long fileId, final boolean markDeleted) {
		LOG.debug("deflateDocumentFile starting...");
		DocumentFileDeflateResult result = (DocumentFileDeflateResult) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query q = session.getNamedQuery("DEFLATE_FILE");
                q.setLong("documentId", documentId);
                q.setLong("fileId", fileId);
                q.setBoolean("markDeleted", markDeleted);
                
                LOG.debug("Executing stored procedure DEFLATE_FILE");
                return q.uniqueResult();
            }
        }); 
		
		LOG.debug("File deflation result code is: " + result.getResultCode());
		
		return result.getResultCode();
	}	
}
