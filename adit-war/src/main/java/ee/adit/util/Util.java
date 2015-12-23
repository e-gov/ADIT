package ee.adit.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.security.auth.x500.X500Principal;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.castor.core.util.Base64Decoder;
import org.castor.core.util.Base64Encoder;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Usertype;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.Message;
import ee.adit.pojo.PersonName;
import ee.adit.service.UserService;

/**
 * Class providing static utility / helper methods.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public final class Util {
	/**
	 * Default constructor.
	 */
	private Util() {
	}

    /**
     * X-Tee namespace URI.
     */
    public static final String XTEE_NAMESPACE = "http://x-tee.riik.ee/xsd/xtee.xsd";

    /**
     * Log4J logger.
     */
    private static Logger logger = Logger.getLogger(Util.class);

    /**
     * Default file extension for temporary files.
     */
    public static final String ADIT_FILE_EXTENSION = ".adit";

    /**
     * The length of the generated random ID.
     */
    public static final int RANDOM_ID_LENGTH = 30;

    
    public static final String DIGIDOC_STAMP_ISSUER = "KLASS3-SK";
    
    //extensions which are allowed to use
    public static final String[] BDOC_FILE_EXTENSIONS = {"bdoc", "asice", "sce"};
    public static final String BDOC_PRIMARY_EXTENSION = "bdoc";
    public static final String DDOC_FILE_EXTENSION = "ddoc";
    
    /**
     * Base64 encodes the specified string.
     *
     * @param string
     *            the string to be encoded
     * @return the base64 encoded string
     * @throws UnsupportedEncodingException
     */
    public static String base64encode(String string) throws UnsupportedEncodingException {
        return new String(Base64Encoder.encode(string.getBytes("UTF-8")));
    }

    /**
     * Base64 encodes the specified byte array.
     *
     * @param data
     *            byte array to be encoded
     * @return the base64 encoded string
     * @throws UnsupportedEncodingException
     */
    public static String base64encode(byte[] data) throws UnsupportedEncodingException {
        return new String(Base64Encoder.encode(data));
    }

    /**
     * Base64 decodes the specified string.
     *
     * @param string
     *            the string to be encoded
     * @return the base64 encoded string
     * @throws UnsupportedEncodingException
     */
    public static String base64decode(String string) throws UnsupportedEncodingException {
        return new String(Base64Decoder.decode(string));
    }

    /**
     * Base64 decodes the specified string.
     *
     * @param string
     *     the string to be encoded
     * @return
     *     base64 decoded bytes
     * @throws UnsupportedEncodingException
     */
    public static byte[] base64DecodeToByteArray(String string) throws UnsupportedEncodingException {
        return Base64Decoder.decode(string);
    }

    /**
     * Generates a random GUID / UUID.
     *
     * @return GUID
     */
    public static String generateGUID() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Generates a random file name with an extension.
     *
     * @return file name
     */
    public static String generateRandomFileName() {
        StringBuffer result = new StringBuffer();
        result.append(generateRandomFileNameWithoutExtension());
        result.append(ADIT_FILE_EXTENSION);
        return result.toString();
    }

    /**
     * Generate a random file name without an extension.
     *
     * @return file name
     */
    public static String generateRandomFileNameWithoutExtension() {
        return generateRandomID();
    }

    /**
     * Generates a random ID of length {@code RANDOM_ID_LENGTH}.
     *
     * @return the random ID as a string
     */
    public static String generateRandomID() {
        StringBuffer result = new StringBuffer();
        Random r = new Random();
        for (int i = 0; i < RANDOM_ID_LENGTH; i++) {
            result.append(r.nextInt(10));
        }
        return result.toString();
    }

    /**
     * Creates an empty temporary file to given folder.
     *
     * @param itemIndex
     *            Number of current file. Enables to give an index to all files
     *            related to same object etc.
     * @param extension
     *            File extension to be given to the temporary file.
     * @param filesFolder
     *            Folder path where the temporary file will be created.
     * @return Full name of created file (absolute path)
     */
    public static String createTemporaryFile(int itemIndex, String extension, String filesFolder) {
        try {
            if (extension == null) {
                extension = "";
            }
            if ((extension.length() > 0) && !extension.startsWith(".")) {
                extension = "." + extension;
            }

            String tmpDir = System.getProperty("java.io.tmpdir", "");
            File filesDir = new File(filesFolder);
            if (filesDir.exists() && filesDir.isDirectory()) {
                tmpDir = filesFolder;
            } else {
                logger.warn("Cannot find folder \"" + filesFolder
                        + "\". Using system temporary folder for temporary files instead.");
            }

            String result = tmpDir + File.separator + String.valueOf((new Date()).getTime())
                    + ((itemIndex > 0) ? "_item" + String.valueOf(itemIndex) : "") + extension;
            int uniqueCounter = 0;
            while ((new File(result)).exists()) {
                ++uniqueCounter;
                result = tmpDir + File.separator + String.valueOf((new Date()).getTime())
                        + ((itemIndex > 0) ? "_item" + String.valueOf(itemIndex) : "") + "_"
                        + String.valueOf(uniqueCounter) + extension;
            }
            File file = new File(result);
            file.createNewFile();
            return result;
        } catch (Exception ex) {
            logger.error("Failed creating a temporary file!", ex);
            return null;
        }
    }

    /**
     * GZip-s and then Base64 encodes the specified file.
     *
     * @param inputFile
     *            absolute path to the file to be processed
     * @param tempDir
     *            directory where to put the result and any temporary files
     * @param deleteTemporaryFiles
     *            parameter specifying if temporary files are to be deleted
     *            immediately after work is complete
     * @return absolute path to the result file
     * @throws IOException
     */
    public static String gzipAndBase64Encode(String inputFile, String tempDir, boolean deleteTemporaryFiles)
            throws IOException {
        String resultFileName = null;

        // Pack data to GZip format
        String zipOutFileName = inputFile + "_zipOutBuffer.adit";
        logger.debug("Packing data to GZip format. Input file: \"" + inputFile + "\", output file: \"" + zipOutFileName
                + "\"");
        FileInputStream in = new FileInputStream(inputFile);
        FileOutputStream zipOutFile = new FileOutputStream(zipOutFileName, false);
        GZIPOutputStream out = new GZIPOutputStream(zipOutFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            logger.debug(new String(buf, 0, len, "UTF-8"));
            out.write(buf, 0, len);
        }
        in.close();
        out.finish();
        out.close();
        logger.debug("GZip complete");

        // Encode the GZipped data to Base64 binary data
        resultFileName = inputFile + "_Base64OutBuffer.adit";
        logger.debug("Encoding zip file to Base64: Output file: " + zipOutFileName);
        in = new FileInputStream(zipOutFileName);
        FileOutputStream b64out = new FileOutputStream(resultFileName, false);
        Base64OutputStream b64outStream = new Base64OutputStream(b64out, true, 64, "\n".getBytes("UTF-8"));
        byte[] b = new byte[66000];
        while ((len = in.read(b)) > 0) {
            b64outStream.write(b, 0, len);
        }
        in.close();
        b64outStream.close();
        b64out.close();

        // Delete temporary files
        try {
            if (deleteTemporaryFiles) {
                File zipFile = new File(zipOutFileName);
                zipFile.delete();
                logger.debug("Deleted temporary file: " + zipOutFileName);
            }
        } catch (Exception e) {
            logger.error("Exception while deleting temporary files: ", e);
        }

        return resultFileName;
    }

    /**
     * GZip-s the specified input file.
     *
     * @param inputFile
     *            absolute path to the file to be processed
     * @param tempDir
     *            directory where to put the result and any temporary files
     * @return absolute path to the result file
     * @throws IOException
     */
    public static String gzipFile(String inputFile, String tempDir) throws IOException {
        FileInputStream in = null;
        FileOutputStream zipOutFile = null;
        GZIPOutputStream out = null;

        // Pack data to GZip format
        String zipOutFileName = inputFile + "_zipOutBuffer.adit";
        logger.debug("Packing data to GZip format. Input file: \"" + inputFile + "\", output file: \"" + zipOutFileName
                + "\"");
        try {
            in = new FileInputStream(inputFile);
            zipOutFile = new FileOutputStream(zipOutFileName, false);
            out = new GZIPOutputStream(zipOutFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                logger.debug(new String(buf, 0, len, "UTF-8"));
                out.write(buf, 0, len);
            }
            out.finish();
        } finally {
            safeCloseStream(out);
            safeCloseStream(zipOutFile);
            safeCloseStream(in);
            out = null;
            zipOutFile = null;
            in = null;
        }

        logger.debug("GZip complete");
        return zipOutFileName;
    }

    /**
     * Encodes file contents to Base64.
     *
     * @param inputFile
     *            absolute path to the file to be processed
     * @param tempDir
     *            directory where to put the result and any temporary files
     * @return absolute path to the result file
     * @throws IOException
     */
    public static String base64EncodeFile(String inputFile, String tempDir) throws IOException {
        String resultFileName = null;

        // Encode data to Base64 binary data
        resultFileName = inputFile + "_Base64OutBuffer.adit";
        logger.debug("Encoding file to Base64: Output file: " + inputFile);
        FileInputStream in = new FileInputStream(inputFile);
        FileOutputStream b64out = new FileOutputStream(resultFileName, false);
        Base64OutputStream b64outStream = new Base64OutputStream(b64out, true, 64, "\n".getBytes("UTF-8"));
        byte[] b = new byte[66000];
        int len = 0;
        while ((len = in.read(b)) > 0) {
            b64outStream.write(b, 0, len);
        }
        in.close();
        b64outStream.close();
        b64out.close();

        return resultFileName;
    }

    /**
     * Base64 decodes and Unzip-s the specified file.
     *
     * @param inputFile
     *            absolute path to the file to be processed
     * @param tempDir
     *            directory where to put the result and any temporary files
     * @param deleteTemporaryFiles
     *            parameter specifying if temporary files are to be deleted
     *            immediately after work is complete
     * @return absolute path to the result file
     * @throws IOException
     */
    public static String base64DecodeAndUnzip(String inputFile, String tempDir, boolean deleteTemporaryFiles)
            throws IOException {

        // Base64 decode
        String base64DecodedFile = inputFile + "_Base64DecodedOutBuffer.adit";
        FileInputStream inputFileStream = new FileInputStream(inputFile);
        FileOutputStream base64DecodedOut = new FileOutputStream(base64DecodedFile, false);
        Base64InputStream base64InputStream = new Base64InputStream(inputFileStream);

        int len;
        byte[] b = new byte[66000];
        while ((len = base64InputStream.read(b)) > 0) {
            base64DecodedOut.write(b, 0, len);
        }
        base64DecodedOut.close();
        base64InputStream.close();
        inputFileStream.close();

        // Unzip
        String unzipOutFileName = inputFile + "_unzipOutBuffer.adit";
        FileOutputStream unzipOutFileStream = new FileOutputStream(unzipOutFileName, false);
        FileInputStream gzipFileInputStream = new FileInputStream(base64DecodedFile);
        GZIPInputStream gzipInputStream = new GZIPInputStream(gzipFileInputStream);

        byte[] buf = new byte[1024];
        while ((len = gzipInputStream.read(buf)) > 0) {
            unzipOutFileStream.write(buf, 0, len);
        }
        unzipOutFileStream.close();
        gzipInputStream.close();
        gzipFileInputStream.close();

        // Delete temporary files
        try {
            if (deleteTemporaryFiles) {
                File zipFile = new File(base64DecodedFile);
                zipFile.delete();
                logger.debug("Deleted temporary file: " + base64DecodedFile);
            }
        } catch (Exception e) {
            logger.error("Exception while deleting temporary files: ", e);
        }

        return unzipOutFileName;
    }

    /**
     * Base64 decodes and Unzip-s the specified file.
     *
     * @param inputFile
     *            absolute path to the file to be processed
     * @param tempDir
     *            directory where to put the result and any temporary files
     * @return absolute path to the result file
     * @throws IOException
     */
    public static String unzip(String inputFile, String tempDir) throws IOException {
        String unzipOutFileName = inputFile + "_unzipOutBuffer.adit";
        FileOutputStream unzipOutFileStream = new FileOutputStream(unzipOutFileName, false);
        FileInputStream gzipFileInputStream = new FileInputStream(inputFile);
        GZIPInputStream gzipInputStream = new GZIPInputStream(gzipFileInputStream);

        int len;
        byte[] buf = new byte[1024];
        while ((len = gzipInputStream.read(buf)) > 0) {
            unzipOutFileStream.write(buf, 0, len);
        }
        unzipOutFileStream.close();
        gzipInputStream.close();
        gzipFileInputStream.close();

        return unzipOutFileName;
    }

    /**
     * Base64 decodes a file.
     *
     * @param inputFile
     *            absolute path to the file to be processed
     * @param tempDir
     *            directory where to put the result and any temporary files
     * @return absolute path to the result file
     * @throws IOException
     */
    public static String base64DecodeFile(String inputFile, String tempDir) throws IOException {

        // Base64 decode
        String base64DecodedFile = inputFile + "_Base64DecodedOutBuffer.adit";
        FileInputStream inputFileStream = new FileInputStream(inputFile);
        FileOutputStream base64DecodedOut = new FileOutputStream(base64DecodedFile, false);
        Base64InputStream base64InputStream = new Base64InputStream(inputFileStream);

        int len;
        byte[] b = new byte[66000];
        while ((len = base64InputStream.read(b)) > 0) {
            base64DecodedOut.write(b, 0, len);
        }
        base64DecodedOut.close();
        base64InputStream.close();
        inputFileStream.close();

        return base64DecodedFile;
    }


    /**
     * Creates a temporary file from input stream.
     *
     * @param inputStream
     *            the data stream
     * @param tempDir
     *            directory where to put the result and any temporary files
     * @return the absolute path to the result file
     * @throws IOException
     */
    public static String createTemporaryFile(InputStream inputStream, String tempDir) throws IOException {

        String temporaryFile = tempDir + File.separator + generateRandomFileName();
        FileOutputStream fileOutputStream = new FileOutputStream(temporaryFile);

        int len;
        byte[] buf = new byte[1024];
        while ((len = inputStream.read(buf)) > 0) {
            fileOutputStream.write(buf, 0, len);
        }

        fileOutputStream.close();
        inputStream.close();

        return temporaryFile;
    }

    /**
     * Creates a temporary from byte array.
     * @param bytesToWrite
     *     Bytes to write to temporary file
     * @param tempDir
     *     Directory where to put the result and any temporary files
     * @return
     *     Absolute path to the result file
     * @throws IOException
     */
    public static String createTemporaryFile(final byte[] bytesToWrite,
    	final String tempDir) throws IOException {
        String temporaryFile = tempDir + File.separator + generateRandomFileName();

        OutputStream fileOutputStream = null;
        try {
        	fileOutputStream = new BufferedOutputStream(new FileOutputStream(temporaryFile));
        	fileOutputStream.write(bytesToWrite);
        } finally {
        	safeCloseStream(fileOutputStream);
        }

        return temporaryFile;
    }

    /**
     * Deletes the file.
     *
     * @param fileName
     *            the absolute path to the file to be deleted
     * @param deleteTemporaryFiles
     *            the parameter specifying if actual deleting is to take place.
     * @return true if file was deleted
     */
    public static boolean deleteFile(String fileName, boolean deleteTemporaryFiles) {
        if (deleteTemporaryFiles) {
            boolean fileDeleted = (new File(fileName)).delete();
            if (fileDeleted) {
                logger.debug("Deleted temporary file: " + fileName);
            } else {
                logger.warn("Could not delete temporary file: " + fileName);
            }
            return fileDeleted;
        } else {
            return false;
        }
    }

    /**
     * Prints the X-Tee header to log.
     *
     * @param header
     *      X-Tee header
     * @param conf
     * 		{@link Configuration} object containing current
     * 		application configuration settings.
     */
    public static void printHeader(CustomXTeeHeader header, Configuration conf) {

        logger.debug("-------- XTeeHeader --------");

        logger.debug("Nimi: " + header.getNimi());
        logger.debug("ID: " + header.getId());
        logger.debug("Isikukood: " + header.getIsikukood());
        logger.debug("Andmekogu: " + header.getAndmekogu());
        logger.debug("Asutus: " + header.getAsutus());
        logger.debug("Allasutus: " + header.getAllasutus());
        logger.debug("Amet: " + header.getAmet());
        logger.debug("Infosüsteem: " + header.getInfosysteem(conf.getXteeProducerName()));

        logger.debug("----------------------------");
    }

    /**
     * Converts a byte array to a HEX string.
     *
     * @param byteArray the bytes to convert
     * @return HEX string
     */
    public static String convertToHexString(final byte[] byteArray) {
		final String hexes = "0123456789ABCDEF";
		if (byteArray == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * byteArray.length);
		for (final byte b : byteArray) {
			hex.append(hexes.charAt((b & 0xF0) >> 4)).append(
					hexes.charAt((b & 0x0F)));
		}
		return hex.toString();
    }

    /**
     * Detects if given string is a valid HEX string.
     *
     * @param potentialHexString
     * 		String to check.
     * @return
     * 		{@code true} if given string is a valid HEX string
     */
    public static boolean isHexString(String potentialHexString) {
    	if (isNullOrEmpty(potentialHexString)) {
    		return false;
    	}

    	if ((potentialHexString.length() % 2) != 0) {
    		return false;
    	}

    	for (int i = 0; i < potentialHexString.length(); i++) {
    	    char currentChar = potentialHexString.charAt(i);
    		boolean isHexChar = (currentChar >= '0' && currentChar <= '9')
    			|| (currentChar >= 'a' && currentChar <= 'f')
    			|| (currentChar >= 'A' && currentChar <= 'F');

    	    if (!isHexChar) {
    	        return false;
    	    }
    	}

    	return true;
    }

    /**
     * Converts HEX string to byte array.
     *
     * @param hexString
     * 		HEX string to convert.
     * @return
     * 		Given HEX string as byte array
     */
    public static byte[] convertHexStringToByteArray(String hexString) {
        if (isNullOrEmpty(hexString)) {
        	return new byte[] {};
        }

    	int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Delete a directory and it's contents.
     *
     * @param dir
     *            Directory to be deleted
     * @return Success of deletion (true, if directory was successfully deleted)
     */
    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return true;
        }
        if (!dir.exists()) {
            return true;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Transforms XML using XSLT stylesheet and writes the result to the file
     * specified.
     *
     * @param inputXMLFileName
     *            absolute path to XML data file
     * @param inputXSLTFileName
     *            absolute path to XSLT file
     * @param outputFileName
     *            absolute path to output file
     * @throws FileNotFoundException
     * @throws TransformerException
     */
    public static void applyXSLT(String inputXMLFileName, String inputXSLTFileName, String outputFileName)
            throws FileNotFoundException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        Source source = new StreamSource(inputXSLTFileName);
        Source dataSource = new StreamSource(inputXMLFileName);
        StreamResult fileOutputStream = new StreamResult(new FileOutputStream(outputFileName));

        Transformer transformer = transformerFactory.newTransformer(source);
        transformer.transform(dataSource, fileOutputStream);
    }

    /**
     * Generates a PDF using FOP.
     *
     * @param outputFileName
     *            absolute path to the resulting PDF file (including the file
     *            name)
     * @param inputFileName
     *            absolute path to the input XML file
     * @throws FileNotFoundException
     */
    public static void generatePDF(String outputFileName, String inputFileName) throws FileNotFoundException {
        FopFactory fopFactory = FopFactory.newInstance();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outputFileName)));

        try {
            // Step 3: Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            // Step 4: Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // Step 5: Setup input and output for XSLT transformation
            Source src = new StreamSource(new File(inputFileName));
            Result res = new SAXResult(fop.getDefaultHandler());

            // Step 6: Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } catch (Exception exc) {
            logger.error("Error while transforming XSL TO PDF: ", exc);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.error("Error while closing outputstream: ", e);
                out = null;
            }
        }

    }

    /**
     * Converts a date to XML format.
     *
     * @param date date
     * @return date in XML format
     */
    public static String dateToXMLDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss");
        return df.format(date);
    }

    /**
     * Converts a date to XML format (only date part is returned).
     *
     * @param date date
     * @return date in XML format
     */
    public static String dateToXMLDatePart(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-d");
        return df.format(date);
    }

    /**
     * Converts an XML date to Date.
     *
     * @param xmlDate
     *     date in XML format
     * @return date
     * @throws ParseException
     */
    public static Date xmlDateToDate(String xmlDate) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss");
        return df.parse(xmlDate);
    }

    /**
     * Converts a date to the estonian format (dd.MM.yyyy HH:mm).
     *
     * @param date
     *     date to be converted
     * @return
     *     string representation of the date in "dd.MM.yyyy HH:mm" format
     */
    public static String dateToEstonianDateString(Date date) {
        if (date == null) {
            return "";
        } else {
            return (new SimpleDateFormat("dd.MM.yyyy HH:mm")).format(date);
        }
    }

    /**
     * Splits out file data from the main XML file. Used for reducing the XML
     * size to be marshalled / unmarshalled.
     *
     * @param xmlFileName
     *            the XML file to be split
     * @param tagLocalName
     *            the local name of the tags to be split out
     * @param noMainFile
     *            specifies if a main file is used
     * @param noSubFiles
     *            specifies if subfiles are created
     * @param replaceMain
     *            specifies if the main file is replaced
     * @param removeSubFileRootTags
     *            specifies if subfile root tags are removed after work is done
     * @return the splitting result
     */
    public static FileSplitResult splitOutTags(String xmlFileName, String tagLocalName, boolean noMainFile,
            boolean noSubFiles, boolean replaceMain, boolean removeSubFileRootTags) {
        FileSplitResult result = new FileSplitResult();

        // Attempt to save extracted files in the same directory
        // where the original file is located
        File originalFile = new File(xmlFileName);
        String filesDir = originalFile.getParent();

        FileInputStream mainInStream = null;
        InputStreamReader mainInReader = null;
        BufferedReader mainReader = null;
        FileOutputStream mainOutStream = null;
        OutputStreamWriter mainOutWriter = null;
        BufferedWriter mainWriter = null;
        FileOutputStream subOutStream = null;
        OutputStreamWriter subOutWriter = null;
        BufferedWriter subWriter = null;

        Pattern startPattern = Pattern.compile("<([\\w]+:)?" + tagLocalName, Pattern.DOTALL | Pattern.CANON_EQ
                | Pattern.CASE_INSENSITIVE);
        Pattern endPattern = Pattern.compile("<\\/([\\w]+:)?" + tagLocalName, Pattern.DOTALL | Pattern.CANON_EQ
                | Pattern.CASE_INSENSITIVE);
        Pattern startAndEndPattern = Pattern.compile("<([\\w]+:)?" + tagLocalName + "([^>])*\\/([\\s])*>",
                Pattern.DOTALL | Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);

        int charsRead = 0;
        char[] readBuffer = new char[1];
        String ioBuffer = "";
        boolean isTag = false;
        boolean isMainDocument = true;
        int currentLevel = 0;
        int itemNr = 0;

        String mainDataFile = null;
        String subFileName = null;

        if (!noMainFile) {
            mainDataFile = createTemporaryFile(0, "adit", filesDir);
        }

        try {
            mainInStream = new FileInputStream(xmlFileName);
            mainInReader = new InputStreamReader(mainInStream, "UTF-8");
            mainReader = new BufferedReader(mainInReader);

            if (!noMainFile) {
                mainOutStream = new FileOutputStream(mainDataFile, false);
                mainOutWriter = new OutputStreamWriter(mainOutStream, "UTF-8");
                mainWriter = new BufferedWriter(mainOutWriter);
            }

            while ((charsRead = mainReader.read(readBuffer, 0, readBuffer.length)) > 0) {
                if (readBuffer[0] == '<') {
                    isTag = true;
                }

                // Kui asume keset lõputähiseta TAGi, siis lisame sümboli
                // puhvrisse
                if (isTag) {
                    ioBuffer += readBuffer[0];
                } else {
                    if (isMainDocument) {
                        if (!noMainFile) {
                            mainWriter.write(readBuffer, 0, charsRead);
                        }
                    } else {
                        if (!noSubFiles) {
                            subWriter.write(readBuffer, 0, charsRead);
                        }
                    }
                }

                if (readBuffer[0] == '>') {
                    isTag = false;

                    Matcher startMatcher = startPattern.matcher(ioBuffer);
                    Matcher endMatcher = endPattern.matcher(ioBuffer);
                    Matcher startAndEndMatcher = startAndEndPattern.matcher(ioBuffer);

                    if (startAndEndMatcher.find()) {
                        if (!noSubFiles) {
                            if (currentLevel == 0) {
                                subFileName = createTemporaryFile(++itemNr, "adit", filesDir);
                                result.getSubFiles().add(subFileName);
                                subOutStream = new FileOutputStream(subFileName, false);
                                subOutWriter = new OutputStreamWriter(subOutStream, "UTF-8");
                                subWriter = new BufferedWriter(subOutWriter);
                                if (removeSubFileRootTags) {
                                    ioBuffer = startAndEndMatcher.replaceAll("");
                                }
                                subWriter.write(ioBuffer);

                                // Kirjutame põhifaili kommentaari, mille alusel
                                // me pärast
                                // eraldatud TAGi tagasi saame panna.
                                if (!noMainFile) {
                                    mainWriter.write("<sysTempFile>" + subFileName + "</sysTempFile>");
                                }

                                safeCloseWriter(subWriter);
                                safeCloseWriter(subOutWriter);
                                safeCloseStream(subOutStream);
                            } else {
                                subWriter.write(ioBuffer);
                            }
                        }
                    } else if (startMatcher.find()) {
                        // Puhvris on eraldatava TAGi algus
                        ++currentLevel;

                        // Veendume, et tegemist on kõige ülemise taseme
                        // algusega
                        if (currentLevel == 1) {
                            isMainDocument = false;
                            if (!noSubFiles) {
                                subFileName = createTemporaryFile(++itemNr, "adit", filesDir);
                                result.getSubFiles().add(subFileName);
                                subOutStream = new FileOutputStream(subFileName, false);
                                subOutWriter = new OutputStreamWriter(subOutStream, "UTF-8");
                                subWriter = new BufferedWriter(subOutWriter);
                            }
                        }

                        if (!noSubFiles) {
                            if (removeSubFileRootTags) {
                                ioBuffer = startMatcher.replaceAll("");
                            }

                            subWriter.write(ioBuffer);

                            // Kirjutame põhifaili kommentaari, mille alusel me
                            // pärast
                            // eraldatud TAGi tagasi saame panna.
                            if (!noMainFile && (currentLevel == 1)) {
                                mainWriter.write("<sysTempFile>" + subFileName + "</sysTempFile>");
                            }
                        }
                    } else if (endMatcher.find()) {
                        if (!noSubFiles) {
                            // Puhvris on eraldatava TAGi lõpp
                            if (removeSubFileRootTags) {
                                ioBuffer = endMatcher.replaceAll("");
                            }

                            subWriter.write(ioBuffer);
                        }
                        // Veendume, et tegemist on kõige ülemise taseme lõpuga
                        if (currentLevel == 1) {
                            if (!noSubFiles) {
                                safeCloseWriter(subWriter);
                                safeCloseWriter(subOutWriter);
                                safeCloseStream(subOutStream);
                            }
                            isMainDocument = true;
                        }
                        --currentLevel;
                    } else {
                        if (isMainDocument) {
                            if (!noMainFile) {
                                mainWriter.write(ioBuffer);
                            }
                        } else {
                            if (!noSubFiles) {
                                subWriter.write(ioBuffer);
                            }
                        }
                    }

                    ioBuffer = "";
                    startMatcher = null;
                    endMatcher = null;
                    startAndEndMatcher = null;
                }
            }

            // Paneme kasutatud failid kinni
            safeCloseReader(mainReader);
            safeCloseReader(mainInReader);
            safeCloseStream(mainInStream);
            safeCloseWriter(mainWriter);
            safeCloseWriter(mainOutWriter);
            safeCloseStream(mainOutStream);
            safeCloseWriter(subWriter);
            safeCloseWriter(subOutWriter);
            safeCloseStream(subOutStream);

            // Nimetame failid ümber nii, et töödeldud fail asendaks algselt
            // ette antud faili.
            if (!noMainFile) {
                if (replaceMain) {
                    (new File(xmlFileName)).delete();
                    (new File(mainDataFile)).renameTo(new File(xmlFileName));
                    result.setMainFile(xmlFileName);
                } else {
                    result.setMainFile(mainDataFile);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed extracting elements " + tagLocalName + " into separate files!", ex);
        } finally {
            // Streamid kinni
            safeCloseReader(mainReader);
            safeCloseReader(mainInReader);
            safeCloseStream(mainInStream);
            safeCloseWriter(mainWriter);
            safeCloseWriter(mainOutWriter);
            safeCloseStream(mainOutStream);
            safeCloseWriter(subWriter);
            safeCloseWriter(subOutWriter);
            safeCloseStream(subOutStream);

            mainInStream = null;
            mainInReader = null;
            mainReader = null;
            mainOutStream = null;
            mainOutWriter = null;
            mainWriter = null;
            subOutStream = null;
            subOutWriter = null;
            subWriter = null;

            startPattern = null;
            endPattern = null;
            startAndEndPattern = null;
            ioBuffer = null;
            mainDataFile = null;
        }

        return result;
    }

    /**
     * Joins a split XML.
     *
     * @param xmlFileName
     *            the main file
     * @param appendTags
     *            specifies the tags where to append
     */
    public static void joinSplitXML(String xmlFileName, String appendTags) {
        logger.debug("Starting XML file merge joinSplitXML()");

        FileInputStream mainInStream = null;
        InputStreamReader mainInReader = null;
        BufferedReader mainReader = null;
        FileInputStream subInStream = null;
        InputStreamReader subInReader = null;
        BufferedReader subReader = null;
        FileOutputStream mainOutStream = null;
        OutputStreamWriter mainOutWriter = null;
        BufferedWriter mainWriter = null;

        int charsRead = 0;
        char[] readBuffer = new char[1];
        String ioBuffer = "";
        boolean isTag = false;
        boolean isPlaceholderTag = false;

        try {
            String filesDir = (new File(xmlFileName)).getParent();
            String resultFile = createTemporaryFile(0, "adit", filesDir);

            mainOutStream = new FileOutputStream(resultFile, false);
            mainOutWriter = new OutputStreamWriter(mainOutStream, "UTF-8");
            mainWriter = new BufferedWriter(mainOutWriter);

            mainInStream = new FileInputStream(xmlFileName);
            mainInReader = new InputStreamReader(mainInStream, "UTF-8");
            mainReader = new BufferedReader(mainInReader);
            while ((charsRead = mainReader.read(readBuffer, 0, readBuffer.length)) > 0) {
                if (readBuffer[0] == '<') {
                    isTag = true;
                }

                // Kui asume keset lõputähiseta TAGi, siis lisame sümboli
                // puhvrisse
                if (isTag || isPlaceholderTag) {
                    ioBuffer += readBuffer[0];
                } else {
                    mainWriter.write(readBuffer, 0, charsRead);
                }

                if (readBuffer[0] == '>') {
                    isTag = false;

                    if (ioBuffer.startsWith("<sysTempFile>")) {
                        isPlaceholderTag = true;
                    }

                    if (ioBuffer.endsWith("</sysTempFile>")) {
                        isPlaceholderTag = false;
                        String subFileName = ioBuffer.replace("<sysTempFile>", "").replace("</sysTempFile>", "").trim();
                        if ((new File(subFileName)).exists()) {
                            if ((appendTags != null) && (appendTags.length() > 0)) {
                                mainWriter.write(("<" + appendTags + ">").toCharArray());
                            }

                            subInStream = new FileInputStream(subFileName);
                            subInReader = new InputStreamReader(subInStream, "UTF-8");
                            subReader = new BufferedReader(subInReader);
                            int subCharsRead = 0;
                            char[] subReadBuffer = new char[102400];
                            while ((subCharsRead = subReader.read(subReadBuffer, 0, subReadBuffer.length)) > 0) {
                                mainWriter.write(subReadBuffer, 0, subCharsRead);
                            }
                            safeCloseReader(subReader);
                            safeCloseReader(subInReader);
                            safeCloseStream(subInStream);

                            // Kustutame ajutise faili
                            (new File(subFileName)).delete();

                            if ((appendTags != null) && (appendTags.length() > 0)) {
                                mainWriter.write(("</" + appendTags + ">").toCharArray());
                            }
                        }
                    } else if (!isPlaceholderTag) {
                        mainWriter.write(ioBuffer);
                    }

                    ioBuffer = "";
                }
            }

            // Paneme sisendfaili kinni, et saaks ülearuse faili ära kustutada
            safeCloseReader(mainReader);
            safeCloseReader(mainInReader);
            safeCloseStream(mainInStream);
            safeCloseWriter(mainWriter);
            safeCloseWriter(mainOutWriter);
            safeCloseStream(mainOutStream);

            mainInStream = null;
            mainInReader = null;
            mainReader = null;
            subInStream = null;
            subInReader = null;
            subReader = null;
            mainWriter = null;
            mainOutWriter = null;
            mainOutStream = null;

            // Kustutame ajutise faili
            logger.debug("Deleting original file \"" + xmlFileName + "\"");
            if (!deleteUntilSucceed(xmlFileName, 20)) {
                logger.debug("Failed deleting original file \"" + xmlFileName + "\"");
            }
            logger.debug("Saving file \"" + resultFile + "\" as \"" + xmlFileName + "\"");
            if (!renameUntilSucceed(resultFile, xmlFileName, 20)) {
                logger.debug("Failed Saving file \"" + resultFile + "\" as \"" + xmlFileName + "\"");
            }
        } catch (Exception ex) {
            logger.error("Failed joining separate files into one XML file!", ex);
        } finally {
            safeCloseReader(mainReader);
            safeCloseReader(mainInReader);
            safeCloseStream(mainInStream);
            safeCloseReader(subReader);
            safeCloseReader(subInReader);
            safeCloseStream(subInStream);
            safeCloseWriter(mainWriter);
            safeCloseWriter(mainOutWriter);
            safeCloseStream(mainOutStream);

            mainInStream = null;
            mainInReader = null;
            mainReader = null;
            subInStream = null;
            subInReader = null;
            subReader = null;
            mainWriter = null;
            mainOutWriter = null;
            mainOutStream = null;

            ioBuffer = null;
        }
    }

    /**
     * Flushes and closes a Reader. Useful when a reader needs to be closed and
     * no reasonable action could be taken in case of an error.
     *
     * @param r
     *            Reader to be closed
     */
    public static void safeCloseReader(Reader r) {
        if (r != null) {
            try {
                r.close();
            } catch (Exception ex) {
                // This exception is intentionally discarded
            } finally {
                r = null;
            }
        }
    }

    /**
     * Flushes and closes a Writer. Useful when a writer needs to be closed and
     * no reasonable action could be taken in case of an error.
     *
     * @param w
     *            Writer to be closed
     */
    public static void safeCloseWriter(Writer w) {
        if (w != null) {
            try {
            	w.flush();
                w.close();
            } catch (Exception ex) {
                // This exception is intentionally discarded
            } finally {
                w = null;
            }
        }
    }

    /**
     * Closes an input stream ignoring all errors. Useful when a stream needs to
     * be closed and no reasonable action could be taken in case of an error.
     *
     * @param s
     *            InputStream to be closed
     */
    public static void safeCloseStream(InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception ex) {
                // This exception is intentionally discarded
            } finally {
                s = null;
            }
        }
    }

    /**
     * Closes an output stream ignoring all errors. Useful when a stream needs
     * to be closed and no reasonable action could be taken in case of an error.
     *
     * @param s
     *            OutputStream to be closed
     */
    public static void safeCloseStream(OutputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception ex) {
                // This exception is intentionally discarded
            } finally {
                s = null;
            }
        }
    }

    /**
     * Tries to delete file until success or timeout.
     *
     * @param fileName
     *            the file to delete
     * @param timeoutSeconds
     *            timeout in seconds
     * @return true, if file deleted
     * @throws InterruptedException
     */
    public static boolean deleteUntilSucceed(String fileName, int timeoutSeconds) throws InterruptedException {
        File f = new File(fileName);

        int elapsedTime = 0;
        if (f.exists() && f.isFile()) {
            while (!f.delete() && (elapsedTime < timeoutSeconds)) {
                Thread.sleep(1000);
                elapsedTime++;
            }
        } else {
            return false;
        }

        return (elapsedTime == timeoutSeconds);
    }

    /**
     * Tries to rename file until success or timeout.
     *
     * @param originalFileName
     *            current file name
     * @param destinationFileName
     *            new file name
     * @param timeoutSeconds
     *            timeout in seconds
     * @return true, if file renamed
     * @throws InterruptedException
     */
    public static boolean renameUntilSucceed(String originalFileName, String destinationFileName, int timeoutSeconds)
            throws InterruptedException {
        File of = new File(originalFileName);
        File df = new File(destinationFileName);

        int elapsedTime = 0;
        if (of.exists() && of.isFile() && !df.exists()) {
            while (!of.renameTo(df) && (elapsedTime < timeoutSeconds)) {
                Thread.sleep(1000);
                elapsedTime++;
            }
        } else {
            return false;
        }

        return (elapsedTime == timeoutSeconds);
    }

    /**
     * Removes country prefix from personal ID code. For example: EE37001010001
     * will be changed to 37001010001
     *
     * @param personalIdCode
     *            Personal ID code with country prefix
     * @return Personal ID code without country prefix
     */
    public static String getPersonalIdCodeWithoutCountryPrefix(String personalIdCode) {
        String fixedUserCode = "";
        if (personalIdCode != null) {
            for (int i = 0; i < personalIdCode.length(); i++) {
                if ("0123456789".contains(String.valueOf(personalIdCode.charAt(i)))) {
                    fixedUserCode = personalIdCode.substring(i);
                    break;
                }
            }
        }
        return fixedUserCode;
    }

    /**
     * Detects if given user code starts with country prefix.
     *
     * @param code
     * 		User code (personal id code or organization code)
     * @return
     * 		{@code true} if given code starts with country prefix
     */
    public static boolean codeStartsWithCountryPrefix(String code) {
    	return ((!isNullOrEmpty(code))
    		&& (code.length() > 1)
    		&& (!"0123456789".contains(String.valueOf(code.charAt(0))))
    		&& (!"0123456789".contains(String.valueOf(code.charAt(1)))));
    }

    /**
     * Extracts the query name from the X-Tee header. The query name must be in
     * the format "[prefix].[queryName].v[versionNumber]"
     *
     * @param fullQueryName
     *            the full query name
     * @return the resulting holder object
     */
    public static XRoadQueryName extractQueryName(String fullQueryName) {
        XRoadQueryName result = new XRoadQueryName();
        result.setName(fullQueryName);
        result.setVersion(1);

        StringTokenizer st = new StringTokenizer(fullQueryName, ".");

        for (int i = 0; st.hasMoreTokens(); i++) {
            if (i == 1) {
                result.setName(st.nextToken());
            } else if (i == 2) {
                try {
                    String tmpVersion = st.nextToken();
                    tmpVersion = tmpVersion.substring(1);
                    result.setVersion(Integer.parseInt(tmpVersion));
                } catch (Exception e) {
                    logger.error("Error while trying to parse X-Road request name version part: ", e);
                    throw new AditInternalException("Error while trying to parse X-Road request name version part: "
                            + fullQueryName);
                }
            } else {
                st.nextToken();
            }
        }

        return result;
    }

    /**
     * Extract content ID from string.
     *
     * @param conentIDString string containing the content ID
     * @return content ID
     */
    public static String extractContentID(String conentIDString) {
        String result = conentIDString;

        try {
            if (conentIDString.indexOf("cid:") != -1) {
                result = conentIDString.replaceAll("cid:", "");
            } else {
                logger.warn("Error extracting attachment content ID from string - prefix 'cid' not found: "
                        + conentIDString);
            }
        } catch (Exception e) {
            logger.warn("Error extracting attachment content ID from string: " + conentIDString);
        }

        return result;
    }

    /**
     * Strip content ID - remove "<", ">".
     *
     * @param contentID content ID string
     * @return stripped content ID
     */
    public static String stripContentID(String contentID) {
        String result = contentID;

        if (contentID != null) {
            result = contentID.trim().replaceAll("<", "").replaceAll(">", "");
        }

        return result;
    }

    /**
     * Convert file to byte array.
     *
     * @param file file
     * @return byte array
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
    	byte[] bytes = null;
    	
        InputStream is = null;
		try {
			is = new FileInputStream(file);
			
			// Get the size of the file
			long length = file.length();

			bytes = new byte[(int) length];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			    offset += numRead;
			}

			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}
		} finally {
			is.close();
		}
        
        return bytes;
    }

    /**
     * Get file content as string.
     *
     * @param f file
     * @return file content
     * @throws IOException
     */
    public static String getFileContents(File f) throws IOException {
        return new String(getBytesFromFile(f), "UTF-8");
    }

    /**
     * Remove country prefix from string.
     *
     * @param code user code
     * @return user code without country prefix
     */
    public static String removeCountryPrefix(String code) {
        if ((code != null) && (code.length() > 2)
        	&& "EE".equalsIgnoreCase(code.substring(0, 2))) {

        	return code.substring(2);
        } else {
            return code;
        }
    }

    /**
     * Add country prefix.
     *
     * @param code user code
     * @param prefix country prefix
     * @return user code with country prefix
     */
    public static String addCountryPrefix(String code, String prefix) {
        return prefix + code;
    }

    /**
     * Determines if given String is null or empty (zero length).
     * Whitespace is not treated as empty string.
     *
     * @param stringToEvaluate String that will be checked for having NULL or empty value
     * @return true, if input String is NULL or has zero length
     */
    public static boolean isNullOrEmpty(String stringToEvaluate) {
    	return ((stringToEvaluate == null) || stringToEvaluate.isEmpty());
    }

    /**
     * Determines if given class contains a field with specified name.
     *
     * @param targetClass class to be examined
     * @param fieldName name of field that will be searched in class
     * @return {@code true} if given class contains a field with given name
     */
    public static boolean classContainsField(Class<?> targetClass, String fieldName) {
    	Field[] declaredFields = targetClass.getDeclaredFields();
    	
    	boolean found = false;
    	for (int i = 0; i < declaredFields.length; i++) {
    		if (declaredFields[i].getName().compareTo(fieldName) == 0) {
    			found = true;
    			break;
    		}
    	}
    	
    	return found;
    }

    /**
     * Finds extension part of given file name.
     *
     * @param fileName
     * 		File name
     * @return
     * 		Extension part of file name (without extension separator ".").
     * 		Will return {@code null} if file name is empty or does not have
     *      an extension (e.g. "makefile")
     */
    public static String getFileExtension(String fileName) {
    	String result = null;

    	if (!isNullOrEmpty(fileName)) {
    		int extensionStart = fileName.lastIndexOf(".") + 1;
    		if ((extensionStart > 0) && (extensionStart < fileName.length())) {
    			result = fileName.substring(extensionStart);
    		}
    	}

    	return result;
    }

    /**
     * Finds part of file name that precedes file extension.
     *
     * @param fileName
     * 		File name without path
     * @return
     * 		File name without extension. Will return {@code null} if file name
     * 		is empty or only consists of extension (e.g. ".svn")
     */
    public static String getFileNameWithoutExtension(String fileName) {
    	String result = null;

    	if (!isNullOrEmpty(fileName)) {
    		int extensionStart = fileName.lastIndexOf(".");
    		if ((extensionStart > 0) && (extensionStart < fileName.length())) {
    			result = fileName.substring(0, extensionStart);
    		} else if (extensionStart < 0) {
    			result = fileName;
    		}
    	}

    	return result;
    }

    /**
     * Calculates MD5 checksum of given file.
     *
     * @param filename
     * 		Full path to file that will be used for checksum calculation.
     * @return
     * 		MD5 chacksum as byte array
     * @throws NoSuchAlgorithmException
     * 		will be thrown if MD5 algorithm is not found for some reason
     * @throws IOException
     *		will be thrown if file does not exist or cannot be read
     */
    public static byte[] calculateMd5Checksum(String filename) throws NoSuchAlgorithmException, IOException  {
    	MessageDigest digest = MessageDigest.getInstance("MD5");
    	BufferedInputStream fileStream = null;

    	try {
	    	fileStream = new BufferedInputStream(new FileInputStream(filename));
	    	int len;
	    	byte[] buffer = new byte[1024];
	    	while ((len = fileStream.read(buffer)) > 0) {
	    		digest.update(buffer, 0, len);
	    	}
    	} finally {
    		safeCloseStream(fileStream);
    	}
    	return digest.digest();
	}

    /**
     * Creates a file name that does not include any forbidden characters.
     *
     * @param namePart
     * 		Name part of file name
     * @param extension
     * 		Extension part of file name
     * @param uniqueCounter
     * 		Text or number that will be appended to file name (after name part)
     * 		to avoid possible file name collisions (e.g. multiple files having
     *      same name in .zip archive)
     * @return
     * 		file name that should not cause problems to common operating systems
     */
    public static String convertToLegalFileName(String namePart, String extension, String uniqueCounter) {
    	int maxLength = 240;
    	List<Character> illegalChars = Arrays.asList(new Character[] {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'});
    	StringBuilder result = new StringBuilder(maxLength + 10);

    	if (!isNullOrEmpty(namePart)) {
	    	for (int i = 0; (i < namePart.length()) && (result.length() < maxLength); i++) {
	    		Character currentChar = namePart.charAt(i);
	    		if (!illegalChars.contains(currentChar)) {
	    			result.append(currentChar);
	    		}
	    	}
    	} else {
    		result.append("document");
    	}

    	if (!isNullOrEmpty(uniqueCounter)) {
    		result.append(uniqueCounter);
    	}

    	if (!isNullOrEmpty(extension)) {
    		result.append(".").append(extension);
    	}

    	return result.toString();
    }

    /**
     * Joins list of strings to a single string (separated by specified delimiter).
     *
     * @param s
     *     List of strings
     * @param delimiter
     *     Item delimiter in resulting string
     * @return
     *     String consisting of list items
     */
    public static String join(List<? extends CharSequence> s, String delimiter) {
    	StringBuilder buffer = new StringBuilder();
    	Iterator<? extends CharSequence> iter = s.iterator();
    	if (iter.hasNext()) {
    	    buffer.append(iter.next());
    	    while (iter.hasNext()) {
	    		buffer.append(delimiter);
	    		buffer.append(iter.next());
    	    }
    	}
    	return buffer.toString();
    }

    /**
     * Joins list of {@link Message} objects to a single string consisting
     * of message values (separated by specified delimiter).
     *
     * @param messages
     *     List of messages
     * @param delimiter
     *     Item delimiter in resulting string
     * @return
     *     String consisting of message values
     */
    public static String joinMessages(List<Message> messages, String delimiter) {
    	StringBuilder buffer = new StringBuilder();
    	Iterator<Message> iter = messages.iterator();
    	if (iter.hasNext()) {
    	    buffer.append(iter.next());
    	    while (iter.hasNext()) {
	    		buffer.append(delimiter);
	    		buffer.append(iter.next().getValue());
    	    }
    	}
    	return buffer.toString();
    }

    /**
     * Finds a message by locale from message list.
     *
     * @param messages
     *     List of messages
     * @param locale
     *     Locale name
     * @return
     *     Message matching the given locale
     */
    public static Message getMessageByLocale(List<Message> messages, Locale locale) {
    	Message result = null;

    	if ((messages != null) && (locale != null) && !isNullOrEmpty(locale.getLanguage())) {
	    	Iterator<Message> iter = messages.iterator();
		    while (iter.hasNext()) {
		    	Message msg = iter.next();
		    	if (locale.getLanguage().equalsIgnoreCase(msg.getLang())) {
		    		result = msg;
		    	}
		    }
    	}

    	return result;
    }

    /**
     * Gets current user based on data from X-Road headers.
     *
     * @param header
     *     X-Road header
     * @param userService
     *     Instance of user service
     * @return
     *     Current user
     */
    public static AditUser getAditUserFromXroadHeader(
    	final CustomXTeeHeader header, final UserService userService) {

    	String userCode = isNullOrEmpty(header.getAllasutus()) ? header.getIsikukood() : header.getAllasutus();
        AditUser user = userService.getUserByID(userCode);
        if (user == null) {
            logger.error("User is not registered. User code: " + userCode);
            AditCodedException aditCodedException = new AditCodedException("user.nonExistent");
            aditCodedException.setParameters(new Object[] {userCode });
            throw aditCodedException;
        }

        return user;
    }

    /**
     * Gets user account of the person who executed current request (even if
     * request was executed using organization account).
     *
     * @param currentUser
     *     Current user (may me an organization account)
     * @param header
     *     X-Road header
     * @param userService
     *     Instance of user service
     * @return
     *     Account of person who executed current request
     */
    public static AditUser getXroadUserFromXroadHeader(
    	final AditUser currentUser, final CustomXTeeHeader header,
    	final UserService userService) {

    	AditUser xroadRequestUser = null;

        if (UserService.USERTYPE_PERSON.equalsIgnoreCase(currentUser.getUsertype().getShortName())) {
            xroadRequestUser = currentUser;
        } else {
            try {
                xroadRequestUser = userService.getUserByID(header.getIsikukood());
            } catch (Exception ex) {
            	logger.error("Error when attempting to find local user matchinig the person that executed a company request.", ex);
            }
        }

        if (xroadRequestUser == null) {
        	xroadRequestUser = new AditUser();
        	xroadRequestUser.setUsertype(new Usertype(UserService.USERTYPE_PERSON));
        	if (header != null) {
        		xroadRequestUser.setUserCode(header.getIsikukood());
        		xroadRequestUser.setActive(false);
        	}
        }

        return xroadRequestUser;
    }

    /**
     * Calculates difference of two dates in milliseconds.
     *
     * @param earlierDate
     * 		Earlier date
     * @param laterDate
     * 		Later date
     * @return
     * 		Difference of given dates in milliseconds
     */
    public static long getDateDiffInMilliseconds(Date earlierDate, Date laterDate) {
    	if ((earlierDate == null) || (laterDate == null)) {
    		return Long.MIN_VALUE;
    	}

    	Calendar earlierCal = Calendar.getInstance();
    	Calendar laterCal = Calendar.getInstance();
    	earlierCal.setTime(earlierDate);
    	laterCal.setTime(laterDate);
    	return laterCal.getTimeInMillis() - earlierCal.getTimeInMillis();
    }

    public static String getAttributeValueFromTag(String tag, String attributeName) {
        String result = "";

        if (!isNullOrEmpty(tag)) {
            String leadIn = attributeName + "=\"";
            int start = tag.indexOf(leadIn);
            if (start > 0) {
                start += leadIn.length();
                int end = tag.indexOf("\"", start);
                if (end > 0) {
                    result = tag.substring(start, end);
                }
            }
        }

        return result;
    }

    public static PersonName splitPersonName(String personName) {
    	PersonName result = new PersonName();

    	if (!isNullOrEmpty(personName)) {
    		if (personName.indexOf(",") > 0) {
    			String[] splitName = personName.split(",");
    			if (splitName.length == 2) {
    				result.setFirstName(splitName[1].trim());
    				result.setSurname(splitName[0].trim());
    			} else {
    				result.setSurname(personName);
    			}
    		} else {
    			String[] splitName = personName.split(" ");
	    		if (splitName.length == 1) {
	    			result.setSurname(personName);
	    		} else {
					result.setFirstName(splitName[0].trim());
					result.setSurname(Util.join(Arrays.asList(Arrays.copyOfRange(splitName, 1, splitName.length)), " ").trim());
	    		}
    		}
    	}

    	return result;
    }
    
    private static String getDetailFromCert (X509Certificate cert, ASN1ObjectIdentifier identifier) {
    	  String result = null;
    	  if(cert!=null) {
	    	  X500Principal  principal = cert.getSubjectX500Principal();
	          X500Name x500name = new X500Name( principal.getName());
	          RDN[] rdns = x500name.getRDNs(identifier);
	          RDN rdn = null;
	          if(rdns!=null && rdns.length>0) {
	        	  rdn = rdns[0];
	          }
	          result = (rdn==null?null:(IETFUtils.valueToString(rdn.getFirst().getValue())));
    	  }
    	  return result;
    }    
    /**
     * Finds serialnumber from certificate, which is user code or company registry code
     * @param cert
     * @return
     */
    public static String getSubjectSerialNumberFromCert (X509Certificate cert) {
    	return getDetailFromCert(cert, BCStyle.SERIALNUMBER);
    }
    /**
     * Determines if filename is BDOC
     * @param filename
     * @return
     */
    public static Boolean isBdocFile (String filename) {
    	String extension = Util.getFileExtension(filename);
    	return isBdocExtension(extension);
    }
    /**
     * Determines if extension is BDOC
     * @param extension
     * @return
     */
    public static Boolean isBdocExtension (String extension) {
    	for (String ext : BDOC_FILE_EXTENSIONS) {
    		if(extension.equalsIgnoreCase(ext)) {
    			return true;
    		}
    	}
    	return false;
    }
    
}
