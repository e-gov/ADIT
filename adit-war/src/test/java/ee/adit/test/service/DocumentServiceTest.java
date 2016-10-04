package ee.adit.test.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import ee.adit.dao.pojo.DocumentFile;
import ee.adit.pojo.ArrayOfFileType;
import ee.adit.service.DocumentService;
import ee.adit.util.Configuration;
import ee.adit.util.StartEndOffsetPair;
import ee.adit.util.Util;
import junit.framework.TestCase;

/**
 * The class <code>DocumentServiceTest</code> contains tests for the class
 * {@link <code>DocumentService</code>}
 *
 * @pattern JUnit Test Case
 *
 * @generatedBy CodePro at 7.04.11 11:32
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class DocumentServiceTest extends TestCase {

	/**
	 * Construct new test instance
	 *
	 * @param name the test name
	 */
	public DocumentServiceTest(String name) {
		super(name);
	}

	public void testMakeFileNameSafeForDigiDocLibrary() {
		assertTrue("".equalsIgnoreCase(DocumentService.makeFileNameSafeForDigiDocLibrary(null)));
		assertTrue("".equalsIgnoreCase(DocumentService.makeFileNameSafeForDigiDocLibrary("")));
		assertTrue("_".equalsIgnoreCase(DocumentService.makeFileNameSafeForDigiDocLibrary("&")));
		assertTrue("x_y".equalsIgnoreCase(DocumentService.makeFileNameSafeForDigiDocLibrary("x&y")));
	}

	public void testFileIsOfRequestedType() {
		ArrayOfFileType types = null;
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_DOCUMENT_FILE, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT, types));

		types = new ArrayOfFileType();
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_DOCUMENT_FILE, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT, types));

		types.setFileType(new ArrayList<String>());
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_DOCUMENT_FILE, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT, types));

		types.getFileType().add(DocumentService.FILETYPE_NAME_DOCUMENT_FILE);
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_DOCUMENT_FILE, types));
		assertFalse(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER, types));
		assertFalse(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT, types));

		types.getFileType().add(DocumentService.FILETYPE_NAME_SIGNATURE_CONTAINER);
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_DOCUMENT_FILE, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER, types));
		assertFalse(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT, types));

		types.getFileType().add(DocumentService.FILETYPE_NAME_SIGNATURE_CONTAINER_DRAFT);
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_DOCUMENT_FILE, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER, types));
		assertTrue(DocumentService.fileIsOfRequestedType(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT, types));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_NullParameters_ExpectFalse() {
		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(null, null));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_OffsetTableParameterNull_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, null));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileNull_ExpectFalse() {
		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);
		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(null, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileDeletedTrue_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		file.setDdocDataFileId("D0");
		file.setDeleted(true);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileDeletedNull_ExpectTrue() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		file.setDdocDataFileId("D0");
		file.setDeleted(null);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertTrue(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileDeletedFalse_ExpectTrue() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		file.setDdocDataFileId("D0");
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertTrue(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileIsSignatureContainer_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER);
		file.setDdocDataFileId("D0");
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileIsSignatureContainerDraft_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT);
		file.setDdocDataFileId("D0");
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileIsZipArchive_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_ZIP_ARCHIVE);
		file.setDdocDataFileId("D0");
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileIsOfUnknownType_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(0);
		file.setDdocDataFileId("D0");
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileDdocIdNull_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		file.setDdocDataFileId(null);
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileDdocIdEmpty_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		file.setDdocDataFileId("");
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_FileDdocIdNotInOffsetTable_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		file.setDdocDataFileId("D1");
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_IncorrectDataInOffsetTable_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDdocDataFileId("D0");
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();

		StartEndOffsetPair offsets = new StartEndOffsetPair();
		fileOffsetsInDdoc.put("D0", offsets);
		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));

		offsets.setStart(200);
		offsets.setEnd(100);
		fileOffsetsInDdoc.put("D0", offsets);
		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));

		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);
		assertTrue(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_SuccessCase_ExpectTrue() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		file.setDdocDataFileId("D0");
		file.setDeleted(false);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertTrue(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testIsNecessaryToRemoveFileContentsAfterSigning_OffsetsAlreadySet_ExpectFalse() {
		DocumentFile file = new DocumentFile();
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		file.setDdocDataFileId("D0");
		file.setDeleted(false);
		file.setDdocDataFileStartOffset(100L);
		file.setDdocDataFileEndOffset(200L);

		Hashtable<String, StartEndOffsetPair> fileOffsetsInDdoc = new Hashtable<String, StartEndOffsetPair>();
		StartEndOffsetPair offsets = new StartEndOffsetPair();
		offsets.setStart(100);
		offsets.setEnd(200);
		fileOffsetsInDdoc.put("D0", offsets);

		assertFalse(DocumentService.isNecessaryToRemoveFileContentsAfterSigning(file, fileOffsetsInDdoc));
	}

	public void testResolveFileTypeId_TypeNameNull_ExpectDocumentFile() {
		assertEquals(DocumentService.FILETYPE_DOCUMENT_FILE, DocumentService.resolveFileTypeId(null));
	}

	public void testResolveFileTypeId_TypeNameEmpty_ExpectDocumentFile() {
		assertEquals(DocumentService.FILETYPE_DOCUMENT_FILE, DocumentService.resolveFileTypeId(""));
	}

	public void testResolveFileTypeId_TypeNameUnknown_ExpectDocumentFile() {
		assertEquals(DocumentService.FILETYPE_DOCUMENT_FILE, DocumentService.resolveFileTypeId("random type name"));
	}

	public void testResolveFileTypeId_TypeNameDocFile_ExpectDocumentFile() {
		assertEquals(DocumentService.FILETYPE_DOCUMENT_FILE, DocumentService.resolveFileTypeId(DocumentService.FILETYPE_NAME_DOCUMENT_FILE));
	}

	public void testResolveFileTypeId_TypeNameDigiDoc_ExpectDigiDoc() {
		assertEquals(DocumentService.FILETYPE_SIGNATURE_CONTAINER, DocumentService.resolveFileTypeId(DocumentService.FILETYPE_NAME_SIGNATURE_CONTAINER));
	}

	public void testResolveFileTypeId_TypeNameDigiDocDraft_ExpectDigiDocDraft() {
		assertEquals(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT, DocumentService.resolveFileTypeId(DocumentService.FILETYPE_NAME_SIGNATURE_CONTAINER_DRAFT));
	}

	public void testResolveFileTypeId_TypeNameZipArchive_ExpectZipArchive() {
		assertEquals(DocumentService.FILETYPE_ZIP_ARCHIVE, DocumentService.resolveFileTypeId(DocumentService.FILETYPE_NAME_ZIP_ARCHIVE));
	}

	public void testIsSignatureContainerDraftExpired_LifetimeNotSetInConf_ExpectFalse() {
		Configuration conf = new Configuration();
		conf.setUnfinishedSignatureLifetimeSeconds(null);

		DocumentService svc = new DocumentService();
		svc.setConfiguration(conf);

		Calendar cal = Calendar.getInstance();
		cal.set(1970, Calendar.JANUARY, 1);

		DocumentFile containerDraft = new DocumentFile();
		containerDraft.setLastModifiedDate(cal.getTime());
		containerDraft.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT);

		assertFalse(svc.isSignatureContainerDraftExpired(containerDraft));
	}

	public void testIsSignatureContainerDraftExpired_LifetimeZeroSetInConf_ExpectFalse() {
		Configuration conf = new Configuration();
		conf.setUnfinishedSignatureLifetimeSeconds(0L);

		DocumentService svc = new DocumentService();
		svc.setConfiguration(conf);

		Calendar cal = Calendar.getInstance();
		cal.set(1970, Calendar.JANUARY, 1);

		DocumentFile containerDraft = new DocumentFile();
		containerDraft.setLastModifiedDate(cal.getTime());
		containerDraft.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT);

		assertFalse(svc.isSignatureContainerDraftExpired(containerDraft));
	}

	public void testIsSignatureContainerDraftExpired_LastModifiedInDistantPast_ExpectTrue() {
		Configuration conf = new Configuration();
		conf.setUnfinishedSignatureLifetimeSeconds(120L);

		DocumentService svc = new DocumentService();
		svc.setConfiguration(conf);

		Calendar cal = Calendar.getInstance();
		cal.set(1970, Calendar.JANUARY, 1);

		DocumentFile containerDraft = new DocumentFile();
		containerDraft.setLastModifiedDate(cal.getTime());
		containerDraft.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT);

		assertTrue(svc.isSignatureContainerDraftExpired(containerDraft));
	}

	public void testIsSignatureContainerDraftExpired_LastModifiedLessThanLifetimeAgo_ExpectFalse() {
		Configuration conf = new Configuration();
		conf.setUnfinishedSignatureLifetimeSeconds(120L);

		DocumentService svc = new DocumentService();
		svc.setConfiguration(conf);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -10);

		DocumentFile containerDraft = new DocumentFile();
		containerDraft.setLastModifiedDate(cal.getTime());
		containerDraft.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT);

		assertFalse(svc.isSignatureContainerDraftExpired(containerDraft));
	}

	public void testIsSignatureContainerDraftExpired_LastModifiedMoreThanLifetimeAgo_ExpectTrue() {
		Configuration conf = new Configuration();
		conf.setUnfinishedSignatureLifetimeSeconds(120L);

		DocumentService svc = new DocumentService();
		svc.setConfiguration(conf);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -150);

		DocumentFile containerDraft = new DocumentFile();
		containerDraft.setLastModifiedDate(cal.getTime());
		containerDraft.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT);

		assertTrue(svc.isSignatureContainerDraftExpired(containerDraft));
	}

	public void testIsSignatureContainerDraftExpired_FileNotContainerDraft_ExpectFalse() {
		Configuration conf = new Configuration();
		conf.setUnfinishedSignatureLifetimeSeconds(120L);

		DocumentService svc = new DocumentService();
		svc.setConfiguration(conf);

		Calendar cal = Calendar.getInstance();
		cal.set(1970, Calendar.JANUARY, 1);

		DocumentFile containerDraft = new DocumentFile();
		containerDraft.setLastModifiedDate(cal.getTime());
		containerDraft.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER);

		assertFalse(svc.isSignatureContainerDraftExpired(containerDraft));
	}

	public void testIsPossibleToSignFile_DocumentFileDeletedFalse_ExpectTrue() {
		DocumentService svc = new DocumentService();
		DocumentFile file = new DocumentFile();
		file.setDeleted(false);
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		assertTrue(svc.isPossibleToSignFile(file));
	}

	public void testIsPossibleToSignFile_DocumentFileDeletedNull_ExpectTrue() {
		DocumentService svc = new DocumentService();
		DocumentFile file = new DocumentFile();
		file.setDeleted(null);
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		assertTrue(svc.isPossibleToSignFile(file));
	}

	public void testIsPossibleToSignFile_DocumentFileDeletedTrue_ExpectFalse() {
		DocumentService svc = new DocumentService();
		DocumentFile file = new DocumentFile();
		file.setDeleted(true);
		file.setDocumentFileTypeId(DocumentService.FILETYPE_DOCUMENT_FILE);
		assertFalse(svc.isPossibleToSignFile(file));
	}

	public void testIsPossibleToSignFile_SignatureContainerDeletedFalse_ExpectFalse() {
		DocumentService svc = new DocumentService();
		DocumentFile file = new DocumentFile();
		file.setDeleted(false);
		file.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER);
		assertFalse(svc.isPossibleToSignFile(file));
	}

	public void testIsPossibleToSignFile_SignatureContainerDraftDeletedFalse_ExpectFalse() {
		DocumentService svc = new DocumentService();
		DocumentFile file = new DocumentFile();
		file.setDeleted(false);
		file.setDocumentFileTypeId(DocumentService.FILETYPE_SIGNATURE_CONTAINER_DRAFT);
		assertFalse(svc.isPossibleToSignFile(file));
	}

	public void testIsPossibleToSignFile_ZipArchiveDeletedFalse_ExpectFalse() {
		DocumentService svc = new DocumentService();
		DocumentFile file = new DocumentFile();
		file.setDeleted(false);
		file.setDocumentFileTypeId(DocumentService.FILETYPE_ZIP_ARCHIVE);
		assertFalse(svc.isPossibleToSignFile(file));
	}

	public void testIsPossibleToSignFile_UnknownFileTypeDeletedFalse_ExpectFalse() {
		DocumentService svc = new DocumentService();
		DocumentFile file = new DocumentFile();
		file.setDeleted(false);
		file.setDocumentFileTypeId(0);
		assertFalse(svc.isPossibleToSignFile(file));
	}

	public void testIsPossibleToSignFile_FileNull_ExpectFalse() {
		DocumentService svc = new DocumentService();
		assertFalse(svc.isPossibleToSignFile(null));
	}

	public void testConvertSignatureValueToByteArray_InputIsHex() {
		DocumentService svc = new DocumentService();
		byte[] inputValue = Util.convertHexStringToByteArray("61303635323963666462653566653165376361346163383066383166393339386632626637353336333538323531646536643231303138636331666139356164333163393330393233636532333339323964623132656539323564636530623938306466366266646665306532303863666161336266653234333335653235393663303533386563366562363264633965643535353333623132326562363735623264353535393736646637636136323835633636616564353766623035616232386238396532386130663232373761396232396566636436333534623832643936326536633539623361373034646136303632633336643366366666616361");
		String resultAsHex = Util.convertToHexString(svc.convertSignatureValueToByteArray(inputValue));
		String expectedValue = "A06529CFDBE5FE1E7CA4AC80F81F9398F2BF7536358251DE6D21018CC1FA95AD31C930923CE233929DB12EE925DCE0B980DF6BFDFE0E208CFAA3BFE24335E2596C0538EC6EB62DC9ED55533B122EB675B2D555976DF7CA6285C66AED57FB05AB28B89E28A0F2277A9B29EFCD6354B82D962E6C59B3A704DA6062C36D3F6FFACA";

		assertTrue(expectedValue.equalsIgnoreCase(resultAsHex));
	}

	public void testConvertSignatureValueToByteArray_InputNotHex() {
		DocumentService svc = new DocumentService();
		byte[] inputValue = Util.convertHexStringToByteArray("A06529CFDBE5FE1E7CA4AC80F81F9398F2BF7536358251DE6D21018CC1FA95AD31C930923CE233929DB12EE925DCE0B980DF6BFDFE0E208CFAA3BFE24335E2596C0538EC6EB62DC9ED55533B122EB675B2D555976DF7CA6285C66AED57FB05AB28B89E28A0F2277A9B29EFCD6354B82D962E6C59B3A704DA6062C36D3F6FFACA");
		String resultAsHex = Util.convertToHexString(svc.convertSignatureValueToByteArray(inputValue));
		String expectedValue = "A06529CFDBE5FE1E7CA4AC80F81F9398F2BF7536358251DE6D21018CC1FA95AD31C930923CE233929DB12EE925DCE0B980DF6BFDFE0E208CFAA3BFE24335E2596C0538EC6EB62DC9ED55533B122EB675B2D555976DF7CA6285C66AED57FB05AB28B89E28A0F2277A9B29EFCD6354B82D962E6C59B3A704DA6062C36D3F6FFACA";

		assertTrue(expectedValue.equalsIgnoreCase(resultAsHex));
	}

	public void testReplaceSignatureInDigiDocContainer() {
		String containerPath = "";
		try {
			DocumentService svc = new DocumentService();

			containerPath = createTestDigiDocContainer();

			StringBuilder sigValue = new StringBuilder(10000);
			sigValue.append("<Signature Id=\"S0\" xmlns=\"http://www.w3.org/2000/09/xmldsig#\">\n");
			sigValue.append("<SignedInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">\n");
			sigValue.append("<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">\n");
			sigValue.append("</CanonicalizationMethod>\n");
			sigValue.append("<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\">\n");
			sigValue.append("</SignatureMethod>\n");
			sigValue.append("<Reference URI=\"#D0\">\n");
			sigValue.append("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			sigValue.append("</DigestMethod>\n");
			sigValue.append("<DigestValue>KRte0hjAUKp/10l9sa+xS5vctvc=</DigestValue>\n");
			sigValue.append("</Reference>\n");
			sigValue.append("<Reference Type=\"http://uri.etsi.org/01903/v1.1.1#SignedProperties\" URI=\"#S0-SignedProperties\">\n");
			sigValue.append("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			sigValue.append("</DigestMethod>\n");
			sigValue.append("<DigestValue>ICuiyQ3aM2cClVaiZkPWziY2dhQ=\n");
			sigValue.append("</DigestValue>\n");
			sigValue.append("</Reference>\n");
			sigValue.append("</SignedInfo><SignatureValue Id=\"S0-SIG\">aIYwqOqwvMq9PtiNvPkBfIyWODYc9g3qNIJyZYIh/XFtPstj6FU7/6gWpkCOQcf/\n");
			sigValue.append("sqr2OIAQ/Q0IHZsYxrfKFJTn3MkXYtBG8bITyQz4D1Qt4rrXCknCtYmIbaBlot2v\n");
			sigValue.append("ThaD/9q3cKqqbh+nwqT+lcXQjFwa17mZQ5NikZPsIek=</SignatureValue>\n");
			sigValue.append("<KeyInfo>\n");
			sigValue.append("<KeyValue>\n");
			sigValue.append("<RSAKeyValue>\n");
			sigValue.append("<Modulus>oe2yI+b9PcOaeWGoZskq9+43TWgme/B/m0/RaleULE0+j6zBA3gsqXU22jEdLkbX\n");
			sigValue.append("NE2fazISbIW6j6Z/BHtD7MpHftuOsjF/pbvJzmxwhrabziIMsnvEZ4CeptCAUaED\n");
			sigValue.append("jZ+zopKPxv6+mVF8SQZsbpnneueizAgnUqpeMiO+TNk=</Modulus>\n");
			sigValue.append("<Exponent>Xodv</Exponent>\n");
			sigValue.append("</RSAKeyValue>\n");
			sigValue.append("</KeyValue>\n");
			sigValue.append("<X509Data><X509Certificate>\n");
			sigValue.append("MIID9TCCAt2gAwIBAgIERapNfDANBgkqhkiG9w0BAQUFADB8MRgwFgYJKoZIhvcN\n");
			sigValue.append("AQkBFglwa2lAc2suZWUxCzAJBgNVBAYTAkVFMSIwIAYDVQQKExlBUyBTZXJ0aWZp\n");
			sigValue.append("dHNlZXJpbWlza2Vza3VzMQ8wDQYDVQQLEwZFU1RFSUQxCjAIBgNVBAQTATExEjAQ\n");
			sigValue.append("BgNVBAMTCUVTVEVJRC1TSzAeFw0wNzAxMTQxNTM0MTlaFw0xMjAxMTMxNjQ0NTBa\n");
			sigValue.append("MIGSMQswCQYDVQQGEwJFRTEPMA0GA1UEChMGRVNURUlEMRowGAYDVQQLExFkaWdp\n");
			sigValue.append("dGFsIHNpZ25hdHVyZTEgMB4GA1UEAxMXTEVNQkVSLEpBQUssMzgwMDUxMzAzMzIx\n");
			sigValue.append("DzANBgNVBAQTBkxFTUJFUjENMAsGA1UEKhMESkFBSzEUMBIGA1UEBRMLMzgwMDUx\n");
			sigValue.append("MzAzMzIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKHtsiPm/T3DmnlhqGbJ\n");
			sigValue.append("KvfuN01oJnvwf5tP0WpXlCxNPo+swQN4LKl1NtoxHS5G1zRNn2syEmyFuo+mfwR7\n");
			sigValue.append("Q+zKR37bjrIxf6W7yc5scIa2m84iDLJ7xGeAnqbQgFGhA42fs6KSj8b+vplRfEkG\n");
			sigValue.append("bG6Z53rnoswIJ1KqXjIjvkzZAgNeh2+jgeswgegwDgYDVR0PAQH/BAQDAgZAMDgG\n");
			sigValue.append("A1UdHwQxMC8wLaAroCmGJ2h0dHA6Ly93d3cuc2suZWUvY3Jscy9lc3RlaWQvZXN0\n");
			sigValue.append("ZWlkLmNybDBRBgNVHSAESjBIMEYGCysGAQQBzh8BAQEBMDcwEgYIKwYBBQUHAgIw\n");
			sigValue.append("BhoEbm9uZTAhBggrBgEFBQcCARYVaHR0cDovL3d3dy5zay5lZS9jcHMvMB8GA1Ud\n");
			sigValue.append("IwQYMBaAFHgXtQX5s1jNWYzeZ15EBkx1hmldMB0GA1UdDgQWBBQ/KxPME2tGYALu\n");
			sigValue.append("43t1hb7iSGbNvDAJBgNVHRMEAjAAMA0GCSqGSIb3DQEBBQUAA4IBAQAlfv17ErRv\n");
			sigValue.append("wflpIZ1K07ffGEKcEKDlXRevVlsUzd4PeYXm5QANur52PADykVPWW6uDeFBJo5xI\n");
			sigValue.append("oYBljPZZWk5GOsa/3kLdDk9ZMeDXCeOG8Z0n+2hKPJu2O/aXxzdi9crTI9jdduHz\n");
			sigValue.append("+oynhFHFRSO9cxFvLHG7Wt0FeUi/RWlUFCvi/sICRyBto/gLcpp7RMdgibHW0jim\n");
			sigValue.append("UqPhq7efiyKh7ADsYnRz1wq55K8VKOXYNx+4jIz4NM25Pl4TFnO5hqkryp6cOnGR\n");
			sigValue.append("JRQeSh4vs5SUaS99159pttKGdzSaYNm0ct+HbWpZZzjME7TvpKGrxWPxt7GbccjP\n");
			sigValue.append("rBCxvVbFY6io</X509Certificate></X509Data></KeyInfo>\n");
			sigValue.append("<Object><QualifyingProperties xmlns=\"http://uri.etsi.org/01903/v1.1.1#\" Target=\"#S0\">\n");
			sigValue.append("Xodv<SignedProperties xmlns=\"http://uri.etsi.org/01903/v1.1.1#\" Id=\"S0-SignedProperties\">\n");
			sigValue.append("<SignedSignatureProperties>\n");
			sigValue.append("<SigningTime>2011-12-11T16:24:41Z</SigningTime>\n");
			sigValue.append("<SigningCertificate>\n");
			sigValue.append("<Cert>\n");
			sigValue.append("<CertDigest>\n");
			sigValue.append("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			sigValue.append("</DigestMethod>\n");
			sigValue.append("<DigestValue>SGjHZq3JauMnxxTz5WtZsuhx3kE=</DigestValue>\n");
			sigValue.append("</CertDigest>\n");
			sigValue.append("<IssuerSerial>\n");
			sigValue.append("<X509IssuerName xmlns=\"http://www.w3.org/2000/09/xmldsig#\">emailAddress=pki@sk.ee/C=EE/O=AS Sertifitseerimiskeskus/OU=ESTEID/SN=1/CN=ESTEID-SK</X509IssuerName>\n");
			sigValue.append("<X509SerialNumber xmlns=\"http://www.w3.org/2000/09/xmldsig#\">1168788860</X509SerialNumber>\n");
			sigValue.append("</IssuerSerial></Cert></SigningCertificate>\n");
			sigValue.append("<SignaturePolicyIdentifier>\n");
			sigValue.append("<SignaturePolicyImplied>\n");
			sigValue.append("</SignaturePolicyImplied>\n");
			sigValue.append("</SignaturePolicyIdentifier>\n");
			sigValue.append("<SignatureProductionPlace>\n");
			sigValue.append("<City></City>\n");
			sigValue.append("<StateOrProvince></StateOrProvince>\n");
			sigValue.append("<PostalCode></PostalCode>\n");
			sigValue.append("<CountryName></CountryName>\n");
			sigValue.append("</SignatureProductionPlace>\n");
			sigValue.append("<SignerRole>\n");
			sigValue.append("<ClaimedRoles>\n");
			sigValue.append("<ClaimedRole></ClaimedRole>\n");
			sigValue.append("</ClaimedRoles>\n");
			sigValue.append("</SignerRole>\n");
			sigValue.append("</SignedSignatureProperties>\n");
			sigValue.append("<SignedDataObjectProperties>\n");
			sigValue.append("</SignedDataObjectProperties>\n");
			sigValue.append("</SignedProperties><UnsignedProperties>\n");
			sigValue.append("<UnsignedSignatureProperties><CompleteCertificateRefs><CertRefs><Cert><CertDigest><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>\n");
			sigValue.append("<DigestValue>gkBzQxlGGptYR4pniNVJEGsgDio=</DigestValue>\n");
			sigValue.append("</CertDigest>\n");
			sigValue.append("<IssuerSerial>\n");
			sigValue.append("<X509IssuerName xmlns=\"http://www.w3.org/2000/09/xmldsig#\">C=EE/O=ESTEID/OU=OCSP/CN=ESTEID-SK OCSP RESPONDER 2005/emailAddress=pki@sk.ee</X509IssuerName>\n");
			sigValue.append("<X509SerialNumber xmlns=\"http://www.w3.org/2000/09/xmldsig#\">1110287041</X509SerialNumber>\n");
			sigValue.append("</IssuerSerial>\n");
			sigValue.append("</Cert></CertRefs></CompleteCertificateRefs><CompleteRevocationRefs>\n");
			sigValue.append("<OCSPRefs>\n");
			sigValue.append("<OCSPRef>\n");
			sigValue.append("<OCSPIdentifier URI=\"#N0\"><ResponderID>C=EE,O=ESTEID,OU=OCSP,CN=ESTEID-SK OCSP RESPONDER 2005,emailAddress=pki@sk.ee</ResponderID>\n");
			sigValue.append("<ProducedAt>2011-12-11T16:24:21Z</ProducedAt>\n");
			sigValue.append("</OCSPIdentifier>\n");
			sigValue.append("<DigestAlgAndValue>\n");
			sigValue.append("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"></DigestMethod>\n");
			sigValue.append("<DigestValue>u+0QtNuqVdPvwyRBbpGh5R+pX9w=</DigestValue>\n");
			sigValue.append("</DigestAlgAndValue></OCSPRef>\n");
			sigValue.append("</OCSPRefs>\n");
			sigValue.append("</CompleteRevocationRefs>\n");
			sigValue.append("<CertificateValues>\n");
			sigValue.append("<EncapsulatedX509Certificate Id=\"S0-RESPONDER_CERT\">MIIDPDCCAiSgAwIBAgIEQi2iwTANBgkqhkiG9w0BAQUFADB8MRgwFgYJKoZIhvcN\n");
			sigValue.append("AQkBFglwa2lAc2suZWUxCzAJBgNVBAYTAkVFMSIwIAYDVQQKExlBUyBTZXJ0aWZp\n");
			sigValue.append("dHNlZXJpbWlza2Vza3VzMQ8wDQYDVQQLEwZFU1RFSUQxCjAIBgNVBAQTATExEjAQ\n");
			sigValue.append("BgNVBAMTCUVTVEVJRC1TSzAeFw0wNTAzMDgxMzA0MDFaFw0xMjAxMTIxMzA0MDFa\n");
			sigValue.append("MG8xCzAJBgNVBAYTAkVFMQ8wDQYDVQQKEwZFU1RFSUQxDTALBgNVBAsTBE9DU1Ax\n");
			sigValue.append("JjAkBgNVBAMTHUVTVEVJRC1TSyBPQ1NQIFJFU1BPTkRFUiAyMDA1MRgwFgYJKoZI\n");
			sigValue.append("hvcNAQkBFglwa2lAc2suZWUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAI8m\n");
			sigValue.append("LeLkRHLxMNCB5Pz8R5DnvPdVxBS91PoHboLnbhjlp1ecByVosjwGpXCGu8tUPuv8\n");
			sigValue.append("1Azgqq97AsSugM1J7Pu0gj4bg0Mf6O/9XyoT7RI7H0BuEn4KJQlFcw7tXizI5KUW\n");
			sigValue.append("FFZ4Qg8kfg0xwrDrLIjusBtRbeRARG3DhH8dgZBpAgMBAAGjVzBVMBMGA1UdJQQM\n");
			sigValue.append("MAoGCCsGAQUFBwMJMB8GA1UdIwQYMBaAFHgXtQX5s1jNWYzeZ15EBkx1hmldMB0G\n");
			sigValue.append("A1UdDgQWBBRM+GJhloJeOPpJDgvA0clxQXdnVTANBgkqhkiG9w0BAQUFAAOCAQEA\n");
			sigValue.append("fD8dP+swtSeigLxL3uUXV/tmQkjre7Ww39Uey71LdtxQ6zC7MDjcsLW13JaU0pRu\n");
			sigValue.append("u/p/eGe6h4/w46tSMsBx/U+D1WnHeCj1ED9SFWwfNQFVz9FkM5JEkPDm7lw5hHox\n");
			sigValue.append("IghRHAC3NMbR3sCrVQA2YELf2WypslROoz8XlRT1LN4pwVehpBeWO7xbQPUtoaxK\n");
			sigValue.append("rSCGumtxtxA3KRJ7POHPTAH4cvipxaZhS1ZcXbKtxsesGW+7KLZirpTBT17ICXEA\n");
			sigValue.append("1CFXDWmJ8MHRhbeNWK3G1PERgTiGtBQV7Z00CzmJPHmb1yfcT27+WZ1W9tRQsjhG\n");
			sigValue.append("EWyMVkNnZooWHIjLpNucQA==</EncapsulatedX509Certificate>\n");
			sigValue.append("</CertificateValues>\n");
			sigValue.append("<RevocationValues><OCSPValues><EncapsulatedOCSPValue Id=\"N0\">\n");
			sigValue.append("MIIBtgoBAKCCAa8wggGrBgkrBgEFBQcwAQEEggGcMIIBmDCCAQGhcTBvMQswCQYD\n");
			sigValue.append("VQQGEwJFRTEPMA0GA1UEChMGRVNURUlEMQ0wCwYDVQQLEwRPQ1NQMSYwJAYDVQQD\n");
			sigValue.append("Ex1FU1RFSUQtU0sgT0NTUCBSRVNQT05ERVIgMjAwNTEYMBYGCSqGSIb3DQEJARYJ\n");
			sigValue.append("cGtpQHNrLmVlGA8yMDExMTIxMTE2MjQyMVowVDBSMD0wCQYFKw4DAhoFAAQUJk2D\n");
			sigValue.append("09/TR+gqtxo/O5Aq31AEQNwEFHgXtQX5s1jNWYzeZ15EBkx1hmldAgRFqk18gAAY\n");
			sigValue.append("DzIwMTExMjExMTYyNDIxWqElMCMwIQYJKwYBBQUHMAECBBR4FTzC7WZh3sNwNA3B\n");
			sigValue.append("/ahGmPjlITANBgkqhkiG9w0BAQUFAAOBgQCOOMBKuW4HESjyIfc1/N8u13hF00oX\n");
			sigValue.append("kA/8YMYpCfGNhNq4EhHVQ/YqQC5ASaIpfw5e29/s2RlwVON6iN0eU41HkGOJipL/\n");
			sigValue.append("bYqnl1zlWveb9mJnnGDbpus/ttzKYMVc6O+EPzE53Z9SDFbNliGUYg5EtuXhjBnF\n");
			sigValue.append("OnHNoUzRf0B4qw==\n");
			sigValue.append("</EncapsulatedOCSPValue>\n");
			sigValue.append("</OCSPValues></RevocationValues></UnsignedSignatureProperties>\n");
			sigValue.append("</UnsignedProperties></QualifyingProperties></Object>\n");
			sigValue.append("</Signature>");

			svc.replaceSignatureInDigiDocContainer(containerPath, "S0", sigValue.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println(containerPath);
		assertTrue(true);
	}

	private String createTestDigiDocContainer() {
		String result = System.getProperty("java.io.tmpdir") + File.separator + java.util.UUID.randomUUID().toString() + ".ddoc";

		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(result), "UTF-8"));
			w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			w.write("<SignedDoc format=\"DIGIDOC-XML\" version=\"1.3\" xmlns=\"http://www.sk.ee/DigiDoc/v1.3.0#\">\n");
			w.write("<DataFile ContentType=\"EMBEDDED_BASE64\" Filename=\"test.txt\" Id=\"D0\" MimeType=\"file\" Size=\"4\" xmlns=\"http://www.sk.ee/DigiDoc/v1.3.0#\">dGVzdA==\n");
			w.write("</DataFile>\n");
			w.write("<Signature Id=\"S0\" xmlns=\"http://www.w3.org/2000/09/xmldsig#\">\n");
			w.write("<SignedInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">\n");
			w.write("<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">\n");
			w.write("</CanonicalizationMethod>\n");
			w.write("<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\">\n");
			w.write("</SignatureMethod>\n");
			w.write("<Reference URI=\"#D0\">\n");
			w.write("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			w.write("</DigestMethod>\n");
			w.write("<DigestValue>KRte0hjAUKp/10l9sa+xS5vctvc=</DigestValue>\n");
			w.write("</Reference>\n");
			w.write("<Reference Type=\"http://uri.etsi.org/01903/v1.1.1#SignedProperties\" URI=\"#S0-SignedProperties\">\n");
			w.write("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			w.write("</DigestMethod>\n");
			w.write("<DigestValue>ICuiyQ3aM2cClVaiZkPWziY2dhQ=\n");
			w.write("</DigestValue>\n");
			w.write("</Reference>\n");
			w.write("</SignedInfo><SignatureValue Id=\"S0-SIG\">0000000000000000000000000000000000000000000000000000000000000000\n");
			w.write("0000000000000000000000000000000000000000000000000000000000000000\n");
			w.write("00000000000000000000000000000000000000000000</SignatureValue>\n");
			w.write("<KeyInfo>\n");
			w.write("<KeyValue>\n");
			w.write("<RSAKeyValue>\n");
			w.write("<Modulus>oe2yI+b9PcOaeWGoZskq9+43TWgme/B/m0/RaleULE0+j6zBA3gsqXU22jEdLkbX\n");
			w.write("NE2fazISbIW6j6Z/BHtD7MpHftuOsjF/pbvJzmxwhrabziIMsnvEZ4CeptCAUaED\n");
			w.write("jZ+zopKPxv6+mVF8SQZsbpnneueizAgnUqpeMiO+TNk=</Modulus>\n");
			w.write("<Exponent>Xodv</Exponent>\n");
			w.write("</RSAKeyValue>\n");
			w.write("</KeyValue>\n");
			w.write("<X509Data><X509Certificate>\n");
			w.write("MIID9TCCAt2gAwIBAgIERapNfDANBgkqhkiG9w0BAQUFADB8MRgwFgYJKoZIhvcN\n");
			w.write("AQkBFglwa2lAc2suZWUxCzAJBgNVBAYTAkVFMSIwIAYDVQQKExlBUyBTZXJ0aWZp\n");
			w.write("dHNlZXJpbWlza2Vza3VzMQ8wDQYDVQQLEwZFU1RFSUQxCjAIBgNVBAQTATExEjAQ\n");
			w.write("BgNVBAMTCUVTVEVJRC1TSzAeFw0wNzAxMTQxNTM0MTlaFw0xMjAxMTMxNjQ0NTBa\n");
			w.write("MIGSMQswCQYDVQQGEwJFRTEPMA0GA1UEChMGRVNURUlEMRowGAYDVQQLExFkaWdp\n");
			w.write("dGFsIHNpZ25hdHVyZTEgMB4GA1UEAxMXTEVNQkVSLEpBQUssMzgwMDUxMzAzMzIx\n");
			w.write("DzANBgNVBAQTBkxFTUJFUjENMAsGA1UEKhMESkFBSzEUMBIGA1UEBRMLMzgwMDUx\n");
			w.write("MzAzMzIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKHtsiPm/T3DmnlhqGbJ\n");
			w.write("KvfuN01oJnvwf5tP0WpXlCxNPo+swQN4LKl1NtoxHS5G1zRNn2syEmyFuo+mfwR7\n");
			w.write("Q+zKR37bjrIxf6W7yc5scIa2m84iDLJ7xGeAnqbQgFGhA42fs6KSj8b+vplRfEkG\n");
			w.write("bG6Z53rnoswIJ1KqXjIjvkzZAgNeh2+jgeswgegwDgYDVR0PAQH/BAQDAgZAMDgG\n");
			w.write("A1UdHwQxMC8wLaAroCmGJ2h0dHA6Ly93d3cuc2suZWUvY3Jscy9lc3RlaWQvZXN0\n");
			w.write("ZWlkLmNybDBRBgNVHSAESjBIMEYGCysGAQQBzh8BAQEBMDcwEgYIKwYBBQUHAgIw\n");
			w.write("BhoEbm9uZTAhBggrBgEFBQcCARYVaHR0cDovL3d3dy5zay5lZS9jcHMvMB8GA1Ud\n");
			w.write("IwQYMBaAFHgXtQX5s1jNWYzeZ15EBkx1hmldMB0GA1UdDgQWBBQ/KxPME2tGYALu\n");
			w.write("43t1hb7iSGbNvDAJBgNVHRMEAjAAMA0GCSqGSIb3DQEBBQUAA4IBAQAlfv17ErRv\n");
			w.write("wflpIZ1K07ffGEKcEKDlXRevVlsUzd4PeYXm5QANur52PADykVPWW6uDeFBJo5xI\n");
			w.write("oYBljPZZWk5GOsa/3kLdDk9ZMeDXCeOG8Z0n+2hKPJu2O/aXxzdi9crTI9jdduHz\n");
			w.write("+oynhFHFRSO9cxFvLHG7Wt0FeUi/RWlUFCvi/sICRyBto/gLcpp7RMdgibHW0jim\n");
			w.write("UqPhq7efiyKh7ADsYnRz1wq55K8VKOXYNx+4jIz4NM25Pl4TFnO5hqkryp6cOnGR\n");
			w.write("JRQeSh4vs5SUaS99159pttKGdzSaYNm0ct+HbWpZZzjME7TvpKGrxWPxt7GbccjP\n");
			w.write("rBCxvVbFY6io</X509Certificate></X509Data></KeyInfo>\n");
			w.write("<Object><QualifyingProperties xmlns=\"http://uri.etsi.org/01903/v1.1.1#\" Target=\"#S0\">\n");
			w.write("Xodv<SignedProperties xmlns=\"http://uri.etsi.org/01903/v1.1.1#\" Id=\"S0-SignedProperties\">\n");
			w.write("<SignedSignatureProperties>\n");
			w.write("<SigningTime>2011-12-11T16:24:41Z</SigningTime>\n");
			w.write("<SigningCertificate>\n");
			w.write("<Cert>\n");
			w.write("<CertDigest>\n");
			w.write("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			w.write("</DigestMethod>\n");
			w.write("<DigestValue>SGjHZq3JauMnxxTz5WtZsuhx3kE=</DigestValue>\n");
			w.write("</CertDigest>\n");
			w.write("<IssuerSerial>\n");
			w.write("<X509IssuerName xmlns=\"http://www.w3.org/2000/09/xmldsig#\">emailAddress=pki@sk.ee/C=EE/O=AS Sertifitseerimiskeskus/OU=ESTEID/SN=1/CN=ESTEID-SK</X509IssuerName>\n");
			w.write("<X509SerialNumber xmlns=\"http://www.w3.org/2000/09/xmldsig#\">1168788860</X509SerialNumber>\n");
			w.write("</IssuerSerial></Cert></SigningCertificate>\n");
			w.write("<SignaturePolicyIdentifier>\n");
			w.write("<SignaturePolicyImplied>\n");
			w.write("</SignaturePolicyImplied>\n");
			w.write("</SignaturePolicyIdentifier>\n");
			w.write("<SignatureProductionPlace>\n");
			w.write("<City></City>\n");
			w.write("<StateOrProvince></StateOrProvince>\n");
			w.write("<PostalCode></PostalCode>\n");
			w.write("<CountryName></CountryName>\n");
			w.write("</SignatureProductionPlace>\n");
			w.write("<SignerRole>\n");
			w.write("<ClaimedRoles>\n");
			w.write("<ClaimedRole></ClaimedRole>\n");
			w.write("</ClaimedRoles>\n");
			w.write("</SignerRole>\n");
			w.write("</SignedSignatureProperties>\n");
			w.write("<SignedDataObjectProperties>\n");
			w.write("</SignedDataObjectProperties>\n");
			w.write("</SignedProperties><UnsignedProperties>\n");
			w.write("<UnsignedSignatureProperties><CompleteCertificateRefs><CertRefs><Cert><CertDigest><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>\n");
			w.write("<DigestValue>gkBzQxlGGptYR4pniNVJEGsgDio=</DigestValue>\n");
			w.write("</CertDigest>\n");
			w.write("<IssuerSerial>\n");
			w.write("<X509IssuerName xmlns=\"http://www.w3.org/2000/09/xmldsig#\">C=EE/O=ESTEID/OU=OCSP/CN=ESTEID-SK OCSP RESPONDER 2005/emailAddress=pki@sk.ee</X509IssuerName>\n");
			w.write("<X509SerialNumber xmlns=\"http://www.w3.org/2000/09/xmldsig#\">1110287041</X509SerialNumber>\n");
			w.write("</IssuerSerial>\n");
			w.write("</Cert></CertRefs></CompleteCertificateRefs><CompleteRevocationRefs>\n");
			w.write("<OCSPRefs>\n");
			w.write("<OCSPRef>\n");
			w.write("<OCSPIdentifier URI=\"#N0\"><ResponderID>C=EE,O=ESTEID,OU=OCSP,CN=ESTEID-SK OCSP RESPONDER 2005,emailAddress=pki@sk.ee</ResponderID>\n");
			w.write("<ProducedAt>2011-12-11T16:24:21Z</ProducedAt>\n");
			w.write("</OCSPIdentifier>\n");
			w.write("<DigestAlgAndValue>\n");
			w.write("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"></DigestMethod>\n");
			w.write("<DigestValue>u+0QtNuqVdPvwyRBbpGh5R+pX9w=</DigestValue>\n");
			w.write("</DigestAlgAndValue></OCSPRef>\n");
			w.write("</OCSPRefs>\n");
			w.write("</CompleteRevocationRefs>\n");
			w.write("<CertificateValues>\n");
			w.write("<EncapsulatedX509Certificate Id=\"S0-RESPONDER_CERT\">MIIDPDCCAiSgAwIBAgIEQi2iwTANBgkqhkiG9w0BAQUFADB8MRgwFgYJKoZIhvcN\n");
			w.write("AQkBFglwa2lAc2suZWUxCzAJBgNVBAYTAkVFMSIwIAYDVQQKExlBUyBTZXJ0aWZp\n");
			w.write("dHNlZXJpbWlza2Vza3VzMQ8wDQYDVQQLEwZFU1RFSUQxCjAIBgNVBAQTATExEjAQ\n");
			w.write("BgNVBAMTCUVTVEVJRC1TSzAeFw0wNTAzMDgxMzA0MDFaFw0xMjAxMTIxMzA0MDFa\n");
			w.write("MG8xCzAJBgNVBAYTAkVFMQ8wDQYDVQQKEwZFU1RFSUQxDTALBgNVBAsTBE9DU1Ax\n");
			w.write("JjAkBgNVBAMTHUVTVEVJRC1TSyBPQ1NQIFJFU1BPTkRFUiAyMDA1MRgwFgYJKoZI\n");
			w.write("hvcNAQkBFglwa2lAc2suZWUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAI8m\n");
			w.write("LeLkRHLxMNCB5Pz8R5DnvPdVxBS91PoHboLnbhjlp1ecByVosjwGpXCGu8tUPuv8\n");
			w.write("1Azgqq97AsSugM1J7Pu0gj4bg0Mf6O/9XyoT7RI7H0BuEn4KJQlFcw7tXizI5KUW\n");
			w.write("FFZ4Qg8kfg0xwrDrLIjusBtRbeRARG3DhH8dgZBpAgMBAAGjVzBVMBMGA1UdJQQM\n");
			w.write("MAoGCCsGAQUFBwMJMB8GA1UdIwQYMBaAFHgXtQX5s1jNWYzeZ15EBkx1hmldMB0G\n");
			w.write("A1UdDgQWBBRM+GJhloJeOPpJDgvA0clxQXdnVTANBgkqhkiG9w0BAQUFAAOCAQEA\n");
			w.write("fD8dP+swtSeigLxL3uUXV/tmQkjre7Ww39Uey71LdtxQ6zC7MDjcsLW13JaU0pRu\n");
			w.write("u/p/eGe6h4/w46tSMsBx/U+D1WnHeCj1ED9SFWwfNQFVz9FkM5JEkPDm7lw5hHox\n");
			w.write("IghRHAC3NMbR3sCrVQA2YELf2WypslROoz8XlRT1LN4pwVehpBeWO7xbQPUtoaxK\n");
			w.write("rSCGumtxtxA3KRJ7POHPTAH4cvipxaZhS1ZcXbKtxsesGW+7KLZirpTBT17ICXEA\n");
			w.write("1CFXDWmJ8MHRhbeNWK3G1PERgTiGtBQV7Z00CzmJPHmb1yfcT27+WZ1W9tRQsjhG\n");
			w.write("EWyMVkNnZooWHIjLpNucQA==</EncapsulatedX509Certificate>\n");
			w.write("</CertificateValues>\n");
			w.write("<RevocationValues><OCSPValues><EncapsulatedOCSPValue Id=\"N0\">\n");
			w.write("MIIBtgoBAKCCAa8wggGrBgkrBgEFBQcwAQEEggGcMIIBmDCCAQGhcTBvMQswCQYD\n");
			w.write("VQQGEwJFRTEPMA0GA1UEChMGRVNURUlEMQ0wCwYDVQQLEwRPQ1NQMSYwJAYDVQQD\n");
			w.write("Ex1FU1RFSUQtU0sgT0NTUCBSRVNQT05ERVIgMjAwNTEYMBYGCSqGSIb3DQEJARYJ\n");
			w.write("cGtpQHNrLmVlGA8yMDExMTIxMTE2MjQyMVowVDBSMD0wCQYFKw4DAhoFAAQUJk2D\n");
			w.write("09/TR+gqtxo/O5Aq31AEQNwEFHgXtQX5s1jNWYzeZ15EBkx1hmldAgRFqk18gAAY\n");
			w.write("DzIwMTExMjExMTYyNDIxWqElMCMwIQYJKwYBBQUHMAECBBR4FTzC7WZh3sNwNA3B\n");
			w.write("/ahGmPjlITANBgkqhkiG9w0BAQUFAAOBgQCOOMBKuW4HESjyIfc1/N8u13hF00oX\n");
			w.write("kA/8YMYpCfGNhNq4EhHVQ/YqQC5ASaIpfw5e29/s2RlwVON6iN0eU41HkGOJipL/\n");
			w.write("bYqnl1zlWveb9mJnnGDbpus/ttzKYMVc6O+EPzE53Z9SDFbNliGUYg5EtuXhjBnF\n");
			w.write("OnHNoUzRf0B4qw==\n");
			w.write("</EncapsulatedOCSPValue>\n");
			w.write("</OCSPValues></RevocationValues></UnsignedSignatureProperties>\n");
			w.write("</UnsignedProperties></QualifyingProperties></Object>\n");
			w.write("</Signature>\n");
			w.write("<Signature Id=\"S1\" xmlns=\"http://www.w3.org/2000/09/xmldsig#\">\n");
			w.write("<SignedInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">\n");
			w.write("<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">\n");
			w.write("</CanonicalizationMethod>\n");
			w.write("<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\">\n");
			w.write("</SignatureMethod>\n");
			w.write("<Reference URI=\"#D0\">\n");
			w.write("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			w.write("</DigestMethod>\n");
			w.write("<DigestValue>KRte0hjAUKp/10l9sa+xS5vctvc=</DigestValue>\n");
			w.write("</Reference>\n");
			w.write("<Reference Type=\"http://uri.etsi.org/01903/v1.1.1#SignedProperties\" URI=\"#S1-SignedProperties\">\n");
			w.write("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			w.write("</DigestMethod>\n");
			w.write("<DigestValue>ICuiyQ3aM2cClVaiZkPWziY2dhQ=\n");
			w.write("</DigestValue>\n");
			w.write("</Reference>\n");
			w.write("</SignedInfo><SignatureValue Id=\"S1-SIG\">0000000000000000000000000000000000000000000000000000000000000000\n");
			w.write("0000000000000000000000000000000000000000000000000000000000000000\n");
			w.write("00000000000000000000000000000000000000000000</SignatureValue>\n");
			w.write("<KeyInfo>\n");
			w.write("<KeyValue>\n");
			w.write("<RSAKeyValue>\n");
			w.write("<Modulus>oe2yI+b9PcOaeWGoZskq9+43TWgme/B/m0/RaleULE0+j6zBA3gsqXU22jEdLkbX\n");
			w.write("NE2fazISbIW6j6Z/BHtD7MpHftuOsjF/pbvJzmxwhrabziIMsnvEZ4CeptCAUaED\n");
			w.write("jZ+zopKPxv6+mVF8SQZsbpnneueizAgnUqpeMiO+TNk=</Modulus>\n");
			w.write("<Exponent>Xodv</Exponent>\n");
			w.write("</RSAKeyValue>\n");
			w.write("</KeyValue>\n");
			w.write("<X509Data><X509Certificate>\n");
			w.write("MIID9TCCAt2gAwIBAgIERapNfDANBgkqhkiG9w0BAQUFADB8MRgwFgYJKoZIhvcN\n");
			w.write("AQkBFglwa2lAc2suZWUxCzAJBgNVBAYTAkVFMSIwIAYDVQQKExlBUyBTZXJ0aWZp\n");
			w.write("dHNlZXJpbWlza2Vza3VzMQ8wDQYDVQQLEwZFU1RFSUQxCjAIBgNVBAQTATExEjAQ\n");
			w.write("BgNVBAMTCUVTVEVJRC1TSzAeFw0wNzAxMTQxNTM0MTlaFw0xMjAxMTMxNjQ0NTBa\n");
			w.write("MIGSMQswCQYDVQQGEwJFRTEPMA0GA1UEChMGRVNURUlEMRowGAYDVQQLExFkaWdp\n");
			w.write("dGFsIHNpZ25hdHVyZTEgMB4GA1UEAxMXTEVNQkVSLEpBQUssMzgwMDUxMzAzMzIx\n");
			w.write("DzANBgNVBAQTBkxFTUJFUjENMAsGA1UEKhMESkFBSzEUMBIGA1UEBRMLMzgwMDUx\n");
			w.write("MzAzMzIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKHtsiPm/T3DmnlhqGbJ\n");
			w.write("KvfuN01oJnvwf5tP0WpXlCxNPo+swQN4LKl1NtoxHS5G1zRNn2syEmyFuo+mfwR7\n");
			w.write("Q+zKR37bjrIxf6W7yc5scIa2m84iDLJ7xGeAnqbQgFGhA42fs6KSj8b+vplRfEkG\n");
			w.write("bG6Z53rnoswIJ1KqXjIjvkzZAgNeh2+jgeswgegwDgYDVR0PAQH/BAQDAgZAMDgG\n");
			w.write("A1UdHwQxMC8wLaAroCmGJ2h0dHA6Ly93d3cuc2suZWUvY3Jscy9lc3RlaWQvZXN0\n");
			w.write("ZWlkLmNybDBRBgNVHSAESjBIMEYGCysGAQQBzh8BAQEBMDcwEgYIKwYBBQUHAgIw\n");
			w.write("BhoEbm9uZTAhBggrBgEFBQcCARYVaHR0cDovL3d3dy5zay5lZS9jcHMvMB8GA1Ud\n");
			w.write("IwQYMBaAFHgXtQX5s1jNWYzeZ15EBkx1hmldMB0GA1UdDgQWBBQ/KxPME2tGYALu\n");
			w.write("43t1hb7iSGbNvDAJBgNVHRMEAjAAMA0GCSqGSIb3DQEBBQUAA4IBAQAlfv17ErRv\n");
			w.write("wflpIZ1K07ffGEKcEKDlXRevVlsUzd4PeYXm5QANur52PADykVPWW6uDeFBJo5xI\n");
			w.write("oYBljPZZWk5GOsa/3kLdDk9ZMeDXCeOG8Z0n+2hKPJu2O/aXxzdi9crTI9jdduHz\n");
			w.write("+oynhFHFRSO9cxFvLHG7Wt0FeUi/RWlUFCvi/sICRyBto/gLcpp7RMdgibHW0jim\n");
			w.write("UqPhq7efiyKh7ADsYnRz1wq55K8VKOXYNx+4jIz4NM25Pl4TFnO5hqkryp6cOnGR\n");
			w.write("JRQeSh4vs5SUaS99159pttKGdzSaYNm0ct+HbWpZZzjME7TvpKGrxWPxt7GbccjP\n");
			w.write("rBCxvVbFY6io</X509Certificate></X509Data></KeyInfo>\n");
			w.write("<Object><QualifyingProperties xmlns=\"http://uri.etsi.org/01903/v1.1.1#\" Target=\"#S1\">\n");
			w.write("Xodv<SignedProperties xmlns=\"http://uri.etsi.org/01903/v1.1.1#\" Id=\"S1-SignedProperties\">\n");
			w.write("<SignedSignatureProperties>\n");
			w.write("<SigningTime>2011-12-11T16:24:41Z</SigningTime>\n");
			w.write("<SigningCertificate>\n");
			w.write("<Cert>\n");
			w.write("<CertDigest>\n");
			w.write("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">\n");
			w.write("</DigestMethod>\n");
			w.write("<DigestValue>SGjHZq3JauMnxxTz5WtZsuhx3kE=</DigestValue>\n");
			w.write("</CertDigest>\n");
			w.write("<IssuerSerial>\n");
			w.write("<X509IssuerName xmlns=\"http://www.w3.org/2000/09/xmldsig#\">emailAddress=pki@sk.ee/C=EE/O=AS Sertifitseerimiskeskus/OU=ESTEID/SN=1/CN=ESTEID-SK</X509IssuerName>\n");
			w.write("<X509SerialNumber xmlns=\"http://www.w3.org/2000/09/xmldsig#\">1168788860</X509SerialNumber>\n");
			w.write("</IssuerSerial></Cert></SigningCertificate>\n");
			w.write("<SignaturePolicyIdentifier>\n");
			w.write("<SignaturePolicyImplied>\n");
			w.write("</SignaturePolicyImplied>\n");
			w.write("</SignaturePolicyIdentifier>\n");
			w.write("<SignatureProductionPlace>\n");
			w.write("<City></City>\n");
			w.write("<StateOrProvince></StateOrProvince>\n");
			w.write("<PostalCode></PostalCode>\n");
			w.write("<CountryName></CountryName>\n");
			w.write("</SignatureProductionPlace>\n");
			w.write("<SignerRole>\n");
			w.write("<ClaimedRoles>\n");
			w.write("<ClaimedRole></ClaimedRole>\n");
			w.write("</ClaimedRoles>\n");
			w.write("</SignerRole>\n");
			w.write("</SignedSignatureProperties>\n");
			w.write("<SignedDataObjectProperties>\n");
			w.write("</SignedDataObjectProperties>\n");
			w.write("</SignedProperties><UnsignedProperties>\n");
			w.write("<UnsignedSignatureProperties><CompleteCertificateRefs><CertRefs><Cert><CertDigest><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>\n");
			w.write("<DigestValue>gkBzQxlGGptYR4pniNVJEGsgDio=</DigestValue>\n");
			w.write("</CertDigest>\n");
			w.write("<IssuerSerial>\n");
			w.write("<X509IssuerName xmlns=\"http://www.w3.org/2000/09/xmldsig#\">C=EE/O=ESTEID/OU=OCSP/CN=ESTEID-SK OCSP RESPONDER 2005/emailAddress=pki@sk.ee</X509IssuerName>\n");
			w.write("<X509SerialNumber xmlns=\"http://www.w3.org/2000/09/xmldsig#\">1110287041</X509SerialNumber>\n");
			w.write("</IssuerSerial>\n");
			w.write("</Cert></CertRefs></CompleteCertificateRefs><CompleteRevocationRefs>\n");
			w.write("<OCSPRefs>\n");
			w.write("<OCSPRef>\n");
			w.write("<OCSPIdentifier URI=\"#N0\"><ResponderID>C=EE,O=ESTEID,OU=OCSP,CN=ESTEID-SK OCSP RESPONDER 2005,emailAddress=pki@sk.ee</ResponderID>\n");
			w.write("<ProducedAt>2011-12-11T16:24:21Z</ProducedAt>\n");
			w.write("</OCSPIdentifier>\n");
			w.write("<DigestAlgAndValue>\n");
			w.write("<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"></DigestMethod>\n");
			w.write("<DigestValue>u+0QtNuqVdPvwyRBbpGh5R+pX9w=</DigestValue>\n");
			w.write("</DigestAlgAndValue></OCSPRef>\n");
			w.write("</OCSPRefs>\n");
			w.write("</CompleteRevocationRefs>\n");
			w.write("<CertificateValues>\n");
			w.write("<EncapsulatedX509Certificate Id=\"S1-RESPONDER_CERT\">MIIDPDCCAiSgAwIBAgIEQi2iwTANBgkqhkiG9w0BAQUFADB8MRgwFgYJKoZIhvcN\n");
			w.write("AQkBFglwa2lAc2suZWUxCzAJBgNVBAYTAkVFMSIwIAYDVQQKExlBUyBTZXJ0aWZp\n");
			w.write("dHNlZXJpbWlza2Vza3VzMQ8wDQYDVQQLEwZFU1RFSUQxCjAIBgNVBAQTATExEjAQ\n");
			w.write("BgNVBAMTCUVTVEVJRC1TSzAeFw0wNTAzMDgxMzA0MDFaFw0xMjAxMTIxMzA0MDFa\n");
			w.write("MG8xCzAJBgNVBAYTAkVFMQ8wDQYDVQQKEwZFU1RFSUQxDTALBgNVBAsTBE9DU1Ax\n");
			w.write("JjAkBgNVBAMTHUVTVEVJRC1TSyBPQ1NQIFJFU1BPTkRFUiAyMDA1MRgwFgYJKoZI\n");
			w.write("hvcNAQkBFglwa2lAc2suZWUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAI8m\n");
			w.write("LeLkRHLxMNCB5Pz8R5DnvPdVxBS91PoHboLnbhjlp1ecByVosjwGpXCGu8tUPuv8\n");
			w.write("1Azgqq97AsSugM1J7Pu0gj4bg0Mf6O/9XyoT7RI7H0BuEn4KJQlFcw7tXizI5KUW\n");
			w.write("FFZ4Qg8kfg0xwrDrLIjusBtRbeRARG3DhH8dgZBpAgMBAAGjVzBVMBMGA1UdJQQM\n");
			w.write("MAoGCCsGAQUFBwMJMB8GA1UdIwQYMBaAFHgXtQX5s1jNWYzeZ15EBkx1hmldMB0G\n");
			w.write("A1UdDgQWBBRM+GJhloJeOPpJDgvA0clxQXdnVTANBgkqhkiG9w0BAQUFAAOCAQEA\n");
			w.write("fD8dP+swtSeigLxL3uUXV/tmQkjre7Ww39Uey71LdtxQ6zC7MDjcsLW13JaU0pRu\n");
			w.write("u/p/eGe6h4/w46tSMsBx/U+D1WnHeCj1ED9SFWwfNQFVz9FkM5JEkPDm7lw5hHox\n");
			w.write("IghRHAC3NMbR3sCrVQA2YELf2WypslROoz8XlRT1LN4pwVehpBeWO7xbQPUtoaxK\n");
			w.write("rSCGumtxtxA3KRJ7POHPTAH4cvipxaZhS1ZcXbKtxsesGW+7KLZirpTBT17ICXEA\n");
			w.write("1CFXDWmJ8MHRhbeNWK3G1PERgTiGtBQV7Z00CzmJPHmb1yfcT27+WZ1W9tRQsjhG\n");
			w.write("EWyMVkNnZooWHIjLpNucQA==</EncapsulatedX509Certificate>\n");
			w.write("</CertificateValues>\n");
			w.write("<RevocationValues><OCSPValues><EncapsulatedOCSPValue Id=\"N0\">\n");
			w.write("MIIBtgoBAKCCAa8wggGrBgkrBgEFBQcwAQEEggGcMIIBmDCCAQGhcTBvMQswCQYD\n");
			w.write("VQQGEwJFRTEPMA0GA1UEChMGRVNURUlEMQ0wCwYDVQQLEwRPQ1NQMSYwJAYDVQQD\n");
			w.write("Ex1FU1RFSUQtU0sgT0NTUCBSRVNQT05ERVIgMjAwNTEYMBYGCSqGSIb3DQEJARYJ\n");
			w.write("cGtpQHNrLmVlGA8yMDExMTIxMTE2MjQyMVowVDBSMD0wCQYFKw4DAhoFAAQUJk2D\n");
			w.write("09/TR+gqtxo/O5Aq31AEQNwEFHgXtQX5s1jNWYzeZ15EBkx1hmldAgRFqk18gAAY\n");
			w.write("DzIwMTExMjExMTYyNDIxWqElMCMwIQYJKwYBBQUHMAECBBR4FTzC7WZh3sNwNA3B\n");
			w.write("/ahGmPjlITANBgkqhkiG9w0BAQUFAAOBgQCOOMBKuW4HESjyIfc1/N8u13hF00oX\n");
			w.write("kA/8YMYpCfGNhNq4EhHVQ/YqQC5ASaIpfw5e29/s2RlwVON6iN0eU41HkGOJipL/\n");
			w.write("bYqnl1zlWveb9mJnnGDbpus/ttzKYMVc6O+EPzE53Z9SDFbNliGUYg5EtuXhjBnF\n");
			w.write("OnHNoUzRf0B4qw==\n");
			w.write("</EncapsulatedOCSPValue>\n");
			w.write("</OCSPValues></RevocationValues></UnsignedSignatureProperties>\n");
			w.write("</UnsignedProperties></QualifyingProperties></Object>\n");
			w.write("</Signature>\n");
			w.write("</SignedDoc>\n");
		} catch (Exception ex) {
			ex.printStackTrace();
        } finally {
        	Util.safeCloseWriter(w);
        	w = null;
        }

		return result;
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
 * methods =
 * package = ee.adit.test.service
 * package.sourceFolder = adit-war/src/test/java
 * superclassType = junit.framework.TestCase
 * testCase = DocumentServiceTest
 * testClassType = ee.adit.service.DocumentService
 */