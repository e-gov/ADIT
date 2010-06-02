package ee.adit.dao.test;

import javax.naming.NamingException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.DocumentTypeDAO;
import ee.adit.dao.pojo.DocumentType;

public class TestDocumentDAO {

	/**
	 * @param args
	 * @throws NamingException 
	 */
	public static void main(String[] args) throws NamingException {
		
		Configuration conf = (new Configuration()).configure();
		SessionFactory sessionFactory = conf.buildSessionFactory();
		
		DocumentTypeDAO documentDAO = new DocumentTypeDAO(sessionFactory);

		DocumentType documentType = documentDAO.load("KIRI");
		
		System.out.println("documentType: " + documentType.getShortName());
		
	}

}
