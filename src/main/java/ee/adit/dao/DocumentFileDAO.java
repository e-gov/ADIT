package ee.adit.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentFileDeflateResult;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.util.Util;

public class DocumentFileDAO extends HibernateDaoSupport {
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
