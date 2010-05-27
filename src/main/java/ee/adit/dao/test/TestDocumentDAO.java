package ee.adit.dao.test;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ee.adit.dao.DocumentType;

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
