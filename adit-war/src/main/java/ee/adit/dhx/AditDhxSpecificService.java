package ee.adit.dhx;

import com.jcabi.aspects.Loggable;

import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.DocumentSharing;
import ee.adit.dvk.api.ml.PojoMessage;
import ee.adit.dvk.api.ml.PojoMessageRecipient;
import ee.adit.dvk.api.ml.PojoOrganization;
import ee.adit.service.DocumentService;
import ee.adit.util.Util;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.AsyncDhxSendDocumentResult;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.util.FileDataHandler;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.context.MessageContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
@Transactional
@EnableAsync
public class AditDhxSpecificService implements DhxImplementationSpecificService {

	private List<InternalXroadMember> members;

	@Autowired
	DvkDAO dvkDao;

	@Autowired
	DhxService dhxService;

	@Autowired
	DhxMarshallerService dhxMarshallerService;

	/**
	 * Returns all organisations from database that are active and marked as own
	 * representees.
	 */
	@Override
	@Loggable
	public List<DhxRepresentee> getRepresentationList() throws DhxException {
		return new ArrayList<DhxRepresentee>();
	}

	@Override
	@Loggable
	public boolean isDuplicatePackage(InternalXroadMember from, String consignmentId) throws DhxException {

		DhxOrganisation dhxOrg = DhxOrganisationFactory.createDhxOrganisation(from);
		String orgCode = DhxUtil.toDvkCapsuleAddressee(dhxOrg.getCode(), dhxOrg.getSystem());
		log.debug("checking duplicates for organisation: {}", orgCode);
		Long cnt = dvkDao.countIncomingMessagesBySenderAndConsignmentId(orgCode, consignmentId);
		if (cnt != null && cnt > 0) {
			return true;
		}
		return false;
	}

