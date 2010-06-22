package ee.adit.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaveDocumentAttachmentParser extends DefaultHandler {

	private String tempDir;
	
	private boolean open;
	
	private OutputStream stream;
	
	private List<String> files;
	
	private int fileCount;
	
	public SaveDocumentAttachmentParser(String tempDir) {
		super();
		this.setTempDir(tempDir);
		this.setFileCount(1);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			if(localName.equalsIgnoreCase("data")) {
				// Check if tag open
				// if tag open, then exception
				if(this.isOpen()) {
					throw new Exception("Tag already open. Invalid XML.");
				}
				String outputFileName = Util.generateRandomFileNameWithoutExtension();
				outputFileName = this.getTempDir() + File.separator + outputFileName + "_" + this.getFileCount() + "_SDv1.adit";
				FileOutputStream fileOutputStream = new FileOutputStream(outputFileName);
				this.setStream(fileOutputStream);
				this.setOpen(true);
				this.addFile(outputFileName);
				this.incrementFileCount();
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// if <data> tag open, then write to outputstream
		if(this.isOpen()) {
			
			System.out.println("start: " + start);
			System.out.println("length: " + length);
			String str = new String(ch, start, length);
			System.out.println("String: " + str);
			
			if(str != null && !str.trim().equalsIgnoreCase("")) {
				try {
					str = str.replaceAll(" ", "");
					str = str.replaceAll("\n", "");
					str = str.replaceAll("\t", "");
					byte[] strBuf = str.getBytes("UTF-8");
					this.getStream().write(strBuf);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// Stop writing data to file
		// 1. Close the stream
		if(localName.equalsIgnoreCase("data")) {
			try {
				if(this.isOpen()) {
					this.getStream().close();
				}
				this.setOpen(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void endDocument() throws SAXException {
		if(this.getFiles() != null && this.getFiles().size() > 0) {
			for(int i = 0; i < this.getFiles().size(); i++) {
				System.out.println("File: " + this.getFiles().get(i));
			}
		}
	}
	
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public OutputStream getStream() {
		return stream;
	}

	public void setStream(OutputStream stream) {
		this.stream = stream;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public void addFile(String file) {
		if(this.getFiles() == null) {
			this.setFiles(new ArrayList<String>());
		}
		this.getFiles().add(file);
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
	
	private void incrementFileCount() {
		this.fileCount++;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
	
}
