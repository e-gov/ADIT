package ee.adit.dao.test;

import javax.naming.NamingException;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class TestDocumentDAO {

	/**
	 * @param args
	 * @throws NamingException 
	 */
	public static void main(String[] args) throws NamingException {
		
		Configuration conf = (new Configuration()).configure();
		SessionFactory sessionFactory = conf.buildSessionFactory();
		
		
		
	}

}