	@Override
	@Loggable
	public String receiveDocument(IncomingDhxPackage document, MessageContext context) throws DhxException {

		log.debug(
				"String receiveDocument(DhxDocument document) " + "externalConsignmentId: {} recipient: {} service: {}",
				document.getExternalConsignmentId(), document.getRecipient(), document.getService());
		SessionFactory sessionFactory = dvkDao.getSessionFactory();
		Long dvkMessageID = null;
		Session dvkSession = sessionFactory.openSession();
		dvkSession.setFlushMode(FlushMode.COMMIT);
		Transaction dvkTransaction = dvkSession.beginTransaction();

		log.info("DVK session flush mode: " + dvkSession.getFlushMode().toString());

		try {
			PojoMessage msg = dhxService.getPojoMessageFromIncomingContainer(document);

			log.trace("document created from incoming package: {}", msg);
			List<PojoMessageRecipient> recipients = dhxService
					.getPojoMessageRecipientsFromIncomingContainer(document.getParsedContainer(), null);
			//in DHX recipient is the one to whom request was sent
			String recipientOrgCode = DhxUtil.toDvkCapsuleAddressee(document.getRecipient().getCode(),
					document.getRecipient().getSystem());
			// Add first recipient data
			if (recipients != null && recipients.size() > 0) {
				PojoMessageRecipient firstRecipient = recipients.get(0);
				if (firstRecipient != null) {
					msg.setRecipientOrgCode(recipientOrgCode);
					msg.setRecipientPersonCode(firstRecipient.getRecipientPersonCode());
					if (Util.isNullOrEmpty(firstRecipient.getRecipientPersonCode())) {
						msg.setRecipientOrgName(firstRecipient.getRecipientOrgName());
					} else {
						msg.setRecipientName(firstRecipient.getRecipientName());
					}
				}
			}

			log.debug("Saving document to DVK database");
			dvkMessageID = (Long) dvkSession.save(msg);
			msg.setDhxReceiptId(String.valueOf(dvkMessageID));
			dvkSession.saveOrUpdate(msg);
			for (PojoMessageRecipient recipient : recipients) {
				recipient.setDhlMessageId(dvkMessageID);
				recipient.setSendingDate(msg.getSendingDate());
				recipient.setDhxConsignmentId(msg.getDhxConsignmentId());
				recipient.setDhxReceiptId(dvkMessageID.toString());
				recipient.setReceivedDate(msg.getReceivedDate());
				recipient.setRecipientStatusId(msg.getRecipientStatusId());
				recipient.setRecipientOrgCode(recipientOrgCode);
				dvkSession.save(recipient);
			}
			if (dvkMessageID == null || dvkMessageID.longValue() == 0) {
				log.error("Error while saving outgoing message to DVK database - no ID returned by save method.");
				throw new DataRetrievalFailureException(
						"Error while saving outgoing message to DVK database - no ID returned by save method.");
			} else {
				log.info("Outgoing message saved to DVK database. ID: " + dvkMessageID);
			}

			log.debug("DVK Message saved to client database. GUID: " + msg.getDhlGuid());
			dvkTransaction.commit();
		} catch (Exception ex) {
			dvkTransaction.rollback();
			log.error(ex.getMessage(), ex);
			throw new DataRetrievalFailureException("Error while adding message to DVK Client database: ", ex);
		} finally {
			if (dvkSession != null) {
				dvkSession.close();
			}
		}

		// Update CLOB
		Session dvkSession2 = sessionFactory.openSession();
		dvkSession2.setFlushMode(FlushMode.COMMIT);
		Transaction dvkTransaction2 = dvkSession2.beginTransaction();

		try {
			// Select the record for update
			PojoMessage dvkMessageToUpdate = (PojoMessage) dvkSession2.load(PojoMessage.class, dvkMessageID,
					LockOptions.UPGRADE);
			dhxService.formatCapsuleRecipientAndSender(document.getParsedContainer(), document.getClient(),
					document.getService(), false);
			Writer dataWriter = dhxMarshallerService.marshallToWriter(document.getParsedContainer());
			// Writer dataWriter =
			// dhxService.getContainerWriter(document.getDocumentFile());
			dvkMessageToUpdate.setData(dataWriter.toString());
			// Commit to DVK database
			dvkTransaction2.commit();

		} catch (Exception e) {
			dvkTransaction2.rollback();

			// Remove the document with empty clob from the database
			Session dvkSession3 = sessionFactory.openSession();
			dvkSession3.setFlushMode(FlushMode.COMMIT);
			Transaction dvkTransaction3 = dvkSession3.beginTransaction();
			try {
				log.debug("Starting to delete document from DVK Client database: " + dvkMessageID);
				PojoMessage dvkMessageToDelete = (PojoMessage) dvkSession3.load(PojoMessage.class, dvkMessageID);
				if (dvkMessageToDelete == null) {
					log.warn("DVK message to delete is not initialized.");
				}
				dvkSession3.delete(dvkMessageToDelete);
				dvkTransaction3.commit();
				log.info("Empty DVK document deleted from DVK Client database. ID: " + dvkMessageID);
			} catch (Exception dvkException) {
				dvkTransaction3.rollback();
				log.error("Error deleting document from DVK database: ", dvkException);
			} finally {
				if (dvkSession3 != null) {
					dvkSession3.close();
				}
			}
			throw new DataRetrievalFailureException("Error while adding message to DVK Client database (CLOB update): ",
					e);
		} finally {
			if (dvkSession2 != null) {
				dvkSession2.close();
			}
		}
		dhxService.cleanupContainer(document.getParsedContainer());
		return dvkMessageID.toString();
	}

	@Override
	@Loggable
	public List<InternalXroadMember> getAdresseeList() throws DhxException {
		if (members == null) {
			members = new ArrayList<InternalXroadMember>();
			List<PojoOrganization> orgs = this.dvkDao.getUsers();
			log.debug("found addressee organisations:" + orgs.size());
			for (PojoOrganization org : orgs) {
				members.add(getInternalXroadMemberFromOrganisation(org));
			}
		}
		return members;
	}

