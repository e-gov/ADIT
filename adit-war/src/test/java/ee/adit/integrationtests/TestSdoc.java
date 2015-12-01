package ee.adit.integrationtests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import org.junit.Test;

import ee.sk.digidoc.KeyInfo;
import ee.sk.digidoc.SignedDoc;
import ee.sk.utils.ConfigManager;

public class TestSdoc {
	
	/**
	 * Digidoc conf. to use.
	 */
	private static final File JDIGIDOC_CFG = new File(
			"src/main/resources/conf/adit-arendus-tomcat-local/jdigidoc.cfg");
	
	private static final File eecCert = new File(
				"src/test/resources/11412090004_sign_certificate.cer");
	
	private static final File normCert = new File(
			"src/test/resources/51001091072_sign_certificate.cer");
	
	public TestSdoc() {
		
//		// Preconfigure Digidoc.
		ConfigManager.init(JDIGIDOC_CFG.getAbsolutePath());
		ConfigManager.addProvider();
		
	} // -TestSdoc
	
	@Test
	public void testEecCert() throws Exception {
		
//		X509Certificate cert = SignedDoc.readCertificate(eecCert);
//		System.err.println(cert);
		
		// taken from DocumentService#prepareSignature(...)
		SignedDoc sdoc = new SignedDoc(SignedDoc.FORMAT_DIGIDOC_XML,
				SignedDoc.VERSION_1_3);
		
		X509Certificate cert = SignedDoc.readCertificate(eecCert);
				
		sdoc.prepareSignature(cert, null, null);
		
		sdoc.writeToFile(File.createTempFile("sdoc-", null));
		
	} // -testSignedDoc
	
}
