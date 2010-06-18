package ee.adit.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.castor.core.util.Base64Encoder;
import org.w3c.dom.Node;

public class Util {

	private static Logger LOG = Logger.getLogger(Util.class);
	
	public static String base64encode(String string) throws UnsupportedEncodingException {
		return new String(Base64Encoder.encode(string.getBytes("UTF-8")));
	}
	
	public static String generateRandomFileName() {
		StringBuffer result = new StringBuffer();
		Random r = new Random();
		for(int i = 0; i < 30; i++) {
			result.append(r.nextInt(10));
		}
		result.append(".adit");
		return result.toString();
	}
		
	public static String gzipAndBase64Encode(String inputFile, String tempDir, boolean deleteTemporaryFiles) throws IOException {
		String resultFileName = null;
		
		// Pack data to GZip format
        String zipOutFileName = inputFile + "_zipOutBuffer.dat";
        LOG.debug("Packing data to GZip format. Output file: " + zipOutFileName);
        FileInputStream in = new FileInputStream(inputFile);
        FileOutputStream zipOutFile = new FileOutputStream(zipOutFileName, false);
        GZIPOutputStream out = new GZIPOutputStream(zipOutFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.finish();
        out.close();
        LOG.debug("GZip complete");
        
        // Encode the GZipped data to Base64 binary data
        resultFileName = inputFile + "_Base64OutBuffer.dat";
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
        	if(deleteTemporaryFiles) {
        		File zipFile = new File(zipOutFileName);
            	zipFile.delete();
            	LOG.debug("Deleted temporary file: " + zipOutFileName);
        	}        	
        } catch(Exception e) {
        	LOG.error("Exception while deleting temporary files: ", e);
        }
        
		return resultFileName;
	}
	
	public static boolean deleteFile(String fileName, boolean deleteTemporaryFiles) {
		if(deleteTemporaryFiles) {
			boolean fileDeleted = (new File(fileName)).delete();
			if(fileDeleted) {
				LOG.debug("Deleted temporary file: " + fileName);
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
	
}
