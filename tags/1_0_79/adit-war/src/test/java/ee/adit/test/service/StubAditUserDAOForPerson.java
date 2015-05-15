package ee.adit.test.service;

import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;
import ee.adit.service.UserService;

/**
 * @author Hendrik Pärna
 * @since 22.04.14
 */
public class StubAditUserDAOForPerson extends AditUserDAO {

    /**
     * Retrieves user by ID.
     *
     * @param userCode user code (ID)
     * @return user
     */
    public AditUser getUserByID(String userCode) {
        AditUser result = new AditUser();
        result.setFullName("Test User");
        result.setUserCode("EE36212240216");
        result.setDvkOrgCode("12345678");
        result.setUsertype(new Usertype(UserService.USERTYPE_PERSON));
        return result;
    }
}
