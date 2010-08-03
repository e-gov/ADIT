package ee.adit.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedOutputStream;
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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Random;
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
import org.castor.core.util.Base64Encoder;

public class Util {

	private static Logger LOG = Logger.getLogger(Util.class);

	public static String base64encode(String string) throws UnsupportedEncodingException {
		return new String(Base64Encoder.encode(string.getBytes("UTF-8")));
	}

	public static String generateGUID() {
		return java.util.UUID.randomUUID().toString();
	}

	public static String generateRandomFileName() {
		StringBuffer result = new StringBuffer();
		result.append(generateRandomFileNameWithoutExtension());
		result.append(".adit");
		return result.toString();
	}

	public static String generateRandomFileNameWithoutExtension() {
		return generateRandomID();
	}

	public static String generateRandomID() {
		StringBuffer result = new StringBuffer();
		Random r = new Random();
		for (int i = 0; i < 30; i++) {
			result.append(r.nextInt(10));
		}
		return result.toString();
	}
	
	/**
	 * Creates an empty temporary file to given folder.
	 * 
	 * @param itemIndex		Number of current file. Enables to give an index to all files related to same object etc.
	 * @param filesFolder	Folder path where the temporary file will be created. 
	 * @return				Full name of created file (absolute path)
	 */
	public static String createTemporaryFile(int itemIndex, String filesFolder) {
		return createTemporaryFile(itemIndex, "", filesFolder);
	}
	
	/**
	 * Creates an empty temporary file to given folder.
	 * 
	 * @param itemIndex		Number of current file. Enables to give an index to all files related to same object etc.
	 * @param extension		File extension to be given to the temporary file. 
	 * @param filesFolder	Folder path where the temporary file will be created. 
	 * @return				Full name of created file (absolute path)
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
        		LOG.warn("Cannot find folder \"" + filesFolder + "\". Using system temporary folder for temporary files instead.");
        	}
        	
            String result = tmpDir + File.separator + String.valueOf((new Date()).getTime()) + ((itemIndex > 0) ? "_item" + String.valueOf(itemIndex) : "") + extension;
            int uniqueCounter = 0;
            while ((new File(result)).exists()) {
                ++uniqueCounter;
                result = tmpDir + File.separator + String.valueOf((new Date()).getTime()) + ((itemIndex > 0) ? "_item" + String.valueOf(itemIndex) : "") + "_" + String.valueOf(uniqueCounter) + extension;
            }
            File file = new File(result);
            file.createNewFile();
            return result;
        } catch (Exception ex) {
        	LOG.error("Failed creating a temporary file!", ex);
            return null;
        }
    }
	
	public static String gzipAndBase64Encode(String inputFile, String tempDir, boolean deleteTemporaryFiles) throws IOException {
		String resultFileName = null;

		// Pack data to GZip format
		String zipOutFileName = inputFile + "_zipOutBuffer.adit";
		LOG.debug("Packing data to GZip format. Output file: " + zipOutFileName);
		FileInputStream in = new FileInputStream(inputFile);
		FileOutputStream zipOutFile = new FileOutputStream(zipOutFileName, false);
		GZIPOutputStream out = new GZIPOutputStream(zipOutFile);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			LOG.debug(new String(buf, "UTF-8"));
			out.write(buf, 0, len);
		}
		in.close();
		out.finish();
		out.close();
		LOG.debug("GZip complete");

		// Encode the GZipped data to Base64 binary data
		resultFileName = inputFile + "_Base64OutBuffer.adit";
		LOG.debug("Encoding zip file to Base64: Output file: " + zipOutFileName);
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
				LOG.debug("Deleted temporary file: " + zipOutFileName);
			}
		} catch (Exception e) {
			LOG.error("Exception while deleting temporary files: ", e);
		}

		return resultFileName;
	}

	public static String base64EncodeFile(String inputFile, String tempDir) throws IOException {
		String resultFileName = null;

		// Encode data to Base64 binary data
		resultFileName = inputFile + "_Base64OutBuffer.adit";
		LOG.debug("Encoding file to Base64: Output file: " + inputFile);
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

	public static String base64DecodeAndUnzip(String inputFile, String tempDir, boolean deleteTemporaryFiles) throws IOException {

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
				LOG.debug("Deleted temporary file: " + base64DecodedFile);
			}
		} catch (Exception e) {
			LOG.error("Exception while deleting temporary files: ", e);
		}

		return unzipOutFileName;
	}

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

	public static boolean deleteFile(String fileName, boolean deleteTemporaryFiles) {
		if (deleteTemporaryFiles) {
			boolean fileDeleted = (new File(fileName)).delete();
			if (fileDeleted) {
				LOG.debug("Deleted temporary file: " + fileName);
			} else {
				LOG.warn("Could not delete temporary file: " + fileName);
			}
			return fileDeleted;
		} else {
			return false;
		}
	}

	public static void printHeader(CustomXTeeHeader header) {

		LOG.debug("-------- XTeeHeader --------");

		LOG.debug("Nimi: " + header.getNimi());
		LOG.debug("ID: " + header.getId());
		LOG.debug("Isikukood: " + header.getIsikukood());
		LOG.debug("Andmekogu: " + header.getAndmekogu());
		LOG.debug("Asutus: " + header.getAsutus());
		LOG.debug("Allasutus: " + header.getAllasutus());
		LOG.debug("Amet: " + header.getAmet());
		LOG.debug("Infosüsteem: " + header.getInfosysteem());

		LOG.debug("----------------------------");
	}

	public static String convertToHexString(final byte[] byteArray) {
		final byte[] HEXES = "0123456789ABCDEF".getBytes();
		if (byteArray == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * byteArray.length);
		for (final byte b : byteArray) {
			hex.append(HEXES[(b >> 4) & 0xF]).append(HEXES[(b) & 0xF]);
		}
		return hex.toString();
	}

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
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 * @throws FileNotFoundException 
	 * @throws TransformerException 
	 */
	public static void applyXSLT(String inputXMLFileName, String inputXSLTFileName, String outputFileName) throws FileNotFoundException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		
		Source source = new StreamSource(inputXSLTFileName);
		Source dataSource = new StreamSource(inputXMLFileName);
		StreamResult fileOutputStream = new StreamResult(new FileOutputStream(outputFileName)); 
		
