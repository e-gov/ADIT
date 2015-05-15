package ee.adit.test.service;

import ee.adit.dao.AditUserDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;
import ee.adit.service.UserService;

/**
 * @author Hendrik Pärna
 * @since 22.04.14
 */
public class StubAditUserDAOForOrg extends AditUserDAO {

    /**
     * Retrieves user by ID.
     *
     * @param userCode user code (ID)
     * @return user
     */
    public AditUser getUserByID(String userCode) {
        AditUser result = new AditUser();
        result.setFullName("MyComp OÜ");
        result.setDvkOrgCode("12345678");
        result.setUserCode("EE12345678");

        Usertype usertype = new Usertype();
        usertype.setShortName(UserService.USERTYPE_COMPANY);
        result.setUsertype(usertype);

        return result;
    }
}
