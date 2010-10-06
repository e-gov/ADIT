package ee.adit.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.DocumentHistory;

public class DocumentHistoryDAO extends HibernateDaoSupport {
	
	private static Logger LOG = Logger.getLogger(DocumentHistoryDAO.class);
	
	public Long save(DocumentHistory documentHistory) {
		return (Long) this.getHibernateTemplate().save(documentHistory);
	}
	
	@SuppressWarnings("unchecked")
	public List<DocumentHistory> getSortedList(Long documentID) {
		String SQL = "from DocumentHistory where documentId = " + documentID + " order by eventDate";
		return this.getSessionFactory().openSession().createQuery(SQL).list();
	}
	
}
