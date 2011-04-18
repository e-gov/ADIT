package ee.adit.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ee.adit.pojo.OutputDocumentFile;
import ee.adit.service.DocumentService;
import ee.adit.util.SimplifiedDigiDocParser;
import ee.adit.util.StartEndOffsetPair;
import ee.adit.util.Util;
import junit.framework.TestCase;

/**
 * The class <code>SimplifiedDigiDocParserTest</code> contains tests for the
 * class {@link <code>SimplifiedDigiDocParser</code>}
 *
 * @pattern JUnit Test Case
 *
 * @generatedBy CodePro at 31.03.11 15:01
 *
 * @author Jaak
 *
 * @version $Revision$
 */
public class SimplifiedDigiDocParserTest extends TestCase {

	/**
	 * Construct new test instance
	 *
	 * @param name the test name
	 */
	public SimplifiedDigiDocParserTest(String name) {
		super(name);
	}

	/**
	 * Run the Hashtable<String,StartEndOffsetPair>
	 * findDigiDocDataFileOffsets(String) method test
	 */
	public void testFindDigiDocDataFileOffsets_3FilesNoSignatures() {
		String pathToDigiDoc = (new File("target/test-classes/ValidDigiDoc_3Files_NoSignatures.ddoc")).getAbsolutePath();
		try {
			Hashtable<String,StartEndOffsetPair> result = SimplifiedDigiDocParser.findDigiDocDataFileOffsets(pathToDigiDoc);
			assertEquals(3, result.size());
			
			StartEndOffsetPair d0 = result.get("D0");
			assertNotNull(d0);
			assertEquals(274, d0.getStart());
			assertEquals(1145822, d0.getEnd());
			assertEquals("BA45C8F60456A672E003A875E469D0EB", Util.convertToHexString(d0.getDataMd5Hash()));
			
			StartEndOffsetPair d1 = result.get("D1");
			assertNotNull(d1);
			assertEquals(1145980, d1.getStart());
			assertEquals(2203356, d1.getEnd());
			assertEquals("2B04DF3ECC1D94AFDDFF082D139C6F15", Util.convertToHexString(d1.getDataMd5Hash()));
			
			StartEndOffsetPair d2 = result.get("D2");
			assertNotNull(d2);
			assertEquals(2203517, d2.getStart());
			assertEquals(3256838, d2.getEnd());
			assertEquals("9D377B10CE778C4938B3C7E2C63A229A", Util.convertToHexString(d2.getDataMd5Hash()));
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	public void testFindDigiDocDataFileOffsets_NoFilesNoSignatures() {
		String pathToDigiDoc = (new File("target/test-classes/ValidDigiDoc_NoFiles_NoSignatures.ddoc")).getAbsolutePath();
		try {
			Hashtable<String,StartEndOffsetPair> result = SimplifiedDigiDocParser.findDigiDocDataFileOffsets(pathToDigiDoc);
			assertEquals(0, result.size());
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	public void testExtractFileContentsFromDdoc_3Files() {
		String pathToDigiDoc = (new File("target/test-classes/ValidDigiDoc_3Files_NoSignatures.ddoc")).getAbsolutePath();
		String temporaryFilesDir = System.getProperty("java.io.tmpdir");
		if (!Util.isNullOrEmpty(temporaryFilesDir) && (new File(temporaryFilesDir)).exists()) {
			List<OutputDocumentFile> files = new ArrayList<OutputDocumentFile>();
			
			OutputDocumentFile f0 = new OutputDocumentFile();
			f0.setId(0L);
			f0.setFileType(DocumentService.FILETYPE_NAME_DOCUMENT_FILE);
			f0.setDdocDataFileId("D0");
			f0.setDdocDataFileStartOffset(274L);
			f0.setDdocDataFileEndOffset(1145822L);
			
			OutputDocumentFile f1 = new OutputDocumentFile();
			f1.setId(1L);
			f1.setFileType(DocumentService.FILETYPE_NAME_DOCUMENT_FILE);
			f1.setDdocDataFileId("D1");
			f1.setDdocDataFileStartOffset(1145980L);
			f1.setDdocDataFileEndOffset(2203356L);
			
			OutputDocumentFile f2 = new OutputDocumentFile();
			f2.setId(2L);
			f2.setFileType(DocumentService.FILETYPE_NAME_DOCUMENT_FILE);
			f2.setDdocDataFileId("D2");
			f2.setDdocDataFileStartOffset(2203517L);
			f2.setDdocDataFileEndOffset(3256838L);
			
			files.add(f0);
			files.add(f1);
			files.add(f2);
			
			InputStream ddocContainerAsStream = null;
			try {
				ddocContainerAsStream = new FileInputStream(pathToDigiDoc);
				SimplifiedDigiDocParser.extractFileContentsFromDdoc(ddocContainerAsStream, files, temporaryFilesDir);
				
				assertNotNull(files.get(0).getSysTempFile());
				assertNotNull(files.get(1).getSysTempFile());
				assertNotNull(files.get(2).getSysTempFile());
				
				String digest0 = Util.convertToHexString(Util.calculateMd5Checksum(files.get(0).getSysTempFile()));
				String digest1 = Util.convertToHexString(Util.calculateMd5Checksum(files.get(1).getSysTempFile()));
				String digest2 = Util.convertToHexString(Util.calculateMd5Checksum(files.get(2).getSysTempFile()));
				
				assertEquals("6933218FA1EF33877128DD745066A847", digest0);
				assertEquals("E042E04EE0642F06420F18831E7491D6", digest1);
				assertEquals("F0C9D0AAF8F78E700AB728C0E7B1C1B2", digest2);
			} catch (Exception ex) {
				fail(ex.getMessage());
			} finally {
				Util.safeCloseStream(ddocContainerAsStream);
			}
		} else {
			// If temporary folder cannot be found for some reason then succeed
			// by default to avoid blocking compilation
			assertTrue(true);
		}
	}
	
	public void testExtractFileContentsFromDdoc_3FilesReversed() {
		String pathToDigiDoc = (new File("target/test-classes/ValidDigiDoc_3Files_NoSignatures.ddoc")).getAbsolutePath();
		String temporaryFilesDir = System.getProperty("java.io.tmpdir");
		if (!Util.isNullOrEmpty(temporaryFilesDir) && (new File(temporaryFilesDir)).exists()) {
			List<OutputDocumentFile> files = new ArrayList<OutputDocumentFile>();
			
			OutputDocumentFile f0 = new OutputDocumentFile();
			f0.setId(0L);
			f0.setFileType(DocumentService.FILETYPE_NAME_DOCUMENT_FILE);
			f0.setDdocDataFileId("D0");
			f0.setDdocDataFileStartOffset(274L);
			f0.setDdocDataFileEndOffset(1145822L);
			
			OutputDocumentFile f1 = new OutputDocumentFile();
			f1.setId(1L);
			f1.setFileType(DocumentService.FILETYPE_NAME_DOCUMENT_FILE);
			f1.setDdocDataFileId("D1");
			f1.setDdocDataFileStartOffset(1145980L);
			f1.setDdocDataFileEndOffset(2203356L);
			
			OutputDocumentFile f2 = new OutputDocumentFile();
			f2.setId(2L);
			f2.setFileType(DocumentService.FILETYPE_NAME_DOCUMENT_FILE);
			f2.setDdocDataFileId("D2");
			f2.setDdocDataFileStartOffset(2203517L);
			f2.setDdocDataFileEndOffset(3256838L);
			
			files.add(f2);
			files.add(f1);
			files.add(f0);
			
			InputStream ddocContainerAsStream = null;
			try {
				ddocContainerAsStream = new FileInputStream(pathToDigiDoc);
				SimplifiedDigiDocParser.extractFileContentsFromDdoc(ddocContainerAsStream, files, temporaryFilesDir);
				
				assertNotNull(files.get(0).getSysTempFile());
				assertNotNull(files.get(1).getSysTempFile());
				assertNotNull(files.get(2).getSysTempFile());
				
				String digest0 = Util.convertToHexString(Util.calculateMd5Checksum(files.get(0).getSysTempFile()));
				String digest1 = Util.convertToHexString(Util.calculateMd5Checksum(files.get(1).getSysTempFile()));
				String digest2 = Util.convertToHexString(Util.calculateMd5Checksum(files.get(2).getSysTempFile()));
				
				assertEquals("6933218FA1EF33877128DD745066A847", digest0);
				assertEquals("E042E04EE0642F06420F18831E7491D6", digest1);
				assertEquals("F0C9D0AAF8F78E700AB728C0E7B1C1B2", digest2);
			} catch (Exception ex) {
				fail(ex.getMessage());
			} finally {
				Util.safeCloseStream(ddocContainerAsStream);
			}
		} else {
			// If temporary folder cannot be found for some reason then succeed
			// by default to avoid blocking compilation
			assertTrue(true);
		}
	}
}

/*$CPS$ This comment was generated by CodePro. Do not edit it.
 * patternId = com.instantiations.assist.eclipse.pattern.testCasePattern
 * strategyId = com.instantiations.assist.eclipse.pattern.testCasePattern.junitTestCase
 * additionalTestNames = 
 * assertTrue = false
 * callTestMethod = true
 * createMain = false
 * createSetUp = false
 * createTearDown = false
 * createTestFixture = false
 * createTestStubs = false
 * methods = findDigiDocDataFileOffsets(QString;)
 * package = ee.adit.test.util
 * package.sourceFolder = adit-war/src/test/java
 * superclassType = junit.framework.TestCase
 * testCase = SimplifiedDigiDocParserTest
 * testClassType = ee.adit.util.SimplifiedDigiDocParser
 */