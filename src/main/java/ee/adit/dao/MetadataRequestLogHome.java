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
 * Home object for domain model class MetadataRequestLog.
 * @see ee.adit.dao.MetadataRequestLog
 * @author Hibernate Tools
 */
public class MetadataRequestLogHome {

	private static final Log log = LogFactory
			.getLog(MetadataRequestLogHome.class);

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

	public void persist(MetadataRequestLog transientInstance) {
		log.debug("persisting MetadataRequestLog instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(MetadataRequestLog instance) {
		log.debug("attaching dirty MetadataRequestLog instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MetadataRequestLog instance) {
		log.debug("attaching clean MetadataRequestLog instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(MetadataRequestLog persistentInstance) {
		log.debug("deleting MetadataRequestLog instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MetadataRequestLog merge(MetadataRequestLog detachedInstance) {
		log.debug("merging MetadataRequestLog instance");
		try {
			MetadataRequestLog result = (MetadataRequestLog) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public MetadataRequestLog findById(long id) {
		log.debug("getting MetadataRequestLog instance with id: " + id);
		try {
			MetadataRequestLog instance = (MetadataRequestLog) sessionFactory
					.getCurrentSession().get("ee.adit.dao.MetadataRequestLog",
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

	public List findByExample(MetadataRequestLog instance) {
		log.debug("finding MetadataRequestLog instance by example");
		try {
			List results = sessionFactory.getCurrentSession().createCriteria(
					"ee.adit.dao.MetadataRequestLog").add(
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
