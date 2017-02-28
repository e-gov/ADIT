package ee.adit.dvk.api.ml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.hibernate.Session;
import org.hibernate.TypeMismatchException;
import org.w3c.dom.Document;

import ee.adit.dvk.api.DVKAPI;
import ee.adit.dvk.api.DVKConstants;

public class Util {
    private static String[] longDateFormats;
    private static String[] shortDateFormats;
    public final static String NewLine = System.getProperty("line.separator");

    private static Logger LOG = LogManager.getLogger(Util.class);

    /**
     * Returns true if list is null or it's empty.
     *
     * @param list
     * @return
     */

    public static boolean isEmpty(List<?> list) {
        return list == null || list.size() == 0;
    }

    public static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    static String getFormatStringPattern(String[] valueNames) {
        return Util.combineFormatString(valueNames, "; ");
    }

    /**
     * Returns true if string is null or it's length is zero.
     *
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    /**
     * @param valueNames
     * @return
     */
    public static String combineFormatString(String[] valueNames, String delim) {
        if (valueNames == null || valueNames.length == 0) {
            return null;
        }

        String res = null;

        for (String s : valueNames) {
            if (res == null) {
                res = s + " = %s";
                continue;
            }

            res += delim + s + " = %s";
        }

        return res;
    }

    static Session ensureSession(Session sess, boolean mustBe) {
        if (sess == null) {
            sess = DVKAPI.getGlobalSession();
        }

        if (mustBe && sess == null) {
            if (sess == null) {
                throw new RuntimeException("DAO operation needs for session object which is absent");
            }
        }

        return sess;
    }

    public static void logInfo(Log log, String msg) {
        if (log != null && log.isInfoEnabled()) {
            log.info(msg);
        }
    }

    public static void logInfo(Log log, String msg, Exception ex) {
        if (log != null && log.isInfoEnabled()) {
            log.info(msg, ex);
        }
    }

