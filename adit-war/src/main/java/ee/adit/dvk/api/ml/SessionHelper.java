package ee.adit.dvk.api.ml;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SessionHelper {
    private Session sess;
    private static Log log = LogFactory.getLog(SessionHelper.class);

    public SessionHelper(Session sess) {
        this.sess = sess;
    }

    public Object get(Class<?> clazz, Serializable id) throws HibernateException {
        assertSession();

        return sess.get(clazz, id);
    }

    public Object get(String entityName, Serializable id) throws HibernateException {
        assertSession();

        return sess.get(entityName, id);
    }

    public void assertSession() {
        if (sess == null) {
            String msg = "Session cannot be null";

            if (log.isErrorEnabled()) {
                log.error(msg);
            }

            throw new NullPointerException(msg);
        }

        if (!sess.isConnected()) {
            String msg = "Session is disconnected";

            if (log.isErrorEnabled()) {
                log.error(msg);
            }

            throw new RuntimeException(msg);
        }

        if (!sess.isOpen()) {
            String msg = "Session cannot be null";

            if (log.isErrorEnabled()) {
                log.error(msg);
            }

            throw new RuntimeException(msg);
        }
    }

    public Transaction beginTransaction() throws HibernateException {
        assertSession();

        return sess.beginTransaction();
    }

    public void rollback(Transaction tx) throws HibernateException {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
            } catch (HibernateException ex) {
                if (log != null) {
                    if (log.isErrorEnabled()) {
                        log.error("Error rolling back transaction", ex);
                    }
                }
                throw ex;
            }
        }
    }

    public Query createQuery(String queryString) throws HibernateException {
        assertSession();

        return sess.createQuery(queryString);
    }

    public int delete(Query query, Transaction tx) throws HibernateException {
        boolean commit = false;

        if (tx == null) {
            assertSession();
            tx = sess.beginTransaction();
            commit = true;
        }

        int res;

        try {
            res = query.executeUpdate();

            if (commit) {
                tx.commit();
            }
        } catch (RuntimeException re) {
            if (commit) {
                rollback(tx);
            }
            // throw again the first exception
            throw re;
        }

        return res;
    }

    public void save(Object instance, Transaction tx) throws HibernateException {

        boolean commit = false;

        if (tx == null) {
            assertSession();
            tx = sess.beginTransaction();
            commit = true;
        }

        try {
            sess.saveOrUpdate(instance);

            if (commit) {
                tx.commit();
            }
        } catch (RuntimeException re) {
            if (commit) {
                rollback(tx);
            }
            // throw again the first exception
            throw re;
        }
    }

    public void delete(Object instance, Transaction tx) throws HibernateException {

        boolean commit = false;

        if (tx == null) {
            assertSession();
            tx = sess.beginTransaction();
            commit = true;
        }

        try {
            sess.delete(instance);

            if (commit) {
                tx.commit();
            }
        } catch (RuntimeException re) {
            if (commit) {
                rollback(tx);
            }
            // throw again the first exception
            throw re;
        }
    }

    public void refresh(Object instance) {
        assertSession();

        sess.refresh(instance);
    }

    public void clearCache() {
        assertSession();

        sess.clear();
    }

    public void evictClass(Class<?> clazz) {
        assertSession();

        sess.getSessionFactory().evict(clazz);
    }

    public void destroy() {
        sess.clear();

        sess = null;
    }

    public void evict(Object instance) throws HibernateException {
        sess.evict(instance);
    }

    public Query getNamedQuery(String queryName) throws HibernateException {
        return sess.getNamedQuery(queryName);
    }

    public void replicate(Object instane) {
        sess.replicate(instane, ReplicationMode.OVERWRITE);
    }
}
