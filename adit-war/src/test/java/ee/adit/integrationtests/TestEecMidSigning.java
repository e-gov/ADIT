package ee.adit.integrationtests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.adit.dao.pojo.AditUser;
import ee.adit.pojo.PrepareSignatureInternalResult;
import ee.adit.service.DocumentService;
import ee.adit.util.Util;
import ee.sk.digidoc.SignedDoc;
import ee.sk.utils.ConfigManager;

/**
 * Tests for For auhentication and signing BDOC files new SIM cards are using
 * ECC prime 256v1 key set and for signing DDOC files RSA 2024 key set.
 * 
 * @author A
 *		
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:**/adit-arendus-tomcat-local/*.xml" })
public class TestEecMidSigning {
	
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(TestEecMidSigning.class);
	
	@Autowired
	private DocumentService ds;
	
	/**
	 * Digidoc conf. to use.
	 */
	private static final File jdigidoc_cfg = new File(
			"src/main/resources/conf/adit-arendus-tomcat-local/jdigidoc.cfg");
			
	/**
	 * Absolute path to signers signing certificate file. Got this with another
	 * helper.
	 * 
	 * For auhentication and signing BDOC files new SIM cards are using ECC
	 * prime 256v1 key set and for signing DDOC files RSA 2024 key set.
	 */
	private final String certFile = new File( 
			"src/test/resources/11412090004_sign_certificate.cer")
					.getAbsolutePath();
	
	/**
	 * ADIT gets the document from DB and temp. stores here for signing logic.
	 */
	private static final String temporaryFilesDir = System
			.getProperty("java.io.tmpdir") + "adit";
			
	public TestEecMidSigning() {
		
		// create "adit" dir if necessary
		File tmp = new File(temporaryFilesDir);
		if (!tmp.exists()) {
			tmp.mkdir();
		}
		
		LOGGER.warn("Temp. dir at - " + TestEecMidSigning.temporaryFilesDir);
		
	} // -TestEecMidSigning
	
	@Before
	public void before() throws Exception {
		
		// Preconfigure Digidoc.
		ConfigManager.init(jdigidoc_cfg.getAbsolutePath());
		ConfigManager.addProvider();
		
		// Check we have DocumentService.
		assertNotNull(ds);
		
	} // -before
	
	@Test
	public void testName() throws Exception {		
		
		// Just looked these up from DB manually.
//		Long documentId = ds.getDocumentDAO().getDocument(99999999901L).getId();
		Long documentId = ds.getDocumentDAO().getDocument(888888888888L).getId();
		LOGGER.info("Testing with documentId == " + documentId); 
		
		// reverse engineer this from CRT - DocumentService checks this
		AditUser xroadUser = new AditUser();
		xroadUser.setUserCode(Util.getSubjectSerialNumberFromCert(
				SignedDoc.readCertificate(certFile)));
		LOGGER.info("xroad user from CRT is " + xroadUser.getUserCode());
		
		// Turn off test-cert check (you need to have test-certs in 
		// jdigidoc*.jar
		// NOTE Remember not to deploy this to live!
		ds.getConfiguration().setDoCheckTestCert(Boolean.FALSE);
		
		// EXECUTE
		PrepareSignatureInternalResult res = ds.prepareSignature(documentId,
				"manifest", "country", "state", "city", "zip", certFile,
				jdigidoc_cfg.getAbsolutePath(), temporaryFilesDir, xroadUser,
				Boolean.TRUE);
				
		// "request.prepareSignature.signer.notCurrentUser = Dokumendi
		// allkirjastamine ebaõnnestus, allkirjastaja peab olema sama isik, kes
		// on infosüsteemi sisenenud."
		// (This is why we reverse engineered the ID straight from CRT.)
		assertFalse("request.prepareSignature.signer.notCurrentUser"
				.equals(res.getErrorCode()));
		
		// request.saveDocument.testcertificate = Teenuse viga (Test ID-kaart)
		// This will happen if you don-t setDoCheckTestCert(false)
		assertFalse("request.saveDocument.testcertificate"
				.equals(res.getErrorCode()));
		
		// expect a known success result
		assertTrue(res.isSuccess());
		
	} // -test
	
}