		Transformer transformer = transformerFactory.newTransformer(source);
		transformer.transform(dataSource, fileOutputStream);
	}
	
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
			LOG.error("Error while transforming XSL TO PDF: ", exc);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				LOG.error("Error while closing outputstream: ", e);
				out = null;
			}
		}

	}
	
	public static String dateToXMLDate(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss");
		return df.format(date);
	}
    
    public static FileSplitResult splitOutTags(String xmlFileName, String tagLocalName, boolean noMainFile, boolean noSubFiles, boolean replaceMain) {
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

        Pattern startPattern = Pattern.compile("<([\\w]+:)?" + tagLocalName, Pattern.DOTALL | Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);
        Pattern endPattern = Pattern.compile("<\\/([\\w]+:)?" + tagLocalName, Pattern.DOTALL | Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);
        Pattern startAndEndPattern = Pattern.compile("<([\\w]+:)?" + tagLocalName + "([^>])*\\/([\\s])*>", Pattern.DOTALL | Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);

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

                // Kui asume keset lõputähiseta TAGi, siis lisame sümboli puhvrisse
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
                                subWriter.write(ioBuffer);

                                // Kirjutame põhifaili kommentaari, mille alusel me pärast
                                // eraldatud TAGi tagasi saame panna.
                                if (!noMainFile) {
                                    mainWriter.write("<!--SYS_INCLUDE_" + (new File(subFileName)).getName() + "-->");
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

                        // Veendume, et tegemist on kõige ülemise taseme algusega
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
                            subWriter.write(ioBuffer);

                            // Kirjutame põhifaili kommentaari, mille alusel me pärast
                            // eraldatud TAGi tagasi saame panna.
                            if (!noMainFile && (currentLevel == 1)) {
                                mainWriter.write("<!--SYS_INCLUDE_" + (new File(subFileName)).getName() + "-->");
                            }
                        }
                    } else if (endMatcher.find()) {
                        if (!noSubFiles) {
                            // Puhvris on eraldatava TAGi lõpp
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
            LOG.error("Failed extracting elements "+ tagLocalName +" into separate files!", ex);
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
    
    public static void joinSplitXML(String xmlFileName, BufferedWriter mainOutWriter) {
        FileInputStream mainInStream = null;
        InputStreamReader mainInReader = null;
        BufferedReader mainReader = null;
        FileInputStream subInStream = null;
        InputStreamReader subInReader = null;
        BufferedReader subReader = null;

        int charsRead = 0;
        char[] readBuffer = new char[1];
        String ioBuffer = "";
        boolean isTag = false;
        String tmpDir = System.getProperty("java.io.tmpdir", "");

        try {
            mainInStream = new FileInputStream(xmlFileName);
            mainInReader = new InputStreamReader(mainInStream, "UTF-8");
            mainReader = new BufferedReader(mainInReader);
            while ((charsRead = mainReader.read(readBuffer, 0, readBuffer.length)) > 0) {
                if (readBuffer[0] == '<') {
                    isTag = true;
                }

                // Kui asume keset lõputähiseta TAGi, siis lisame sümboli puhvrisse
                if (isTag) {
                    ioBuffer += readBuffer[0];
                } else {
                    mainOutWriter.write(readBuffer, 0, charsRead);
                }

                if (readBuffer[0] == '>') {
                    isTag = false;

                    if (ioBuffer.startsWith("<!--SYS_INCLUDE_")) {
                        String subFileName = tmpDir + File.separator + ioBuffer.replace("<!--SYS_INCLUDE_", "").replace("-->", "").trim();
                        if ((new File(subFileName)).exists()) {
                            subInStream = new FileInputStream(subFileName);
                            subInReader = new InputStreamReader(subInStream, "UTF-8");
                            subReader = new BufferedReader(subInReader);
                            int subCharsRead = 0;
                            char[] subReadBuffer = new char[102400];
                            while ((subCharsRead = subReader.read(subReadBuffer, 0, subReadBuffer.length)) > 0) {
                                mainOutWriter.write(subReadBuffer, 0, subCharsRead);
                            }
                            safeCloseReader(subReader);
                            safeCloseReader(subInReader);
                            safeCloseStream(subInStream);

                            // Kustutame ajutise faili
                            (new File(subFileName)).delete();
                        }
                    } else {
                        mainOutWriter.write(ioBuffer);
                    }

                    ioBuffer = "";
                }
            }

            // Paneme sisendfaili kinni, et saaks ülearuse faili ära kustutada
            safeCloseReader(mainReader);
            safeCloseReader(mainInReader);
            safeCloseStream(mainInStream);

            // Kustutame ajutise faili 
            (new File(xmlFileName)).delete();
        } catch (Exception ex) {
        	LOG.error("Failed joining separate files into one XML file!", ex);
        } finally {
            safeCloseReader(mainReader);
            safeCloseReader(mainInReader);
            safeCloseStream(mainInStream);
            safeCloseReader(subReader);
            safeCloseReader(subInReader);
            safeCloseStream(subInStream);

            mainInStream = null;
            mainInReader = null;
            mainReader = null;
            subInStream = null;
            subInReader = null;
            subReader = null;

            ioBuffer = null;
        }
    }
    
    /**
     * Flushes and closes a Reader.
     * Useful when a reader needs to be closed and no
     * reasonable action could be taken in case of an error. 
     * 
     * @param r		Reader to be closed
     */
    public static void safeCloseReader(Reader r) {
        if (r != null) {
            try {
                r.close();
            } catch (Exception ex) {
            	LOG.warn("Failed closing reader!", ex);
            } finally {
                r = null;
            }
        }
    }

    /**
     * Flushes and closes a Writer.
     * Useful when a writer needs to be closed and no
     * reasonable action could be taken in case of an error. 
     * 
     * @param w		Writer to be closed
     */
    public static void safeCloseWriter(Writer w) {
        if (w != null) {
            try {
                w.flush();
                w.close();
            } catch (Exception ex) {
            	LOG.warn("Failed closing writer!", ex);
            } finally {
                w = null;
            }
        }
    }

    /**
     * Closes an input stream ignoring all errors.
     * Useful when a stream needs to be closed and no
     * reasonable action could be taken in case of an error. 
     * 
     * @param s		InputStream to be closed
     */
    public static void safeCloseStream(InputStream s) {
        if (s != null) {
            try { s.close(); }
            catch (Exception ex) { LOG.warn("Failed closing input stream!", ex); }
            finally { s = null; }
        }
    }

    /**
     * Closes an output stream ignoring all errors.
     * Useful when a stream needs to be closed and no
     * reasonable action could be taken in case of an error. 
     * 
     * @param s		OutputStream to be closed
     */
    public static void safeCloseStream(OutputStream s) {
        if (s != null) {
            try { s.close(); }
            catch (Exception ex) { LOG.warn("Failed closing output stream!", ex); }
            finally { s = null; }
        }
    }
}
