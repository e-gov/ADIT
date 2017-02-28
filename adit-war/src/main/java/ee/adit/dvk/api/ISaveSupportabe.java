package ee.adit.dvk.api;

import org.hibernate.HibernateException;

/**
 * @author User
 *         Interface for DVK entries ensuring that this entry has capability to insert a new
 *         or save changes in an existing record from the table this entry is mapped to.
 */
public interface ISaveSupportabe {
    /**
     * Inserts a new or saves changes in an existing record and in case of success, sets entry's
     * state to persistent one and resets isDirty flag. If this entry is IOrganization or ISetting
     * that may contain pending descendants with changes will save them too.
     *
     * @throws HibernateException
     */
    void save() throws HibernateException;
}
