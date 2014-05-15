package ee.adit.integrationtests;

import dvk.api.ml.PojoMessage;
import ee.adit.dao.dvk.DvkDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * @author Hendrik Pärna
 * @since 15.05.14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:integration-tests.xml"})
public class AppSetupTest {
    @Autowired
    private DvkDAO dvkDAO;

    @Test
    public void justTestAppContextSetup() throws Exception {
        Assert.notNull(dvkDAO);
        Assert.notNull(dvkDAO.getHibernateTemplate());
        Assert.notNull(dvkDAO.getSessionFactory());

        PojoMessage message = dvkDAO.getMessage(81);
    }

}
