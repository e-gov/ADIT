package ee.adit.dao;

// Generated 21.05.2010 14:01:23 by Hibernate Tools 3.2.4.GA

import java.util.List;
import javax.naming.InitialContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class Usertypes.
 * @see ee.adit.dao.Usertypes
 * @author Hibernate Tools
 */
public class UsertypesHome {

	private static final Log log = LogFactory.getLog(UsertypesHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(Usertypes transientInstance) {
		log.debug("persisting Usertypes instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(Usertypes instance) {
		log.debug("attaching dirty Usertypes instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(Usertypes instance) {
		log.debug("attaching clean Usertypes instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(Usertypes persistentInstance) {
		log.debug("deleting Usertypes instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public Usertypes merge(Usertypes detachedInstance) {
		log.debug("merging Usertypes instance");
		try {
			Usertypes result = (Usertypes) sessionFactory.getCurrentSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public Usertypes findById(java.lang.String id) {
		log.debug("getting Usertypes instance with id: " + id);
		try {
			Usertypes instance = (Usertypes) sessionFactory.getCurrentSession()
					.get("ee.adit.dao.Usertypes", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(Usertypes instance) {
		log.debug("finding Usertypes instance by example");
		try {
			List results = sessionFactory.getCurrentSession().createCriteria(
					"ee.adit.dao.Usertypes").add(Example.create(instance))
					.list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
