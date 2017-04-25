package ee.adit.dhx.api.container;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.ValidityException;

import ee.adit.dhx.api.ml.Util;

public class AccessRightsBeginDateHandler implements FieldHandler {
    private static final String FORMAT = "yyyy-MM-dd";

    /**
     * Creates a new AccessRightsBeginDateHandler instance.
     */
    public AccessRightsBeginDateHandler() {
        super();
    }

    /**
     * Returns the value of the field from the object.
     *
     * @param object
     *      The object
     * @return
     *      The value of the field
     * @throws IllegalStateException
     *      The Java object has changed and is no longer supported by
     *      this handler, or the handler is not compatible with the Java object
     */
    public Object getValue(Object object) throws IllegalStateException {
        AccessRights accessRights = (AccessRights) object;
        Date value = (Date) accessRights.getBeginDate();
        if (value == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);
        return formatter.format(value);
    }

    /**
     * Sets the value of the field on the object.
     *
     * @param object
     *      The object
     * @param value
     *      The new value
     * @throws IllegalStateException
     *      The Java object has changed and is no longer supported by
     *      this handler, or the handler is not compatible with the Java object
     * @throws IllegalArgumentException
     *      The value passed is not of a supported type
     */
    public void setValue(Object object, Object value) throws IllegalStateException, IllegalArgumentException {
        ((AccessRights) object).setBeginDate(Util.parseDate((String) value));
    }

    /**
     * Creates a new instance of the object described by this field.
     *
     * @param parent
     *      The object for which the field is created
     * @return
     *      A new instance of the field's value
     * @throws IllegalStateException
     *      This field is a simple type and cannot be instantiated
     */
    public Object newInstance(Object parent) throws IllegalStateException {
        // -- Since it's marked as a string...just return null,
        // -- it's not needed.
        return null;
    }

    /**
     * Sets the value of the field to a default value.
     *
     * Reference fields are set to null, primitive fields are set to their
     * default value, collection fields are emptied of all elements.
     *
     * @param object
     *      The object
     * @throws IllegalStateException
     *      The Java object has changed and is no longer supported by
     *      this handler, or the handler is not compatible with the Java object
     */
    public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
        ((AccessRights) object).setBeginDate(null);
    }

    /**
     * @deprecated No longer supported
     */
    public void checkValidity(Object object) throws ValidityException, IllegalStateException {
        // do nothing
    }
}
