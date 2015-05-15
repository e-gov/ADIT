package ee.adit.util;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
import org.castor.core.util.Base64Decoder;
import org.castor.core.util.Base64Encoder;

import ee.adit.dao.pojo.Document;
import ee.adit.exception.AditInternalException;

/**
 * Class providing static utility / helper methods.
 * 
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public final class Util {

    private Util() { }
    
    /**
     * X-Tee namespace URI
     */
    public static final String XTEE_NAMESPACE = "http://x-tee.riik.ee/xsd/xtee.xsd";

    /**
     * Log4J logger
     */
    private static Logger logger = Logger.getLogger(Util.class);

    /**
     * Default file extension for temporary files
     */
    public static final String ADIT_FILE_EXTENSION = ".adit";

    /**
     * The length of the generated random ID.
     */
    public static final int RANDOM_ID_LENGTH = 30;

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
        Base64OutputStream b64outStream = new Base64OutputStream(b64out);
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
        Base64OutputStream b64outStream = new Base64OutputStream(b64out);
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
     * @param deleteTemporaryFiles
     *            parameter specifying if temporary files are to be deleted
     *            immediately after work is complete
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
     * Prints the X-Tee header to log
     * 
     * @param header
     *            X-Tee header
     */
    public static void printHeader(CustomXTeeHeader header) {

        logger.debug("-------- XTeeHeader --------");

        logger.debug("Nimi: " + header.getNimi());
        logger.debug("ID: " + header.getId());
        logger.debug("Isikukood: " + header.getIsikukood());
        logger.debug("Andmekogu: " + header.getAndmekogu());
        logger.debug("Asutus: " + header.getAsutus());
        logger.debug("Allasutus: " + header.getAllasutus());
        logger.debug("Amet: " + header.getAmet());
        logger.debug("Infosüsteem: " + header.getInfosysteem());

        logger.debug("----------------------------");
    }

    /**
     * Converts a byte array to a HEX string.
     * 
     * @param byteArray the bytes to convert
     * @return HEX string
     */
    public static String convertToHexString(final byte[] byteArray) {
        final byte[] hexes = "0123456789ABCDEF".getBytes();
        if (byteArray == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * byteArray.length);
        for (final byte b : byteArray) {
            hex.append(hexes[(b >> 4) & 0xF]).append(hexes[(b) & 0xF]);
        }
        return hex.toString();
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
     * @param date
     * @return date in XML format
     */
    public static String dateToXMLDatePart(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-d");
        return df.format(date);
    }

    /**
     * Converts an XML date to Date.
     * 
     * @param date
     *            in XML format
     * @return date
     * @throws ParseException
     */
    public static Date xmlDateToDate(String xmlDate) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss");
        return df.parse(xmlDate);
    }

    /**
     * Converts a date to the estonian format (dd.MM.yyyy HH:mm)
     * 
     * @param date
     * @return string representation of the date in "dd.MM.yyyy HH:mm" format
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
                logger.error("Exception: ", ex);
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
                logger.error("Exception: ", ex);
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
                logger.error("Exception: ", ex);
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
                logger.error("Exception: ", ex);
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
     * Extract content ID from string
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
     * Strip content ID - remove "<", ">"
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
     * Convert file to byte array
     * 
     * @param file file
     * @return byte array
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    /**
     * Get file content as string
     * 
     * @param f file
     * @return file content
     * @throws IOException
     */
    public static String getFileContents(File f) throws IOException {
        return new String(getBytesFromFile(f), "UTF-8");
    }

    /**
     * Remove country prefix from string
     * 
     * @param code user code
     * @return user code without country prefix
     */
    public static String removeCountryPrefix(String code) {
        if (code != null) {
            return code.replace("EE", "");
        } else {
            return null;
        }
    }

    /**
     * Add country prefix
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
     * @param stringToEvaluate
     * 		String that will be checked for having NULL or empty value
     * @return
     * 		true, if input String is NULL or has zero length 
     */
    public static boolean isNullOrEmpty(String stringToEvaluate) {
    	return ((stringToEvaluate == null) || stringToEvaluate.isEmpty());
    }
    
    public static boolean classContainsField(Class targetClass, String fieldName) {
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
}
