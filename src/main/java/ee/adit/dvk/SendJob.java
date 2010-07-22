package ee.adit.dvk;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import dvk.api.container.Container;
import dvk.api.container.v2.ContainerVer2;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.exception.AditInternalException;

public class SendJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(SendJob.class);

	public static final int DVK_CONTAINER_VERSION = 2;
	
	private DocumentDAO documentDAO;
	
	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		
		try {
			LOG.info("Executing scheduled job: Send documents to DVK");
			
			// TODO: Fetch all the documents that have document_sharing records that have type "send_dvk" and dvk_status_id is null or "100" (puudub)
			List<Document> documents = this.getDocuments();
			
			if(documents == null || documents.size() == 0) {
				LOG.info("No documents found.");
			} else {
				LOG.info("Number of documents to be sent to DVK: " + documents.size());
				
				ContainerVer2 dvkContainer = new ContainerVer2();
				dvkContainer.setVersion(DVK_CONTAINER_VERSION);
				
				
				
				dvkContainer.save2File("C:\test_dvkcontainer_ver2.xml");
				
			}
			
			// TODO: Construct a DVK XML container for every document that is found
			
			// TODO: Save the document in DVK Client database (including the DVK XML container and recipient data)
			
			// TODO: Save the document DVK_ID to ADIT database
			
		} catch (Exception e) {
			LOG.error("Error executing scheduled DVK sending: ", e);
		}

	}

	private List<Document> getDocuments() {
		return this.getDocumentDAO().getDocumentsForDVK();
	}

	public DocumentDAO getDocumentDAO() {
		return documentDAO;
	}

	public void setDocumentDAO(DocumentDAO documentDAO) {
		this.documentDAO = documentDAO;
	}
	
	public static void main(String[] args) throws MarshalException, ValidationException, IOException, MappingException {
		ContainerVer2 dvkContainer = new ContainerVer2();
		dvkContainer.setVersion(DVK_CONTAINER_VERSION);
		dvkContainer.save2File("C:\test_dvkcontainer_ver2.xml");
	}
	
}
