package ee.adit.dao.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.DocumentType;

public class TestDocumentDAO {

	private static Logger LOG = Logger.getLogger(TestDocumentDAO.class);
	
	/**
	 * @param args
	 * @throws NamingException 
	 */
	public static void main(String[] args) throws NamingException {
		
		Configuration conf = (new Configuration()).configure();
		SessionFactory sessionFactory = conf.buildSessionFactory();
		
		DocumentTypeDAO docTypeDAO = new DocumentTypeDAO();
		docTypeDAO.setSessionFactory(sessionFactory);
		
		Collection<DocumentType> docTypes = docTypeDAO.listDocumentTypes();
		
		LOG.debug("Number of document types found: " + docTypes.size());
		
		Iterator<DocumentType> i = docTypes.iterator();
		while(i.hasNext()) {
			LOG.debug("DocumentType: " + i.next().getShortName());
		}
		
	}

}
