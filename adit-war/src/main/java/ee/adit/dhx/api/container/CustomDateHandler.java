package ee.adit.dhx.api.container;

import java.lang.reflect.Field;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.exolab.castor.mapping.ConfigurableFieldHandler;
import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.ValidityException;

import ee.adit.dhx.api.ml.Util;

/**
 * The FieldHandler for the Date class
 *
 */
public class CustomDateHandler implements FieldHandler, ConfigurableFieldHandler
{
	/**
	 * Creates a new MyDateHandler instance
	 */
	private String fieldName;
	private String info;

	public CustomDateHandler() {
		super();
	}

	public void setConfiguration(Properties config) throws ValidityException {
		fieldName = config.getProperty("fieldName");

		if (fieldName == null) {
			throwException("Required parameter \"fieldName\" is missing for CustomDateHandler.", null);
		}

		info = config.getProperty("info");

		if (info == null) {
			throwException("Required parameter \"info\" is missing for CustomDateHandler.", null);
		}
	}

	/**
	 * Returns the value of the field from the object.
	 *
	 * @param object
	 *          The object
	 * @return The value of the field
	 * @throws IllegalStateException
	 *           The Java object has changed and is no longer supported by this handler, or the
	 *           handler is not compatible with the Java object
	 */
	public Object getValue(Object object) throws IllegalStateException {
		Field field = null;
		Class<?> clazz = object.getClass();

		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (SecurityException e) {
			throw e;
		} catch (NoSuchFieldException e) {
			String msg = "CustomDateHandler did not find field \"" + fieldName + "\" in class \"" + clazz.getName() + "\"";
			throwException(msg, e);
		}

		Date date = null;

		try {
			field.setAccessible(true);
			date = (Date) field.get(object);
		} catch (Exception e) {
			throwException(null, e);
		}

		if (date == null) {
			return null;
		}

		SimpleDateFormat formatter = new SimpleDateFormat();
		return formatter.format(date);
	}

	/**
	 * Sets the value of the field on the object.
	 *
	 * @param object
	 *          The object
	 * @param value
	 *          The new value
	 * @throws IllegalStateException
	 *           The Java object has changed and is no longer supported by this handler, or the
	 *           handler is not compatible with the Java object
	 * @throws IllegalArgumentException
	 *           The value passed is not of a supported type
	 */
	public void setValue(Object object, Object value) throws IllegalStateException, IllegalArgumentException {
		Field field = null;
		Class<?> clazz = object.getClass();

		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (SecurityException e) {
			throw e;
		} catch (NoSuchFieldException e) {
			String msg = "CustomDateHandler did not find field \"" + fieldName + "\" in class \"" + clazz.getName() + "\"";
			throwException(msg, e);
		}

		try {
			field.setAccessible(true);
			field.set(object, Util.parseDate((String) value));
		} catch (Exception e) {
			throwException(null, e);
		}
	}

	private void throwException(String msg, Exception ex) {
		if (msg == null) {
			msg = info;
		} else {
			msg = info + "\n" + msg;
		}

		throw (ex == null) ? new RuntimeException(msg) : new RuntimeException(msg, ex);
	}

	/**
	 * Creates a new instance of the object described by this field.
	 *
	 * @param parent
	 *          The object for which the field is created
	 * @return A new instance of the field's value
	 * @throws IllegalStateException
	 *           This field is a simple type and cannot be instantiated
	 */
	public Object newInstance(Object parent) throws IllegalStateException {
		// -- Since it's marked as a string...just return null,
		// -- it's not needed.
		return null;
	}

	/**
	 * Sets the value of the field to a default value.
	 *
	 * Reference fields are set to null, primitive fields are set to their default value, collection
	 * fields are emptied of all elements.
	 *
	 * @param object
	 *          The object
	 * @throws IllegalStateException
	 *           The Java object has changed and is no longer supported by this handler, or the
	 *           handler is not compatible with the Java object
	 */
	public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
		LetterMetaData letter = (LetterMetaData) object;
		letter.setSignDate(null);
	}

	/**
	 * @deprecated No longer supported
	 */
	@Deprecated
	public void checkValidity(Object object) throws ValidityException, IllegalStateException {
		// do nothing
	}

}
