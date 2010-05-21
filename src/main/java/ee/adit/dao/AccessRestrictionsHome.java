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
 * Home object for domain model class AccessRestrictions.
 * @see ee.adit.dao.AccessRestrictions
 * @author Hibernate Tools
 */
public class AccessRestrictionsHome {

	private static final Log log = LogFactory
			.getLog(AccessRestrictionsHome.class);

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

	public void persist(AccessRestrictions transientInstance) {
		log.debug("persisting AccessRestrictions instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(AccessRestrictions instance) {
		log.debug("attaching dirty AccessRestrictions instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(AccessRestrictions instance) {
		log.debug("attaching clean AccessRestrictions instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(AccessRestrictions persistentInstance) {
		log.debug("deleting AccessRestrictions instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public AccessRestrictions merge(AccessRestrictions detachedInstance) {
		log.debug("merging AccessRestrictions instance");
		try {
			AccessRestrictions result = (AccessRestrictions) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public AccessRestrictions findById(ee.adit.dao.AccessRestrictionsId id) {
		log.debug("getting AccessRestrictions instance with id: " + id);
		try {
			AccessRestrictions instance = (AccessRestrictions) sessionFactory
					.getCurrentSession().get("ee.adit.dao.AccessRestrictions",
							id);
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

	public List findByExample(AccessRestrictions instance) {
		log.debug("finding AccessRestrictions instance by example");
		try {
			List results = sessionFactory.getCurrentSession().createCriteria(
					"ee.adit.dao.AccessRestrictions").add(
					Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
