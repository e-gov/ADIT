package ee.adit.dao;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditInternalException;
import ee.adit.service.DocumentService;

public class DocumentSharingDAO extends HibernateDaoSupport{

	private static Logger LOG = Logger.getLogger(DocumentSharingDAO.class);
	
	public Long save(DocumentSharing documentSharing) {
		return (Long) this.getHibernateTemplate().save(documentSharing);
	}
	
	public void update(DocumentSharing documentSharing) throws AditInternalException {
		Session session = null;
		Transaction transaction = null;
		try {
			
			session = this.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			session.saveOrUpdate(documentSharing);
			transaction.commit();
			
		} catch (Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			throw new AditInternalException("Error while updating DocumentSharing: ", e);
		} finally {
			if(session != null) {
				session.close();
			}
		}		
	}
	
	public List<DocumentSharing> getDVKSharings(Long documentID) {
		String SQL = "from DocumentSharing where documentId = " + documentID + " and documentSharingType = '" + DocumentService.SharingType_SendDvk + "'";
		return this.getSessionFactory().openSession().createQuery(SQL).list();
	}
	
}
