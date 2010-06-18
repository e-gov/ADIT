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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.castor.core.util.Base64Encoder;

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
		
	public static String gzipPack(String rawDataFileName, String tempDir) throws IOException {
		String resultFileName = null;
		
		// Pack data to GZip format
        String zipOutFileName = tempDir + File.separator + rawDataFileName + "_zipOutBuffer.dat";
        FileInputStream in = new FileInputStream( tempDir + File.separator + rawDataFileName);
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
		
        // Encode the GZipped data to Base64 binary data
        resultFileName = tempDir + File.separator + rawDataFileName + "_Base64OutBuffer.dat";
        in = new FileInputStream(zipOutFileName);
        FileOutputStream b64out = new FileOutputStream(resultFileName, false);
        Base64OutputStream b64outStream = new Base64OutputStream(b64out);
        byte[] b = new byte[66000];
        while ((len = in.read(b)) > 0) {
        	b64outStream.write(b, 0, len);
        }
        in.close();
        b64out.close();
        b64outStream.close();
        
		return resultFileName;
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
