package ee.adit.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import ee.adit.dao.DocumentDAO;
import ee.adit.pojo.OutputDocumentFile;

public class SimplifiedDigiDocParser {
	private static Logger logger = Logger.getLogger(SimplifiedDigiDocParser.class);
	
	public static Hashtable<String, StartEndOffsetPair> findDigiDocDataFileOffsets(String pathToDigiDoc) throws IOException {
		Hashtable<String, StartEndOffsetPair> result = new Hashtable<String, StartEndOffsetPair>();
		
        FileInputStream inStream = null;
        InputStreamReader textReader = null;
        BufferedReader bufferedReader = null;
        
        try {
            inStream = new FileInputStream(pathToDigiDoc);
            textReader = new InputStreamReader(inStream, "UTF-8");
            bufferedReader = new BufferedReader(textReader);
            
            String pendingDataFileId = null;
            StartEndOffsetPair pendingOffsetPair = null;
            
            long currentOffset = 0;
            boolean inTag = false;
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
                        } else if ("</DataFile>".equalsIgnoreCase(tagText.toString())) {
                        	pendingOffsetPair.setEnd(currentOffset - 11);
                        	result.put(pendingDataFileId, pendingOffsetPair);
                        }
                        tagText.delete(0, tagText.length());
                        break;
                    default:
                        if (inTag) {
                            tagText.append(currentChar[0]);
                        }
                        break;
                }
            }
            
        } finally {
            Util.safeCloseReader(bufferedReader);
            Util.safeCloseReader(textReader);
            Util.safeCloseStream(inStream);
            inStream = null;
            textReader = null;
            bufferedReader = null;
        }
		
		return result;
	}
	
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
    
    public static void extractFileContentsFromDdoc(
		InputStream ddocContainerAsStream,
		List<OutputDocumentFile> files,
		final String temporaryFilesDir) throws IOException {
    	
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
		    		logger.info("Extracting file contents from signature container. File ID: " + file.getId());
                	
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
	                logger.info("File contnts extracted into file: " + outputFileName);
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
    }
}