	@Loggable
	private InternalXroadMember getInternalXroadMemberFromOrganisation(PojoOrganization org) {
		PojoOrganization mainOrg = org;
		DhxRepresentee representee = null;
		if (org.getRepresentor() != null) {
			mainOrg = org.getRepresentor();
			representee = getRepresenteeFromOrganisation(org);

		}
		InternalXroadMember member = new InternalXroadMember(mainOrg.getXroadInstance(), mainOrg.getMemberClass(),
				mainOrg.getCode(), mainOrg.getSubSystem(), mainOrg.getName(), representee);
		log.trace("created member: {}", member);
		return member;
	}

	@Loggable
	private DhxRepresentee getRepresenteeFromOrganisation(PojoOrganization org) {
		return new DhxRepresentee(org.getOrgCode(), org.getRepresenteeStart(), org.getRepresenteeEnd(), org.getName(),
				org.getSubSystem());
	}

	@Override
	@Loggable
	public void saveAddresseeList(List<InternalXroadMember> members) throws DhxException {
		try {
			updateNotActiveAdressees(members);
			List<PojoOrganization> organisations = new ArrayList<PojoOrganization>();
			for (InternalXroadMember member : members) {
				if (member.getRepresentee() == null) {
					organisations.add(dhxService.getOrganisationFromInternalXroadMember(member));
				}
			}
			log.debug("saving not representees. " + organisations.size());
			dvkDao.updateOrganisations(organisations);
			organisations = new ArrayList<PojoOrganization>();
			for (InternalXroadMember member : members) {
				if (member.getRepresentee() != null && dhxService.isRepresenteeValid(member)) {
					organisations.add(dhxService.getOrganisationFromInternalXroadMember(member));
				}
			}
			log.debug("saving representees. " + organisations.size());
			dvkDao.updateOrganisations(organisations);

			this.members = members;
		} catch (Exception ex) {
			throw new DhxException("Error occured while updating organisations.", ex);
		}
	}

	private void updateNotActiveAdressees(List<InternalXroadMember> members) throws DhxException {
		// adressees count should not be too big, get ALL
		try {
			List<PojoOrganization> allOrgs = this.dvkDao.getUsers();
			Boolean found = false;
			List<PojoOrganization> changedOrgs = new ArrayList<PojoOrganization>();
			for (PojoOrganization org : allOrgs) {
				if (log.isDebugEnabled()) {
					log.debug("checking if organisation needs to be deactivated organisation: {}", org);
				}
				// we need to check only active organisations
				if (org.isDhlCapable()) {
					found = false;
					for (InternalXroadMember member : members) {
						if (log.isTraceEnabled()) {
							log.trace("checking against member: {}", member);
						}
						if (member.getRepresentee() == null) {
							if (member.getMemberCode().equals(org.getCode())
									&& (member.getSubsystemCode() == null && org.getSubSystem() == null
											|| member.getSubsystemCode().equals(org.getSubSystem()))) {
								found = true;
								break;
							}
						} else {
							if (member.getRepresentee().getRepresenteeCode().equals(org.getCode())
									&& (member.getRepresentee().getRepresenteeSystem() == null
											&& org.getSubSystem() == null)
									|| (member.getRepresentee().getRepresenteeSystem() != null
											&& org.getSubSystem() != null && member.getRepresentee()
													.getRepresenteeSystem().equals(org.getSubSystem()))) {
								found = true;
								break;
							}
						}
					}
				} else {
					found = true;
				}
				if (!found) {
					if (log.isDebugEnabled()) {
						log.debug("organisation is not found in renewed address list, "
								+ "deactivating it. organisation: {}", org);
					}
					org.setDhlCapable(false);
					changedOrgs.add(org);
				}
			}
			dvkDao.updateOrganisations(changedOrgs);
		} catch (Exception ex) {
			throw new DhxException("Error occured while updating inactive organisations.", ex);
		}
	}

