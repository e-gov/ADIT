package ee.adit.dvk.api;

import java.io.File;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ee.adit.dvk.api.ml.Util;

/**
 * @author User
 *         Class operates with global session and session cache box
 */
public class DVKAPI {
    /**
     * Error occurred at last server test
     */
    private static Exception serverTestError;

    /**
     * Hibernate session
     */
    private static Session session;

    /**
     * Returns last server error occurred at test or null.
     *
     * @return java.lang.Exception
     */
    public static Exception getTestServerError() {
        return serverTestError;
    }

    /**
     * Tries to open session with configuration settings from the
     * specified file.
     *
     * @param configFileName relative or absolute path to Hibernate configuration settings
     * @return true if test succeeded otherwise false
     */
    public static boolean testServer(String configFileName) {
        serverTestError = null;

        Session sess = null;

        try {
            SessionFactory sf = Util.isEmpty(configFileName) ? createSessionFactory(DVKConstants.DefaultConfigFileName)
                    : createSessionFactory(configFileName);

            sess = sf.openSession();

            return true;
        } catch (Exception ex) {
            serverTestError = ex;
            return false;
        } finally {
            if (sess != null) {
                sess.close();
            }
        }
    }

    /**
     * Creates new session factory with provided configuration file.
     *
     * @param configFileName configuration file
     * @return {@link SessionFactory}
     */
    public static SessionFactory createSessionFactory(String configFileName) {
        if (configFileName == null || configFileName.length() == 0) {
            throw new RuntimeException("Config file name cannot be null.\n"
                    + "To use default config file name call parameterless method 'CreateSessionFactory'.");
        }

        try {
            return new Configuration().configure(configFileName).buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Creates new session factory with provided configuration file.
     * @param configFile {@link File}
     * @return {@link SessionFactory}
     */
    public static SessionFactory createSessionFactory(File configFile) {
        if (configFile == null) {
            throw new RuntimeException("Config file cannot be null.\n"
                    + "To use default config file name call parameterless method 'CreateSessionFactory'.");
        }

        try {
            return new Configuration().configure(configFile).buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Returns globally accessible session instance or null.
     *
     * @return org.hibernate.Session
     */
    public static Session getGlobalSession() {
        return session;
    }

    /**
     * Opens globally accessible session.
     *
     * @param configFileName Hibernate configuration file
     * @return {@link Session}
     * @throws HibernateException exception
     */
    public static Session openGlobalSession(String configFileName) throws HibernateException {
        if (Util.isEmpty(configFileName)) {
            throw new RuntimeException("Configuration file is absent");
        }

        if (session == null) {
            session = createSessionFactory(configFileName).openSession();
        }

        return session;
    }

    /**
     * Closes globally accessible session.
     */
    public static void closeGlobalSession() {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    /**
     * Creates and returns an object for retrieving and manipulating DVK data entries
     * for the session.
     *
     * @param sess Hibernate session
     * @return object for for with DVK entries
     */
    public static ISessionCacheBox createSessionCacheBox(Session sess) {
        return new ee.adit.dvk.api.ml.DvkSessionCacheBox(sess);
    }

    /**
     * @author User
     *         DVK entry type
     */
    public enum DvkType {
        Counter, Organization, Occupation, Subdivision, Settings, SettingsFolder, Message, MessageRecipient
    }
}
