package ee.adit.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.DocumentFile;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.exception.AditException;
import ee.adit.pojo.DocumentSendingData;
import ee.adit.pojo.DocumentSendingRecipient;
import ee.adit.pojo.DocumentSharingData;
import ee.adit.pojo.DocumentSharingRecipient;
import ee.adit.pojo.GetDocumentListRequest;
import ee.adit.pojo.GetDocumentListResponseAttachment;
import ee.adit.pojo.OutputDocument;
import ee.adit.pojo.OutputDocumentFile;
import ee.adit.pojo.OutputDocumentFilesList;
import ee.adit.pojo.SaveDocumentRequestAttachmentFile;
import ee.adit.service.DocumentService;
import ee.adit.util.Util;
import ee.sk.digidoc.DataFile;
import ee.sk.digidoc.DigiDocException;
import ee.sk.digidoc.Signature;
import ee.sk.digidoc.SignatureProductionPlace;
import ee.sk.digidoc.SignatureValue;
import ee.sk.digidoc.SignedDoc;
import ee.sk.utils.ConfigManager;

public class DocumentDAO extends HibernateDaoSupport {

	private static Logger LOG = Logger.getLogger(DocumentDAO.class);
	
	public long getUsedSpaceForUser(String userCode) {
		
		// TODO: add conditions for files - do not count in deflated/deleted
		
		Boolean deflated = new Boolean(true);
		Boolean deleted = new Boolean(true);
		
		//List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and doc.deleted != ? and doc.deflated != ?)", new Object[] {userCode, new Boolean(true), new Boolean(true)});
		List<DocumentFile> userFiles = this.getHibernateTemplate().find("from DocumentFile docFile where docFile.document in (select doc.id from Document doc where doc.creatorCode = ? and (doc.deflated is null or doc.deflated != ?) and (doc.deleted is null or doc.deleted != ?))", new Object[] {userCode, deflated, deleted});
		
		long result = 0;
		for(DocumentFile docFile : userFiles) {
			long fileSize = docFile.getFileSizeBytes();
			result += fileSize;
		}
		
		return result;
	}
	
