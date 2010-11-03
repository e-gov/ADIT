package ee.adit.dao;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentHistory;
import ee.adit.exception.AditInternalException;

public class DocumentHistoryDAO extends HibernateDaoSupport {
	
	private static Logger LOG = Logger.getLogger(DocumentHistoryDAO.class);
	
	public Long save(DocumentHistory documentHistory) {
		Long result = null;
		Session session = null;
		Transaction transaction = null;
		try {
			
			session = this.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			result = (Long) session.save(documentHistory);
			transaction.commit();
			
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			throw new AditInternalException("Error while saving DocumentHistory: ", e);
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
		//return (Long) this.getHibernateTemplate().save(documentHistory);
	}
	
	@SuppressWarnings("unchecked")
	public List<DocumentHistory> getSortedList(Long documentID) {
		String SQL = "from DocumentHistory where documentId = " + documentID + " order by eventDate";
		
		List<DocumentHistory> result = null;
		Session session = null;
		Transaction transaction = null;
		try {
			
			session = this.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			result = session.createQuery(SQL).list();
			transaction.commit();
			
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			throw new AditInternalException("Error while fetching DocumentHistory list: ", e);
		} finally {
			if(session != null)
				session.close();
		}
		
		return result;
	}
	
}
