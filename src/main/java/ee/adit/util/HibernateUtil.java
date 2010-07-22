package ee.adit.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Startup Hibernate and provide access to the singleton SessionFactory
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static Session session;
    
    /*static {
          try {
                // Create the SessionFactory from hibernate.cfg.xml
                sessionFactory = new org.hibernate.cfg.Configuration().configure().buildSessionFactory();
                session = sessionFactory.openSession();

          } catch (Throwable ex) {
                System.err.println("Initial SessionFactory creation failed." + ex);
                throw new ExceptionInInitializerError(ex);
          }
    }*/
    
    public HibernateUtil() {
    	;
    }
    
    public static synchronized Session getSession() {
          if(sessionFactory == null){
                try {
                      // Create the SessionFactory from hibernate.cfg.xml
                      sessionFactory = new org.hibernate.cfg.Configuration().configure().buildSessionFactory();
                      session = sessionFactory.openSession();

                } catch (Throwable ex) {
                      System.err.println("Initial SessionFactory creation failed." + ex);
                      throw new ExceptionInInitializerError(ex);
                }
          }
          return session;
    }
}