	public Document getDocument(long id) {
		LOG.debug("Attempting to load document from database. Document id: " + String.valueOf(id));
		return (Document) this.getHibernateTemplate().get(Document.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public GetDocumentListResponseAttachment getDocumentSearchResult(
			final GetDocumentListRequest param,
			final String userCode,
			final String temporaryFilesDir,
			final String filesNotFoundMessageBase) {
		
		GetDocumentListResponseAttachment result = null;
		
		try {
			result = (GetDocumentListResponseAttachment) getHibernateTemplate().execute(new HibernateCallback() {
				
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException
	            {
					GetDocumentListResponseAttachment innerResult = new GetDocumentListResponseAttachment();
					innerResult.setDocumentList(new ArrayList<OutputDocument>());
					Criteria criteria = session.createCriteria(Document.class, "doc");
					
					// General parameters
					criteria.add(
						Restrictions.or(
							Restrictions.isNull("deleted"),
							Restrictions.eq("deleted", false)
						)
					);
					
					// Document "folder" (local, incoming, outgoing)
					if (param.getFolder() != null) {
						// TODO: Mille järgi üldse otsustada, kas dokument
						// on kohalik või saadetud?
						if (param.getFolder().equalsIgnoreCase("local")) {
							criteria.add(Restrictions.eq("creatorCode", userCode));
							criteria.add(
								Restrictions.or(
									Restrictions.isNull("documentSharings"),
									Restrictions.isEmpty("documentSharings")
								)
							);
						} else if (param.getFolder().equalsIgnoreCase("incoming")) {
							// "Incoming" means that:
							// - someone else is document creator
							// - document kas been shared to user
							criteria.add(Restrictions.ne("creatorCode", userCode));
							DetachedCriteria sharedToMeSubquery = DetachedCriteria.forClass(Document.class, "doc1")
							.createCriteria("documentSharings", "sh1")
							.add(Restrictions.eq("userCode", userCode))
						    .add(Property.forName("doc.id").eqProperty("doc1.id"))
						    .setProjection(Projections.id());
							criteria.add(Subqueries.exists(sharedToMeSubquery));
						} else if (param.getFolder().equalsIgnoreCase("outgoing")) {
							// "Outgoing" means that:
							// - user is document ownar
							// - and document has been sent or shared to someone else
							criteria.add(Restrictions.eq("creatorCode", userCode));
							criteria.add(Restrictions.isNotNull("documentSharings"));
							criteria.add(Restrictions.isNotEmpty("documentSharings"));
						} else {
							DetachedCriteria sharedToMeSubquery = DetachedCriteria.forClass(Document.class, "doc1")
							.createCriteria("documentSharings", "sh1")
							.add(Restrictions.eq("userCode", userCode))
						    .add(Property.forName("doc.id").eqProperty("doc1.id"))
						    .setProjection(Projections.id());
							criteria.add(
								Restrictions.or(
									Restrictions.eq("creatorCode", userCode),
									Subqueries.exists(sharedToMeSubquery)
								)
							);
						}
					}
					
					// Document type
					if ((param.getDocumentTypes() != null)
						&& (param.getDocumentTypes().getDocumentType() != null)
						&& !param.getDocumentTypes().getDocumentType().isEmpty()) {
						
						Disjunction disjunction = Restrictions.disjunction();
						for (String docType : param.getDocumentTypes().getDocumentType()) {
							disjunction.add(Restrictions.eq("documentType", docType));
						}
						criteria.add(disjunction);
					}
					
					// Document DVK status
					if ((param.getDocumentDvkStatuses() != null)
						&& (param.getDocumentDvkStatuses().getStatusId() != null)
						&& !param.getDocumentDvkStatuses().getStatusId().isEmpty()) {
						
						Disjunction disjunction = Restrictions.disjunction();
						for (Long statusId : param.getDocumentDvkStatuses().getStatusId()) {
							disjunction.add(Restrictions.eq("documentDvkStatusId", statusId));
						}
						criteria.add(disjunction);
					}
					
					// Document workflow status
					if ((param.getDocumentWorkflowStatuses() != null)
						&& (param.getDocumentWorkflowStatuses().getStatusId() != null)
						&& !param.getDocumentWorkflowStatuses().getStatusId().isEmpty()) {
						
						Disjunction disjunction = Restrictions.disjunction();
						for (Long statusId : param.getDocumentWorkflowStatuses().getStatusId()) {
							disjunction.add(Restrictions.eq("documentWfStatusId", statusId));
						}
						criteria.add(disjunction);
					}
					
					// Has the document been viewed?
					// - if user is document creator, then it is viewed
					// - if user received this document then check viewing status
					// - if user shared the document then check recipient viewing statuses
					if (param.isHasBeenViewed()) {
						// TODO:
					}
					
					// Include deflated documents
					if (!param.isIsDeflated()) {
						criteria.add(
								Restrictions.or(
									Restrictions.isNull("deflated"),
									Restrictions.eq("deflated", false)
								)
							);
					}
					
					// Creator application
					if ((param.getCreatorApplications() != null)
						&& (param.getCreatorApplications().getCreatorApplication() != null)
						&& !param.getCreatorApplications().getCreatorApplication().isEmpty()) {
						
						Disjunction disjunction = Restrictions.disjunction();
						for (String appName : param.getCreatorApplications().getCreatorApplication()) {
							disjunction.add(Restrictions.eq("remoteApplication", appName));
						}
						criteria.add(disjunction);
					}

					// TODO: Phrase search
					if ((param.getSearchPhrase() != null) && (param.getSearchPhrase().length() > 0)) {
						Disjunction disjunction = Restrictions.disjunction();
						disjunction.add(Restrictions.like("title", param.getSearchPhrase(), MatchMode.ANYWHERE));
						disjunction.add(Restrictions.like("creatorCode", param.getSearchPhrase(), MatchMode.ANYWHERE));
						disjunction.add(Restrictions.like("creatorName", param.getSearchPhrase(), MatchMode.ANYWHERE));
						
						DetachedCriteria sigSubquery = DetachedCriteria.forClass(Document.class, "doc2")
							.createCriteria("signatures", "sig")
							.add(Restrictions.or(
								Restrictions.like("signerCode", param.getSearchPhrase(), MatchMode.ANYWHERE),
								Restrictions.like("signerName", param.getSearchPhrase(), MatchMode.ANYWHERE)))
					    .add(Property.forName("doc.id").eqProperty("doc2.id"))
					    .setProjection(Projections.id());
						disjunction.add(Subqueries.exists(sigSubquery));
						
						DetachedCriteria shareSubquery = DetachedCriteria.forClass(Document.class, "doc3")
						.createCriteria("documentSharings", "sh")
						.add(Restrictions.or(
							Restrictions.like("userCode", param.getSearchPhrase(), MatchMode.ANYWHERE),
							Restrictions.like("userName", param.getSearchPhrase(), MatchMode.ANYWHERE)))
					    .add(Property.forName("doc.id").eqProperty("doc3.id"))
					    .setProjection(Projections.id());
						disjunction.add(Subqueries.exists(shareSubquery));
						
						DetachedCriteria filesSubquery = DetachedCriteria.forClass(Document.class, "doc4")
						.createCriteria("documentFiles", "files")
						.add(Restrictions.or(
							Restrictions.like("fileName", param.getSearchPhrase(), MatchMode.ANYWHERE),
							Restrictions.like("description", param.getSearchPhrase(), MatchMode.ANYWHERE)))
					    .add(Property.forName("doc.id").eqProperty("doc4.id"))
					    .setProjection(Projections.id());
						disjunction.add(Subqueries.exists(filesSubquery));
						
						criteria.add(disjunction);
					}
					
					
					// First get total number of matching documents
				    criteria.setProjection(Projections.rowCount());
				    innerResult.setTotal(((Long) criteria.uniqueResult()).intValue());
				    criteria.setProjection(null);
				    criteria.setResultTransformer(Criteria.ROOT_ENTITY);
				    
				    // Then apply paging and ordering
				    // and get the final list
					int startIndex = (param.getStartIndex() != null) ? param.getStartIndex().intValue() : 0;
					if (startIndex < 1) {
						startIndex = 1;
					}
					int maxResults = (param.getMaxResults() != null) ? param.getMaxResults().intValue() : 20;
					if (maxResults < 1) {
						maxResults = 20;
					} else if (maxResults > 100) {
						maxResults = 100;
					}
					criteria.setFirstResult(startIndex-1);
					criteria.setMaxResults(maxResults);
					criteria.addOrder(Order.desc("id"));
					List<Document> docList = criteria.list();
					
					for (Document doc : docList) {
						OutputDocument resultDoc = dbDocumentToOutputDocument(
							doc,
							null,
							true,
							true,
							false,
							temporaryFilesDir,
							filesNotFoundMessageBase);
						innerResult.getDocumentList().add(resultDoc);
					}
					
					return innerResult;
	            }
	        });
		} catch (DataAccessException ex) {
			if (ex.getRootCause() instanceof AditException) {
				throw (AditException) ex.getRootCause();
			} else {
				throw ex;
			}
		}
		return result;
	}
	
	public OutputDocument getDocumentWithFiles(
			final long documentId,
			final List<Long> fileIdList,
			final boolean includeSignatures,
			final boolean includeSharings,
			final boolean includeFileContents,
			final String temporaryFilesDir,
			final String filesNotFoundMessageBase) throws Exception {
		
		if (documentId <= 0) {
			throw new IllegalArgumentException("Document ID must be a positive integer. Currently supplied ID was " + documentId + ".");
		}
		
		OutputDocument result = null;
		
		try {
			LOG.debug("Attempting to load document files for document " + documentId);
			result = (OutputDocument) getHibernateTemplate().execute(new HibernateCallback() {
				
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Document doc = (Document)session.get(Document.class, documentId);
					return dbDocumentToOutputDocument(
							doc,
							fileIdList,
							includeSignatures,
							includeSharings,
							includeFileContents,
							temporaryFilesDir,
							filesNotFoundMessageBase);
	            }
	        });
		} catch (DataAccessException ex) {
			if (ex.getRootCause() instanceof AditException) {
				throw (AditException) ex.getRootCause();
			} else {
				throw ex;
			}
		}
		return result;
	}
	
	private OutputDocument dbDocumentToOutputDocument(
			final Document doc,
			final List<Long> fileIdList,
			final boolean includeSignatures,
			final boolean includeSharings,
			final boolean includeFileContents,
			final String temporaryFilesDir,
			final String filesNotFoundMessageBase) throws SQLException {
		
		long totalBytes = 0;
		OutputDocument result = new OutputDocument();
		List<OutputDocumentFile> outputFilesList = new ArrayList<OutputDocumentFile>();
    	List<DocumentFile> filesList = new ArrayList<DocumentFile>(doc.getDocumentFiles());            	
    	
    	// Check if all requested files exist
    	if ((fileIdList != null) && !fileIdList.isEmpty()) {
    		List<Long> internalIdList = new ArrayList<Long>();
    		internalIdList.addAll(fileIdList);
    		
    		for (DocumentFile docFile : filesList) {
				if (!docFile.getDeleted() && internalIdList.contains((Long)docFile.getId())) {
					internalIdList.remove((Long)docFile.getId());
				}
    		}
    		
    		// If some files did not exist or were deleted
    		// then return error message
    		if (!internalIdList.isEmpty()) {
    			String idListString = "";
    			for (Long id : internalIdList) {
    				idListString += " " + id;
    			}
    			idListString = idListString.trim().replaceAll(" ", ",");
    			throw new SQLException(new AditException(filesNotFoundMessageBase + " " + idListString));
    		}
    	}
    	
    	int itemIndex = 0;
    	for (DocumentFile docFile : filesList) {
    		if (!docFile.getDeleted()) {
        		if ((fileIdList == null) || fileIdList.isEmpty() || fileIdList.contains(docFile.getId())) {
        			OutputDocumentFile f = new OutputDocumentFile();
        			f.setContentType(docFile.getContentType());
        			f.setDescription(docFile.getDescription());
        			f.setId(docFile.getId());
        			f.setName(docFile.getFileName());
        			f.setSizeBytes(docFile.getFileSizeBytes());
        			
        			// Read file data from BLOB and write it to temporary file.
        			// This is necessary to avoid storing potentially large
        			// amounts of binary data in server memory.
        			if (includeFileContents) {
            			itemIndex++;
        				String outputFileName = Util.generateRandomFileNameWithoutExtension();
        				outputFileName = temporaryFilesDir + File.separator + outputFileName + "_" + itemIndex + "_GDFv1.adit";
        				InputStream blobDataStream = null;
        				FileOutputStream fileOutputStream = null;
        				try {
        					byte[] buffer = new byte[10240];
        					int len = 0;
        					blobDataStream = docFile.getFileData().getBinaryStream();
        					fileOutputStream = new FileOutputStream(outputFileName);
        					while ((len = blobDataStream.read(buffer)) > 0) {
        						fileOutputStream.write(buffer, 0, len);
        						totalBytes += len;
        					}
        					f.setTmpFileName(outputFileName);
        				} catch (IOException ex) {
        					throw new HibernateException(ex);
        				} finally {
        					try {
        						if (blobDataStream != null) {
        							blobDataStream.close();
        						}
        						blobDataStream = null;
        					} catch (Exception ex) {}
        					
        					try {
        						if (fileOutputStream != null) {
        							fileOutputStream.close();
        						}
        						fileOutputStream = null;
        					} catch (Exception ex) {}
        				}
        			}
    				
    				outputFilesList.add(f);
        		}
    		}
    	}
    	
    	OutputDocumentFilesList filesListWrapper = new OutputDocumentFilesList();
    	filesListWrapper.setFiles(outputFilesList);
    	filesListWrapper.setTotalFiles(outputFilesList.size());
    	filesListWrapper.setTotalBytes(totalBytes);
    	result.setFiles(filesListWrapper);
    	
    	
    	// Signatures
    	if (includeSignatures) {
        	List<ee.adit.pojo.Signature> docSignatures = new ArrayList<ee.adit.pojo.Signature>();
        	if ((doc.getSignatures() != null) && (!doc.getSignatures().isEmpty())) {
				Iterator it = doc.getSignatures().iterator();
				while (it.hasNext()) {
					ee.adit.dao.pojo.Signature sig = (ee.adit.dao.pojo.Signature)it.next();
					ee.adit.pojo.Signature outSig = new ee.adit.pojo.Signature();
					outSig.setCity(sig.getCity());
					outSig.setCountry(sig.getCountry());
					outSig.setManifest((sig.getSignerRole() + " " + sig.getResolution()).trim());
					outSig.setSignerCode(sig.getSignerCode());
					outSig.setSignerName(sig.getSignerName());
					outSig.setState(sig.getCounty());
					outSig.setZip(sig.getPostIndex());
					docSignatures.add(outSig);
				}
        	}
        	result.setSignatures(docSignatures);
    	}
    	
    	// Sharing/sending
    	if (includeSharings) {
    		DocumentSendingData sendingData = new DocumentSendingData();
    		sendingData.setUserList(new ArrayList<DocumentSendingRecipient>());
    		DocumentSharingData sharingData = new DocumentSharingData();
    		sharingData.setUserList(new ArrayList<DocumentSharingRecipient>());
    		
    		if ((doc.getDocumentSharings() != null) && (!doc.getDocumentSharings().isEmpty())) {
				Iterator it = doc.getDocumentSharings().iterator();
				while (it.hasNext()) {
					DocumentSharing sharing = (DocumentSharing)it.next();
					
					if ((sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SharingType_Share))
						|| (sharing.getDocumentSharingType().equalsIgnoreCase(DocumentService.SharingType_Sign))) {
						
						DocumentSharingRecipient rec = new DocumentSharingRecipient();
						rec.setCode(sharing.getUserCode());
						rec.setHasBeenViewed((sharing.getLastAccessDate() != null));
						rec.setName(sharing.getUserName());
						rec.setOpenedTime(sharing.getLastAccessDate());
						rec.setWorkflowStatusId(sharing.getDocumentWfStatus());
						sharingData.getUserList().add(rec);
					} else {
						DocumentSendingRecipient rec = new DocumentSendingRecipient();
						rec.setCode(sharing.getUserCode());
						rec.setHasBeenViewed((sharing.getLastAccessDate() != null));
						rec.setName(sharing.getUserName());
						rec.setOpenedTime(sharing.getLastAccessDate());
						rec.setWorkflowStatusId(sharing.getDocumentWfStatus());
						rec.setDvkStatusId(sharing.getDocumentDvkStatus());
						sendingData.getUserList().add(rec);
					}
				}
    		}
    		
    		result.setSentTo(sendingData);
    		result.setSharedTo(sharingData);
    	}
    	
    	// Dokumendi andmed
    	result.setCreated(doc.getCreationDate());
    	result.setCreatorApplication(doc.getRemoteApplication());
    	result.setCreatorCode(doc.getCreatorCode());
    	result.setCreatorName(doc.getCreatorName());
    	
    	// TODO: Siin peaks vist tegelikult olema dokumendi
    	// lisanud isiku andmed, kui dokumendi omanikuks on asutus
    	result.setCreatorUserCode(doc.getCreatorCode());
    	result.setCreatorUserName(doc.getCreatorName());
    	
    	result.setDeflated(doc.getDeflated());
    	result.setDeflatingDate(doc.getDeflateDate());
    	result.setDocumentType(doc.getDocumentType());
    	result.setDvkId(doc.getDvkId());
    	result.setDvkStatusId(doc.getDocumentDvkStatusId());
    	result.setGuid(doc.getGuid());
    	result.setId(doc.getId());
    	// TODO: innerResult.setLastAccessed(doc.getl)
    	result.setLastModified(doc.getLastModifiedDate());
    	result.setLocked(doc.getLocked());
    	result.setLockingDate(doc.getLockingDate());
    	// TODO: innerResult.setPreviousDocumentId(previousDocumentId);
    	// TODO: previous document GUID
    	result.setSignable(doc.getSignable());
    	result.setTitle(doc.getTitle());
    	result.setWorkflowStatusId(doc.getDocumentWfStatusId());
    	
    	return result;
	}
	
	
	/**
	 * FIXME - ei tööta praegu salvestamine
	 * @param document
	 * @param files
	 * @return
	 */
	public Long save(final Document document, final List<SaveDocumentRequestAttachmentFile> files) {
		Long result = null;
				
		result = (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
			
			@Override
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				
				Set<DocumentFile> documentFiles = new HashSet<DocumentFile>();
				
				if (files != null) {
					for(int i = 0; i < files.size(); i++) {
						DocumentFile documentFile = new DocumentFile();
						SaveDocumentRequestAttachmentFile attachmentFile = files.get(i);
						String fileName = attachmentFile.getTmpFileName();
						
						FileInputStream fileInputStream = null;
						try {
							fileInputStream = new FileInputStream(fileName);
						} catch (FileNotFoundException e) {
							LOG.error("Error saving document file: ", e);
						}
						
						long length = (new File(fileName)).length();
						
						Blob fileData = Hibernate.createBlob(fileInputStream, length, session);
						documentFile.setFileData(fileData);
						documentFile.setContentType(attachmentFile.getContentType());
						documentFile.setDescription(attachmentFile.getDescription());
						documentFile.setFileName(attachmentFile.getName());
						documentFile.setFileSizeBytes(length);
						documentFile.setDocument(document);
						documentFiles.add(documentFile);
					}
				}
				
				document.setDocumentFiles(documentFiles);
				session.saveOrUpdate(document);
				LOG.debug("Saved document ID: " + document.getId());
				return document.getId();
			}
		});
		
		return result;
	}
	
	public String prepareSignature(
		final long documentId,
		final String manifest,
		final String country,
		final String state,
		final String city,
		final String zip,
		final String certFile,
		final String digidocConfigFile,
		final String temporaryFilesDir) {
		
		String signatureDigest = null;
		signatureDigest = (String) this.getHibernateTemplate().execute(new HibernateCallback() {
			
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String digestHex = null;
				File uniqueDir = null;
				try {
					ConfigManager.init(digidocConfigFile);
					SignedDoc sdoc = new SignedDoc(SignedDoc.FORMAT_DIGIDOC_XML, SignedDoc.VERSION_1_3);
					
					String[] claimedRoles = null;
					if ((manifest != null) && (manifest.length() > 0)) {
						claimedRoles = new String[] { manifest };
					}
					SignatureProductionPlace address = null;
					if (((country != null) && (country.length() > 0))
						|| ((state != null) && (state.length() > 0))
						|| ((city != null) && (city.length() > 0))
						|| ((zip != null) && (zip.length() > 0))) {
						
						address = new SignatureProductionPlace();
						address.setCountryName(country);
						address.setStateOrProvince(state);
						address.setCity(city);
						address.setPostalCode(zip);
					}
					
					// Create unique subdirectory for files
					uniqueDir = new File(temporaryFilesDir + File.separator + documentId);
					int uniqueCounter = 0;
					while (uniqueDir.exists()) {
						uniqueDir = new File(temporaryFilesDir + File.separator + documentId + "_" + (++uniqueCounter));
					}
					uniqueDir.mkdir();
					
					Document doc = (Document) session.get(Document.class, documentId);
					List<DocumentFile> filesList = new ArrayList<DocumentFile>(doc.getDocumentFiles());
			    	for (DocumentFile docFile : filesList) {
			    		if (!docFile.getDeleted()) {
	        				String outputFileName = uniqueDir.getAbsolutePath() + File.separator + docFile.getFileName(); 
	        				
	        				InputStream blobDataStream = null;
	        				FileOutputStream fileOutputStream = null;
	        				try {
	        					byte[] buffer = new byte[10240];
	        					int len = 0;
	        					blobDataStream = docFile.getFileData().getBinaryStream();
	        					fileOutputStream = new FileOutputStream(outputFileName);
	        					while ((len = blobDataStream.read(buffer)) > 0) {
	        						fileOutputStream.write(buffer, 0, len);
	        					}
	        					
	        					// Add file to signature container
	        					sdoc.addDataFile(new File(outputFileName), docFile.getContentType(), DataFile.CONTENT_EMBEDDED_BASE64);
	        				} catch (IOException ex) {
	        					throw new HibernateException(ex);
	        				} finally {
	        					try {
	        						if (blobDataStream != null) {
	        							blobDataStream.close();
	        						}
	        						blobDataStream = null;
	        					} catch (Exception ex) {}
	        					
	        					try {
	        						if (fileOutputStream != null) {
	        							fileOutputStream.close();
	        						}
	        						fileOutputStream = null;
	        					} catch (Exception ex) {}
	        				}
	        				
	        				
			    		}
			    	}
					
			    	// Add signature and calculate digest
					X509Certificate cert = SignedDoc.readCertificate(certFile);
					Signature sig = sdoc.prepareSignature(cert,	claimedRoles, address);
					byte[] digest = sig.calculateSignedInfoDigest();
					digestHex = Util.convertToHexString(digest);
					
					// Topis
					byte[] dummySignature = new byte[128];
					for (int i = 0; i < dummySignature.length; i++) {
						dummySignature[i] = 0;
					}
					sig.setSignatureValue(dummySignature);
					
					
					// Save container to file.
					String containerFileName = Util.generateRandomFileNameWithoutExtension();
					containerFileName = temporaryFilesDir + File.separator + containerFileName + "_PSv1.adit";
					sdoc.writeToFile(new File(containerFileName));
					
					// Add signature container to document table
					FileInputStream fileInputStream = null;
					try {
						fileInputStream = new FileInputStream(containerFileName);
					} catch (FileNotFoundException e) {
						LOG.error("Error reading digidoc container file: ", e);
					}
					long length = (new File(containerFileName)).length();
					Blob containerData = Hibernate.createBlob(fileInputStream, length, session);
					doc.setSignatureContainer(containerData);
					
					// Update document
					session.update(doc);
				} catch (DigiDocException ex) {
					throw new HibernateException(ex);
				} finally {
					// Delete temporary directory that was created
					// only for this method.
					try { Util.deleteDir(uniqueDir); }
					catch (Exception ex) {}
				}
				
				return digestHex;
			}
		});
		
		return signatureDigest;
	}
}
