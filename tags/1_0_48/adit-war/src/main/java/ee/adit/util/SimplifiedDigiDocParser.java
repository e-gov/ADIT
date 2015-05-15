package ee.adit.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;

import ee.adit.pojo.OutputDocumentFile;

/**
 * Class containing methods for DigiDoc file manipulation (mainly for
 * data file extraction and finding offsets of data file contents).
 *
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public final class SimplifiedDigiDocParser {
	/**
	 * Default constructor.
	 */
	private SimplifiedDigiDocParser() {
	}

	private static Logger logger = Logger.getLogger(SimplifiedDigiDocParser.class);

	/**
	 * Finds offsets of all data files in specified DigiDoc container.
	 *
	 * @param pathToDigiDoc
	 *     Full path to DigiDoc container
	 * @return
	 *     Hashtable containing data file offsets
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static Hashtable<String, StartEndOffsetPair> findDigiDocDataFileOffsets(
		String pathToDigiDoc) throws IOException, NoSuchAlgorithmException {

		Hashtable<String, StartEndOffsetPair> result = new Hashtable<String, StartEndOffsetPair>();

        FileInputStream inStream = null;
        InputStreamReader textReader = null;
        BufferedReader bufferedReader = null;

        // Helper streams for MD5 hash calculation
    	MessageDigest md5Digest = null;
    	BufferedOutputStream dummyFileOutStream = null;
        DigestOutputStream digestCalculatorStream = null;
        BufferedOutputStream base64OutputStream = null;

        try {
            inStream = new FileInputStream(pathToDigiDoc);
            textReader = new InputStreamReader(inStream, "UTF-8");
            bufferedReader = new BufferedReader(textReader);

            String pendingDataFileId = null;
            StartEndOffsetPair pendingOffsetPair = null;

            long currentOffset = 0;
            boolean inTag = false;
            boolean inFileData = false;
            char[] currentChar = new char[1];
            StringBuilder tagText = new StringBuilder(512);

            while (bufferedReader.read(currentChar, 0, currentChar.length) > 0) {
                currentOffset++;
            	switch (currentChar[0]) {
                    case '<':
                        tagText.append(currentChar[0]);
                        inTag = true;
                        break;
                    case '>':
                        tagText.append(currentChar[0]);
                        inTag = false;
                        if (tagText.toString().startsWith("<DataFile ")) {
                        	pendingDataFileId = getAttributeValueFromTag(tagText.toString(), "Id");
                        	pendingOffsetPair = new StartEndOffsetPair();
                        	pendingOffsetPair.setStart(currentOffset);

                        	inFileData = true;
                        	md5Digest = MessageDigest.getInstance("MD5");
                        	String outFileName = pathToDigiDoc + pendingDataFileId;
                            dummyFileOutStream = new BufferedOutputStream(new FileOutputStream(outFileName, false));
                            digestCalculatorStream = new DigestOutputStream(dummyFileOutStream, md5Digest);
                            base64OutputStream = new BufferedOutputStream(new Base64OutputStream(digestCalculatorStream, false));
                        } else if ("</DataFile>".equalsIgnoreCase(tagText.toString())) {
                        	pendingOffsetPair.setEnd(currentOffset - 11);

                        	inFileData = false;
                        	try {
                        		base64OutputStream.close();
                        		pendingOffsetPair.setDataMd5Hash(digestCalculatorStream.getMessageDigest().digest());
                        	} catch (Exception ex) {
                        		logger.warn("Failed closing MD5 digest stream after digest calculation in DigiDoc extraction");
                        	}
                        	result.put(pendingDataFileId, pendingOffsetPair);
                        }
                        tagText.delete(0, tagText.length());
                        break;
                    default:
                        if (inTag) {
                            tagText.append(currentChar[0]);
                        } else if (inFileData) {
                        	base64OutputStream.write((byte) currentChar[0]);
                        }
                        break;
                }
            }

        } finally {
            Util.safeCloseStream(base64OutputStream);
        	Util.safeCloseReader(bufferedReader);
            Util.safeCloseReader(textReader);
            Util.safeCloseStream(inStream);
            inStream = null;
            textReader = null;
            bufferedReader = null;
            base64OutputStream = null;
        }

		return result;
	}

	/**
	 * Gets XML attribute value.
	 *
	 * @param tag
	 *     XML tag as String
	 * @param attributeName
	 *     Attribute name
	 * @return
	 *     Value of specified attribute
	 */
    private static String getAttributeValueFromTag(String tag, String attributeName) {
        String result = "";

        if (!Util.isNullOrEmpty(tag)) {
            String leadIn = attributeName + "=\"";
            int start = tag.indexOf(leadIn);
            if (start > 0) {
                start += leadIn.length();
                int end = tag.indexOf("\"", start);
                if (end > 0) {
                    result = tag.substring(start, end);
                }
            }
        }

        return result;
    }

    /**
     * Extracts file contents from DigiDoc container.
     *
     * @param ddocContainerAsStream
     *     DigiDoc container as {@link InputStream}
     * @param files
     *     Files to be extracted
     * @param temporaryFilesDir
     *     Path of applications temporary files folder
     * @return
     *     Total bytes extracted
     * @throws IOException
     */
    public static long extractFileContentsFromDdoc(
		InputStream ddocContainerAsStream,
		List<OutputDocumentFile> files,
		final String temporaryFilesDir) throws IOException {

    	long totalBytesExtracted = 0L;

    	// Make sure that offsets are not null so that sorting in next
    	// step would have more reliable input data.
    	for (OutputDocumentFile file : files) {
    		if (file.getDdocDataFileStartOffset() == null) {
    			file.setDdocDataFileStartOffset(0L);
    		}
    		if (file.getDdocDataFileEndOffset() == null) {
    			file.setDdocDataFileEndOffset(0L);
    		}
    	}

    	// Make sure that files are sorted by start offset
    	Collections.sort(files);

        InputStreamReader textReader = null;
        BufferedReader bufferedReader = null;
        FileOutputStream outStream = null;
        OutputStreamWriter textWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
        	textReader = new InputStreamReader(ddocContainerAsStream, "UTF-8");
            bufferedReader = new BufferedReader(textReader);

        	long currentOffset = 0;
	    	for (OutputDocumentFile file : files) {
                if (file.getDdocDataFileStartOffset() > 0L) {
		    		String outputFileName = Util.generateRandomFileNameWithoutExtension();
	                outputFileName = temporaryFilesDir + File.separator + outputFileName + "_" + file.getId() + ".adit";

	                long skipLength = file.getDdocDataFileStartOffset() - currentOffset;
		    		if (skipLength > 0) {
		    			bufferedReader.skip(skipLength);
		    			currentOffset += skipLength;
		    		}

		    		outStream = new FileOutputStream(outputFileName, false);
		    		textWriter = new OutputStreamWriter(outStream, "UTF-8");
		    		bufferedWriter = new BufferedWriter(textWriter);

	                int len = 0;
	                char[] buffer = new char[1];
		    		while ((currentOffset < file.getDdocDataFileEndOffset()) && ((len = bufferedReader.read(buffer)) > 0)) {
	                	currentOffset += len;
	                	bufferedWriter.write(buffer, 0, len);
	                }

	                Util.safeCloseWriter(bufferedWriter);
	                Util.safeCloseWriter(textWriter);
	                Util.safeCloseStream(outStream);

	                file.setSysTempFile(outputFileName);

	                // Lets trust database values instead of decoding base64 data
	                totalBytesExtracted += (file.getSizeBytes() == null) ? 0L : file.getSizeBytes();
                }
	    	}
        } finally {
            Util.safeCloseReader(bufferedReader);
            Util.safeCloseReader(textReader);
            Util.safeCloseWriter(bufferedWriter);
            Util.safeCloseWriter(textWriter);
            Util.safeCloseStream(outStream);
            textReader = null;
            bufferedReader = null;
            outStream = null;
            textWriter = null;
            bufferedWriter = null;
        }

        return totalBytesExtracted;
    }
}