    public static void logDebug(Log log, String msg) {
        if (log != null && log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    public static void logDebug(Log log, String msg, Exception ex) {
        if (log != null && log.isDebugEnabled()) {
            log.debug(msg, ex);
        }
    }

    public static void logError(Log log, String msg) {
        if (log != null && log.isErrorEnabled()) {
            log.error(msg);
        }
    }

    public static void logError(Log log, String msg, Exception ex) {
        if (log != null && log.isErrorEnabled()) {
            log.error(msg, ex);
        }
    }

    public static boolean hasSameValue(String a, String b) {
        return ((a == b) || ((a != null) && a.equals(b)));
    }
    
    public static boolean hasSameValue(Date a, Date b) {
        return ((a == b) || ((a != null) && a.equals(b)));
    }

    public static boolean hasSameValue(Long a, Long b) {
        return ((a == b) || ((a != null) && a.equals(b)));
    }

    public static boolean hasSameValue(Boolean a, Boolean b) {
        return ((a == b) || ((a != null) && a.equals(b)));
    }

    public static boolean hasSameValue(BigDecimal a, BigDecimal b) {
        return ((a == b) || ((a != null) && a.equals(b)));
    }

    public static Object findOtherEqualDvkContent(List<?> list, Object elem) {
        int sz = list.size();

        if (sz == 0) {
            return null;
        }

        for (int i = 0; i < sz; ++i) {
            Object e = list.get(i);
            if (e == elem) {
                continue;
            }

            if (e.equals(elem)) {
                return e;
            }
        }

        return null;
    }

    public static String combineFields(List<String> names, List<Object> values) {

        for (int i = 0, j = values.size(); i < j; i++) {
            try {
                Object value = values.get(i);

                if (value == null) {
                    values.set(i, "null");
                } else if (value instanceof Collection<?>) {
                    values.set(i, "Collection");
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        int i = 0;
        StringBuffer sb = new StringBuffer();

        for (Object v : values) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(names.get(i));
            sb.append("=");
            sb.append(v);

            ++i;
        }

        return sb.toString();
    }

    public static String getDump(Object obj) {
        if (obj == null) {
            return null;
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        List<Object> values = new ArrayList<Object>(fields.length);
        List<String> names = new ArrayList<String>(fields.length);

        readClassFields(obj.getClass(), obj, names, values);

        return Util.combineFields(names, values);
    }

    private static void readClassFields(Class<?> clazz, Object obj, List<String> names, List<Object> values) {
        Field[] fields = clazz.getDeclaredFields();

        for (Field fld : fields) {
            try {
                fld.setAccessible(true);
                names.add(fld.getName());
                values.add(fld.get(obj));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        Class<?> superclass = clazz.getSuperclass();

        if (superclass != null) {
            if (Object.class.equals(superclass)) {
                return;
            }

            readClassFields(superclass, obj, names, values);
        }
    }

    public static void copyValues(Object src, Object target) {
        if (src == null || target == null) {
            return;
        }

        Class<?> clazz = src.getClass();

        if (!clazz.equals(target.getClass())) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field fld : fields) {
            try {
                if (!Modifier.isFinal(fld.getModifiers())) {
                    fld.setAccessible(true);
                    fld.set(target, fld.get(src));
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static BigDecimal getBigDecimal(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof String) {
            // String
            return new BigDecimal((String) value);
        } else if (value instanceof Number) {
            if (value instanceof Long) {
                // Long
                return new BigDecimal((Long) value);
            } else if (value instanceof Integer) {
                // Integer
                return new BigDecimal((Integer) value);
            } else if (value instanceof BigInteger) {
                // BigInteger
                return new BigDecimal((BigInteger) value);
            }
        }

        throw new TypeMismatchException("Caanot convert value of " + value.getClass().getName() + " to " + BigDecimal.class.getName());
    }

    public static Long getLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof String) {
            // String
            return Long.valueOf((String) value);
        } else if (value instanceof Number) {
            if (value instanceof Integer) {
                // Integer
                return (long) ((Integer) value).intValue();
            }
        }

        throw new TypeMismatchException("Caanot convert value of " + value.getClass().getName() + " to " + Long.class.getName());
    }

    public static Date parseDate(String strDate) {
        if (Util.isEmpty(strDate)) {
            return null;
        }

        String[] formats;
        SimpleDateFormat df = new SimpleDateFormat();
        df.setLenient(false);

        int i = 0;

        if (strDate.contains("T")) {
            if (longDateFormats == null) {
                longDateFormats = formats = new String[6];
                formats[i++] = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
                formats[i++] = "yyyy-MM-dd'T'HH:mm:ss.SSZ";
                formats[i++] = "yyyy-MM-dd'T'HH:mm:ss.SSS";
                formats[i++] = "yyyy-MM-dd'T'HH:mm:ss.SZ";
                formats[i++] = "yyyy-MM-dd'T'HH:mm:ssZ";
                formats[i++] = "yyyy-MM-dd'T'HH:mm:ss";
            } else {
                formats = longDateFormats;
            }
        } else {
            if (shortDateFormats == null) {
                shortDateFormats = formats = new String[2];
                formats[i++] = "yyyy-MM-ddZ";
                formats[i++] = "yyyy-MM-dd";
            } else {
                formats = shortDateFormats;
            }
        }

        for (String s : formats) {
            df.applyPattern(s);

            try {
                return df.parse(strDate);
            } catch (ParseException e) {
                continue;
            }
        }

        System.out.println("Warning: unexpected date format for value: " + strDate);
        // throw new RuntimeException("Unexpected date format for value: " + strDate);

        return null;
    }

    public static String readFileContent(String filePath) {
        StringBuilder contents = new StringBuilder();

        try {
            File file = new File(filePath);

            if (!file.exists()) {
                throw new FileNotFoundException();
            }

            InputStream is = new BufferedInputStream(new FileInputStream(file));
            Reader reader = new InputStreamReader(is, DVKConstants.CHARSET_UTF8);
            BufferedReader input = new BufferedReader(reader);// new FileReader(file)

            try {
                String line = null;

                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(NewLine);
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();
    }

    public static void writeFileContent(String filePath, String text) throws IOException {
        FileOutputStream stream = new FileOutputStream(filePath);
        OutputStreamWriter out = new OutputStreamWriter(stream, DVKConstants.CHARSET_UTF8);

        try {
            out.write(text);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } finally {
            out.close();
        }
    }

    public static String zipToString(String zip) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(zip));
        ZipInputStream zipIn = new ZipInputStream(in);
        zipIn.getNextEntry();

        byte[] buffer = new byte[512];

        int len;
        StringBuffer sb_result = new StringBuffer();

        while ((len = zipIn.read(buffer)) > 0) {
            sb_result.append(new String(buffer, 0, len));
        }

        zipIn.closeEntry();
        zipIn.close();
        String result = sb_result.toString();
        return result;
    }

    public static void zipFiles() {
        // These are the files to include in the ZIP file
        String[] filenames = new String[]{"filename1", "filename2"};

        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        try {
            // Create the ZIP file
            String outFilename = "outfile.zip";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));

            // Compress the files
            for (int i = 0; i < filenames.length; i++) {
                FileInputStream in = new FileInputStream(filenames[i]);

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(filenames[i]));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
        }
    }

    public static void zipFiles(String[] files, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];

        ZipOutputStream zipOut = new ZipOutputStream(out);

        // Compress the files
        for (String path : files) {
            FileInputStream in = new FileInputStream(path);
            String fileName = (new File(path)).getName();
            zipOut.putNextEntry(new ZipEntry(fileName));

            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                zipOut.write(buf, 0, len);
            }

            // Complete the entry
            zipOut.closeEntry();
            in.close();
        }

        // Complete the ZIP file
        zipOut.close();
    }

    public static String transformXml2String(Document doc) {
        StringWriter writer = null;

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            writer = new StringWriter();
            Result result = new StreamResult(writer);
            Source source = new DOMSource(doc);
            transformer.transform(source, result);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    public static void decodeStream(BufferedReader in, OutputStream out) throws IOException {
        while (true) {
            String s = in.readLine();
            if (s == null)
                break;
            byte[] buf = Base64Coder.decode(s);
            out.write(buf);
        }
    }

    public static void encodeStream(InputStream in, BufferedWriter out) throws IOException {
        int lineLength = 72;
        byte[] buf = new byte[lineLength / 4 * 3];
        while (true) {
            int len = in.read(buf);
            if (len <= 0)
                break;
            out.write(Base64Coder.encode(buf, len));
            out.write("\n");
            //out.newLine();
        }
    }

    public static String zipAndEncode(String filePath) {

        LOG.debug("Encoding file: " + filePath);

        try {
            ByteArrayOutputStream zipOut = new ByteArrayOutputStream();

            Util.zipFiles(new String[]{filePath}, zipOut);
            zipOut.close();

            BufferedInputStream encodeIn = new BufferedInputStream(new ByteArrayInputStream(zipOut.toByteArray()));
            StringWriter sw = new StringWriter();
            BufferedWriter writer = new BufferedWriter(sw);

            Util.encodeStream(encodeIn, writer);
            writer.flush();

            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String decodeAndUnzip(String strEncodedAndZipped) {
        try {
            StringReader reader = new StringReader(strEncodedAndZipped);
            BufferedReader buffReader = new BufferedReader(reader);

            ByteArrayOutputStream base64Out = new ByteArrayOutputStream();
            Util.decodeStream(buffReader, base64Out);

            ZipInputStream is = new ZipInputStream(new ByteArrayInputStream(base64Out.toByteArray()));

            String result = null;

            while (is.getNextEntry() != null) {
                if (result != null) {
                    throw new RuntimeException("Zip has more than one entry. Unexpected input.");
                }

                byte[] buffer = new byte[512];

                int len;
                StringBuffer buff = new StringBuffer();

                while ((len = is.read(buffer)) > 0) {
                    buff.append(new String(buffer, 0, len));
                }

                is.closeEntry();

                result = buff.toString();
            }

            is.close();

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
