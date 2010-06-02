package ee.adit.dao;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ee.adit.dao.pojo.DocumentType;

public class DocumentTypeDAO {

	private HibernateTemplate hibernateTemplate;

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}
	
	public Collection<DocumentType> listDocumentTypes() {
		return this.getHibernateTemplate().find("from DocumentType documentType");
	}
	
}
