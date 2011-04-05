package ee.adit.test.util;

import ee.adit.util.Util;
import junit.framework.TestCase;

/**
 * The class <code>UtilTest</code> contains tests for the class {@link
 * <code>Util</code>}
 *
 * @pattern JUnit Test Case
 *
 * @generatedBy CodePro at 31.03.11 17:36
 *
 * @author Jaak
 *
 * @version $Revision$
 */
public class UtilTest extends TestCase {

	/**
	 * Construct new test instance
	 *
	 * @param name the test name
	 */
	public UtilTest(String name) {
		super(name);
	}

	/**
	 * Run the String getFileExtension(String) method test
	 */
	public void testGetFileExtension() {
		assertNull(Util.getFileExtension(null));
		assertNull(Util.getFileExtension(""));
		assertNull(Util.getFileExtension("."));
		assertNull(Util.getFileExtension("test"));
		assertEquals("txt", Util.getFileExtension("test.txt"));
		assertEquals("svn", Util.getFileExtension(".svn"));
		assertEquals("TXT", Util.getFileExtension("test.TXT"));
		assertEquals("gz", Util.getFileExtension("test.txt.gz"));
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
 * methods = getFileExtension(QString;)
 * package = ee.adit.test.util
 * package.sourceFolder = adit-war/src/test/java
 * superclassType = junit.framework.TestCase
 * testCase = UtilTest
 * testClassType = ee.adit.util.Util
 */