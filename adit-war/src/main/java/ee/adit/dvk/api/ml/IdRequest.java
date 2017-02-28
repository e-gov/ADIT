package ee.adit.dvk.api.ml;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;

public class IdRequest {
    private static final String IdRequestTemplate = "select %s as id from %s where %s = %s";
    private static final String IdRequestTemplateOfTypeText = "select %s as id from %s where %s = '%s'";

    public static BigDecimal getBigDecimalId(BigDecimal lookingId, String columnName, String pojoTypeName, Session sess) {
        String q = String.format(IdRequestTemplate, columnName, pojoTypeName, columnName, lookingId);

        return (BigDecimal) sess.createQuery(q).uniqueResult();
    }

    public static BigDecimal getBigDecimalId(String query, Session sess) {
        return (BigDecimal) sess.createQuery(query).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<BigDecimal> getBigDecimalIds(String columnName, String pojoTypeName, Session sess) {
        String q = String.format("select %s as id from %s", columnName, pojoTypeName);

        return sess.createQuery(q).list();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringIds(String columnName, String pojoTypeName, Session sess) {
        String q = String.format("select %s as id from %s", columnName, pojoTypeName);

        return sess.createQuery(q).list();
    }

    public static String getStringId(String lookingId, String columnName, String pojoTypeName, Session sess) {
        String q = String.format(IdRequestTemplateOfTypeText, columnName, pojoTypeName, columnName, lookingId);

        return (String) sess.createQuery(q).uniqueResult();
    }

    public static Long getLongId(Long lookingId, String columnName, String pojoTypeName, Session sess) {
        String q = String.format(IdRequestTemplate, columnName, pojoTypeName, columnName, lookingId);

        return (Long) sess.createQuery(q).uniqueResult();
    }

    public static BigDecimal getSubdivisionId(String whereClause, Session sess) {
        String query = String.format("select %s from %s where %s", "subdivisionCode", PojoSubdivision.PojoName, whereClause);
        return (BigDecimal) sess.createQuery(query).uniqueResult();
    }

	/*public static BigDecimal getOccupationId(String whereClause, Session sess, Object... params) {
        String strQuery = String.format("select %s from %s where ", "occupationCode", PojoOccupation.PojoName, whereClause);
		Query query = sess.createQuery(strQuery);

		if (params != null && params.length > 0) {
			UtilSql.setQueryParams(query, params);
		}

		return (BigDecimal) query.uniqueResult();
	}*/
}
