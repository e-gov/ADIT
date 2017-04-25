package ee.adit.service.dhx;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.jcabi.aspects.Loggable;

import ee.adit.dao.DhxUserDAO;
import ee.adit.dhx.AditDhxConfig;
import ee.adit.dhx.api.container.Organisation;
import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.DecRecipient;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.dao.pojo.DhxUser;
import ee.adit.service.DocumentService;
import ee.adit.util.Configuration;
import ee.adit.util.Util;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DhxService {

	@Autowired
	DhxUserDAO dhxDao;

	@Autowired
	SoapConfig config;

	@Autowired
	DhxMarshallerService dhxMarshallerService;

	@Autowired
	AddressService addressService;

	@Autowired
	Configuration configuration;
	
	@Autowired
	AditDhxConfig aditDhxConfig;

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
	public DhxUser getDhxUserFromInternalXroadMember(InternalXroadMember member) throws DhxException {
		return getDhxUserFromInternalXroadMember(member, false);
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
	public DhxUser getDhxUserFromInternalXroadMember(InternalXroadMember member, Boolean representorOnly)
			throws DhxException {
		Boolean newMember = false;
		DhxUser organisation = dhxDao.getOrganisationByCodeAndSubsystem(member.getMemberCode(),
				member.getSubsystemCode(), config.getDhxSubsystemPrefix());
		if (organisation == null) {
			log.debug("Organisation is not found, creating new one.");
			newMember = true;
			organisation = new DhxUser();
		}
		// update representors data only if needed
		if (member.getRepresentee() == null || representorOnly) {
			organisation.setActive(true);
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
					toSingleCapsuleAddressee(member.getMemberCode(), member.getSubsystemCode()));
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
			DhxUser representeeOrganisation = dhxDao.getOrganisationByCodeAndSubsystem(
					member.getRepresentee().getRepresenteeCode(), member.getRepresentee().getRepresenteeSystem(),
					config.getDhxSubsystemPrefix());
			if (representeeOrganisation == null) {
				log.debug("Representee organisation is not found, creating new one.");
				representeeOrganisation = new DhxUser();
			}
			representeeOrganisation.setActive(true);
			representeeOrganisation.setOrgName(member.getRepresentee().getRepresenteeName());
			representeeOrganisation.setOrgCode(member.getRepresentee().getRepresenteeCode());
			representeeOrganisation.setSubSystem(member.getRepresentee().getRepresenteeSystem());
			representeeOrganisation.setOrganisationIdentificator(toSingleCapsuleAddressee(
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

	/**
	 * Sometimes DHX addressee(incoming document) and ADIT addresse(outgoing
	 * document might be different. In DHX there must be always registration
	 * code, in ADIT there might be system also. That method changes recipient
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
				} else {
					senderOraganisationCode = toSingleCapsuleAddressee(sender.getCode(), sender.getSystem());
					recipientOrganisationCode = toSingleCapsuleAddressee(recipient.getCode(),
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
				DhxUser org = dhxDao.getOrganisationByIdentificator(decRecipient.getOrganisationCode());
				decRecipient.setOrganisationCode(org.getOrgCode());
			}
		}
	}


	public Object getContainerFromFile(File containerFile) throws DhxException {
		return dhxMarshallerService.unmarshall(containerFile);
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

	public String saveContainerToFile(Object parsedContainer) throws DhxException, FileNotFoundException {
		FileOutputStream fos = null;
		try {
			// Write the DVK Container to temporary file
			String temporaryFile = configuration.getTempDir() + File.separator + Util.generateRandomFileName();
			fos = new FileOutputStream(temporaryFile);
			dhxMarshallerService.marshallToOutputStreamNoNamespacePrefixes(parsedContainer, fos, true);
			return temporaryFile;
		} finally {
			FileUtil.safeCloseStream(fos);
		}
	}
	
	/**
	 * Method returns single string addressee from memberCode and subsystem. For
	 * example if subsystem is DHX, method returns memberCode value, if
	 * subsystem is something different, then mehtod return subsystem.memberCode.
	 * 
	 * @param memberCode
	 *            memberCode to use to transform to single string capsule addressee
	 * @param subsystem
	 *            subsystem to use to transform to single string capsule addressee
	 * @return single string capsule addressee
	 */
	@Loggable
	public String toSingleCapsuleAddressee(String memberCode, String subsystem) {
		String singleStringCode = null;
		if (!StringUtil.isNullOrEmpty(subsystem)
				&& subsystem.startsWith(aditDhxConfig.getConfig().getDhxSubsystemPrefix() + ".")) {
			String system = subsystem.substring(aditDhxConfig.getConfig().getDhxSubsystemPrefix().length() + 1);
			// String perfix = subsystem.substring(0, index);
			log.debug("found system with subsystem: " + system);
			if (isSpecialOrganisation(system)) {
				singleStringCode = system;
			} else {
				singleStringCode = system + "." + memberCode;
			}

		} else if (!StringUtil.isNullOrEmpty(subsystem)
				&& !subsystem.equals(aditDhxConfig.getConfig().getDhxSubsystemPrefix())) {
			if (isSpecialOrganisation(subsystem)) {
				singleStringCode = subsystem;
			} else {
				singleStringCode = subsystem + "." + memberCode;
			}
		} else {

			singleStringCode = memberCode;
		}
		return singleStringCode;
	}

	/**
	 * Method defines if organisation is one of the special organisations that
	 * are in the capsule without registration code, but with system name.
	 * 
	 * @param organisationCode
	 *            organisation code to check
	 * @return whether organistion code is special or not
	 */
	@Loggable
	public Boolean isSpecialOrganisation(String organisationCode) {
		String specialOrgs = "," + aditDhxConfig.getSpecialOrganisations() + ",";
		log.debug("specialOrgs: " + specialOrgs + "  organisationCode:" + organisationCode);
		if (specialOrgs.indexOf("," + organisationCode + ",") >= 0) {
			return true;
		}
		return false;
	}
}
