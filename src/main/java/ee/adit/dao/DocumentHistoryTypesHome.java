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
 * Home object for domain model class DocumentHistoryTypes.
 * @see ee.adit.dao.DocumentHistoryTypes
 * @author Hibernate Tools
 */
public class DocumentHistoryTypesHome {

	private static final Log log = LogFactory
			.getLog(DocumentHistoryTypesHome.class);

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

	public void persist(DocumentHistoryTypes transientInstance) {
		log.debug("persisting DocumentHistoryTypes instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(DocumentHistoryTypes instance) {
		log.debug("attaching dirty DocumentHistoryTypes instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(DocumentHistoryTypes instance) {
		log.debug("attaching clean DocumentHistoryTypes instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(DocumentHistoryTypes persistentInstance) {
		log.debug("deleting DocumentHistoryTypes instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public DocumentHistoryTypes merge(DocumentHistoryTypes detachedInstance) {
		log.debug("merging DocumentHistoryTypes instance");
		try {
			DocumentHistoryTypes result = (DocumentHistoryTypes) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public DocumentHistoryTypes findById(long id) {
		log.debug("getting DocumentHistoryTypes instance with id: " + id);
		try {
			DocumentHistoryTypes instance = (DocumentHistoryTypes) sessionFactory
					.getCurrentSession().get(
							"ee.adit.dao.DocumentHistoryTypes", id);
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

	public List findByExample(DocumentHistoryTypes instance) {
		log.debug("finding DocumentHistoryTypes instance by example");
		try {
			List results = sessionFactory.getCurrentSession().createCriteria(
					"ee.adit.dao.DocumentHistoryTypes").add(
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
