package ee.adit.dvk;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
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
import dvk.api.container.v2.MetaManual;
import dvk.api.container.v2.Metainfo;

import ee.adit.dao.DocumentDAO;
import ee.adit.dao.pojo.Document;
import ee.adit.exception.AditInternalException;
import ee.adit.service.DocumentService;

public class SendJob extends QuartzJobBean {

	private static Logger LOG = Logger.getLogger(SendJob.class);

	
	
	private DocumentService documentService;
	
	protected void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		
		try {
			LOG.info("Executing scheduled job: Send documents to DVK");
			
			// Send documents to DVK Client database
			this.getDocumentService().sendDocumentsToDVK();
			
		} catch (Exception e) {
			LOG.error("Error executing scheduled DVK sending: ", e);
		}

	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
	
}
