package ee.adit.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.RemoteApplication;

public class RemoteApplicationDAO extends HibernateDaoSupport {

    public RemoteApplication getByShortName(String remoteApplicationShortName) {
        return (RemoteApplication) this.getHibernateTemplate().get(RemoteApplication.class, remoteApplicationShortName);
    }

}
