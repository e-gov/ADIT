package ee.adit.dvk.api;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.adit.dvk.api.ml.Util;

/**
 * @author User
 *         Class that simplifies getting of where clause when an HQL query is composing.
 */
public abstract class SelectCriteria {
    /**
     * The Hibernate date format
     */
    public static final String HIBERNATE_DATE_FORMAT = "dd MMMM yyyy";

    /**
     * A special field to hold already complex condition which will
     * be append to the resulting where clause.
     */
    private String customCondition;

    /**
     * Formats the date in Hibernate format.
     *
     * @param date
     * @return formatted string
     */
    public static final String formatDate(final Date date) {
        return new SimpleDateFormat(HIBERNATE_DATE_FORMAT).format(date);
    }

    /**
     * Returns complex where clause, combined from fields which values aren't null values.
     *
     * @param list pairs of filed name / field value
     * @return where clause
     */
    protected String getWhereClause(List<?> list) {
        StringBuffer buff = new StringBuffer();

        for (int i = 0, j = list.size(); i < j; i += 2) {
            String fieldName = (String) list.get(i);
            Object value = list.get(i + 1);

            if (buff.length() > 0) {
                buff.append(" and ");
            }

            buff.append("(");
            buff.append(fieldName);

            if (value == null) {
                buff.append(" is null");
            } else if (value instanceof Date) {
                buff.append("='");
                buff.append(formatDate((Date) value));
                buff.append("'");
            } else if (value instanceof String) {
                buff.append("='");
                buff.append(value);
                buff.append("'");
            } else {
                buff.append("=");
                buff.append(value);
            }

            buff.append(")");
        }

        return buff.toString();
    }

    /**
     * Sets field value
     *
     * @param fieldName
     * @param value
     */
    public void setValue(String fieldName, Object value) {
        try {
            Field f = getClass().getDeclaredField(fieldName);

            if (f != null) {
                f.setAccessible(true);
                f.set(this, value);
            } else {
                throw new RuntimeException("Field " + fieldName + " not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Resets all values to null.
     */
    public void reset() {
        Field[] fields = getClass().getDeclaredFields();

        for (Field f : fields) {
            try {
                f.setAccessible(true);
                f.set(this, null);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns compound where clause of values provided by class-implementation and
     * custom condition field residing in this class.
     *
     * @return
     */
    public String getWhereClause() {
        List<Object> list = new ArrayList<Object>();

        Field[] fields = getClass().getDeclaredFields();

        for (Field f : fields) {
            try {
                f.setAccessible(true);
                Object value = f.get(this);

                if (value != null) {
                    list.add(f.getName());
                    list.add(value);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        try {
            return getWhereClause(list);
        } finally {
            list.clear();
        }
    }

    /**
     * Returns custom condition value.
     *
     * @return
     */
    public String getCustomCondition() {
        return customCondition;
    }

    /**
     * Sets custom condition value.
     *
     * @param condition
     * @param append
     */
    public void setCustomCondition(String condition, boolean append) {
        if (append) {
            if (Util.isEmpty(customCondition)) {
                customCondition = condition;
            } else {
                customCondition += " and " + condition;
            }
        } else {
            customCondition = condition;
        }
    }

}