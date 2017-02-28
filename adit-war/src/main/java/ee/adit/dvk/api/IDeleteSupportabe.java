package ee.adit.dvk.api;

import org.hibernate.HibernateException;

/**
 * @author User
 *         Interface for DVK entries which intend to provide delete facility.
 */
public interface IDeleteSupportabe {
    /**
     * Deletes the entry from the data storage.
     *
     * @throws HibernateException exception
     */
    void delete() throws HibernateException;
}
