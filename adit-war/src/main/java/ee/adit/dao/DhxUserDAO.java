package ee.adit.dao;

import java.util.ArrayList;



import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Service;

import ee.adit.dao.pojo.Document;
import ee.adit.dhx.AditDhxConfig;
import ee.adit.dao.pojo.DhxUser;
import ee.adit.service.DocumentService;
import ee.ria.dhx.util.StringUtil;

/**
 * DHX data access class. Provides methods for manipulating data in DHX client
 * database.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 */
public class DhxUserDAO extends HibernateDaoSupport {

	private static Logger logger = LogManager.getLogger(DhxUserDAO.class);

	/**
	 * Session factory.
	 */
	private SessionFactory sessionFactory;
	
	@Autowired
	private AditDhxConfig dhxConfig;


	/**
	 * Updates organisation.
	 *
	 * @param organisation
	 *            organisation
	 * @throws Exception
	 */
	public void updateOrganisation(DhxUser organisation) throws Exception {
		logger.info("Updating DHX organisation. id:" + organisation.getDhxUserId());
		logger.info("DHX organisation ID: " + organisation.getCode());
		this.getHibernateTemplate().saveOrUpdate(organisation);
	}

	/**
	 * Updates organisation.
	 *
	 * @param organisation
	 *            organisation
	 * @throws Exception
	 */
	public void updateOrganisations(List<DhxUser> organisations) throws Exception {
		logger.info("Updating DHX organisations");
		if (organisations != null && organisations.size() > 0) {
			for (DhxUser org : organisations) {
				updateOrganisation(org);
			}
		}
	}

	

	/**
	 * Retrieve organisation by code and subsystem.
	 *
	 * @return found organisation.
	 */
	public DhxUser getOrganisationByCodeAndSubsystem(String code, String subsystem, String subsystemPrefix) {
		DhxUser result = null;
		if(StringUtil.isNullOrEmpty(subsystem)) {
			subsystem = subsystemPrefix;
		}
		String sql = "from DhxUser org where org.orgCode ='" + code + "' and COALESCE(org.subSystem, '" + subsystemPrefix + "')='" + subsystem + "'";

		Session session = null;
		Transaction transaction = null;
		try {
			session = this.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			Query query = session.createQuery(sql);
			//query.setParameter("code", code);
			//query.setParameter("subsystem", subsystem);
			result = (DhxUser) query.uniqueResult();
			session.getTransaction().commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}

			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return result;
	}
	
	
	/**
	 * Retrieve organisation by code and subsystem.
	 *
	 * @return found organisation.
	 */
	public DhxUser getOrganisationByIdentificator(String identificator) {
		DhxUser result = null;
		String sql = "from DhxUser org where org.organisationIdentificator ='" + identificator + "'";

		Session session = null;
		Transaction transaction = null;
		try {
			session = this.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			Query query = session.createQuery(sql);
			result = (DhxUser) query.uniqueResult();
			session.getTransaction().commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}

			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return result;
	}

	/**
	 * Fetches organisation by ID.
	 *
	 * @param id
	 *            organisation ID
	 * @return organisation
	 */
	public DhxUser getOrganisation(long id) {
		logger.debug("Attempting to load organisation from database. organisation id: " + String.valueOf(id));
		return (DhxUser) this.getHibernateTemplate().get(DhxUser.class, id);
	}
	
		
	

	/**
	 * Retrieves DHX users list.
	 *
	 * @return list of users.
	 */
	@SuppressWarnings("unchecked")
	public List<DhxUser> getUsers() {
		List<DhxUser> result = new ArrayList<DhxUser>();
		String sql = "from DhxUser where active = true and organisationIdentificator is not null";

		logger.debug("Fetching organizations...");

		Session session = null;
		Transaction transaction = null;
		try {
			session = this.getSessionFactory().openSession();
			transaction = session.beginTransaction();

			result = session.createQuery(sql).list();

			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}

			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return result;
	}

}