	@Override
	@Loggable
	public void saveSendResult(DhxSendDocumentResult finalResult, List<AsyncDhxSendDocumentResult> retryResults) {
		Session dvkSession = null;
		log.info("saveSendResult invoked.");
		try {
			SessionFactory sessionFactory = dvkDao.getSessionFactory();
			dvkSession = sessionFactory.openSession();
			Transaction dvkTransaction = dvkSession.beginTransaction();
			String recipientIdStr = finalResult.getSentPackage().getInternalConsignmentId();
			Long recipientId = Long.parseLong(recipientIdStr);
			log.debug("searching recipient to save send result. " + recipientId);
			PojoMessageRecipient recipient = dvkDao.getMessageRecipient(recipientId);
			if (recipient == null) {
				throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
						"Recipient is not found in database. recipientId: " + recipientId);
			}
			SendDocumentResponse docResponse = finalResult.getResponse();
			log.trace("saving send result for recipient: {}", recipient);
			boolean allSent = true;
			if (docResponse.getFault() == null) {
				log.debug("Document was succesfuly sent to DHX");
				recipient.setDhxReceiptId(docResponse.getReceiptId());
				recipient.setSendingStatusId(DocumentService.DVK_STATUS_SENT);
			} else {
				allSent = false;
				log.debug("Fault occured while sending document to DHX");
				log.debug("All attempts to send documents were done. Saving document as failed.");
				recipient.setSendingStatusId(DocumentService.DVK_STATUS_ABORTED);
				String faultString = "";
				if (retryResults != null && retryResults.size() > 0) {
					faultString = faultString + " Total retries count: " + retryResults.size()
							+ " Results for individual retries: ";
					for (AsyncDhxSendDocumentResult result : retryResults) {
						faultString = faultString + "\n Retry date: " + result.getTryDate() + " Technical error:";
						if (result.getResult().getOccuredException() != null) {
							faultString = faultString + " Error message:"
									+ result.getResult().getOccuredException().getMessage() + " Stacktrace: "
									+ ExceptionUtils.getStackTrace(result.getResult().getOccuredException());
						}
					}
				}
				faultString = docResponse.getFault().getFaultString() + faultString;
				if (!StringUtil.isNullOrEmpty(docResponse.getFault().getFaultCode())) {
					recipient.setFaultCode(docResponse.getFault().getFaultCode().substring(0,
							docResponse.getFault().getFaultCode().length() > 250 ? 250
									: docResponse.getFault().getFaultCode().length()));
				}
				recipient.setFaultString(docResponse.getFault().getFaultString());
				recipient.setFaultDetail(
						faultString.substring(0, (faultString.length() > 1900 ? 1900 : faultString.length())));
			}
			if (allSent) {
				List<PojoMessageRecipient> recipients = dvkDao.getMessageRecipients(recipient.getDhlMessageId(), false);
				for (PojoMessageRecipient docRecipient : recipients) {
					if (!new Long(docRecipient.getSendingStatusId()).equals(DocumentService.DVK_STATUS_SENT)
							&& !docRecipient.getId().equals(recipient.getId())) {
						allSent = false;
						break;
					}
				}
			}
			dvkSession.saveOrUpdate(recipient);
			if (allSent) {
				log.debug("all of the documents recipients are in received status, "
						+ "setting same status to the document.");
				PojoMessage msg = dvkDao.getMessage(recipient.getDhlMessageId());
				msg.setSendingStatusId(DocumentService.DVK_STATUS_SENT);
				dvkSession.saveOrUpdate(msg);
			}
			dvkTransaction.commit();
		} catch (Throwable ex) {
			log.error("Error occured while saving send results. " + ex.getMessage(), ex);
		} finally {
			try {
				dhxService.cleanupContainer(finalResult.getSentPackage().getParsedContainer());
			} catch (DhxException ex) {
				log.error("Error occured while cleaningup container.", ex);
			}
			if (finalResult.getSentPackage().getDocumentFile() instanceof FileDataHandler) {
				((FileDataHandler) finalResult.getSentPackage().getDocumentFile()).deleteFile();
			}
		}

	}
}
