package ee.adit.dhx;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.jcabi.aspects.Loggable;

import ee.adit.dao.dvk.DvkDAO;
import ee.adit.dvk.api.container.Organisation;
import ee.adit.dvk.api.container.v2_1.ContainerVer2_1;
import ee.adit.dvk.api.container.v2_1.DecRecipient;
import ee.adit.dvk.api.container.v2_1.Recipient;
import ee.adit.dvk.api.ml.PojoMessage;
import ee.adit.dvk.api.ml.PojoMessageRecipient;
import ee.adit.dvk.api.ml.PojoOrganization;
import ee.adit.service.DocumentService;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DhxService {

	@Autowired
	DvkDAO dvkDao;

	@Autowired
	SoapConfig config;

	@Autowired
	DhxMarshallerService dhxMarshallerService;
	
	@Autowired
	AddressService addressService;
	
	/**
	 * Method finds or creates new Organisation object according to data from
	 * InternalXroadMember. If object was not found in database, new object is
	 * created but not saved to database.
	 * 
	 * @param member
	 *            {@link InternalXroadMember} to find Organisation for
	 * @return created or found {@link Organisation}
	 * @throws DhxException
	 *             thrown if error occurs
	 */
	@Loggable
	public PojoOrganization getOrganisationFromInternalXroadMember(InternalXroadMember member) throws DhxException {
		return getOrganisationFromInternalXroadMember(member, false);
	}

	/**
	 * Method finds or creates new Organisation object according to data from
	 * InternalXroadMember. If object was not found in database, new object is
	 * created but not saved to database.
	 * 
	 * @param member
	 *            {@link InternalXroadMember} to find Organisation for
	 * @param representorOnly
	 *            whether to search only representor
	 * @return created or found {@link Organisation}
	 * @throws DhxException
	 *             thrown if error occurs
	 */
	@Loggable
	public PojoOrganization getOrganisationFromInternalXroadMember(InternalXroadMember member, Boolean representorOnly)
			throws DhxException {
		Boolean newMember = false;
		PojoOrganization organisation = dvkDao.getOrganisationByCodeAndSubsystem(member.getMemberCode(),
				member.getSubsystemCode(), config.getDhxSubsystemPrefix());
		if (organisation == null) {
			log.debug("Organisation is not found, creating new one.");
			newMember = true;
			organisation = new PojoOrganization();
		}
		// update representors data only if needed
		if (member.getRepresentee() == null || representorOnly) {
			organisation.setDhlCapable(true);
			organisation.setMemberClass(member.getMemberClass());
			organisation.setOrgName(member.getName());
			organisation.setOrgCode(member.getMemberCode());
			organisation.setSubSystem(member.getSubsystemCode());
			organisation.setXroadInstance(member.getXroadInstance());
			organisation.setDhxOrganisation(true);
			organisation.setRepresentor(null);
			organisation.setRepresenteeStart(null);
			organisation.setRepresenteeEnd(null);
			organisation.setOrganisationIdentificator(
					DhxUtil.toDvkCapsuleAddressee(member.getMemberCode(), member.getSubsystemCode()));
		}
		if (member.getRepresentee() != null && !representorOnly) {
			log.debug("Organisation is representee.");
			if (newMember) {
				// we cannot create new representor with representee. first
				// insert representor without representee, then representee
				throw new DhxException(DhxExceptionEnum.DATA_ERROR,
						"Trying to insert representee, but representor is not in database! representor:"
								+ member.getMemberCode() + "/" + member.getSubsystemCode() + " representee: "
								+ member.getRepresentee().getRepresenteeCode() + "/"
								+ member.getRepresentee().getRepresenteeSystem());
			}
			PojoOrganization representeeOrganisation = dvkDao.getOrganisationByCodeAndSubsystem(
					member.getRepresentee().getRepresenteeCode(), member.getRepresentee().getRepresenteeSystem(),
					config.getDhxSubsystemPrefix());
			if (representeeOrganisation == null) {
				log.debug("Representee organisation is not found, creating new one.");
				representeeOrganisation = new PojoOrganization();
			}
			representeeOrganisation.setDhlCapable(true);
			representeeOrganisation.setOrgName(member.getRepresentee().getRepresenteeName());
			representeeOrganisation.setOrgCode(member.getRepresentee().getRepresenteeCode());
			representeeOrganisation.setSubSystem(member.getRepresentee().getRepresenteeSystem());
			representeeOrganisation.setOrganisationIdentificator(DhxUtil.toDvkCapsuleAddressee(
					member.getRepresentee().getRepresenteeCode(), member.getRepresentee().getRepresenteeSystem()));
			if (member.getRepresentee().getStartDate() != null) {
				representeeOrganisation.setRepresenteeStart(member.getRepresentee().getStartDate());
			} else if (representeeOrganisation.getRepresenteeStart() != null) {
				representeeOrganisation.setRepresenteeStart(new Date());
			}
			if (member.getRepresentee().getEndDate() != null) {
				representeeOrganisation.setRepresenteeEnd(member.getRepresentee().getEndDate());
			}
			representeeOrganisation.setRepresentor(organisation);
			representeeOrganisation.setDhxOrganisation(true);
			representeeOrganisation.setMemberClass(null);
			representeeOrganisation.setXroadInstance(null);
			// organisation.addRepresentee(representeeOrganisation);
			organisation = representeeOrganisation;
		}
		return organisation;
	}

	/**
	 * Checks is representee is valid(by start and end date of the representee).
	 * 
	 * @param member
	 *            representee member to check
	 * @return whther representee is valid
	 * @throws DhxException
	 *             exception thrown if representee is null or start date is null
	 */
	public Boolean isRepresenteeValid(InternalXroadMember member) throws DhxException {
		Long curDate = new Date().getTime();
		if (member.getRepresentee() == null || member.getRepresentee().getStartDate() == null) {
			throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
					"Something went wrong! Start date of representee is empty "
							+ "or organisation is not representee!");
		}
		if (member.getRepresentee().getStartDate().getTime() <= curDate && (member.getRepresentee().getEndDate() == null
				|| member.getRepresentee().getEndDate().getTime() >= curDate)) {
			return true;
		} else {
			return false;
		}
	}

	public PojoMessage getPojoMessageFromIncomingContainer(IncomingDhxPackage document) throws DhxException {
		switch (document.getParsedContainerVersion()) {
		case V21:
			return getPojoMessageFromIncomingContainerV21(document);
		default:
			throw new DhxException("Unknown version of container!");
		}
	}

	/*
	 * public Writer getContainerWriter(DataHandler handler) throws DhxException
	 * { InputStream is = null; Writer dataWriter = null; try { is =
	 * handler.getInputStream(); dataWriter = new StringWriter(); byte[] buf =
	 * new byte[1024]; int len; while ((len = is.read(buf)) > 0) {
	 * dataWriter.write(new String(buf, 0, len, "UTF-8")); } is.close();
	 * dataWriter.close(); } catch (IOException ex) { throw new
	 * DhxException("Error occured while reading container.", ex); } finally {
	 * FileUtil.safeCloseStream(is); FileUtil.safeCloseWriter(dataWriter); }
	 * return dataWriter; }
	 */
	/**
	 * Sometimes DHX addressee(incoming document) and DVK addresse(outgoing
	 * document might be different. In DHX there must be always registration
	 * code, in DVK there might be system also. That method changes recipient
	 * and sender in capsule accordingly.
	 * 
	 * @param containerObject
	 *            container Object to do changes in
	 * @param sender
	 *            sender organisation
	 * @param recipient
	 *            recipient organisation
	 * @param outgoingContainer
	 *            defines wether it is incoming or outgoing container.
	 * @throws DhxException
	 *             thrown if error occurs
	 */
	@Loggable
	public void formatCapsuleRecipientAndSender(Object containerObject, InternalXroadMember senderMember,
			InternalXroadMember recipientMember, Boolean outgoingContainer) throws DhxException {
		CapsuleVersionEnum version = CapsuleVersionEnum.forClass(containerObject.getClass());
		switch (version) {
		case V21:
			DecContainer container = (DecContainer) containerObject;
			if (container != null) {
				DhxOrganisation sender = DhxOrganisationFactory.createDhxOrganisation(senderMember);
				DhxOrganisation recipient = DhxOrganisationFactory.createDhxOrganisation(recipientMember);
				String senderOraganisationCode = null;
				String recipientOrganisationCode = null;
				String recipientOrganisationCodeToFind = null;
				if (outgoingContainer) {
					/*
					 * senderOraganisationCode = sender.getRegistrationCode();
					 * recipientOrganisationCode =
					 * recipient.getRegistrationCode();
					 * recipientOrganisationCodeToFind = persistenceService
					 * .toDvkCapsuleAddressee(recipient.getRegistrationCode(),
					 * recipient.getSubSystem());
					 */
				} else {
					senderOraganisationCode = DhxUtil.toDvkCapsuleAddressee(sender.getCode(), sender.getSystem());
					recipientOrganisationCode = DhxUtil.toDvkCapsuleAddressee(recipient.getCode(),
							recipient.getSystem());
					recipientOrganisationCodeToFind = recipient.getCode();
				}
				log.debug("senderOraganisationCode:" + senderOraganisationCode + " recipientOrganisationCode:"
						+ recipientOrganisationCode);
				container.getTransport().getDecSender().setOrganisationCode(senderOraganisationCode);
				for (DecContainer.Transport.DecRecipient decRecipient : container.getTransport().getDecRecipient()) {
					if (decRecipient.getOrganisationCode().equals(recipientOrganisationCodeToFind)) {
						decRecipient.setOrganisationCode(recipientOrganisationCode);
					}
				}
			}
			break;
		default:
			throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
					"Unable to find adressees for given verion. version:" + version.toString());
		}
	}

	public List<PojoMessageRecipient> getPojoMessageRecipientsFromIncomingContainer(Object containerObject,
			Long pojoMessageId) throws DhxException {
		return getPojoMessageRecipientsFromContainer(containerObject, pojoMessageId, true);
	}

	/**
	 * Document which is being sent from ADIT contains ADIT recipient and
	 * sender. Need to tranform them to DHX standard.
	 * 
	 * @param containerObject
	 *            container Object to do changes in
	 * @throws DhxException
	 *             thrown if error occurs
	 */
	@Loggable
	public void formatCapsuleRecipientAndSenderAditContainerV21(ContainerVer2_1 containerObject) throws DhxException {
		if (containerObject != null) {
			String senderOraganisationCode = null;
			senderOraganisationCode = config.getMemberCode();
			log.debug("senderOraganisationCode:" + senderOraganisationCode);
			containerObject.getTransport().getDecSender().setOrganisationCode(senderOraganisationCode);
			for (DecRecipient decRecipient : containerObject.getTransport().getDecRecipient()) {
				PojoOrganization org = dvkDao.getOrganisationByIdentificator(decRecipient.getOrganisationCode());
				decRecipient.setOrganisationCode(org.getOrgCode());
			}
		}
	}

	public List<PojoMessageRecipient> getPojoMessageRecipientsFromOutgoingAditContainerV21(
			ContainerVer2_1 containerObject, Long pojoMessageId) throws DhxException {
		List<PojoMessageRecipient> recipients = new ArrayList<PojoMessageRecipient>();
		for (DecRecipient recipient : containerObject.getTransport().getDecRecipient()) {
			// add only own recipients
			PojoMessageRecipient pojoRecipient = new PojoMessageRecipient();
			pojoRecipient.setRecipientOrgCode(recipient.getOrganisationCode());
			pojoRecipient
					.setRecipientPersonCode(recipient.getPersonalIdCode() == null ? "" : recipient.getPersonalIdCode());
			pojoRecipient.setRecipientDivisionName(recipient.getStructuralUnit());
			pojoRecipient.setRecipientDivisionId(BigDecimal.valueOf(0));
			pojoRecipient.setRecipientPositionId(BigDecimal.valueOf(0));
			pojoRecipient.setDhlMessageId(pojoMessageId);
			boolean found = false;
			for (PojoMessageRecipient rec : recipients) {
				if (DhxUtil.bothNullOrEqual(rec.getRecipientOrgCode(), pojoRecipient.getRecipientOrgCode())) {
					found = true;
					break;
				}
			}
			// add same recipient only 1 time
			if (!found) {
				recipients.add(pojoRecipient);
			}
		}

		for (Recipient recipient : containerObject.getRecipient()) {
			for (PojoMessageRecipient pojoRec : recipients) {
				if ((recipient.getOrganisation() == null || pojoRec.getRecipientOrgCode()
						.equalsIgnoreCase(recipient.getOrganisation().getOrganisationCode()))
						&& pojoRec.getRecipientPersonCode() != null && recipient.getPerson() != null
						&& recipient.getPerson().getPersonalIdCode() != null && pojoRec.getRecipientPersonCode()
								.equalsIgnoreCase(recipient.getPerson().getPersonalIdCode())) {
					if (recipient.getOrganisation() != null) {
						pojoRec.setRecipientOrgName(recipient.getOrganisation().getName());
						pojoRec.setRecipientPositionName(recipient.getOrganisation().getPositionTitle());
					}
					pojoRec.setRecipientName(recipient.getPerson().getName());
				}
			}
		}
		return recipients;
	}

	private List<PojoMessageRecipient> getPojoMessageRecipientsFromContainer(Object containerObject, Long pojoMessageId,
			Boolean incoming) throws DhxException {
		CapsuleVersionEnum version = CapsuleVersionEnum.forClass(containerObject.getClass());
		switch (version) {
		case V21:
			return getPojoMessageRecipientsFromContainerV21((DecContainer) containerObject, pojoMessageId, incoming);
		default:
			throw new DhxException("Unknown version of container!");
		}
	}

	public Object getContainerFromFile(File containerFile) throws DhxException {
		return dhxMarshallerService.unmarshall(containerFile);
	}

	private List<PojoMessageRecipient> getPojoMessageRecipientsFromContainerV21(DecContainer container,
			Long pojoMessageId, Boolean incoming) throws DhxException {
		List<PojoMessageRecipient> recipients = new ArrayList<PojoMessageRecipient>();
		for (DecContainer.Transport.DecRecipient recipient : container.getTransport().getDecRecipient()) {
			// add only own recipients
			if (!incoming || config.getMemberCode().equals(recipient.getOrganisationCode())) {
				PojoMessageRecipient pojoRecipient = new PojoMessageRecipient();
				pojoRecipient.setRecipientOrgCode(recipient.getOrganisationCode());
				pojoRecipient.setRecipientPersonCode(
						recipient.getPersonalIdCode() == null ? "" : recipient.getPersonalIdCode());
				pojoRecipient.setRecipientDivisionName(recipient.getStructuralUnit());
				pojoRecipient.setRecipientDivisionId(BigDecimal.valueOf(0));
				pojoRecipient.setRecipientPositionId(BigDecimal.valueOf(0));
				pojoRecipient.setDhlMessageId(pojoMessageId);
				boolean found = false;
				for (PojoMessageRecipient rec : recipients) {
					if (DhxUtil.bothNullOrEqual(rec.getRecipientOrgCode(), pojoRecipient.getRecipientOrgCode())
							&& (!incoming || DhxUtil.bothNullOrEqual(rec.getRecipientPersonCode(),
									pojoRecipient.getRecipientPersonCode()))) {
						found = true;
						break;
					}
				}
				// add same recipient only 1 time
				if (!found) {
					recipients.add(pojoRecipient);
				}
			}
		}

		for (DecContainer.Recipient recipient : container.getRecipient()) {
			for (PojoMessageRecipient pojoRec : recipients) {
				if ((recipient.getOrganisation() == null || pojoRec.getRecipientOrgCode()
						.equalsIgnoreCase(recipient.getOrganisation().getOrganisationCode()))
						&& pojoRec.getRecipientPersonCode() != null && recipient.getPerson() != null
						&& recipient.getPerson().getPersonalIdCode() != null && pojoRec.getRecipientPersonCode()
								.equalsIgnoreCase(recipient.getPerson().getPersonalIdCode())) {
					if (recipient.getOrganisation() != null) {
						pojoRec.setRecipientOrgName(recipient.getOrganisation().getName());
						pojoRec.setRecipientPositionName(recipient.getOrganisation().getPositionTitle());
					}
					pojoRec.setRecipientName(recipient.getPerson().getName());
				}
			}
		}
		return recipients;
	}

	private PojoMessage getPojoMessageFromIncomingContainerV21(IncomingDhxPackage document) throws DhxException {
		DecContainer container = (DecContainer) document.getParsedContainer();
		PojoMessage message = new PojoMessage();
		// message.setCaseName(caseName);
		// Writer writer = getContainerWriter(document.getDocumentFile());
		// first set empty data, it will be added later
		message.setData(" ");
		message.setDhlFolderName(container.getDecMetadata().getDecFolder());
		message.setDhlGuid(container.getRecordMetadata().getRecordGuid());
		message.setDhlId(container.getDecMetadata().getDecId().longValue());
		message.setDhxConsignmentId(document.getExternalConsignmentId());
		message.setIsIncoming(true);
		message.setSendingDate(container.getDecMetadata().getDecReceiptDate().toGregorianCalendar().getTime());	
		message.setReceivedDate(new Date());
		message.setRecipientStatusId(DocumentService.DVK_STATUS_MISSING);
		if(container.getRecordCreator() != null && container.getRecordCreator().getPerson() != null) {
			message.setSenderName(container.getRecordCreator().getPerson().getName());
		}
		DhxOrganisation org = DhxOrganisationFactory.createDhxOrganisation(document.getClient());
		String orgCode = DhxUtil.toDvkCapsuleAddressee(org.getCode(), org.getSystem());
		message.setSenderOrgCode(orgCode);
		if(container.getRecordCreator() != null && container.getRecordCreator().getOrganisation() != null) {
			message.setSenderOrgName(container.getRecordCreator().getOrganisation().getOrganisationCode());
		}
		message.setSenderPersonCode(container.getTransport().getDecSender().getPersonalIdCode());
		// message.setMetaxml(metaxml);
		// message.setQueryId(queryId);
		message.setReceivedDate(new Date());
		return message;
	}

	/**
	 * When all actions with contianer are complete, delete file which might be
	 * related to the container.
	 * 
	 * @param containerObject
	 *            container to cleanup
	 * @throws DhxException
	 *             thrown when error occurs
	 */
	@Loggable
	public void cleanupContainer(Object containerObject) throws DhxException {
		if (containerObject == null) {
			return;
		}
		CapsuleVersionEnum version = CapsuleVersionEnum.forClass(containerObject.getClass());
		switch (version) {
		case V21:
			DecContainer container = (DecContainer) containerObject;
			if (container != null && container.getFile() != null) {
				for (ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.File decFile : container
						.getFile()) {
					if (decFile.getZipBase64Content() != null) {
						decFile.getZipBase64Content().delete();
					}
				}
			}
			break;
		default:
			throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
					"Unable to cleanup container of given version. version:" + version.toString());
		}
	}
}
