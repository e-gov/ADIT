package ee.adit.integrationtests;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.adit.dao.pojo.AditUser;
import ee.adit.pojo.PrepareSignatureInternalResult;
import ee.adit.service.DocumentService;
import ee.adit.util.Util;

/**
 * Tests for For authentication and signing BDOC files new SIM cards are using
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
	private static final File JDIGIDOC_CFG = new File(
			"src/main/resources/conf/adit-arendus-tomcat-local/jdigidoc.cfg");
			
	/**
	 * Absolute path to signers signing certificate file. Got this with another
	 * helper.
	 * 
	 * For authentication and signing BDOC files new SIM cards are using ECC
	 * prime 256v1 key set and for signing DDOC files RSA 2024 key set.
	 */
	private final String eecCrt = new File( 
			"src/test/resources/11412090004_sign_certificate.cer")
					.getAbsolutePath();
	
	private final String normalCert = new File(
			"src/test/resources/37901130250_sign_certificate.cer")
			.getAbsolutePath();
	
	/**
	 * ADIT gets the document from DB and temp. stores here for signing logic.
	 */
	private static final String temporaryFilesDir = System
			.getProperty("java.io.tmpdir") + "adit";
	
	private static final Boolean doPreferBdoc = Boolean.TRUE;
	
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
		
		// Check we have DocumentService.
		assertNotNull(ds);
		
		// Turn off test-cert check (you need to have test-certs in jdigidoc*.jar
		// NOTE Remember not to deploy this to live!
		ds.getConfiguration().setDoCheckTestCert(Boolean.FALSE);
		
	} // -before
	
	@Test
	public void testNormal() throws Exception {
		
		final String crt = normalCert;
		
		Long documentId = ds.getDocumentDAO().getDocument(15L).getId();
		AditUser xroadUser = getAditUserFromCrt(crt);	// NORMAL
		
		// PREPARE
		PrepareSignatureInternalResult res = ds.prepareSignature(documentId,
				"manifest", "country", "state", "city", "zip", crt,
				JDIGIDOC_CFG.getAbsolutePath(), temporaryFilesDir, xroadUser,
				doPreferBdoc);
		
		// expect a known success result
		assertTrue(res.isSuccess());
		
	} // -testNormal
	
	@Ignore
	@Test
	public void testEec() throws Exception {		
		
		final String crt = eecCrt;
		
		// Just looked these up from DB manually.
		Long documentId = ds.getDocumentDAO().getDocument(13207L).getId();
		LOGGER.info("Testing with documentId == " + documentId); 
		
		AditUser xroadUser = getAditUserFromCrt(crt);

		// PREPARE
		PrepareSignatureInternalResult res = ds.prepareSignature(documentId,
				"manifest", "country", "state", "city", "zip", crt,
				JDIGIDOC_CFG.getAbsolutePath(), temporaryFilesDir, xroadUser,
				doPreferBdoc);
				
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
		
		String signatureFileName = eecCrt;
		String requestPersonalCode = xroadUser.getUserCode();
		AditUser currentUser = xroadUser;
		String digidocConfigFile = JDIGIDOC_CFG.getAbsolutePath();
		
		// CONFIRM
//		ds.confirmSignature(documentId, signatureFileName, requestPersonalCode,
//				currentUser, digidocConfigFile, temporaryFilesDir);
		
		// digidoc.extract.invalidSignature = DigiDoc faili salvestamine
		// ebaõnnestus, kuna failist leiti kehtetu allkiri. Kehtetu allkirja
		// andnud isik: {0}
		
	} // -test
	
	/**
	 * Reverse-engineers {@link AditUser#getUserCode()} from CRT file.
	 * @param crtFile
	 * @return
	 * @throws DigiDocException
	 */
	private AditUser getAditUserFromCrt(String crtFile) {
		
		// reverse engineer this from CRT - DocumentService checks this
		AditUser result = new AditUser();
		result.setUserCode(Util.getSubjectSerialNumberFromCert(Util.readCertificate(crtFile)));
		
		LOGGER.info("xroad user from CRT is " + result.getUserCode());
		
		return result;
		
	} // -getAditUserFromCrt
	
}
