package ee.adit.dao.test;

import javax.naming.NamingException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ee.adit.dao.pojo.DocumentType;

public class TestDocumentDAO {

	/**
	 * @param args
	 * @throws NamingException 
	 */
	public static void main(String[] args) throws NamingException {
		
		Configuration conf = (new Configuration()).configure();
		SessionFactory sessionFactory = conf.buildSessionFactory();
		
		Session sess = sessionFactory.getCurrentSession();
		sess.beginTransaction();
		
		DocumentType docType = new DocumentType("KIRI");
		sess.save(docType);	
		
		sess.getTransaction().commit();

		
		
	}

}
