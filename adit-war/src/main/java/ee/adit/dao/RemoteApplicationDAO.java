package ee.adit.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.RemoteApplication;

/**
 * Remote application data access class. Provides methods for retrieving and manipulating
 * remote application data.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class RemoteApplicationDAO extends HibernateDaoSupport {

    /**
     * Fetch remote application by short name.
     * 
     * @param remoteApplicationShortName remote application short name
     * @return remote application
     */
    public RemoteApplication getByShortName(String remoteApplicationShortName) {
        return (RemoteApplication) this.getHibernateTemplate().get(RemoteApplication.class, remoteApplicationShortName);
    }

}
